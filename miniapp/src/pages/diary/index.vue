<template>
  <view :class="['page-shell-safe', themeClass]">
    <view class="section-shell">
      <view class="section-copy">
        <view class="section-copy__title">筛选</view>
        <view class="section-copy__desc">按可见范围和关键词筛选你的日记内容。</view>
      </view>

      <view class="block-stack">
        <FilterTabs v-model="activeTab" :items="tabItems" />
      </view>

      <view class="block-stack">
        <u-search
          v-model="keyword"
          placeholder="搜索标题或正文"
          :show-action="false"
          shape="round"
          bg-color="var(--color-surface-soft)"
          border-color="#eadfd0"
          color="var(--color-text-primary)"
          placeholder-color="var(--color-text-muted)"
          search-icon-color="var(--color-primary-strong)"
          @search="loadDiaries"
          @custom="loadDiaries"
        />
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">最近记录</view>
          <view class="section-copy__desc">{{ list.length }} 条内容</view>
        </view>
      </view>

      <view v-if="list.length" class="list-stack">
        <view
          v-for="item in list"
          :key="item.id"
          class="diary-entry"
          @tap="goDetail(item.id)"
        >
          <view class="diary-entry__head">
            <view class="diary-entry__title">{{ item.title }}</view>
            <view class="diary-entry__mood">{{ resolveDiaryMoodLabel(item.mood, '平静') }}</view>
          </view>
          <view class="diary-entry__content">{{ item.content }}</view>
          <view class="diary-entry__meta">
            <view>{{ item.recordDate }}</view>
            <view>{{ item.likeCount }} 点赞 / {{ item.commentCount }} 评论</view>
          </view>
        </view>
      </view>
      <EmptyStateCard
        v-else
        class="page-section"
        title="没有匹配的日记"
        description="可以切换筛选条件，或者先从首页写下今天的第一篇记录。"
        mode="search"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
const { themeClass } = useTheme()

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
.diary-entry {
  padding: 26rpx 28rpx;
  border-radius: 28rpx;
  border: 1rpx solid var(--color-border);
  background: var(--color-surface);
  box-shadow: 0 18rpx 48rpx rgba(67, 41, 26, 0.08);
}

.diary-entry__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.diary-entry__title {
  max-width: 72%;
  color: var(--color-text-primary);
  font-size: 30rpx;
  font-weight: 600;
}

.diary-entry__mood {
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #f4e4da;
  color: #9b6547;
  font-size: 20rpx;
}

.diary-entry__content {
  margin-top: 12rpx;
  color: #6b6359;
  font-size: 24rpx;
  line-height: 1.7;
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.diary-entry__meta {
  margin-top: 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  color: #918577;
  font-size: 22rpx;
}
</style>
