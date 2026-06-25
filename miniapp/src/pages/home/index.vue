<template>
  <view class="page-shell-safe home-page">
    <!-- 1. Hero 渐变头部 -->
    <view class="home-hero">
      <view class="home-hero__top">
        <view class="home-hero__date">
          <text class="home-hero__date-en">{{ todayEN }}</text>
          <text class="home-hero__date-dot">·</text>
          <text class="home-hero__date-cn">{{ todayShort }}</text>
        </view>
      </view>
      <view class="home-hero__greeting">
        <text class="home-hero__greeting-text">{{ greeting }}.</text>
      </view>
      <view class="home-hero__sub">
        把今天，认真留下来。
        <text class="home-hero__sub-count">今年已记 {{ recordedCount }} 天</text>
      </view>

      <!-- 快捷入口浮在 Hero 底部 -->
      <view class="home-shortcuts">
        <view
          v-for="item in quickActions"
          :key="item.key"
          class="home-shortcuts__item"
          hover-class="home-shortcuts__item--pressed"
          :hover-stay-time="120"
          @click="handleQuickAction(item.key, item.path)"
        >
          <view class="home-shortcuts__icon" :style="{ background: item.gradient }">
            <text class="home-shortcuts__icon-text">{{ item.icon }}</text>
          </view>
          <view class="home-shortcuts__label">{{ item.title }}</view>
        </view>
      </view>
    </view>

    <!-- 1.5 功能入口卡片 -->
    <view class="home-entries">
      <view
        class="home-entries__card home-entries__card--diary"
        hover-class="home-entries__card--pressed"
        :hover-stay-time="120"
        @click="goDiaryList"
      >
        <view class="home-entries__icon">
          <text class="home-entries__icon-text">📖</text>
        </view>
        <view class="home-entries__info">
          <text class="home-entries__title">日记本</text>
          <text class="home-entries__desc">记录每一天的故事</text>
        </view>
        <text class="home-entries__arrow">›</text>
      </view>
      <view
        class="home-entries__card home-entries__card--checkin"
        hover-class="home-entries__card--pressed"
        :hover-stay-time="120"
        @click="goCheckinList"
      >
        <view class="home-entries__icon">
          <text class="home-entries__icon-text">✓</text>
        </view>
        <view class="home-entries__info">
          <text class="home-entries__title">打卡</text>
          <text class="home-entries__desc">坚持每天的好习惯</text>
        </view>
        <text class="home-entries__arrow">›</text>
      </view>
    </view>

    <!-- 2. 近七天时间轴 -->
    <view class="home-card">
      <view class="home-card__row">
        <text class="home-card__title">近七天</text>
        <text class="home-card__hint">实心 = 有记录</text>
      </view>
      <view class="home-timeline">
        <view
          v-for="item in calendarItems"
          :key="item.date"
          class="home-timeline__day"
          :class="{ 'home-timeline__day--today': isToday(item.date) }"
        >
          <view class="home-timeline__num">{{ dayNum(item.date) }}</view>
          <view class="home-timeline__week">{{ weekShort(item.date) }}</view>
          <view
            class="home-timeline__dot"
            :class="{ 'home-timeline__dot--on': item.hasDiary }"
          />
        </view>
      </view>
    </view>

    <!-- 3. 今日概览 -->
    <view class="home-card">
      <view class="home-card__row">
        <text class="home-card__title">今日</text>
        <text class="home-card__hint">{{ todayShort }}</text>
      </view>
      <view class="home-overview">
        <view
          v-for="(m, idx) in metrics"
          :key="m.label"
          class="home-overview__item"
        >
          <view class="home-overview__indicator" :style="{ background: overviewColors[idx] }" />
          <view class="home-overview__info">
            <text class="home-overview__label">{{ m.label }}</text>
            <text class="home-overview__value">{{ m.value }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 自定义 TabBar -->
    <TabBar current="home" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useGreeting } from '@/composables/useGreeting'
import { fetchCalendarSummaryRecent } from '@/api/calendar'
import type { DaySummary } from '@/types/domain'
import { tokenStorage } from '@/utils/storage'
import { getLastLedgerBook } from '@/utils/ledger-book'
import TabBar from '@/components/business/tab-bar/index.vue'

const greeting = useGreeting()
const calendarItems = ref<DaySummary[]>([])

const overviewColors = [
  'var(--color-primary-gradient)',
  'var(--color-ledger-gradient)',
  'var(--color-diary-gradient)'
]

