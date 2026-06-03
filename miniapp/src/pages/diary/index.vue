<template>
  <view class="page-shell-safe diary-page">
    <!-- 顶部 Hero -->
    <view class="diary-hero">
      <view class="diary-hero__title">我的日记</view>
      <view class="diary-hero__sub">记录生活中的每一刻</view>
    </view>

    <!-- 筛选区 -->
    <view class="diary-filter">
      <FilterTabs v-model="activeTab" :items="tabItems" />
      <view class="diary-filter__search">
        <u-search
          v-model="keyword"
          placeholder="搜索标题或正文"
          :show-action="false"
          shape="round"
          bg-color="var(--color-surface)"
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
        class="diary-card"
        hover-class="diary-card--pressed"
        :hover-stay-time="120"
        @tap="goDetail(item.id)"
      >
        <view class="diary-card__accent" />
        <view class="diary-card__body">
          <view class="diary-card__header">
            <text class="diary-card__title">{{ item.title }}</text>
            <view class="diary-card__mood">{{ resolveDiaryMoodLabel(item.mood, '平静') }}</view>
          </view>
          <text class="diary-card__content">{{ item.content }}</text>
          <view class="diary-card__footer">
            <text class="diary-card__date">{{ item.recordDate }}</text>
            <view class="diary-card__stats">
              <text v-if="item.likeCount" class="diary-card__stat">{{ item.likeCount }} 赞</text>
              <text v-if="item.commentCount" class="diary-card__stat">{{ item.commentCount }} 评论</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    <EmptyStateCard
      v-else
      class="diary-empty"
      title="没有匹配的日记"
      description="可以切换筛选条件，或者从首页写下第一篇记录。"
      mode="search"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import FilterTabs from '@/components/business/filter-tabs'
import { fetchDiaryList } from '@/api/diary'
import type { DiaryItem, Id } from '@/types/domain'
import { resolveDiaryMoodLabel } from '@/utils/diary-display'

const tabItems = [
  { label: '全部', value: 'ALL' },
  { label: '私有', value: 'PRIVATE' },
  { label: '共享', value: 'SHARED' },
  { label: '公开', value: 'PUBLIC' }
]

const activeTab = ref<string>('ALL')
const keyword = ref('')
const list = ref<DiaryItem[]>([])

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
    console.log('[diary] fetch list result', result)
    list.value = result.records || []
  } catch (error) {
    console.error('[diary] fetch list failed', error)
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
/* ============================================================
 * Diary List v4 · Warm Modern
 * ========================================================= */

.diary-page {
  padding-bottom: var(--space-10);
}

/* Hero 头部 */
.diary-hero {
  background: var(--hero-diary-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-7) var(--space-6) var(--space-6);
}

.diary-hero__title {
  color: var(--color-text-primary);
  font-size: var(--font-display);
  font-weight: var(--weight-bold);
}

.diary-hero__sub {
  margin-top: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-body);
}

/* 筛选区 */
.diary-filter {
  margin-top: var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5) var(--space-5) var(--space-4);
}

.diary-filter__search {
  margin-top: var(--space-4);
}

/* 日记列表 */
.diary-list {
  margin-top: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.diary-card {
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  display: flex;
  transition: all var(--motion-fast) var(--ease-standard);
}

.diary-card--pressed {
  transform: scale(0.98);
  box-shadow: var(--shadow-press);
}

/* 左侧彩色指示条 */
.diary-card__accent {
  width: 8rpx;
  flex-shrink: 0;
  background: var(--color-diary-gradient);
}

.diary-card__body {
  flex: 1;
  min-width: 0;
  padding: var(--space-5) var(--space-5) var(--space-4);
}

.diary-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.diary-card__title {
  flex: 1;
  min-width: 0;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.diary-card__mood {
  flex-shrink: 0;
  padding: 6rpx 18rpx;
  border-radius: var(--radius-full);
  background: var(--color-diary-soft);
  color: var(--color-diary);
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
}

.diary-card__content {
  margin-top: var(--space-3);
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

.diary-card__footer {
  margin-top: var(--space-4);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.diary-card__date {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.diary-card__stats {
  display: flex;
  gap: var(--space-3);
}

.diary-card__stat {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.diary-empty {
  margin-top: var(--space-6);
}
</style>
