<template>
  <view class="page-shell-safe diary-page">
    <!-- 顶栏 -->
    <view class="diary-header">
      <view class="diary-header__left">
        <text class="diary-header__title">我的日记</text>
        <text class="diary-header__count">{{ list.length }} 篇</text>
      </view>
      <view class="diary-header__actions">
        <view class="diary-header__action" hover-class="diary-header__action--pressed" @click="goEditor">
          <text class="diary-header__action-icon">✏️</text>
          <text class="diary-header__action-label">写日记</text>
        </view>
        <view class="diary-header__action" hover-class="diary-header__action--pressed" @click="loadDiaries()">
          <text class="diary-header__action-icon">↻</text>
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

    <!-- 时间线列表（按天分组） -->
    <view v-if="list.length" class="timeline">
      <template v-for="(dayGroup, dgIdx) in groupedList" :key="dayGroup.date">
        <!-- 日期分隔头 -->
        <view class="timeline-day">
          <view class="timeline-day__dot" />
          <view class="timeline-day__label">
            <text class="timeline-day__day">{{ dayNum(dayGroup.date) }}</text>
            <text class="timeline-day__month">{{ monthShort(dayGroup.date) }}</text>
          </view>
        </view>

        <!-- 当天的条目列表 -->
        <view
          v-for="(item, itemIdx) in dayGroup.items"
          :key="item.id"
          class="timeline-item"
          :class="{ 'timeline-item--last': itemIdx === dayGroup.items.length - 1 }"
          @tap="goDetail(item.id)"
        >
          <!-- 左侧竖线 -->
          <view class="timeline-line">
            <view
              v-if="!(dgIdx === groupedList.length - 1 && itemIdx === dayGroup.items.length - 1)"
              class="timeline-line__bar"
            />
          </view>

          <!-- 内容卡片 -->
          <view class="timeline-card" hover-class="timeline-card--pressed" :hover-stay-time="120">
            <text class="timeline-card__title">{{ item.title }}</text>

            <text class="timeline-card__excerpt">{{ item.content }}</text>

            <!-- 图片预览 -->
            <view v-if="item.mediaPaths?.length" class="timeline-card__images">
              <view
                v-for="(path, pidx) in item.mediaPaths.slice(0, 4)"
                :key="pidx"
                class="timeline-card__image-wrap"
                :class="'timeline-card__image-wrap--' + Math.min(item.mediaPaths.length, 4)"
              >
                <image
                  :src="resolveImage(path)"
                  mode="aspectFill"
                  class="timeline-card__image"
                />
              </view>
              <view
                v-if="item.mediaPaths.length > 4"
                class="timeline-card__image-wrap timeline-card__image-wrap--more"
              >
                <view class="timeline-card__image-overlay">
                  <text class="timeline-card__image-overlay-text">+{{ item.mediaPaths.length - 4 }}</text>
                </view>
                <image
                  :src="resolveImage(item.mediaPaths[4])"
                  mode="aspectFill"
                  class="timeline-card__image"
                />
              </view>
            </view>

            <!-- 底部元信息 -->
            <view class="timeline-card__footer">
              <view class="timeline-card__chips">
                <text v-if="item.weather" class="timeline-card__chip">{{ resolveDiaryWeatherLabel(item.weather) }}</text>
                <text v-if="item.mood" class="timeline-card__chip">{{ resolveDiaryMoodLabel(item.mood, '') }}</text>
                <text v-if="item.likeCount" class="timeline-card__chip">❤ {{ item.likeCount }}</text>
                <text v-if="item.commentCount" class="timeline-card__chip">💬 {{ item.commentCount }}</text>
              </view>
              <text v-if="item.locationName" class="timeline-card__location">📍 {{ item.locationName }}</text>
            </view>
          </view>
        </view>
      </template>
    </view>
    <EmptyStateCard
      v-else
      :title="keyword ? '没有匹配的日记' : '还没有日记'"
      :description="keyword ? '换个关键词试试' : '点击上方 ✏️ 写下第一篇日记'"
      :mode="keyword ? 'search' : 'page'"
    />

    <!-- 底部加载状态 -->
    <LoadMore :state="loadingMore ? 'loading' : noMore ? 'noMore' : 'hidden'" />

    <!-- 自定义 TabBar -->
    <TabBar current="diary" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onReachBottom, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import FilterTabs from '@/components/business/filter-tabs'
import LoadMore from '@/components/business/load-more/index.vue'
import { fetchDiaryList } from '@/api/diary'
import { OSS_BASE_URL } from '@/config/app'
import type { DiaryItem, Id } from '@/types/domain'
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { resolveDiaryMoodLabel, resolveDiaryWeatherLabel } from '@/utils/diary-display'
import TabBar from '@/components/business/tab-bar/index.vue'

onShareAppMessage(() => ({ title: '我的日记' }))
onShareTimeline(() => ({ title: '我的日记' }))
const MONTH_SHORT = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

const tabItems = [
  { label: '全部', value: 'ALL' },
  { label: '私有', value: 'PRIVATE' },
  { label: '共享', value: 'SHARED' },
  { label: '公开', value: 'PUBLIC' }
]

const activeTab = ref<string>('ALL')
const keyword = ref('')
const list = ref<DiaryItem[]>([])
const pageNum = ref(1)
const total = ref(0)
const loadingMore = ref(false)
const noMore = ref(false)
const PAGE_SIZE = 10

