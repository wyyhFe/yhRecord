<template>
  <view class="home-page">
    <!-- 1. Hero 渐变头部 -->
    <view class="home-hero">
      <!-- 轮播图 + 浮在左上角的扫码按钮 -->
      <view class="home-hero__banner" :style="{ paddingTop: (statusBarHeight ) + 'px' }">
        <!-- 扫码按钮浮在轮播图左上角，与微信胶囊按钮同行 -->
        <view class="home-hero__scan" hover-class="home-hero__scan--pressed" @tap="handleScanLogin"
              :style="{ top: (statusBarHeight + 8) + 'px' }">
          <image class="home-hero__scan-icon" src="/static/sys.png" mode="aspectFit" />
          <text class="home-hero__scan-text">扫一扫</text>
        </view>

        <swiper class="home-hero__swiper" :indicator-dots="true" :autoplay="false" :circular="true">
          <swiper-item>
            <view class="home-hero__swiper-slide" style="background: linear-gradient(135deg, #c47c52, #e8b896);">
              <text class="home-hero__swiper-title">欢迎回来 👋</text>
              <text class="home-hero__swiper-desc">记录生活的每一个瞬间</text>
            </view>
          </swiper-item>
          <swiper-item>
            <view class="home-hero__swiper-slide" style="background: linear-gradient(135deg, #6b8f71, #a3c9a8);">
              <text class="home-hero__swiper-title">今天打卡了吗？</text>
              <text class="home-hero__swiper-desc">坚持就是胜利 ✊</text>
            </view>
          </swiper-item>
        </swiper>
      </view>

      <view class="home-hero__bottom">
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

  <!-- 全局登录弹窗 -->
  <LoginSheet v-model="showLogin" @success="handleLoginSuccess" />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onLoad, onPullDownRefresh, onShow, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { useGreeting } from '@/composables/useGreeting'
import { fetchCalendarSummaryRecent, fetchYearlyDiaryCount } from '@/api/calendar'
import type { DaySummary } from '@/types/domain'
import { tokenStorage } from '@/utils/storage'
import { getLastLedgerBook } from '@/utils/ledger-book'
import { confirmQrLogin } from '@/api/auth'
import LoginSheet from '@/components/business/login-sheet'
import TabBar from '@/components/business/tab-bar/index.vue'

onShareAppMessage(() => ({ title: '把今天，认真留下来' }))
onShareTimeline(() => ({ title: '把今天，认真留下来' }))

const greeting = useGreeting()
const statusBarHeight = ref(20)
uni.getSystemInfo({
  success: (info: any) => { statusBarHeight.value = info.statusBarHeight || 20 }
})

