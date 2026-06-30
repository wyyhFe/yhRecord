<template>
  <view class="heatmap">
    <!-- 顶部统计 -->
    <view class="heatmap__stats">
      <view class="heatmap__stat">
        <text class="heatmap__stat-value">🔥 {{ data.currentStreak }}</text>
        <text class="heatmap__stat-label">连续打卡</text>
      </view>
      <view class="heatmap__stat">
        <text class="heatmap__stat-value">🏆 {{ data.bestStreak }}</text>
        <text class="heatmap__stat-label">历史最佳</text>
      </view>
      <view class="heatmap__stat">
        <text class="heatmap__stat-value">{{ data.monthCheckinDays }}/{{ data.monthTotalDays }}</text>
        <text class="heatmap__stat-label">本月打卡</text>
      </view>
    </view>

    <!-- 月份导航 -->
    <view class="heatmap__nav">
      <view class="heatmap__nav-btn" @tap="prevMonth">‹</view>
      <text class="heatmap__nav-title">{{ data.year }}年{{ data.month }}月</text>
      <view class="heatmap__nav-btn" @tap="nextMonth">›</view>
    </view>

    <!-- 星期表头 -->
    <view class="heatmap__weekdays">
      <text v-for="d in weekdays" :key="d" class="heatmap__weekday">{{ d }}</text>
    </view>

    <!-- 日历格子 -->
    <view class="heatmap__grid">
      <!-- 月初空白填充 -->
      <view v-for="i in leadingBlanks" :key="'blank-' + i" class="heatmap__cell heatmap__cell--empty" />
      <!-- 每天的格子 -->
      <view
        v-for="day in data.days"
        :key="day.date"
        class="heatmap__cell"
        :class="cellClass(day)"
        @tap="$emit('day-tap', day)"
      >
        <text class="heatmap__cell-num">{{ dayNum(day.date) }}</text>
      </view>
    </view>

    <!-- 色阶图例 -->
    <view class="heatmap__legend">
      <text class="heatmap__legend-label">少</text>
      <view class="heatmap__legend-cell heatmap__legend-cell--0" />
      <view class="heatmap__legend-cell heatmap__legend-cell--1" />
      <view class="heatmap__legend-cell heatmap__legend-cell--2" />
      <view class="heatmap__legend-cell heatmap__legend-cell--3" />
      <text class="heatmap__legend-label">多</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { HeatmapData, HeatmapDay } from '@/types/domain'

const props = defineProps<{
  data: HeatmapData
}>()

const emit = defineEmits<{
  'day-tap': [day: HeatmapDay]
  'month-change': [year: number, month: number]
}>()

const weekdays = ['一', '二', '三', '四', '五', '六', '日']

// 月初前面需要几个空白格（周一开始）
const leadingBlanks = computed(() => {
  if (!props.data.days.length) return 0
  const firstDay = new Date(props.data.days[0].date)
  // getDay(): 0=周日, 1=周一 ... 6=周六，转换为周一开始
  const day = firstDay.getDay()
  return day === 0 ? 6 : day - 1
})

function dayNum(date: string) {
  return Number(date.slice(8, 10))
}

function cellClass(day: HeatmapDay) {
  const classes: string[] = []
  const today = new Date().toISOString().slice(0, 10)
  if (day.date === today) classes.push('heatmap__cell--today')

  if (day.totalTasks === 0) {
    classes.push('heatmap__cell--empty')
  } else {
    const rate = day.completedTasks / day.totalTasks
    if (rate === 0) classes.push('heatmap__cell--0')
    else if (rate < 0.5) classes.push('heatmap__cell--1')
    else if (rate < 1) classes.push('heatmap__cell--2')
    else classes.push('heatmap__cell--3')
  }
  return classes
}

function prevMonth() {
  let { year, month } = props.data
  month--
  if (month < 1) { month = 12; year-- }
  emit('month-change', year, month)
}

function nextMonth() {
  let { year, month } = props.data
  month++
  if (month > 12) { month = 1; year++ }
  emit('month-change', year, month)
}
</script>

<style scoped lang="scss">
.heatmap {
  // 外层由 checkin-card 包裹，自身不需要背景和圆角
}

/* 统计栏 */
.heatmap__stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: var(--space-4);
}

.heatmap__stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
}

.heatmap__stat-value {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.heatmap__stat-label {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

/* 月份导航 */
.heatmap__nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}

.heatmap__nav-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
  font-size: 32rpx;
}

.heatmap__nav-title {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

/* 星期表头 */
.heatmap__weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: var(--space-2);
}

.heatmap__weekday {
  text-align: center;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

/* 日历格子 */
.heatmap__grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6rpx;
}

.heatmap__cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-tiny);
  transition: all var(--motion-fast) var(--ease-standard);
}

.heatmap__cell-num {
  font-size: 20rpx;
  font-weight: var(--weight-medium);
}

.heatmap__cell--empty {
  background: transparent;
}

.heatmap__cell--0 {
  background: var(--color-surface-soft);
  .heatmap__cell-num { color: var(--color-text-muted); }
}

.heatmap__cell--1 {
  background: rgba(232, 130, 122, 0.18);
  .heatmap__cell-num { color: var(--color-text-secondary); }
}

.heatmap__cell--2 {
  background: rgba(232, 130, 122, 0.45);
  .heatmap__cell-num { color: #fff; }
}

.heatmap__cell--3 {
  background: var(--color-checkin);
  .heatmap__cell-num { color: #fff; }
}

.heatmap__cell--today {
  box-shadow: 0 0 0 3rpx var(--color-checkin);
}

/* 色阶图例 */
.heatmap__legend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: var(--space-4);
}

.heatmap__legend-label {
  color: var(--color-text-muted);
  font-size: 18rpx;
}

.heatmap__legend-cell {
  width: 24rpx;
  height: 24rpx;
  border-radius: 6rpx;
}

.heatmap__legend-cell--0 { background: var(--color-surface-soft); }
.heatmap__legend-cell--1 { background: rgba(232, 130, 122, 0.18); }
.heatmap__legend-cell--2 { background: rgba(232, 130, 122, 0.45); }
.heatmap__legend-cell--3 { background: var(--color-checkin); }
</style>
