<template>
  <view class="page-shell-safe user-profile-page">
    <!-- 用户信息头部 -->
    <view class="up-hero">
      <view class="up-hero__avatar">
        <image
          v-if="userInfo?.avatarPath"
          :src="resolveImage(userInfo.avatarPath)"
          mode="aspectFill"
          class="up-hero__avatar-img"
        />
        <text v-else class="up-hero__avatar-text">{{ avatarText }}</text>
      </view>
      <text class="up-hero__name">{{ userInfo?.nickname || '匿名用户' }}</text>
      <text v-if="userInfo?.signature" class="up-hero__sign">{{ userInfo.signature }}</text>
      <text v-else class="up-hero__sign up-hero__sign--muted">这个人还没有签名</text>
      <view
        v-if="!isSelf"
        class="up-follow-btn"
        :class="{ 'up-follow-btn--active': following }"
        hover-class="up-follow-btn--pressed"
        @tap="toggleFollow"
      >
        <text class="up-follow-btn__text">{{ following ? '已关注' : '＋ 关注' }}</text>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="up-stats">
      <view class="up-stats__item">
        <text class="up-stats__value">{{ userInfo?.publicDiaryCount ?? 0 }}</text>
        <text class="up-stats__label">公开日记</text>
      </view>
      <view class="up-stats__divider" />
      <view class="up-stats__item">
        <text class="up-stats__value">{{ joinDate }}</text>
        <text class="up-stats__label">加入时间</text>
      </view>
      <view class="up-stats__divider" />
      <view class="up-stats__item">
        <text class="up-stats__value">{{ daysSinceJoin }}</text>
        <text class="up-stats__label">入驻天数</text>
      </view>
    </view>

    <!-- 公开日记列表 -->
    <view class="up-section">
      <view class="up-section__header">
        <text class="up-section__title">公开日记</text>
        <text class="up-section__count">{{ diaryList.length }} 篇</text>
      </view>

      <view v-if="diaryList.length" class="up-list">
        <view
          v-for="item in diaryList"
          :key="item.id"
          class="up-card"
          hover-class="up-card--pressed"
          :hover-stay-time="120"
          @tap="goDetail(item.id)"
        >
          <text v-if="item.title" class="up-card__title">{{ item.title }}</text>
          <text class="up-card__excerpt">{{ truncateContent(item.content) }}</text>

          <!-- 图片预览 -->
          <view v-if="item.mediaPaths?.length" class="up-card__images">
            <image
              v-for="(path, pidx) in item.mediaPaths.slice(0, 3)"
              :key="pidx"
              :src="resolveImage(path)"
              mode="aspectFill"
              class="up-card__image"
            />
          </view>

          <!-- 底部元信息 -->
          <view class="up-card__footer">
            <text class="up-card__date">{{ formatDate(item.recordDate) }}</text>
            <view class="up-card__actions">
              <view class="up-card__action">
                <text class="up-card__action-icon">❤</text>
                <text class="up-card__action-num">{{ item.likeCount || 0 }}</text>
              </view>
              <view class="up-card__action">
                <text class="up-card__action-icon">💬</text>
                <text class="up-card__action-num">{{ item.commentCount || 0 }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <EmptyStateCard
        v-else-if="!loading"
        title="还没有公开日记"
        description="这个人还没有发布公开日记"
        mode="page"
      />

      <!-- 加载中骨架 -->
      <view v-if="loading && !diaryList.length" class="up-skeleton">
        <view v-for="i in 3" :key="i" class="up-skeleton__item" />
      </view>

      <!-- 底部加载状态 -->
      <LoadMore :state="loadingMore ? 'loading' : noMore ? 'noMore' : 'hidden'" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import LoadMore from '@/components/business/load-more/index.vue'
import { fetchPublicUserProfile } from '@/api/user'
import { fetchUserPublicDiaries } from '@/api/diary'
import { followUser, unfollowUser, fetchFollowStatus, fetchFollowCounts } from '@/api/follow'
import { OSS_BASE_URL } from '@/config/app'
import { useAppStore } from '@/stores/app'
import type { PublicUserProfile, DiaryItem, Id } from '@/types/domain'

const userId = ref<Id>('')
const appStore = useAppStore()
const currentUserId = computed(() => appStore.profile?.id || '')
const userInfo = ref<PublicUserProfile | null>(null)
const diaryList = ref<DiaryItem[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const noMore = ref(false)
const currentPage = ref(1)
const pageSize = 10
const following = ref(false)
const followCounts = ref({ following: 0, followers: 0 })

const isSelf = computed(() => currentUserId.value === userId.value)

function resolveImage(path: string) {
  return path.startsWith('http') ? path : `${OSS_BASE_URL}/${path}`
}

const avatarText = computed(() => {
  const name = userInfo.value?.nickname?.trim()
  return name ? name.slice(0, 1) : '?'
})

const joinDate = computed(() => {
  if (!userInfo.value?.createdAt) return '--'
  const d = new Date(userInfo.value.createdAt)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})

const daysSinceJoin = computed(() => {
  if (!userInfo.value?.createdAt) return '0'
  const d = new Date(userInfo.value.createdAt)
  const now = new Date()
  const diff = Math.floor((now.getTime() - d.getTime()) / (1000 * 60 * 60 * 24))
  return String(diff)
})

function truncateContent(content: string): string {
  if (!content) return ''
  return content.length > 100 ? content.slice(0, 100) + '...' : content
}

function formatDate(date: string): string {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

async function loadUserInfo() {
  try {
    userInfo.value = await fetchPublicUserProfile(userId.value)
  } catch {
    // 静默处理
  }
}

async function loadFollowData() {
  if (!currentUserId.value || currentUserId.value === userId.value) return
  try {
    const [status, counts] = await Promise.all([
      fetchFollowStatus(userId.value),
      fetchFollowCounts(userId.value)
    ])
    following.value = status.following
    followCounts.value = counts
  } catch {
    // 静默
  }
}

async function toggleFollow() {
  if (following.value) {
    await unfollowUser(userId.value)
    following.value = false
    followCounts.value.followers = Math.max(0, followCounts.value.followers - 1)
    uni.$feedback.success('已取消关注')
  } else {
    await followUser(userId.value)
    following.value = true
    followCounts.value.followers++
    uni.$feedback.success('关注成功')
  }
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
    const res = await fetchUserPublicDiaries(userId.value, {
      current: currentPage.value,
      size: pageSize
    })
    const items = res.list || []
    if (reset) {
      diaryList.value = items
    } else {
      diaryList.value = [...diaryList.value, ...items]
    }
    noMore.value = diaryList.value.length >= res.total
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

onLoad((options) => {
  const uid = options?.userId
  if (uid) {
    userId.value = uid
    loadUserInfo()
    loadFollowData()
    loadDiaries(true)
  }
})

onReachBottom(() => {
  if (!noMore.value && !loadingMore.value) {
    loadDiaries(false)
  }
})
</script>

<style scoped lang="scss">
.user-profile-page {
  padding-bottom: var(--bottom-padding);
}

/* ---------- 用户信息头部 ---------- */
.up-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-8) var(--space-6) var(--space-5);
  background: var(--hero-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
}

.up-hero__avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: var(--radius-full);
  background: var(--color-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin-bottom: var(--space-4);
  box-shadow: 0 0 0 6rpx rgba(255, 255, 255, 0.5);
}

.up-hero__avatar-img {
  width: 100%;
  height: 100%;
}

.up-hero__avatar-text {
  font-size: 52rpx;
  font-weight: var(--weight-bold);
  color: var(--color-primary);
}

.up-hero__name {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
  line-height: var(--leading-tight);
}

.up-hero__sign {
  margin-top: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  text-align: center;
}

.up-hero__sign--muted {
  color: var(--color-text-muted);
}

/* 关注按钮 */
.up-follow-btn {
  margin-top: var(--space-4);
  padding: var(--space-2) var(--space-8);
  border-radius: var(--radius-full);
  border: 2rpx solid var(--color-primary);
  background: transparent;
  transition: all var(--motion-fast) var(--ease-standard);
}

.up-follow-btn--active {
  background: var(--color-primary-soft);
  border-color: transparent;
}

.up-follow-btn--pressed {
  transform: scale(0.93);
  opacity: 0.7;
}

.up-follow-btn__text {
  color: var(--color-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

/* ---------- 统计信息 ---------- */
.up-stats {
  margin: var(--space-4) var(--space-6) 0;
  display: flex;
  align-items: center;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5) 0;
}

.up-stats__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.up-stats__value {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-bold);
}

.up-stats__label {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.up-stats__divider {
  width: 1rpx;
  height: 48rpx;
  background: var(--color-divider);
}

/* ---------- 日记列表区域 ---------- */
.up-section {
  margin-top: var(--space-5);
  padding: 0 var(--space-4);
}

.up-section__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: var(--space-4);
  padding: 0 var(--space-2);
}

.up-section__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.up-section__count {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

/* ---------- 日记卡片 ---------- */
.up-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.up-card {
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
  transition: all var(--motion-fast) var(--ease-standard);
}

.up-card--pressed {
  transform: scale(0.98);
  box-shadow: var(--shadow-press);
}

.up-card__title {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
  margin-bottom: var(--space-2);
  line-height: var(--leading-snug);
}

.up-card__excerpt {
  display: block;
  color: var(--color-text-secondary);
  font-size: var(--font-body);
  line-height: var(--leading-relaxed);
  word-break: break-all;
}

.up-card__images {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-4);
}

.up-card__image {
  flex: 1;
  height: 200rpx;
  border-radius: var(--radius-small);
}

.up-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--space-4);
  padding-top: var(--space-3);
  border-top: 1rpx solid var(--color-divider);
}

.up-card__date {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.up-card__actions {
  display: flex;
  gap: var(--space-5);
}

.up-card__action {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.up-card__action-icon {
  font-size: var(--font-caption);
}

.up-card__action-num {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}

/* ---------- 骨架屏 ---------- */
.up-skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.up-skeleton__item {
  height: 200rpx;
  border-radius: var(--radius-large);
  background: var(--color-surface-soft);
  animation: up-skeleton-pulse 1.5s ease-in-out infinite;
}

@keyframes up-skeleton-pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 0.3; }
}
</style>
