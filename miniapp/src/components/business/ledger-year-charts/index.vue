<template>
  <view class="stats">
    <!-- 吸顶面板：tabs + 日期 + 类型 -->
    <view class="stats__panel">
      <view class="stats__tabs">
        <view
          v-for="tab in periodTabs"
          :key="tab.value"
          class="stats__tab"
          :class="{ 'stats__tab--active': period === tab.value }"
          @tap="period = tab.value"
        >
          <text class="stats__tab-text">{{ tab.label }}</text>
        </view>
      </view>
      <view class="stats__toolbar">
        <view class="stats__nav">
          <view class="stats__nav-btn" @tap="prevPeriod">
            <text class="stats__nav-arrow">‹</text>
          </view>
          <text class="stats__nav-label">{{ periodLabel }}</text>
          <view class="stats__nav-btn" @tap="nextPeriod">
            <text class="stats__nav-arrow">›</text>
          </view>
        </view>
        <view class="stats__type-pills">
          <view
            class="stats__pill"
            :class="entryType === 'EXPENSE' ? 'stats__pill--expense' : ''"
            @tap="entryType = 'EXPENSE'"
          >
            <text class="stats__pill-text">支出</text>
          </view>
          <view
            class="stats__pill"
            :class="entryType === 'INCOME' ? 'stats__pill--income' : ''"
            @tap="entryType = 'INCOME'"
          >
            <text class="stats__pill-text">收入</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 指标卡片 -->
    <view v-if="data" class="stats__summary">
      <text class="stats__summary-title">{{ periodPrefix }}{{ entryType === 'EXPENSE' ? '支出' : '收入' }}：<text class="stats__summary-amount">¥{{ data.totalAmount.toFixed(2) }}</text></text>
    </view>

    <view v-if="data" class="stats__metrics">
      <view class="stats__metric">
        <text class="stats__metric-label">日均{{ entryType === 'EXPENSE' ? '支出' : '收入' }}</text>
        <text class="stats__metric-value">¥{{ data.dailyAverage.toFixed(2) }}</text>
      </view>
      <view class="stats__metric">
        <text class="stats__metric-label">比{{ periodPrefix }}{{ comparisonLabel }}</text>
        <text class="stats__metric-value" :class="comparisonClass">{{ comparisonText }}</text>
      </view>
      <view class="stats__metric">
        <text class="stats__metric-label">收支结余</text>
        <text class="stats__metric-value" :class="data.balance >= 0 ? 'text-success' : 'text-danger'">¥{{ data.balance.toFixed(2) }}</text>
      </view>
    </view>

    <!-- 趋势图 -->
    <view v-if="data" class="stats__chart-card">
      <text class="stats__chart-title">📈 {{ periodPrefix }}趋势</text>
      <EChartPanel :option="trendOption" height="360rpx" />
    </view>

    <!-- 分类饼图 -->
    <view v-if="data?.categories?.length" class="stats__chart-card">
      <text class="stats__chart-title">🏷️ {{ entryType === 'EXPENSE' ? '支出' : '收入' }}分类构成</text>

      <view class="stats__donut-wrap">
        <EChartPanel :option="pieOption" height="320rpx" />
      </view>

      <!-- 分类列表 -->
      <view class="stats__cat-list">
        <view v-for="(cat, idx) in data.categories" :key="idx" class="stats__cat-item">
          <view class="stats__cat-dot" :style="{ background: pieColors[idx % pieColors.length] }" />
          <text class="stats__cat-name">{{ cat.tagName }}</text>
          <text class="stats__cat-amount">¥{{ cat.amount.toFixed(2) }}</text>
          <text class="stats__cat-ratio">{{ (cat.ratio * 100).toFixed(1) }}%</text>
        </view>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="stats__empty">
      <text class="stats__empty-text">加载中...</text>
    </view>

    <!-- 空状态 -->
    <view v-if="!data && !loading" class="stats__empty">
      <text class="stats__empty-text">暂无统计数据</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ChartOption } from 'uni-echarts/shared'
