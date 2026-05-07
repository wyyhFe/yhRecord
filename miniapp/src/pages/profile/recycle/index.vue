<template>
  <view :class="['page-shell-safe', themeClass]">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">已删除内容</view>
          <view class="section-copy__desc">
            日记和账单删除后会在这里保留 15 天，你可以恢复或彻底删除。
          </view>
        </view>
        <u-tag text="回收站" type="warning" plain shape="circle" />
      </view>
    </view>

    <view class="page-section">
      <view v-if="items.length" class="list-stack">
        <view v-for="item in items" :key="item.id" class="list-card">
          <view class="list-card__head">
            <view class="list-card__main">
              <view class="list-card__title-row">
                <view class="list-card__title">{{ item.title || formatResourceType(item.resourceType) }}</view>
                <u-tag
                  :text="formatResourceType(item.resourceType)"
                  plain
                  shape="circle"
                  type="warning"
                  size="mini"
                />
              </view>
              <view v-if="item.subtitle" class="list-card__summary">{{ item.subtitle }}</view>
              <view class="list-card__meta">删除时间：{{ formatDateTime(item.deletedAt) }}</view>
              <view class="list-card__meta">到期时间：{{ formatDateTime(item.expireAt) }}</view>
            </view>
          </view>

          <view class="action-grid-2">
            <u-button
              type="primary"
              shape="circle"
              color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
              @click="restore(item)"
            >
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
import { useTheme } from '@/composables/useTheme'
const { themeClass } = useTheme()

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
  if (type === 'LEDGER_ENTRY') return '账单'
  return type
}

function formatDateTime(value: string) {
  if (!value) return '--'
  return value.replace('T', ' ').slice(0, 16)
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

<style scoped lang="scss">
.list-card__main {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 10rpx;
}

.list-card__title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.list-card__title {
  color: var(--color-text-primary);
  font-size: 30rpx;
  font-weight: 700;
}

.list-card__summary {
  color: #5e4b3a;
  font-size: 26rpx;
  line-height: 1.6;
}

.list-card__meta {
  color: var(--color-text-muted);
  font-size: 24rpx;
}
</style>
