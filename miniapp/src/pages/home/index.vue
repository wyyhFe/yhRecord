<template>
  <view class="page-shell-safe home-page">
    <!-- 1. Masthead：编辑感日期条（左英文 / 右年份+第几天） -->
    <view class="home-masthead">
      <view class="home-masthead__col home-masthead__col--left">
        <view class="home-masthead__primary">{{ todayEN }}</view>
        <view class="home-masthead__secondary">{{ todayShort }} · {{ todayWeek }}</view>
      </view>
      <view class="home-masthead__rule" />
      <view class="home-masthead__col home-masthead__col--right">
        <view class="home-masthead__secondary">{{ now.getFullYear() }}</view>
        <view class="home-masthead__primary">NO. {{ dayOfYear }}</view>
      </view>
    </view>

    <!-- 2. Hero：超大字问候 -->
    <view class="home-hero">
      <view class="home-hero__greet">
        <text>{{ greeting }}</text><text class="home-hero__period">.</text>
      </view>
      <view class="home-hero__sub">
        把今天，认真留下来。<text class="home-hero__sub-em"> 你今年已经记到第 {{ recordedCount }} 天。</text>
      </view>
    </view>

    <!-- 3. 最近七天 · 编辑感时间轴（号数 + 周几 + 状态点 + 底线） -->
    <view class="home-section">
      <view class="home-section__head">
        <view class="home-section__no">01</view>
        <view class="home-section__title">最近七天</view>
        <view class="home-section__legend">实心 = 有日记</view>
      </view>

      <view class="home-timeline">
        <view
          v-for="item in calendarItems"
          :key="item.date"
          class="home-tl-day"
          :class="{ 'home-tl-day--today': isToday(item.date) }"
        >
          <view class="home-tl-day__num">{{ dayNum(item.date) }}</view>
          <view class="home-tl-day__week">{{ weekShort(item.date) }}</view>
          <view
            class="home-tl-day__dot"
            :class="{ 'home-tl-day__dot--on': item.hasDiary }"
          />
        </view>
        <view class="home-timeline__axis" />
      </view>
    </view>

    <!-- 4. 今日 · 目录式数据列表 -->
    <view class="home-section">
      <view class="home-section__head">
        <view class="home-section__no">02</view>
        <view class="home-section__title">今日</view>
        <view class="home-section__legend">{{ todayShort }}</view>
      </view>

      <view class="home-stats">
        <view
          v-for="(m, idx) in metrics"
          :key="m.label"
          class="home-stat"
          :class="{ 'home-stat--first': idx === 0 }"
        >
          <view class="home-stat__label">{{ m.label }}</view>
          <view class="home-stat__dots" />
          <view class="home-stat__right">
            <view class="home-stat__value">{{ m.value }}</view>
            <view class="home-stat__hint">{{ m.hint }}</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 5. 入口 · 目录式行 -->
    <view class="home-section">
      <view class="home-section__head">
        <view class="home-section__no">03</view>
        <view class="home-section__title">入口</view>
        <view class="home-section__legend">高频动作</view>
      </view>

      <view class="home-menu">
        <view
          v-for="(item, idx) in quickActions"
          :key="item.key"
          class="home-menu-row"
          :class="{ 'home-menu-row--first': idx === 0 }"
          hover-class="home-menu-row--pressed"
          :hover-stay-time="120"
          @click="handleQuickAction(item.key, item.path)"
        >
          <view class="home-menu-row__index">{{ String(idx + 1).padStart(2, '0') }}</view>
          <view class="home-menu-row__body">
            <view class="home-menu-row__title">{{ item.title }}</view>
            <view class="home-menu-row__desc">{{ item.description }}</view>
          </view>
          <view class="home-menu-row__arrow">→</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useGreeting } from '@/composables/useGreeting'
import { fetchCalendarSummary } from '@/api/calendar'
import type { DaySummary } from '@/types/domain'
import { tokenStorage } from '@/utils/storage'
import { getLastLedgerBook } from '@/utils/ledger-book'

const greeting = useGreeting()
const calendarItems = ref<DaySummary[]>([])

