<template>
  <view class="page-shell-safe diary-page">
    <!-- Hero -->
    <view class="diary-hero">
      <view class="diary-hero__top">
        <text class="diary-hero__date">{{ todayDisplay }}</text>
      </view>
      <view class="diary-hero__title">我的日记</view>
      <view class="diary-hero__sub">
        已记录 <text class="diary-hero__highlight">{{ list.length }}</text> 篇
      </view>

      <view class="diary-hero__actions">
        <view class="diary-hero__action" hover-class="diary-hero__action--pressed" @click="goEditor">
          <text class="diary-hero__action-icon">✏️</text>
          <text class="diary-hero__action-label">写日记</text>
        </view>
        <view class="diary-hero__action" hover-class="diary-hero__action--pressed" @click="loadDiaries">
          <text class="diary-hero__action-icon">↻</text>
          <text class="diary-hero__action-label">刷新</text>
        </view>
      </view>
    </view>

    <!-- 筛选 -->
    <view class="diary-card-filter">
      <FilterTabs v-model="activeTab" :items="tabItems" />
      <view class="diary-card-filter__search">
        <u-search
          v-model="keyword"
          placeholder="搜索标题或正文"
          :show-action="false"
          shape="round"
          bg-color="var(--color-surface-soft)"
          border-color="transparent"
          color="var(--color-text-primary)"
          placeholder-color="var(--color-text-muted)"
          search-icon-color="var(--color-text-muted)"
          @search="loadDiaries"
          @custom="loadDiaries"
        />
      </view>
    </view>

    <!-- 日记列表 -->
    <view v-if="list.length" class="diary-list">
      <view
        v-for="item in list"
        :key="item.id"
        class="diary-item"
        hover-class="diary-item--pressed"
        :hover-stay-time="120"
        @tap="goDetail(item.id)"
      >
        <!-- 日期标签 -->
        <view class="diary-item__date-badge">
          <text class="diary-item__date-day">{{ dayNum(item.recordDate) }}</text>
          <text class="diary-item__date-month">{{ monthShort(item.recordDate) }}</text>
        </view>

        <view class="diary-item__body">
          <view class="diary-item__header">
            <text class="diary-item__title">{{ item.title }}</text>
            <view v-if="item.mood" class="diary-item__mood">{{ resolveDiaryMoodLabel(item.mood, '') }}</view>
          </view>
          <text class="diary-item__content">{{ item.content }}</text>
          <view class="diary-item__footer">
            <view class="diary-item__meta">
              <text v-if="item.weather" class="diary-item__meta-item">{{ resolveDiaryWeatherLabel(item.weather) }}</text>
              <text v-if="item.locationName" class="diary-item__meta-item">📍 {{ item.locationName }}</text>
            </view>
            <view class="diary-item__stats">
              <text v-if="item.likeCount" class="diary-item__stat">❤ {{ item.likeCount }}</text>
              <text v-if="item.commentCount" class="diary-item__stat">💬 {{ item.commentCount }}</text>
            </view>
          </view>
          <!-- 图片预览 -->
          <view v-if="item.mediaPaths?.length" class="diary-item__images">
            <image
              v-for="(path, idx) in item.mediaPaths.slice(0, 3)"
              :key="idx"
              :src="resolveImage(path)"
              mode="aspectFill"
              class="diary-item__image"
            />
            <view v-if="item.mediaPaths.length > 3" class="diary-item__image-more">
              <text class="diary-item__image-more-text">+{{ item.mediaPaths.length - 3 }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    <EmptyStateCard
      v-else
      class="diary-empty"
      :title="keyword ? '没有匹配的日记' : '还没有日记'"
      :description="keyword ? '换个关键词试试' : '点击上方 ✏️ 写下第一篇日记'"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import FilterTabs from '@/components/business/filter-tabs'
import { fetchDiaryList } from '@/api/diary'
import { OSS_BASE_URL } from '@/config/app'
import type { DiaryItem, Id } from '@/types/domain'
import { resolveDiaryMoodLabel, resolveDiaryWeatherLabel } from '@/utils/diary-display'

const MONTH_SHORT = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
const WEEK = ['日', '一', '二', '三', '四', '五', '六']
const now = new Date()
const todayDisplay = `${now.getMonth() + 1}月${now.getDate()}日 周${WEEK[now.getDay()]}`

const tabItems = [
  { label: '全部', value: 'ALL' },
  { label: '私有', value: 'PRIVATE' },
  { label: '共享', value: 'SHARED' },
  { label: '公开', value: 'PUBLIC' }
]

const activeTab = ref<string>('ALL')
const keyword = ref('')
const list = ref<DiaryItem[]>([])

function dayNum(date: string) {
  return Number(date.slice(8, 10))
}

function monthShort(date: string) {
  return MONTH_SHORT[Number(date.slice(5, 7)) - 1]
}

function resolveImage(path: string) {
  return path.startsWith('http') ? path : `${OSS_BASE_URL}/${path}`
}

function goEditor() {
  uni.navigateTo({ url: '/pages/diary/editor' })
}

function goDetail(id: Id) {
  uni.navigateTo({ url: `/pages/diary/detail?id=${id}` })
}

async function loadDiaries() {
  try {
    const visibility =
      activeTab.value === 'ALL'
        ? undefined
        : (activeTab.value as 'PRIVATE' | 'SHARED' | 'PUBLIC')
    const result = await fetchDiaryList({
      keyword: keyword.value.trim(),
      visibility
    })
    list.value = result.records || []
  } catch (error) {
    list.value = []
    uni.$feedback.error(error)
  }
}

watch(
  [activeTab, keyword],
  () => {
    loadDiaries()
  },
  { immediate: true }
)

onShow(() => {
  loadDiaries()
})
</script>

<style scoped lang="scss">
.diary-page {
  padding-bottom: var(--space-10);
}

/* ========== Hero ========== */
.diary-hero {
  background: var(--color-diary-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-7) var(--space-6) var(--space-8);
  color: #fff;
}

.diary-hero__top {
  margin-bottom: var(--space-2);
}

.diary-hero__date {
  font-size: var(--font-meta);
  opacity: 0.8;
}

.diary-hero__title {
  font-size: var(--font-hero);
  font-weight: var(--weight-bold);
  line-height: 1.1;
}

.diary-hero__sub {
  margin-top: var(--space-3);
  font-size: var(--font-body);
  opacity: 0.85;
}

.diary-hero__highlight {
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.diary-hero__actions {
  margin-top: var(--space-5);
  display: flex;
  gap: var(--space-3);
}

.diary-hero__action {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) 0;
  border-radius: var(--radius-medium);
  background: rgba(255, 255, 255, 0.18);
  transition: all var(--motion-fast) var(--ease-standard);
}

.diary-hero__action--pressed {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(0.95);
}

.diary-hero__action-icon {
  font-size: 36rpx;
  line-height: 1;
}

.diary-hero__action-label {
  font-size: var(--font-tiny);
  opacity: 0.9;
}

/* ========== 筛选 ========== */
.diary-card-filter {
  margin: var(--space-4) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-4) var(--space-5);
}

