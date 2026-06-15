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
      <EChartPanel :option="monthlyTrendOption" height="400rpx" />
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

      <!-- 分类概要 -->
      <view v-if="activeDistributions.length" class="dist-summary">
        <view v-for="(item, idx) in activeDistributions.slice(0, 5)" :key="idx" class="dist-item">
          <view class="dist-item__bar-bg">
            <view class="dist-item__bar-fill" :style="{ width: (item.ratio * 100) + '%', background: distColors[idx % distColors.length] }" />
          </view>
          <view class="dist-item__info">
            <text class="dist-item__label">{{ item.label }}</text>
            <text class="dist-item__amount">¥{{ item.amount.toFixed(2) }}</text>
          </view>
          <text class="dist-item__ratio">{{ (item.ratio * 100).toFixed(1) }}%</text>
        </view>
      </view>

      <EChartPanel v-if="activeDistributions.length" :option="distributionOption" height="360rpx" />
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

const distColors = ['#9B7EC8', '#CFA052', '#5BAE7C', '#5B8DBE', '#D4956B']

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

const distributionOption = computed<ChartOption>(() => ({
  color: distColors,
  animation: true,
  animationDuration: 600,
  grid: { left: 16, right: 40, top: 12, bottom: 12, containLabel: true },
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, confine: true },
  xAxis: {
    type: 'value',
    axisLine: { show: false },
    axisTick: { show: false },
    splitLine: { lineStyle: { color: colors.value.surfaceSoft, type: 'dashed' } },
    axisLabel: { show: false }
  },
  yAxis: {
    type: 'category',
    data: activeDistributions.value.map((item) => item.label),
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: colors.value.textPrimary, fontSize: 11 }
  },
  series: [{
    type: 'bar',
    barMaxWidth: 16,
    data: activeDistributions.value.map((item, idx) => ({
      value: Number(item.amount.toFixed(2)),
      itemStyle: {
        borderRadius: [0, 8, 8, 0],
        color: {
          type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
          colorStops: [
            { offset: 0, color: distColors[idx % distColors.length] },
            { offset: 1, color: distColors[idx % distColors.length] + '80' }
          ]
        }
      }
    })),
    label: {
      show: true,
      position: 'right',
      color: colors.value.textMuted,
      fontSize: 10,
      formatter(params: { dataIndex: number }) {
        const ratio = activeDistributions.value[params.dataIndex]?.ratio || 0
        return `${(ratio * 100).toFixed(1)}%`
      }
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

/* 分类概要 */
.dist-summary {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.dist-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.dist-item__bar-bg {
  flex: 1;
  height: 12rpx;
  border-radius: 6rpx;
  background: var(--color-surface-soft);
  overflow: hidden;
}

.dist-item__bar-fill {
  height: 100%;
  border-radius: 6rpx;
  transition: width 0.4s ease;
}

.dist-item__info {
  width: 160rpx;
  flex-shrink: 0;
}

.dist-item__label {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dist-item__amount {
  display: block;
  color: var(--color-text-muted);
  font-size: 18rpx;
}

.dist-item__ratio {
  width: 80rpx;
  text-align: right;
  color: var(--color-text-secondary);
  font-size: var(--font-tiny);
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