const quickActions = [
  {
    key: 'diary',
    title: '写日记',
    description: '记录天气、心情、图片和位置。',
    path: '/pages/diary/editor'
  },
  {
    key: 'ledger',
    title: '记一笔',
    description: '优先进入最近使用的账本，没有账本就先去账本管理。',
    path: '/pages/ledger/index'
  },
  {
    key: 'checkin',
    title: '打卡任务',
    description: '把重复动作固定成习惯节律。',
    path: '/pages/checkin/editor'
  },
  {
    key: 'memory',
    title: '去年今日',
    description: '快速回看同一天的内容。',
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

// 近七天里"有日记"的天数（masthead 副标题用）
const recordedCount = computed(
  () => calendarItems.value.filter((item) => item.hasDiary).length
)

// === 日期辅助 ===
const WEEK_LABEL = ['日', '一', '二', '三', '四', '五', '六']
const WEEK_EN = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT']
const MONTH_EN = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC']

function pad2(n: number) {
  return String(n).padStart(2, '0')
}

const now = new Date()
const todayISO = `${now.getFullYear()}-${pad2(now.getMonth() + 1)}-${pad2(now.getDate())}`
const todayShort = `${now.getMonth() + 1}月${now.getDate()}日`
const todayWeek = `周${WEEK_LABEL[now.getDay()]}`
const todayEN = `${MONTH_EN[now.getMonth()]} ${pad2(now.getDate())}  ${WEEK_EN[now.getDay()]}`

// 今天是一年的第几天（masthead 右侧 NO.xxx 用）
function getDayOfYear(d: Date) {
  const start = new Date(d.getFullYear(), 0, 0)
  const diff = d.getTime() - start.getTime()
  return Math.floor(diff / (1000 * 60 * 60 * 24))
}
const dayOfYear = String(getDayOfYear(now)).padStart(3, '0')

function dayNum(date: string) {
  return Number(date.slice(8, 10))
}

function weekShort(date: string) {
  // YYYY-MM-DD 是 ISO 兼容写法，跨端解析稳定
  return WEEK_LABEL[new Date(date).getDay()]
}

function isToday(date: string) {
  return date === todayISO
}

function buildFallbackSummary() {
  // 未登录或拉取失败的兜底：围绕"今天"前后回推 7 天，保证"今天"标记不会错位
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
    const result = await fetchCalendarSummary(now.getFullYear(), now.getMonth() + 1)
    calendarItems.value = result.days.slice(-7)
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

onLoad(() => {
  loadSummary()
})
</script>

<style scoped lang="scss">
/* ============================================================
 * Home 页 v2 · 「杂志编辑感」
 * 设计核心：去装饰，纯靠横线 / 字号 / 对齐做节奏。
 * 每个 section 顶部一条粗规则线 + 编号「01」+ 标题，
 * 内部全部用 hairline + 大数字 + 排版对齐表达层次。
 * ========================================================= */

.home-page {
  font-feature-settings: 'tnum';
}

/* ---------- 1. Masthead ---------- */
.home-masthead {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0 var(--space-2);
}

.home-masthead__col {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  gap: 2rpx;
}

.home-masthead__col--right {
  align-items: flex-end;
}

.home-masthead__primary {
  color: var(--color-mark);
  font-size: var(--font-meta);
  font-weight: var(--weight-bold);
  letter-spacing: 3rpx;
  font-variant-numeric: tabular-nums;
}

.home-masthead__secondary {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  letter-spacing: 2rpx;
  font-variant-numeric: tabular-nums;
}

.home-masthead__rule {
  flex: 1;
  height: 1rpx;
  background: var(--color-divider);
}

/* ---------- 2. Hero ---------- */
.home-hero {
  margin-top: var(--space-7);
  margin-bottom: var(--space-8);
}

.home-hero__greet {
  display: inline-flex;
  align-items: baseline;
  color: var(--color-text-primary);
  font-size: var(--font-hero);
  font-weight: var(--weight-bold);
  line-height: 1.05;
  letter-spacing: -1rpx;
}

.home-hero__period {
  color: var(--color-mark);
  margin-left: -4rpx;
}

.home-hero__sub {
  margin-top: var(--space-4);
  max-width: 540rpx;
  color: var(--color-text-secondary);
  font-size: var(--font-body);
  line-height: var(--leading-loose);
}

.home-hero__sub-em {
  color: var(--color-text-primary);
  font-weight: var(--weight-semibold);
}

/* ---------- 3. Section 通用骨架 ---------- */
.home-section {
  margin-top: var(--space-7);
}

.home-section__head {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
  padding-bottom: var(--space-3);
  border-bottom: 2rpx solid var(--color-text-primary);
}

.home-section__no {
  flex: 0 0 auto;
  color: var(--color-mark);
  font-size: var(--font-meta);
  font-weight: var(--weight-bold);
  letter-spacing: 2rpx;
  font-variant-numeric: tabular-nums;
}

.home-section__title {
  flex: 0 0 auto;
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
  letter-spacing: 1rpx;
}

.home-section__legend {
  flex: 1;
  text-align: right;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  letter-spacing: 1rpx;
  font-variant-numeric: tabular-nums;
}

/* ---------- 3. Timeline ---------- */
.home-timeline {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: var(--space-5);
  padding-bottom: var(--space-4);
}

.home-tl-day {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
}

.home-tl-day__num {
  color: var(--color-text-secondary);
  font-size: var(--font-section);
  font-weight: var(--weight-semibold);
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.home-tl-day__week {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  letter-spacing: 1rpx;
}

.home-tl-day__dot {
  margin-top: var(--space-2);
  width: 14rpx;
  height: 14rpx;
  border-radius: var(--radius-full);
  border: 1rpx solid var(--color-mark);
  background: transparent;
}

.home-tl-day__dot--on {
  background: var(--color-mark);
}

/* 今天：号数变 primary + 加大字 + 加粗周几 + 点变大 */
.home-tl-day--today .home-tl-day__num {
  color: var(--color-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.home-tl-day--today .home-tl-day__week {
  color: var(--color-primary);
  font-weight: var(--weight-semibold);
}

.home-tl-day--today .home-tl-day__dot {
  width: 18rpx;
  height: 18rpx;
  border-color: var(--color-primary);
}

.home-tl-day--today .home-tl-day__dot.home-tl-day__dot--on {
  background: var(--color-primary);
}

/* 底部时间轴线 */
.home-timeline__axis {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1rpx;
  background: var(--color-divider);
}

/* ---------- 4. Stats（目录式列表） ---------- */
.home-stats {
  margin-top: var(--space-3);
}

.home-stat {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5) 0;
  border-bottom: 1rpx solid var(--color-divider);
}

.home-stat__label {
  flex: 0 0 auto;
  width: 96rpx;
  color: var(--color-text-secondary);
  font-size: var(--font-body);
  font-weight: var(--weight-medium);
  letter-spacing: 2rpx;
}

/* "目录"风的虚线连接 —— 用 radial-gradient 模拟点状 leader */
.home-stat__dots {
  flex: 1;
  align-self: center;
  height: 4rpx;
  background-image: radial-gradient(circle, var(--color-text-muted) 1rpx, transparent 1.5rpx);
  background-size: 12rpx 4rpx;
  background-repeat: repeat-x;
  background-position: 0 50%;
  opacity: 0.5;
}

.home-stat__right {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2rpx;
}

.home-stat__value {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
  line-height: 1;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.5rpx;
}

.home-stat__hint {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  letter-spacing: 1rpx;
  font-variant-numeric: tabular-nums;
}

/* ---------- 5. Menu（目录式入口） ---------- */
.home-menu {
  margin-top: var(--space-1);
}

.home-menu-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-2);
  border-bottom: 1rpx solid var(--color-divider);
  transition: background var(--motion-fast) var(--ease-standard);
}

.home-menu-row--pressed {
  background: var(--color-primary-soft);
}

.home-menu-row__index {
  flex: 0 0 auto;
  width: 64rpx;
  color: var(--color-mark);
  font-size: var(--font-caption);
  font-weight: var(--weight-bold);
  letter-spacing: 3rpx;
  font-variant-numeric: tabular-nums;
}

.home-menu-row__body {
  flex: 1;
  min-width: 0;
}

.home-menu-row__title {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-bold);
  line-height: var(--leading-tight);
  letter-spacing: 1rpx;
}

.home-menu-row__desc {
  margin-top: var(--space-1);
  color: var(--color-text-muted);
  font-size: var(--font-meta);
  line-height: var(--leading-relaxed);
}

.home-menu-row__arrow {
  flex: 0 0 auto;
  color: var(--color-mark);
  font-size: var(--font-section);
  font-weight: var(--weight-medium);
  opacity: 0.5;
  transition: transform var(--motion-fast) var(--ease-standard),
              opacity var(--motion-fast) var(--ease-standard);
}

.home-menu-row--pressed .home-menu-row__arrow {
  transform: translateX(8rpx);
  opacity: 1;
}
</style>