const quickActions = [
  {
    key: 'diary',
    title: '写日记',
    icon: '✏',
    gradient: 'var(--color-diary-gradient)',
    path: '/pages/diary/editor'
  },
  {
    key: 'ledger',
    title: '记一笔',
    icon: '¥',
    gradient: 'var(--color-ledger-gradient)',
    path: '/pages/ledger/index'
  },
  {
    key: 'checkin',
    title: '打卡',
    icon: '✓',
    gradient: 'var(--color-checkin-gradient)',
    path: '/pages/checkin/editor'
  },
  {
    key: 'memory',
    title: '回忆',
    icon: '◈',
    gradient: 'var(--color-memory-gradient)',
    path: '/pages/memory/index'
  }
]

const metrics = computed(() => {
  const today = calendarItems.value.at(-1)
  return [
    {
      label: '日记',
      value: today?.hasDiary ? '已记录' : '待记录',
      hint: `数量 ${today?.diaryCount ?? 0}`
    },
    {
      label: '打卡',
      value: today?.hasCheckin ? '已完成' : '待完成',
      hint: `次数 ${today?.checkinCount ?? 0}`
    },
    {
      label: '回忆',
      value: `${calendarItems.value.filter((item) => item.hasDiary).length}`,
      hint: '近七天有日记'
    }
  ]
})

const recordedCount = computed(
  () => calendarItems.value.filter((item) => item.hasDiary).length
)

const WEEK_LABEL = ['日', '一', '二', '三', '四', '五', '六']
const WEEK_EN = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT']
const MONTH_EN = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC']

function pad2(n: number) {
  return String(n).padStart(2, '0')
}

const now = new Date()
const todayISO = `${now.getFullYear()}-${pad2(now.getMonth() + 1)}-${pad2(now.getDate())}`
const todayShort = `${now.getMonth() + 1}月${now.getDate()}日`
const todayEN = `${MONTH_EN[now.getMonth()]} ${pad2(now.getDate())}`

function dayNum(date: string) {
  return Number(date.slice(8, 10))
}

function weekShort(date: string) {
  return WEEK_LABEL[new Date(date).getDay()]
}

function isToday(date: string) {
  return date === todayISO
}