.diary-card-filter__search {
  margin-top: var(--space-3);
}

/* ========== 日记列表 ========== */
.diary-list {
  margin: var(--space-4) var(--space-4) 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.diary-item {
  display: flex;
  gap: var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
  transition: all var(--motion-fast) var(--ease-standard);
}

.diary-item--pressed {
  transform: scale(0.98);
  box-shadow: var(--shadow-press);
}

/* 日期标签 */
.diary-item__date-badge {
  width: 80rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-2) 0;
  border-radius: var(--radius-medium);
  background: var(--color-diary-soft);
  flex-shrink: 0;
}

.diary-item__date-day {
  color: var(--color-diary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
  line-height: 1;
}

.diary-item__date-month {
  color: var(--color-diary);
  font-size: var(--font-tiny);
  margin-top: 4rpx;
}

/* 内容区 */
.diary-item__body {
  flex: 1;
  min-width: 0;
}

.diary-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}

.diary-item__title {
  flex: 1;
  min-width: 0;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.diary-item__mood {
  flex-shrink: 0;
  font-size: 28rpx;
}

.diary-item__content {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-caption);
  line-height: var(--leading-relaxed);
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.diary-item__footer {
  margin-top: var(--space-3);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.diary-item__meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.diary-item__meta-item {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.diary-item__stats {
  display: flex;
  gap: var(--space-3);
}

.diary-item__stat {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

/* 图片预览 */
.diary-item__images {
  margin-top: var(--space-3);
  display: flex;
  gap: 8rpx;
}

.diary-item__image {
  width: 140rpx;
  height: 140rpx;
  border-radius: var(--radius-small);
}

.diary-item__image-more {
  width: 140rpx;
  height: 140rpx;
  border-radius: var(--radius-small);
  background: var(--color-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}

.diary-item__image-more-text {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

.diary-empty {
  margin: var(--space-6) var(--space-4) 0;
}
</style>