import EChartPanel from '@/components/business/echart-panel/index.vue'
import { useThemeColors } from '@/composables/useThemeColors'
import { fetchPeriodStatistics, type PeriodStatistics } from '@/api/ledger'
import type { Id } from '@/types/domain'

const colors = useThemeColors()
const pieColors = ['#9B7EC8', '#CFA052', '#5BAE7C', '#5B8DBE', '#D4956B', '#E8635F', '#7EC8A0', '#C87E7E']

const props = defineProps<{
  bookId?: Id
}>()

const period = ref<'week' | 'month' | 'year'>('month')
const entryType = ref<'EXPENSE' | 'INCOME'>('EXPENSE')
const data = ref<PeriodStatistics | null>(null)
const loading = ref(false)

// 当前锚点日期（周=本周一，月=当月1号，年=当年1月1号）
const anchor = ref(new Date())

const periodTabs = [
  { label: '周报', value: 'week' as const },
  { label: '月报', value: 'month' as const },
  { label: '年报', value: 'year' as const }
]

// 日期范围计算
const dateRange = computed(() => {
  const a = anchor.value
  const y = a.getFullYear()
  const m = a.getMonth()
  const d = a.getDate()
  const dayOfWeek = a.getDay() === 0 ? 6 : a.getDay() - 1 // 周一=0

  if (period.value === 'week') {
    const start = new Date(y, m, d - dayOfWeek)
    const end = new Date(start.getFullYear(), start.getMonth(), start.getDate() + 6)
    return { start: fmt(start), end: fmt(end) }
  }
  if (period.value === 'month') {
    const start = new Date(y, m, 1)
    const end = new Date(y, m + 1, 0)
    return { start: fmt(start), end: fmt(end) }
  }
  // year
  return { start: `${y}-01-01`, end: `${y}-12-31` }
})

const periodLabel = computed(() => {
  const { start, end } = dateRange.value
  const sy = start.slice(0, 4), sm = start.slice(5, 7), sd = start.slice(8, 10)
  const ey = end.slice(0, 4), em = end.slice(5, 7), ed = end.slice(8, 10)

  if (period.value === 'week') {
    return `${sy}.${sm}.${sd} ~ ${em}.${ed}`
  }
  if (period.value === 'month') {
    return `${sy}年${Number(sm)}月`
  }
  return `${sy}年`
})

const periodPrefix = computed(() => {
  if (period.value === 'week') return '本周'
  if (period.value === 'month') return '本月'
  return '本年'
})

const comparisonLabel = computed(() => {
  if (period.value === 'week') return '上周'
  if (period.value === 'month') return '上月'
  return '去年'
})

const comparisonText = computed(() => {
  if (!data.value) return '--'
  const prev = data.value.previousTotal
  const curr = data.value.totalAmount
  if (prev === 0) return curr > 0 ? '+100%' : '0%'
  const pct = ((curr - prev) / prev * 100)
  const sign = pct >= 0 ? '+' : ''
  return `${sign}${pct.toFixed(1)}%`
})

const comparisonClass = computed(() => {
  if (!data.value) return ''
  const prev = data.value.previousTotal
  const curr = data.value.totalAmount
  if (curr > prev) return entryType.value === 'EXPENSE' ? 'text-danger' : 'text-success'
  if (curr < prev) return entryType.value === 'EXPENSE' ? 'text-success' : 'text-danger'
  return ''
})

