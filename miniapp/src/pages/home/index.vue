<template>
  <view :class="['page-shell-safe', themeClass]">
    <view class="page-head">
      <view class="section-head">
        <view class="section-copy">
          <view class="page-head__eyebrow">{{ greeting }}</view>
          <view class="page-head__title">把一天认真记录下来</view>
          <view class="page-head__desc">
            日记、记账、打卡和回忆会落在同一条时间线上，今天的变化和过去的痕迹都能快速串起来。
          </view>
        </view>
        <u-tag text="首页" type="warning" plain shape="circle" />
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">今日概览</view>
          <view class="section-copy__desc">先看今天的记录状态，再决定从哪里开始。</view>
        </view>
        <u-icon name="calendar" size="42" color="var(--color-primary)" />
      </view>

      <view class="metric-grid">
        <view v-for="item in metrics" :key="item.label" class="metric-card">
          <view class="metric-card__label">{{ item.label }}</view>
          <view class="metric-card__value">{{ item.value }}</view>
          <view class="metric-card__hint">{{ item.hint }}</view>
        </view>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">最近七天</view>
          <view class="section-copy__desc">日记和打卡共用一条状态带。</view>
        </view>
      </view>

      <view class="block-stack home-status-strip">
        <view v-for="item in calendarItems" :key="item.date" class="home-status-item">
          <view class="home-status-item__date">{{ item.date.slice(5) }}</view>
          <view
            class="home-status-item__badge"
            :class="item.hasDiary ? 'home-status-item__badge--done' : 'home-status-item__badge--todo'"
          >
            {{ item.hasDiary ? '有日记' : '未写日记' }}
          </view>
          <view class="home-status-item__hint">
            {{ item.hasCheckin ? '已打卡' : '未打卡' }}
          </view>
        </view>
      </view>
    </view>

    <view class="page-section section-shell overflow-hidden">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">快速入口</view>
          <view class="section-copy__desc">高频动作尽量一步完成。</view>
        </view>
      </view>

      <view class="quick-action-grid">
        <view
          v-for="item in quickActions"
          :key="item.key"
          class="quick-action-card"
          @tap="handleQuickAction(item.key, item.path)"
        >
          <view class="quick-action-card__title">{{ item.title }}</view>
          <view class="quick-action-card__desc">{{ item.description }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
const { themeClass } = useTheme()

import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
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

function buildFallbackSummary() {
  calendarItems.value = Array.from({ length: 7 }).map((_, index) => ({
    date: `2026-03-${String(index + 14).padStart(2, '0')}`,
    hasDiary: index % 2 === 0,
    diaryCount: index % 2 === 0 ? 1 : 0,
    hasCheckin: index % 3 !== 0,
    checkinCount: index % 3 !== 0 ? 1 : 0,
    memorialCount: 0
  }))
}

async function loadSummary() {
  if (!tokenStorage.getAccessToken()) {
    buildFallbackSummary()
    return
  }

  const now = new Date()
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
.home-status-strip {
  display: flex;
  gap: 12rpx;
  overflow-x: auto;
  white-space: nowrap;
}

.home-status-item {
  flex: 0 0 auto;
  min-width: 144rpx;
  padding: 20rpx 18rpx;
  border-radius: 24rpx;
  background: var(--color-surface);
  border: 1rpx solid var(--color-border);
}

.home-status-item__date {
  color: var(--color-text-primary);
  font-size: 24rpx;
  font-weight: 600;
}

.home-status-item__badge {
  margin-top: 12rpx;
  display: inline-flex;
  align-items: center;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
}

.home-status-item__badge--done {
  background: #e6f5ea;
  color: #287d49;
}

.home-status-item__badge--todo {
  background: var(--color-surface-soft);
  color: #b06a42;
}

.home-status-item__hint {
  margin-top: 12rpx;
  color: var(--color-text-muted);
  font-size: 22rpx;
}

.quick-action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.quick-action-card {
  min-height: 180rpx;
  padding: var(--space-5) var(--space-4);
  border-radius: var(--radius-large);
  background:
    radial-gradient(circle at top right, var(--color-primary-soft), transparent 40%),
    linear-gradient(180deg, var(--color-surface) 0%, var(--color-surface-soft) 100%);
  border: 1rpx solid var(--color-border);
  box-shadow: var(--shadow-card);
}

.quick-action-card__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.quick-action-card__desc {
  margin-top: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  line-height: var(--leading-relaxed);
}
</style>
