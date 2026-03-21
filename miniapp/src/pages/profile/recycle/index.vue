<template>
  <AppPage>
    <AppHero
      eyebrow="回收站"
      title="已删除内容"
      description="删除后的日记会在这里保留 15 天，你可以恢复或彻底删除。"
      badge="Recycle"
    />

    <SectionBlock title="回收站列表" subtitle="这里只展示已软删除的内容">
      <view v-if="items.length" class="space-y-[20rpx]">
        <view v-for="item in items" :key="item.id" class="glass-panel px-[24rpx] py-[24rpx]">
          <view class="flex items-start justify-between gap-[20rpx]">
            <view>
              <view class="text-[28rpx] font-semibold text-ink">{{ formatResourceType(item.resourceType) }}</view>
              <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">删除时间：{{ item.deletedAt }}</view>
              <view class="mt-[6rpx] text-[22rpx] text-[#7f7366]">到期时间：{{ item.expireAt }}</view>
            </view>
            <view class="text-[22rpx] text-[#a56d4b]">ID {{ item.resourceId }}</view>
          </view>

          <view class="mt-[20rpx] grid grid-cols-2 gap-[16rpx]">
            <BaseButton @tap="restore(item)">恢复</BaseButton>
            <BaseButton @tap="forceDelete(item)">彻底删除</BaseButton>
          </view>
        </view>
      </view>
      <EmptyState v-else icon="🗑️" title="回收站是空的" description="还没有进入回收站的内容。" />
    </SectionBlock>
  </AppPage>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/app-hero'
import SectionBlock from '@/components/business/section-block'
import EmptyState from '@/components/business/empty-state'
import BaseButton from '@/components/base/base-button'
import {
  fetchRecycleBinList,
  forceDeleteRecycleBinItem,
  restoreRecycleBinItem,
  type RecycleBinItem
} from '@/api/recycle'

const items = ref<RecycleBinItem[]>([])

function formatResourceType(type: string) {
  if (type === 'DIARY') return '日记'
  return type
}

async function loadRecycleBin() {
  items.value = await fetchRecycleBinList()
}

async function restore(item: RecycleBinItem) {
  await restoreRecycleBinItem(item.id, item.resourceId)
  uni.showToast({ title: '已恢复', icon: 'success' })
  await loadRecycleBin()
}

async function forceDelete(item: RecycleBinItem) {
  const result = await uni.showModal({
    title: '确认彻底删除',
    content: '彻底删除后不可恢复。'
  })
  if (!result.confirm) return

  await forceDeleteRecycleBinItem(item.id, item.resourceId)
  uni.showToast({ title: '已彻底删除', icon: 'success' })
  await loadRecycleBin()
}

onMounted(() => {
  loadRecycleBin().catch(() => undefined)
})
</script>