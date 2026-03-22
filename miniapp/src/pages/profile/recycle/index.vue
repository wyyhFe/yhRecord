<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">已删除内容</view>
          <view class="section-copy__desc">删除后的日记会在这里保留 15 天，你可以恢复或彻底删除。</view>
        </view>
        <u-tag text="回收" type="warning" plain shape="circle" />
      </view>
    </view>

    <view class="page-section">
      <view v-if="items.length" class="list-stack">
        <view v-for="item in items" :key="item.id" class="list-card">
          <view class="list-card__head">
            <view>
              <view class="list-card__title">{{ formatResourceType(item.resourceType) }}</view>
              <view class="list-card__meta">删除时间：{{ item.deletedAt }}</view>
              <view class="list-card__meta">到期时间：{{ item.expireAt }}</view>
            </view>
            <view class="list-card__aside">ID {{ item.resourceId }}</view>
          </view>

          <view class="action-grid-2">
            <u-button type="primary" shape="circle" color="linear-gradient(135deg, #c47c52 0%, #d7a648 100%)" @click="restore(item)">
              恢复
            </u-button>
            <u-button type="error" shape="circle" plain @click="forceDelete(item)">彻底删除</u-button>
          </view>
        </view>
      </view>
      <EmptyStateCard
        v-else
        title="回收站是空的"
        description="还没有进入回收站的内容。"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import EmptyStateCard from '@/components/business/empty-state-card'
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
  uni.$feedback.success('已恢复')
  await loadRecycleBin()
}

async function forceDelete(item: RecycleBinItem) {
  const result = await uni.showModal({
    title: '确认彻底删除',
    content: '彻底删除后不可恢复。'
  })
  if (!result.confirm) return

  await forceDeleteRecycleBinItem(item.id, item.resourceId)
  uni.$feedback.success('已彻底删除')
  await loadRecycleBin()
}

onMounted(() => {
  loadRecycleBin().catch(() => undefined)
})
</script>
