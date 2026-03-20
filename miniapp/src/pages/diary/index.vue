<template>
  <AppPage>
    <AppHero
      eyebrow="日记本"
      title="写下今天，也保留昨天"
      description="支持标签、搜索、可见范围和提醒设置，筛选条件会直接进入后端查询。"
      badge="Diary"
    />

    <SectionBlock title="筛选" subtitle="按可见范围和关键词做真实过滤">
      <FilterTabs v-model="activeTab" :items="tabItems" />
      <input
        v-model="keyword"
        class="glass-panel mt-[18rpx] h-[84rpx] px-[24rpx] text-[26rpx]"
        placeholder="搜索标题或正文"
        confirm-type="search"
        @confirm="loadDiaries"
      />
    </SectionBlock>

    <SectionBlock title="最近记录" :subtitle="`${list.length} 条内容`" action-text="新建">
      <view class="mb-[18rpx]">
        <BaseButton @tap="goEditor">写一篇新日记</BaseButton>
      </view>

      <view v-if="list.length" class="flex flex-col gap-[20rpx]">
        <DiaryCard
          v-for="item in list"
          :key="item.id"
          :title="item.title"
          :content="item.content"
          :mood="item.mood || '平静'"
          :record-date="item.recordDate"
          :footer="`${item.likeCount} 赞 · ${item.commentCount} 评论`"
          @click="goDetail(item.id)"
        />
      </view>
      <EmptyState
        v-else
        icon="📝"
        title="没有匹配的日记"
        description="可以先写下今天的感受，或者切换可见范围重新筛选。"
      />
    </SectionBlock>
  </AppPage>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/AppHero.vue'
import SectionBlock from '@/components/business/SectionBlock.vue'
import DiaryCard from '@/components/business/DiaryCard.vue'
import FilterTabs from '@/components/business/FilterTabs.vue'
import EmptyState from '@/components/business/EmptyState.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import { fetchDiaryList } from '@/api/diary'
import type { DiaryItem } from '@/types/domain'

const tabItems = [
  { label: '全部', value: 'ALL' },
  { label: '私有', value: 'PRIVATE' },
  { label: '共享', value: 'SHARED' },
  { label: '公开', value: 'PUBLIC' }
]

const activeTab = ref<string>('ALL')
const keyword = ref('')
const list = ref<DiaryItem[]>([])

function goEditor() {
  uni.navigateTo({ url: '/pages/diary/editor' })
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/diary/detail?id=${id}` })
}

/**
 * 日记列表的可见范围和关键词都直接带到后端查询。
 * 这样 tab 不只是视觉切换，而是真正影响返回结果。
 */
async function loadDiaries() {
  try {
    const visibility = activeTab.value === 'ALL' ? undefined : (activeTab.value as 'PRIVATE' | 'SHARED' | 'PUBLIC')
    const result = await fetchDiaryList({ keyword: keyword.value.trim(), visibility })
    list.value = result.records
  } catch {
    // 联调失败时保留演示数据，避免页面直接空白。
    list.value = [
      {
        id: 1,
        title: '被晚风修复的一天',
        content: '下班后沿着湖边慢慢走，风很轻，脑子终于不那么吵了。',
        recordDate: '2026-03-20',
        visibility: 'PRIVATE',
        likeCount: 3,
        commentCount: 1,
        mediaPaths: [],
        mood: '放松',
        weather: '晴'
      }
    ]
  }
}

watch([activeTab, keyword], () => {
  loadDiaries()
}, { immediate: true })

// 从编辑页或详情页返回时重新加载一次，保持列表状态最新。
onShow(() => {
  loadDiaries()
})
</script>
