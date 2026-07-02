<template>
  <view class="page-shell-safe discover-page">
    <!-- Hero 头部 -->
    <view class="discover-hero">
      <view class="discover-hero__top">
        <text class="discover-hero__title">日记大厅</text>
        <text class="discover-hero__sub">看看大家都在记录什么</text>
      </view>
      <view class="discover-hero__stats">
        <view class="discover-hero__stat">
          <text class="discover-hero__stat-num">{{ list.length }}</text>
          <text class="discover-hero__stat-label">篇公开日记</text>
        </view>
      </view>
    </view>

    <!-- 日记大厅列表 -->
    <view v-if="list.length" class="discover-feed">
      <view
        v-for="item in list"
        :key="item.id"
        class="discover-card"
        hover-class="discover-card--pressed"
        :hover-stay-time="120"
        @tap="goDetail(item.id)"
      >
        <!-- 作者信息 -->
        <view class="discover-card__author" @tap.stop="goUserProfile(item.authorId)">
          <view class="discover-card__avatar">
            <image
              v-if="item.authorAvatar"
              :src="resolveImage(item.authorAvatar)"
              mode="aspectFill"
              class="discover-card__avatar-img"
            />
            <text v-else class="discover-card__avatar-text">{{ (item.authorNickname || '?').charAt(0) }}</text>
          </view>
          <view class="discover-card__author-info">
            <text class="discover-card__author-name">{{ item.authorNickname || '匿名用户' }}</text>
            <text class="discover-card__date">{{ formatDate(item.recordDate) }}</text>
          </view>
          <text v-if="item.locationName" class="discover-card__location">📍 {{ item.locationName }}</text>
        </view>

        <!-- 日记内容 -->
        <text v-if="item.title" class="discover-card__title">{{ item.title }}</text>
        <text class="discover-card__content">{{ truncateContent(item.content) }}</text>

        <!-- 图片预览 -->
        <view v-if="item.mediaPaths?.length" class="discover-card__images">
          <view
            v-for="(path, pidx) in item.mediaPaths.slice(0, 3)"
            :key="pidx"
            class="discover-card__image-wrap"
            :class="'discover-card__image-wrap--' + Math.min(item.mediaPaths.length, 3)"
          >
            <image
              :src="resolveImage(path)"
              mode="aspectFill"
              class="discover-card__image"
            />
          </view>
          <view
            v-if="item.mediaPaths.length > 3"
            class="discover-card__image-wrap discover-card__image-wrap--more"
            :class="'discover-card__image-wrap--3'"
          >
            <view class="discover-card__image-overlay">
              <text class="discover-card__image-overlay-text">+{{ item.mediaPaths.length - 3 }}</text>
            </view>
            <image
              :src="resolveImage(item.mediaPaths[3])"
              mode="aspectFill"
              class="discover-card__image"
            />
          </view>
        </view>

        <!-- 底部互动信息 -->
        <view class="discover-card__footer">
          <view class="discover-card__chips">
            <text v-if="item.weather" class="discover-card__chip">{{ item.weather }}</text>
            <text v-if="item.mood" class="discover-card__chip">{{ item.mood }}</text>
          </view>
          <view class="discover-card__actions">
            <view class="discover-card__action">
              <text class="discover-card__action-icon">❤</text>
              <text class="discover-card__action-num">{{ item.likeCount || 0 }}</text>
            </view>
            <view class="discover-card__action">
              <text class="discover-card__action-icon">💬</text>
              <text class="discover-card__action-num">{{ item.commentCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <EmptyStateCard
      v-else-if="!loading"
      title="还没有公开日记"
      description="去写一篇公开日记，让大家看到你的故事"
      mode="page"
    />

    <!-- 加载中骨架 -->
    <view v-if="loading && !list.length" class="discover-skeleton">
      <view v-for="i in 3" :key="i" class="discover-skeleton__item" />
    </view>

    <!-- 底部加载状态 -->
    <LoadMore :state="loadingMore ? 'loading' : noMore ? 'noMore' : 'hidden'" />

    <!-- 自定义 TabBar -->
    <TabBar current="discover" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShareAppMessage, onShareTimeline, onReachBottom, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import LoadMore from '@/components/business/load-more/index.vue'
import TabBar from '@/components/business/tab-bar/index.vue'
import { fetchDiaryHall } from '@/api/diary'
import { resolveImage } from '@/utils/image'
import type { DiaryItem, Id } from '@/types/domain'

onShareAppMessage(() => ({ title: '发现' }))
onShareTimeline(() => ({ title: '发现' }))

const list = ref<DiaryItem[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const noMore = ref(false)
const currentPage = ref(1)
const pageSize = 10

function truncateContent(content: string): string {
  if (!content) return ''
  return content.length > 120 ? content.slice(0, 120) + '...' : content
}

function formatDate(date: string): string {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

async function loadDiaries(reset = false) {
  if (reset) {
    currentPage.value = 1
    noMore.value = false
  }
  if (loading.value || loadingMore.value) return

  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const res = await fetchDiaryHall({
      current: currentPage.value,
      size: pageSize
    })
    const items = res.list || []
    if (reset) {
      list.value = items
    } else {
      list.value = [...list.value, ...items]
    }
    noMore.value = list.value.length >= res.total
    if (!noMore.value) {
      currentPage.value++
    }
  } catch {
    // 静默处理
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function goDetail(id: Id) {
  uni.navigateTo({ url: `/pages/diary/detail?id=${id}&public=1` })
}

function goUserProfile(userId?: Id) {
  if (!userId) return
  uni.navigateTo({ url: `/pages/discover/user-profile?userId=${userId}` })
}

onShow(() => {
  uni.hideTabBar({ animation: false })
  loadDiaries(true)
})

onReachBottom(() => {
  if (!noMore.value && !loadingMore.value) {
    loadDiaries(false)
  }
})

onPullDownRefresh(() => {
  loadDiaries(true).finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style scoped lang="scss">
.discover-page {
  padding-bottom: var(--bottom-padding-with-tabbar);
}

/* ---------- Hero 头部 ---------- */
.discover-hero {
  background: var(--hero-diary-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-7) var(--space-6) var(--space-7);
}

.discover-hero__top {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.discover-hero__title {
  color: var(--color-text-primary);
  font-size: var(--font-display);
  font-weight: var(--weight-bold);
  line-height: 1.1;
}

.discover-hero__sub {
  color: var(--color-text-secondary);
  font-size: var(--font-body);
}

.discover-hero__stats {
  margin-top: var(--space-5);
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.discover-hero__stat {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
}

.discover-hero__stat-num {
  color: var(--color-diary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.discover-hero__stat-label {
  color: var(--color-text-muted);
  font-size: var(--font-caption);
}

/* ---------- 日记卡片 ---------- */
.discover-feed {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-4) 0;
}

.discover-card {
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5) var(--space-5) var(--space-4);
  transition: all var(--motion-fast) var(--ease-standard);
}

.discover-card--pressed {
  transform: scale(0.98);
  box-shadow: var(--shadow-press);
}

/* 作者信息行 */
.discover-card__author {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.discover-card__avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.discover-card__avatar-img {
  width: 100%;
  height: 100%;
  border-radius: var(--radius-full);
}

.discover-card__avatar-text {
  color: var(--color-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-bold);
}

.discover-card__author-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2rpx;
  min-width: 0;
}

.discover-card__author-name {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.discover-card__date {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.discover-card__location {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  flex-shrink: 0;
}

/* 日记内容 */
.discover-card__title {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
  margin-bottom: var(--space-2);
  line-height: var(--leading-snug);
}

.discover-card__content {
  display: block;
  color: var(--color-text-secondary);
  font-size: var(--font-body);
  line-height: var(--leading-relaxed);
  word-break: break-all;
}

/* 图片预览 */
.discover-card__images {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-4);
}

.discover-card__image-wrap {
  position: relative;
  border-radius: var(--radius-small);
  overflow: hidden;
}

.discover-card__image-wrap--1 {
  width: 100%;
  height: 360rpx;
}

.discover-card__image-wrap--2 {
  flex: 1;
  height: 280rpx;
}

.discover-card__image-wrap--3 {
  flex: 1;
  height: 220rpx;
}

.discover-card__image-wrap--more {
  position: relative;
}

.discover-card__image {
  width: 100%;
  height: 100%;
}

.discover-card__image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.discover-card__image-overlay-text {
  color: #FFFFFF;
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

/* 底部互动 */
.discover-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--space-4);
  padding-top: var(--space-3);
  border-top: 1rpx solid var(--color-divider);
}

.discover-card__chips {
  display: flex;
  gap: var(--space-2);
}

.discover-card__chip {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  background: var(--color-surface-soft);
  padding: 4rpx var(--space-3);
  border-radius: var(--radius-full);
}

.discover-card__actions {
  display: flex;
  gap: var(--space-5);
}

.discover-card__action {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.discover-card__action-icon {
  font-size: var(--font-caption);
}

.discover-card__action-num {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}

/* ---------- 骨架屏 ---------- */
.discover-skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-4);
}

.discover-skeleton__item {
  height: 280rpx;
  border-radius: var(--radius-large);
  background: var(--color-surface-soft);
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

@keyframes skeleton-pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 0.3; }
}
</style>
