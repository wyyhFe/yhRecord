<template>
  <view class="year-charts">
    <!-- 月度趋势 -->
    <view class="chart-card">
      <view class="chart-card__header">
        <text class="chart-card__title">📊 月度收支趋势</text>
      </view>
      <view class="chart-card__legend">
        <view class="chart-legend__item">
          <view class="chart-legend__dot chart-legend__dot--expense" />
          <text class="chart-legend__text">支出</text>
        </view>
        <view class="chart-legend__item">
          <view class="chart-legend__dot chart-legend__dot--income" />
          <text class="chart-legend__text">收入</text>
        </view>
      </view>
      <EChartPanel :option="monthlyTrendOption" height="360rpx" />
    </view>

    <!-- 支出分类 -->
    <view class="chart-card">
      <view class="chart-card__header">
        <text class="chart-card__title">🏷️ 支出分类</text>
        <text class="chart-card__subtitle">{{ activeMonthLabel }}</text>
      </view>

      <!-- 月份选择 -->
      <scroll-view scroll-x class="month-scroll" :show-scrollbar="false">
        <view class="month-scroll__inner">
          <view
            v-for="item in items"
            :key="item.month"
            class="month-chip"
            :class="{ 'month-chip--active': item.month === activeMonth }"
            @tap="activeMonth = item.month"
          >
            <text class="month-chip__text">{{ item.month }}月</text>
          </view>
        </view>
      </scroll-view>

      <view v-if="activeDistributions.length" class="dist-layout">
        <!-- 环形图 -->
        <view class="dist-ring-wrap">
          <EChartPanel :option="donutOption" height="320rpx" />
        </view>

        <!-- 图例列表 -->
        <view class="dist-legend">
          <view v-for="(item, idx) in activeDistributions" :key="idx" class="dist-legend__item">
            <view class="dist-legend__dot" :style="{ background: distColors[idx % distColors.length] }" />
            <text class="dist-legend__label">{{ item.label }}</text>
            <text class="dist-legend__amount">¥{{ item.amount.toFixed(0) }}</text>
            <text class="dist-legend__ratio">{{ (item.ratio * 100).toFixed(1) }}%</text>
          </view>
        </view>
      </view>

      <view v-else class="chart-empty">
        <text class="chart-empty__text">当前月份没有支出分类数据</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ChartOption } from 'uni-echarts/shared'
import EChartPanel from '@/components/business/echart-panel/index.vue'
import { useThemeColors } from '@/composables/useThemeColors'

const colors = useThemeColors()

const distColors = ['#9B7EC8', '#CFA052', '#5BAE7C', '#5B8DBE', '#D4956B', '#E8635F', '#7EC8A0', '#C87E7E']

interface DistributionItem {
  label: string
  amount: number
  ratio: number
}

interface YearMonthChartItem {
  month: number
  expense: number
  income: number
  distributions: DistributionItem[]
}

const props = defineProps<{
  year: number
  items: YearMonthChartItem[]
}>()

const activeMonth = ref<number>()

watch(
  () => props.items,
  (items) => {
    if (!items.length) { activeMonth.value = undefined; return }
    if (items.some((item) => item.month === activeMonth.value)) return
    activeMonth.value = items[0].month
  },
  { immediate: true }
)

const activeItem = computed(() => props.items.find((item) => item.month === activeMonth.value) || props.items[0])
const activeDistributions = computed(() => activeItem.value?.distributions || [])
const activeMonthLabel = computed(() => {
  if (!activeItem.value) return `${props.year} 年`
  return `${activeItem.value.month} 月`
})

const totalExpense = computed(() => {
  return activeDistributions.value.reduce((sum, item) => sum + item.amount, 0)
})

const monthlyTrendOption = computed<ChartOption>(() => ({
  color: ['#E8635F', '#5BAE7C'],
  animation: true,
  animationDuration: 600,
  grid: { left: 16, right: 16, top: 24, bottom: 16, containLabel: true },
  tooltip: { trigger: 'axis', confine: true },
  xAxis: {
    type: 'category',
    data: props.items.map((item) => `${item.month}月`),
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
  series: [
    {
      name: '支出',
      type: 'bar',
      barMaxWidth: 20,
      data: props.items.map((item) => Number(item.expense.toFixed(2))),
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#E8635F' },
            { offset: 1, color: '#F2A09E' }
          ]
        }
      }
    },
    {
      name: '收入',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: props.items.map((item) => Number(item.income.toFixed(2))),
      lineStyle: { width: 3, color: '#5BAE7C' },
      itemStyle: { borderWidth: 3, borderColor: colors.value.bg, color: '#5BAE7C' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(91,174,124,0.20)' },
            { offset: 1, color: 'rgba(91,174,124,0)' }
          ]
        }
      }
    }
  ]
}))

const donutOption = computed<ChartOption>(() => ({
  color: distColors,
  animation: true,
  animationDuration: 800,
  series: [{
    type: 'pie',
    radius: ['50%', '75%'],
    center: ['50%', '50%'],
    avoidLabelOverlap: false,
    label: {
      show: true,
      position: 'center',
      formatter: `¥{total|${totalExpense.value.toFixed(0)}}\n{label|总支出}`,
      rich: {
        total: {
          fontSize: 22,
          fontWeight: 'bold',
          color: colors.value.textPrimary,
          lineHeight: 30
        },
        label: {
          fontSize: 11,
          color: colors.value.textMuted,
          lineHeight: 18
        }
      }
    },
    labelLine: { show: false },
    emphasis: {
      scaleSize: 6
    },
    data: activeDistributions.value.map((item) => ({
      name: item.label,
      value: Number(item.amount.toFixed(2))
    })),
    itemStyle: {
      borderRadius: 4,
      borderColor: colors.value.bg,
      borderWidth: 2
    }
  }]
}))
</script>

<style scoped lang="scss">
.year-charts {
  margin-top: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* ========== 图表卡片 ========== */
.chart-card {
  margin: 0 var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.chart-card__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}

.chart-card__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.chart-card__subtitle {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

/* 图例 */
.chart-legend {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-3);
}

.chart-legend__item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.chart-legend__dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: var(--radius-tiny);
}

.chart-legend__dot--expense {
  background: #E8635F;
}

.chart-legend__dot--income {
  background: #5BAE7C;
}

.chart-legend__text {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

/* 月份滚动 */
.month-scroll {
  margin-bottom: var(--space-4);
  white-space: nowrap;
}

.month-scroll__inner {
  display: inline-flex;
  gap: var(--space-2);
}

.month-chip {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  transition: all var(--motion-fast) var(--ease-standard);
}

.month-chip--active {
  background: var(--color-ledger);
}

.month-chip__text {
  color: var(--color-text-secondary);
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
}

.month-chip--active .month-chip__text {
  color: #fff;
  font-weight: var(--weight-semibold);
}

/* ========== 环形图 + 图例布局 ========== */
.dist-layout {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.dist-ring-wrap {
  display: flex;
  justify-content: center;
}

/* 图例列表 */
.dist-legend {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.dist-legend__item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.dist-legend__dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 6rpx;
  flex-shrink: 0;
}

.dist-legend__label {
  flex: 1;
  min-width: 0;
  color: var(--color-text-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dist-legend__amount {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  flex-shrink: 0;
}

.dist-legend__ratio {
  width: 80rpx;
  text-align: right;
  color: var(--color-text-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
  flex-shrink: 0;
}

/* 空状态 */
.chart-empty {
  padding: var(--space-6) 0;
  text-align: center;
}

.chart-empty__text {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}
</style>