function buildFallbackSummary() {
  calendarItems.value = Array.from({ length: 7 }).map((_, index) => {
    const d = new Date(now)
    d.setDate(d.getDate() - (6 - index))
    const date = `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
    return {
      date,
      hasDiary: index % 2 === 0,
      diaryCount: index % 2 === 0 ? 1 : 0,
      hasCheckin: index % 3 !== 0,
      checkinCount: index % 3 !== 0 ? 1 : 0,
      memorialCount: 0
    }
  })
}

async function loadSummary() {
  if (!tokenStorage.getAccessToken()) {
    buildFallbackSummary()
    return
  }

  try {
    const res = await fetchCalendarSummaryRecent(7)
    // 按日期升序排列，确保时间轴从左到右是过去→今天
    const sorted = [...res.days].sort((a, b) => a.date.localeCompare(b.date))
    calendarItems.value = sorted
  } catch {
    buildFallbackSummary()
  }
}

function handleQuickAction(key: string, path: string) {
  if (key === 'ledger') {
    const lastBook = getLastLedgerBook()
    if (lastBook) {
      uni.navigateTo({
        url: `/pages/ledger/index?bookId=${lastBook.id}&bookName=${encodeURIComponent(lastBook.name)}&openEntry=1`
      })
      return
    }
    uni.navigateTo({ url: '/pages/ledger/books' })
    return
  }

  uni.navigateTo({ url: path })
}

function goDiaryList() {
  uni.navigateTo({ url: '/pages/diary/index' })
}

function goCheckinList() {
  uni.navigateTo({ url: '/pages/checkin/index' })
}

onLoad(() => {
  loadSummary()
})

onShow(() => {
  uni.hideTabBar({ animation: false })
})
</script>

<style scoped lang="scss">
/* ============================================================
 * Home v4 · Warm Modern
 * 渐变 Hero + 彩色快捷入口 + 精致卡片
 * ========================================================= */

.home-page {
  padding-bottom: var(--bottom-padding-with-tabbar);
}

/* ---------- 1. Hero 渐变头部 ---------- */
.home-hero {
  background: var(--hero-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-7) var(--space-6) var(--space-9);
  position: relative;
}

.home-hero__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.home-hero__date {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.home-hero__date-en {
  color: var(--color-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.home-hero__date-dot {
  color: var(--color-primary);
  font-size: var(--font-caption);
  opacity: 0.5;
}

.home-hero__date-cn {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
}

.home-hero__greeting {
  margin-top: var(--space-5);
}

.home-hero__greeting-text {
  color: var(--color-text-primary);
  font-size: var(--font-hero);
  font-weight: var(--weight-bold);
  line-height: 1.1;
  letter-spacing: -1rpx;
}

.home-hero__sub {
  margin-top: var(--space-3);
  color: var(--color-text-secondary);
  font-size: var(--font-body);
  line-height: var(--leading-relaxed);
}

.home-hero__sub-count {
  color: var(--color-primary);
  font-weight: var(--weight-semibold);
}

/* ---------- 2. 快捷入口（浮在 Hero 底部） ---------- */
.home-shortcuts {
  display: flex;
  justify-content: space-between;
  margin-top: var(--space-7);
}

.home-shortcuts__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  transition: all var(--motion-fast) var(--ease-standard);
}

.home-shortcuts__item--pressed {
  transform: scale(0.92);
}

.home-shortcuts__icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: var(--radius-large);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
  transition: all var(--motion-fast) var(--ease-standard);
}

.home-shortcuts__icon-text {
  color: #FFFFFF;
  font-size: 40rpx;
  font-weight: var(--weight-bold);
  line-height: 1;
}

.home-shortcuts__label {
  color: var(--color-text-primary);
  font-size: var(--font-caption);
  font-weight: var(--weight-medium);
}

/* ---------- 1.5 功能入口卡片 ---------- */
.home-entries {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-top: var(--space-4);
  padding: 0 var(--space-4);
}

.home-entries__card {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  padding: var(--space-5) var(--space-6);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  transition: all var(--motion-fast) var(--ease-standard);
}

.home-entries__card--diary {
  background: var(--color-surface);
}

.home-entries__card--checkin {
  background: var(--color-surface);
}

.home-entries__card--pressed {
  transform: scale(0.97);
  box-shadow: var(--shadow-press);
}

.home-entries__icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: var(--radius-medium);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.home-entries__card--diary .home-entries__icon {
  background: var(--color-diary-soft);
}

.home-entries__card--checkin .home-entries__icon {
  background: var(--color-checkin-soft);
}

.home-entries__icon-text {
  font-size: 36rpx;
  line-height: 1;
}

.home-entries__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.home-entries__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.home-entries__desc {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.home-entries__arrow {
  color: var(--color-text-muted);
  font-size: var(--font-title);
  font-weight: var(--weight-regular);
}

/* ---------- 3. 通用卡片 ---------- */
.home-card {
  margin-top: var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5) var(--space-6);
}

.home-card__row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: var(--space-5);
}

.home-card__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.home-card__hint {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

/* ---------- 4. 时间轴 ---------- */
.home-timeline {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.home-timeline__day {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding-bottom: var(--space-2);
}

.home-timeline__num {
  color: var(--color-text-secondary);
  font-size: var(--font-title);
  font-weight: var(--weight-medium);
  line-height: 1;
}

.home-timeline__week {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.home-timeline__dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: var(--radius-full);
  background: var(--color-divider);
}

.home-timeline__dot--on {
  background: var(--color-primary);
  box-shadow: 0 0 0 6rpx var(--color-primary-soft);
}

.home-timeline__day--today .home-timeline__num {
  color: var(--color-primary);
  font-size: var(--font-display);
  font-weight: var(--weight-bold);
}

.home-timeline__day--today .home-timeline__week {
  color: var(--color-primary);
  font-weight: var(--weight-semibold);
}

.home-timeline__day--today .home-timeline__dot {
  width: 16rpx;
  height: 16rpx;
  background: var(--color-primary);
  box-shadow: 0 0 0 6rpx var(--color-primary-soft);
}

/* ---------- 5. 今日概览 ---------- */
.home-overview {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.home-overview__item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
}

.home-overview__indicator {
  width: 12rpx;
  height: 48rpx;
  border-radius: 6rpx;
  flex-shrink: 0;
}

.home-overview__info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
}

.home-overview__label {
  color: var(--color-text-secondary);
  font-size: var(--font-body);
  font-weight: var(--weight-medium);
}

.home-overview__value {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-bold);
}
</style>