/** 首页扫码登录管理后台 */
async function handleScanLogin() {
  if (!tokenStorage.getAccessToken()) {
    showLogin.value = true
    return
  }
  try {
    const res = await uni.scanCode({ onlyFromCamera: true, scanType: ['qrCode'] })
    const text = res.result || ''
    if (!text.startsWith('LR_LOGIN:')) {
      uni.showToast({ title: '无效的二维码', icon: 'none' })
      return
    }
    const confirmRes = await uni.showModal({
      title: '确认登录',
      content: '确认在浏览器端登录管理后台？'
    })
    if (!confirmRes.confirm) return
    await confirmQrLogin(text.slice('LR_LOGIN:'.length))
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (err: any) {
    if (err?.errMsg?.includes('cancel')) return
    uni.$feedback.error(err, undefined, '扫码登录失败')
    console.error('[QrLogin-Home]', err)
  }
}
const calendarItems = ref<DaySummary[]>([])
const recordedCount = ref(0)
const showLogin = ref(false)
let initialLoad = true

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
    const [res, yearCount] = await Promise.all([
      fetchCalendarSummaryRecent(7),
      fetchYearlyDiaryCount()
    ])
    // 按日期升序排列，确保时间轴从左到右是过去→今天
    const sorted = [...res.days].sort((a, b) => a.date.localeCompare(b.date))
    calendarItems.value = sorted
    recordedCount.value = yearCount
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

function handleLoginSuccess() {
  loadSummary()
}

/**
 * 从微信扫一扫打开的 URL 中提取 QR 登录 token。
 * 微信扫一扫会将完整的二维码 URL 通过 `q` 参数传给小程序。
 */
async function handleQrScanParam(q: string) {
  const match = q.match(/[?&]token=([^&\s#]+)/)
  const qrToken = match ? decodeURIComponent(match[1]) : null
  if (!qrToken) return

  // 需要先登录
  if (!tokenStorage.getAccessToken()) {
    showLogin.value = true
    // 登录成功后自动处理
    const unwatch = watch(showLogin, async (val) => {
      if (!val && tokenStorage.getAccessToken()) {
        unwatch()
        await doConfirmQrLogin(qrToken)
      }
    })
    return
  }
  await doConfirmQrLogin(qrToken)
}

/** URL Link 直接传入 token（无需解析 URL） */
async function handleQrScanParamDirect(qrToken: string) {
  if (!tokenStorage.getAccessToken()) {
    showLogin.value = true
    const unwatch = watch(showLogin, async (val) => {
      if (!val && tokenStorage.getAccessToken()) {
        unwatch()
        await doConfirmQrLogin(qrToken)
      }
    })
    return
  }
  await doConfirmQrLogin(qrToken)
}

async function doConfirmQrLogin(qrToken: string) {
  try {
    const confirmRes = await uni.showModal({
      title: '确认登录',
      content: '确认在浏览器端登录管理后台？'
    })
    if (!confirmRes.confirm) return
    await confirmQrLogin(qrToken)
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (err: any) {
    uni.$feedback.error(err, undefined, '扫码登录失败')
    console.error('[QrLogin-Home]', err)
  }
}

onLoad((query) => {
  if (query?.showLogin === '1') {
    showLogin.value = true
  }
  // 方式 1：微信扫一扫打开（需配置「扫普通链接二维码打开小程序」，个人主体暂不支持）
  if (query?.q) {
    handleQrScanParam(query.q as string)
  }
  // 方式 2：URL Link 跳转（微信扫一扫 → 网页 → 生成 URL Link → 唤起小程序）
  if (query?.qrToken) {
    handleQrScanParamDirect(query.qrToken as string)
  }
  loadSummary()
})

onShow(() => {
  try { uni.hideTabBar({ animation: false }) } catch {}
  if (initialLoad) { initialLoad = false; return }
  loadSummary()
})

onPullDownRefresh(() => {
  loadSummary().finally(() => uni.stopPullDownRefresh())
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
  position: relative;
}

/* 轮播图 */
.home-hero__banner {
  position: relative;
  padding-top: var(--space-6);
  padding-bottom: var(--space-4);
}

.home-hero__swiper {
  height: 320rpx;
  overflow: hidden;
}

.home-hero__swiper-slide {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding: var(--space-5) var(--space-6);
  border-radius: var(--radius-large);
}

.home-hero__swiper-title {
  color: #ffffff;
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.home-hero__swiper-desc {
  color: rgba(255, 255, 255, 0.8);
  font-size: var(--font-meta);
  margin-top: var(--space-2);
}

/* 扫码按钮：绝对定位浮在轮播图左上角 */
.home-hero__scan {
  position: absolute;
  left: var(--space-6);
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  border-radius: 48rpx;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: all var(--motion-fast) var(--ease-standard);
}

.home-hero__scan--pressed {
  transform: scale(0.92);
  background: rgba(255, 255, 255, 0.35);
}

.home-hero__scan-icon {
  width: 32rpx;
  height: 32rpx;
  display: block;
}

.home-hero__scan-text {
  color: #ffffff;
  font-size: 22rpx;
  font-weight: var(--weight-medium);
  line-height: 1;
}

.home-hero__bottom {
  padding: var(--space-4) var(--space-6) 0;
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
  padding-left: var(--space-6);
  padding-right: var(--space-6);
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