// 趋势图
const trendOption = computed<ChartOption>(() => {
  if (!data.value) return {}
  const trend = data.value.dailyTrend
  const isWeek = period.value === 'week'
  const xLabels = trend.map((item) => {
    if (isWeek) return ['一', '二', '三', '四', '五', '六', '日'][new Date(item.date).getDay() === 0 ? 6 : new Date(item.date).getDay() - 1]
    if (period.value === 'month') return Number(item.date.slice(8, 10)).toString()
    return `${Number(item.date.slice(5, 7))}月`
  })
  const values = trend.map((item) => Number(item.amount.toFixed(2)))
  const barColor = entryType.value === 'EXPENSE' ? '#E8635F' : '#5BAE7C'
  const barColorLight = entryType.value === 'EXPENSE' ? '#F2A09E' : '#A0D8B8'

  return {
    animation: true,
    animationDuration: 500,
    grid: { left: 16, right: 16, top: 16, bottom: 16, containLabel: true },
    tooltip: {
      trigger: 'axis',
      confine: true,
      backgroundColor: 'rgba(0,0,0,0.75)',
      borderColor: 'transparent',
      borderRadius: 8,
      padding: [8, 12],
      textStyle: { color: '#fff', fontSize: 12 },
      formatter(params: unknown) {
        const item = Array.isArray(params) ? params[0] : params as { name?: string; value?: number }
        return `${item.name || ''}  ¥${(item.value || 0).toFixed(2)}`
      }
    },
    xAxis: {
      type: 'category',
      data: xLabels,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: colors.value.textMuted, fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: colors.value.surfaceSoft, type: 'dashed' } },
      axisLabel: { color: colors.value.textMuted, fontSize: 10 }
    },
    series: [{
      type: 'bar',
      barMaxWidth: isWeek ? 32 : (period.value === 'month' ? 12 : 20),
      data: values,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: barColor },
            { offset: 1, color: barColorLight }
          ]
        }
      }
    }]
  }
})

// 饼图
const pieOption = computed<ChartOption>(() => {
  if (!data.value?.categories?.length) return {}
  return {
    color: pieColors,
    animation: true,
    animationDuration: 600,
    tooltip: {
      trigger: 'item',
      confine: true,
      backgroundColor: 'rgba(0,0,0,0.75)',
      borderColor: 'transparent',
      textStyle: { color: '#fff', fontSize: 12 },
      formatter(params: { name: string; value: number; percent: number }) {
        return `${params.name}\n¥${params.value.toFixed(2)} (${params.percent.toFixed(1)}%)`
      }
    },
    series: [{
      type: 'pie',
      radius: ['0%', '70%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: true,
      selectedMode: 'single',
      selectedOffset: 10,
      label: {
        show: true,
        position: 'outside',
        formatter(params: { name: string; percent: number }) {
          return `{name|${params.name}}\n{pct|${params.percent.toFixed(1)}%}`
        },
        rich: {
          name: { fontSize: 10, color: colors.value.textSecondary, lineHeight: 14 },
          pct: { fontSize: 12, fontWeight: 'bold', color: colors.value.textPrimary, lineHeight: 16 }
        }
      },
      labelLine: { show: true, length: 10, length2: 14, smooth: true },
      emphasis: { scaleSize: 6 },
      data: data.value.categories.map((item, idx) => ({
        name: item.tagName,
        value: Number(item.amount.toFixed(2)),
        selected: idx === 0
      })),
      itemStyle: { borderRadius: 4, borderColor: colors.value.bg, borderWidth: 2 }
    }]
  }
})

function fmt(d: Date) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function prevPeriod() {
  const a = new Date(anchor.value)
  if (period.value === 'week') a.setDate(a.getDate() - 7)
  else if (period.value === 'month') a.setMonth(a.getMonth() - 1)
  else a.setFullYear(a.getFullYear() - 1)
  anchor.value = a
}

function nextPeriod() {
  const a = new Date(anchor.value)
  if (period.value === 'week') a.setDate(a.getDate() + 7)
  else if (period.value === 'month') a.setMonth(a.getMonth() + 1)
  else a.setFullYear(a.getFullYear() + 1)
  anchor.value = a
}

async function loadStats() {
  loading.value = true
  try {
    const result = await fetchPeriodStatistics({
      startDate: dateRange.value.start,
      endDate: dateRange.value.end,
      type: entryType.value,
      bookId: props.bookId
    })
    console.log('[stats] loaded', result)
    data.value = result
  } catch (error) {
    console.error('[stats] loadStats failed', error)
    data.value = null
  } finally {
    loading.value = false
  }
}

watch([dateRange, entryType, () => props.bookId], () => { loadStats() }, { immediate: true })
</script>

<style scoped lang="scss">
.stats {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* ========== 吸顶面板 ========== */
.stats__panel {
  position: sticky;
  top: 0;
  z-index: 1;
  background: #E8EFF6;
  border-radius: var(--radius-large) var(--radius-large) 0 0;
  overflow: hidden;
}

/* ========== tabs ========== */
.stats__tabs {
  display: flex;
  gap: 0;
  padding: var(--space-3) var(--space-3) 0;
}

.stats__tab {
  flex: 1;
  text-align: center;
  padding: var(--space-3) 0;
  border-radius: var(--radius-medium) var(--radius-medium) 0 0;
  background: rgba(255, 255, 255, 0.4);
  transition: all var(--motion-fast) var(--ease-standard);
}

.stats__tab--active {
  background: #fff;
}

.stats__tab-text {
  color: #666;
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}

.stats__tab--active .stats__tab-text {
  color: #1C1C1E;
  font-weight: var(--weight-bold);
}

/* ========== 工具栏（白底区域） ========== */
.stats__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  background: #fff;
  padding: var(--space-4) var(--space-4) var(--space-3);
  border-radius: 0 0 var(--radius-large) var(--radius-large);
}

.stats__nav {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex: 1;
  min-width: 0;
}

.stats__nav-btn {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-tiny);
  flex-shrink: 0;
}