const groupedList = computed(() => {
  const groups: Record<string, DiaryItem[]> = {}
  for (const item of list.value) {
    if (!groups[item.recordDate]) groups[item.recordDate] = []
    groups[item.recordDate].push(item)
  }
  return Object.entries(groups).map(([date, items]) => ({ date, items }))
})

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

async function loadDiaries(reset = true) {
  if (reset) {
    pageNum.value = 1
    noMore.value = false
  }
  const currentPage = reset ? 1 : pageNum.value
  const visibility =
    activeTab.value === 'ALL'
      ? undefined
      : (activeTab.value as 'PRIVATE' | 'SHARED' | 'PUBLIC')
  try {
    const result = await fetchDiaryList({
      keyword: keyword.value.trim(),
      visibility,
      current: currentPage,
      size: PAGE_SIZE
    })
    const items = result.list || []
    total.value = result.total
    if (reset) {
      list.value = items
    } else {
      list.value = [...list.value, ...items]
    }
    noMore.value = list.value.length >= total.value
  } catch (error) {
    if (reset) list.value = []
    uni.$feedback.error(error)
  }
}

async function loadMore() {
  if (loadingMore.value || noMore.value) return
  loadingMore.value = true
  pageNum.value++
  try {
    await loadDiaries(false)
  } catch {
    pageNum.value--
  } finally {
    loadingMore.value = false
  }
}

watch(
  [activeTab, keyword],
  () => { loadDiaries(true) },
  { immediate: true }
)

onShow(() => { loadDiaries(true) })

onPullDownRefresh(() => {
  loadDiaries(true).finally(() => uni.stopPullDownRefresh())
})

onReachBottom(() => {
  loadMore()
})
</script>

<style scoped lang="scss">
.diary-page {
}

/* ========== 顶栏 ========== */
.diary-header {
  padding: var(--space-5) var(--space-6) var(--space-3);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.diary-header__left {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
}

.diary-header__title {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.diary-header__count {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.diary-header__actions {
  display: flex;
  gap: var(--space-2);
}

.diary-header__action {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  transition: all var(--motion-fast) var(--ease-standard);
}

.diary-header__action--pressed {
  transform: scale(0.93);
  opacity: 0.7;
}

.diary-header__action-icon {
  font-size: 28rpx;
  line-height: 1;
}

.diary-header__action-label {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
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

/* ========== 时间线 ========== */
.timeline {
  margin: var(--space-4) var(--space-4) 0;
  padding-left: 24rpx;
}

/* 日期分隔头 */
.timeline-day {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
  padding-top: var(--space-4);
}

.timeline-day__dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: var(--radius-full);
  background: var(--color-diary);
  flex-shrink: 0;
  box-shadow: 0 0 0 6rpx var(--color-diary-soft);
}

.timeline-day__label {
  display: flex;
  align-items: baseline;
  gap: var(--space-1);
}

.timeline-day__day {
  color: var(--color-text-primary);
  font-size: var(--font-display);
  font-weight: var(--weight-bold);
  line-height: 1;
}

.timeline-day__month {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

/* 单条日记 */
.timeline-item {
  display: flex;
  gap: var(--space-3);
  position: relative;
}

.timeline-item--last {
  margin-bottom: var(--space-4);
}

/* 左侧竖线 */
.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 12rpx;
  flex-shrink: 0;
  padding: 4rpx 0;
}

.timeline-line__bar {
  width: 2rpx;
  flex: 1;
  background: var(--color-border);
}

/* 内容卡片 */
.timeline-card {
  flex: 1;
  min-width: 0;
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-medium);
  box-shadow: var(--shadow-card);
  margin-bottom: var(--space-3);
  transition: all var(--motion-fast) var(--ease-standard);
}

.timeline-card--pressed {
  transform: scale(0.98);
  box-shadow: var(--shadow-press);
}

.timeline-card__title {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
  line-height: var(--leading-snug);
}

.timeline-card__excerpt {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  line-height: var(--leading-relaxed);
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 图片预览 */
.timeline-card__images {
  margin-top: var(--space-3);
  display: flex;
  flex-wrap: wrap;
  gap: 6rpx;
  border-radius: var(--radius-small);
  overflow: hidden;
}

.timeline-card__image-wrap {
  position: relative;
  overflow: hidden;
}

.timeline-card__image-wrap--1 {
  width: 100%;
  height: 240rpx;
}

.timeline-card__image-wrap--2 {
  width: calc(50% - 3rpx);
  height: 170rpx;
}

.timeline-card__image-wrap--3 {
  width: calc(33.33% - 4rpx);
  height: 150rpx;
}

.timeline-card__image-wrap--4,
.timeline-card__image-wrap--more {
  width: calc(25% - 4.5rpx);
  height: 150rpx;
}

.timeline-card__image {
  width: 100%;
  height: 100%;
  background: var(--color-surface-soft);
}

.timeline-card__image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.timeline-card__image-overlay-text {
  color: #fff;
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
  line-height: 1;
}

/* 底部元信息 */
.timeline-card__footer {
  margin-top: var(--space-3);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.timeline-card__chips {
  display: flex;
  gap: var(--space-1);
  flex-wrap: wrap;
  flex: 1;
  min-width: 0;
}

.timeline-card__chip {
  padding: 2rpx 12rpx;
  border-radius: var(--radius-full);
  background: var(--color-diary-soft);
  color: var(--color-diary);
  font-size: 18rpx;
  font-weight: var(--weight-medium);
}

.timeline-card__location {
  color: var(--color-text-muted);
  font-size: 18rpx;
  flex-shrink: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200rpx;
}



</style>
