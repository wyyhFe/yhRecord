<template>
  <view class="year-charts">
    <view class="year-charts__card">
      <view class="year-charts__title">月度收支趋势</view>
      <view class="year-charts__desc">按月份查看收入和支出的变化趋势。</view>
      <EChartPanel :option="monthlyTrendOption" height="420rpx" />
    </view>

    <view class="year-charts__card">
      <view class="year-charts__title">支出分类分布</view>
      <view class="year-charts__desc">{{ activeMonthLabel }}</view>

      <scroll-view scroll-x class="year-charts__tabs">
        <view class="year-charts__tab-row">
          <view
            v-for="item in items"
            :key="item.month"
            class="year-charts__tab"
            :class="item.month === activeMonth ? 'year-charts__tab--active' : ''"
            @tap="activeMonth = item.month"
          >
            {{ item.month }}月
          </view>
        </view>
      </scroll-view>

      <EChartPanel v-if="activeDistributions.length" :option="distributionOption" height="420rpx" />
      <view v-else class="note-card">当前月份没有可展示的支出分类。</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ChartOption } from 'uni-echarts/shared'
import EChartPanel from '@/components/business/echart-panel/index.vue'

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
    if (!items.length) {
      activeMonth.value = undefined
      return
    }
    if (items.some((item) => item.month === activeMonth.value)) return
    activeMonth.value = items[0].month
  },
  { immediate: true }
)

const activeItem = computed(() => props.items.find((item) => item.month === activeMonth.value) || props.items[0])
const activeDistributions = computed(() => activeItem.value?.distributions || [])
const activeMonthLabel = computed(() => {
  if (!activeItem.value) return `${props.year} 年`
  return `${props.year} 年 ${activeItem.value.month} 月支出分类`
})

const monthlyTrendOption = computed<ChartOption>(() => ({
  color: ['#d35d56', '#2c9b67'],
  animation: false,
  grid: {
    left: 20,
    right: 20,
    top: 36,
    bottom: 20,
    containLabel: true
  },
  tooltip: {
    trigger: 'axis',
    confine: true
  },
  legend: {
    top: 0,
    itemWidth: 18,
    itemHeight: 10,
    textStyle: {
      color: 'var(--color-text-secondary)',
      fontSize: 11
    }
  },
  xAxis: {
    type: 'category',
    data: props.items.map((item) => `${item.month}月`),
    axisLine: {
      lineStyle: {
        color: '#d8c6b3'
      }
    },
    axisLabel: {
      color: 'var(--color-text-muted)',
      fontSize: 11
    },
    axisTick: {
      show: false
    }
  },
  yAxis: {
    type: 'value',
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    },
    splitLine: {
      lineStyle: {
        color: '#f0e6da'
      }
    },
    axisLabel: {
      color: 'var(--color-text-muted)',
      fontSize: 11
    }
  },
  series: [
    {
      name: '支出',
      type: 'bar',
      barMaxWidth: 18,
      data: props.items.map((item) => Number(item.expense.toFixed(2))),
      itemStyle: {
        borderRadius: [8, 8, 0, 0]
      }
    },
    {
      name: '收入',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 7,
      data: props.items.map((item) => Number(item.income.toFixed(2))),
      lineStyle: {
        width: 3
      },
      itemStyle: {
        borderWidth: 2,
        borderColor: '#ffffff'
      }
    }
  ]
}))

const distributionOption = computed<ChartOption>(() => ({
  color: ['var(--color-primary)'],
  animation: false,
  grid: {
    left: 24,
    right: 20,
    top: 16,
    bottom: 20,
    containLabel: true
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    confine: true,
    formatter(params: unknown) {
      const first = Array.isArray(params) ? params[0] : params
      const item = first as { name?: string; value?: number }
      return `${item.name || ''}: ${(item.value || 0).toFixed(2)}`
    }
  },
  xAxis: {
    type: 'value',
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    },
    splitLine: {
      lineStyle: {
        color: '#f0e6da'
      }
    },
    axisLabel: {
      color: 'var(--color-text-muted)',
      fontSize: 11
    }
  },
  yAxis: {
    type: 'category',
    data: activeDistributions.value.map((item) => item.label),
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    },
    axisLabel: {
      color: '#5e4b3a',
      fontSize: 11
    }
  },
  series: [
    {
      type: 'bar',
      barMaxWidth: 18,
      data: activeDistributions.value.map((item) => Number(item.amount.toFixed(2))),
      label: {
        show: true,
        position: 'right',
        color: 'var(--color-text-muted)',
        formatter(params: { dataIndex: number }) {
          const ratio = activeDistributions.value[params.dataIndex]?.ratio || 0
          return `${(ratio * 100).toFixed(1)}%`
        }
      },
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 1,
          y2: 0,
          colorStops: [
            { offset: 0, color: '#e3ae6c' },
            { offset: 1, color: 'var(--color-primary)' }
          ]
        },
        borderRadius: [0, 10, 10, 0]
      }
    }
  ]
}))
</script>

<style scoped lang="scss">
.year-charts {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.year-charts__card {
  padding: 24rpx 28rpx 28rpx;
  border-radius: 28rpx;
  background: var(--color-bg);
}

.year-charts__title {
  color: var(--color-text-primary);
  font-size: 30rpx;
  font-weight: 700;
}

.year-charts__desc {
  margin-top: 8rpx;
  color: var(--color-text-muted);
  font-size: 24rpx;
}

.year-charts__tabs {
  margin: 20rpx 0 8rpx;
  white-space: nowrap;
}

.year-charts__tab-row {
  display: inline-flex;
  gap: 12rpx;
}

.year-charts__tab {
  min-width: 92rpx;
  padding: 14rpx 20rpx;
  border-radius: 999rpx;
  background: #f5ecdf;
  color: var(--color-text-secondary);
  font-size: 24rpx;
  text-align: center;
}

.year-charts__tab--active {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  color: var(--color-bg);
  font-weight: 600;
}
</style>
