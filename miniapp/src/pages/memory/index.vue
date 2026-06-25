<template>
  <view class="page-shell-safe memory-page">
    <!-- Hero -->
    <view class="memory-hero">
      <view class="memory-hero__top">
        <text class="memory-hero__date">{{ targetDate }}</text>
      </view>
      <view class="memory-hero__title">去年今日</view>
      <view class="memory-hero__sub">回看去年同一天的日记、打卡和纪念日</view>
      <view class="memory-hero__stats">
        <view class="memory-hero__stat">
          <text class="memory-hero__stat-value">{{ detail.diaries.length }}</text>
          <text class="memory-hero__stat-label">日记</text>
        </view>
        <view class="memory-hero__stat">
          <text class="memory-hero__stat-value">{{ detail.checkins.length }}</text>
          <text class="memory-hero__stat-label">打卡</text>
        </view>
        <view class="memory-hero__stat">
          <text class="memory-hero__stat-value">{{ detail.memorialDays.length }}</text>
          <text class="memory-hero__stat-label">纪念日</text>
        </view>
      </view>
    </view>

    <!-- 日记 -->
    <view class="memory-card">
      <view class="memory-card__header">
        <text class="memory-card__title">📝 日记</text>
        <text class="memory-card__badge">{{ detail.diaries.length }}</text>
      </view>
      <view v-if="detail.diaries.length" class="memory-list">
        <view v-for="item in detail.diaries" :key="item.id" class="memory-diary" @tap="goDiaryDetail(item.id)">
          <text class="memory-diary__title">{{ item.title }}</text>
          <text class="memory-diary__meta">{{ resolveDiaryMoodLabel(item.mood, '平静') }} · {{ item.recordDate }}</text>
          <text class="memory-diary__content">{{ item.content }}</text>
        </view>
      </view>
      <EmptyStateCard v-else title="去年今天没有日记" description="等你留下更多内容后会自动展示" mode="history" />
    </view>

    <!-- 打卡 -->
    <view class="memory-card">
      <view class="memory-card__header">
        <text class="memory-card__title">✅ 打卡</text>
        <text class="memory-card__badge">{{ detail.checkins.length }}</text>
      </view>
      <view v-if="detail.checkins.length" class="memory-list">
        <view v-for="item in detail.checkins" :key="item.id" class="memory-checkin">
          <view class="memory-checkin__left">
            <text class="memory-checkin__name">{{ item.name }}</text>
            <text class="memory-checkin__meta">{{ item.description || '暂无描述' }}</text>
          </view>
          <view class="memory-checkin__count">
            <text class="memory-checkin__count-text">{{ item.totalCount }} 次</text>
          </view>
        </view>
      </view>
      <EmptyStateCard v-else title="去年今天没有打卡" description="如果那一天完成过任务，这里会展示" mode="history" />
    </view>

    <!-- 纪念日 -->
    <view class="memory-card">
      <view class="memory-card__header">
        <text class="memory-card__title">📅 纪念日</text>
        <text class="memory-card__badge">{{ detail.memorialDays.length }}</text>
      </view>
      <view v-if="detail.memorialDays.length" class="memory-list">
        <view v-for="item in detail.memorialDays" :key="item.id" class="memory-memorial">
          <text class="memory-memorial__title">{{ item.title }}</text>
          <text class="memory-memorial__meta">{{ item.type || '纪念日' }} · {{ item.memorialDate }}</text>
          <text v-if="item.remark" class="memory-memorial__remark">{{ item.remark }}</text>
        </view>
      </view>
      <EmptyStateCard v-else title="去年今天没有纪念日" description="命中纪念日规则后会自动展示" mode="history" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import { fetchOnThisDay } from '@/api/calendar'
import type { CalendarDayDetail, Id } from '@/types/domain'
import { resolveDiaryMoodLabel } from '@/utils/diary-display'

const defaultDate = new Date()
const queryDate = reactive({ value: defaultDate.toISOString().slice(0, 10) })
const detail = reactive<CalendarDayDetail>({ date: '', diaries: [], checkins: [], memorialDays: [] })

const targetDate = computed(() => detail.date || fallbackDateLabel())

function fallbackDateLabel() {
  const date = new Date(`${queryDate.value}T00:00:00`)
  date.setFullYear(date.getFullYear() - 1)
  return date.toISOString().slice(0, 10)
}

function goDiaryDetail(id: Id) { uni.navigateTo({ url: `/pages/diary/detail?id=${id}` }) }

async function loadOnThisDay() {
  try {
    const result = await fetchOnThisDay(queryDate.value)
    detail.date = result.date
    detail.diaries = result.diaries || []
    detail.checkins = result.checkins || []
    detail.memorialDays = result.memorialDays || []
  } catch (error) {
    detail.date = fallbackDateLabel()
    detail.diaries = []
    detail.checkins = []
    detail.memorialDays = []
    uni.$feedback.error(error, undefined, '加载去年今日失败')
  }
}

onLoad((query) => { if (query?.date) queryDate.value = String(query.date) })
onShow(() => { loadOnThisDay() })
</script>

<style scoped lang="scss">
.memory-page {
  padding-bottom: var(--bottom-padding);
}

/* ========== Hero ========== */
.memory-hero {
  background: var(--color-memory-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-7) var(--space-6) var(--space-8);
  color: #fff;
}

.memory-hero__top {
  margin-bottom: var(--space-2);
}

.memory-hero__date {
  font-size: var(--font-meta);
  opacity: 0.8;
}

.memory-hero__title {
  font-size: var(--font-hero);
  font-weight: var(--weight-bold);
  line-height: 1.1;
}

.memory-hero__sub {
  margin-top: var(--space-3);
  font-size: var(--font-body);
  opacity: 0.85;
}

.memory-hero__stats {
  margin-top: var(--space-5);
  display: flex;
  background: rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-medium);
  padding: var(--space-3) 0;
}

.memory-hero__stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.memory-hero__stat-value {
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.memory-hero__stat-label {
  font-size: var(--font-tiny);
  opacity: 0.8;
}

/* ========== 卡片 ========== */
.memory-card {
  margin: var(--space-4) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.memory-card__header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.memory-card__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.memory-card__badge {
  padding: 2rpx 14rpx;
  border-radius: var(--radius-full);
  background: var(--color-memory-soft);
  color: var(--color-memory);
  font-size: var(--font-tiny);
  font-weight: var(--weight-semibold);
}

/* ========== 列表 ========== */
.memory-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.memory-diary {
  padding: var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
}

.memory-diary__title {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.memory-diary__meta {
  display: block;
  margin-top: 4rpx;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.memory-diary__content {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-caption);
  line-height: var(--leading-relaxed);
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.memory-checkin {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
}

.memory-checkin__left {
  flex: 1;
  min-width: 0;
}

.memory-checkin__name {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.memory-checkin__meta {
  display: block;
  margin-top: 4rpx;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.memory-checkin__count {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  flex-shrink: 0;
  margin-left: var(--space-3);
}

.memory-checkin__count-text {
  color: var(--color-checkin);
  font-size: var(--font-tiny);
  font-weight: var(--weight-semibold);
}

.memory-memorial {
  padding: var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
}

.memory-memorial__title {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.memory-memorial__meta {
  display: block;
  margin-top: 4rpx;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.memory-memorial__remark {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-caption);
}
</style>