.stats__nav-btn:active {
  background: var(--color-surface-soft);
}

.stats__nav-arrow {
  color: #999;
  font-size: 32rpx;
  font-weight: var(--weight-bold);
}

.stats__nav-label {
  color: #1C1C1E;
  font-size: var(--font-meta);
  font-weight: var(--weight-bold);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
  padding: 0 var(--space-2);
}

.stats__type-pills {
  display: flex;
  gap: 0;
  background: var(--color-surface-soft);
  border-radius: var(--radius-full);
  padding: 4rpx;
  flex-shrink: 0;
}

.stats__pill {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  transition: all var(--motion-fast) var(--ease-standard);
}

.stats__pill--expense {
  background: #E8635F;
}

.stats__pill--income {
  background: #5BAE7C;
}

.stats__pill-text {
  color: #999;
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
}

.stats__pill--expense .stats__pill-text,
.stats__pill--income .stats__pill-text {
  color: #fff;
  font-weight: var(--weight-semibold);
}

/* ========== 总额摘要 ========== */
.stats__summary {
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-4) var(--space-5);
}

.stats__summary-title {
  color: #1C1C1E;
  font-size: var(--font-body);
  font-weight: var(--weight-medium);
}

.stats__summary-amount {
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

/* ========== 指标卡片 ========== */
.stats__metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
}

.stats__metric {
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.stats__metric-label {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.stats__metric-value {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

/* ========== 图表卡片 ========== */
.stats__chart-card {
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.stats__chart-title {
  display: block;
  margin-bottom: var(--space-3);
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.stats__donut-wrap {
  display: flex;
  justify-content: center;
}

/* ========== 分类列表 ========== */
.stats__cat-list {
  margin-top: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.stats__cat-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.stats__cat-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 6rpx;
  flex-shrink: 0;
}

.stats__cat-name {
  flex: 1;
  min-width: 0;
  color: var(--color-text-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stats__cat-amount {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  flex-shrink: 0;
}

.stats__cat-ratio {
  width: 80rpx;
  text-align: right;
  color: var(--color-text-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
  flex-shrink: 0;
}

/* ========== 状态 ========== */
.stats__empty {
  padding: var(--space-8) 0;
  text-align: center;
}

.stats__empty-text {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.text-success { color: var(--color-success) !important; }
.text-danger { color: var(--color-danger) !important; }
</style>
