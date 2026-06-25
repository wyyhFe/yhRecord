<template>
  <view class="page-shell-safe recycle-page">
    <!-- 列表 -->
    <view v-if="items && items.length" class="recycle-list">
      <view v-for="item in items" :key="item.id" class="recycle-item">
        <view class="recycle-item__header">
          <text class="recycle-item__title">{{ item.title || formatResourceType(item.resourceType) }}</text>
          <view class="recycle-item__type-badge">
            <text class="recycle-item__type-text">{{ formatResourceType(item.resourceType) }}</text>
          </view>
        </view>
        <text v-if="item.subtitle" class="recycle-item__subtitle">{{ item.subtitle }}</text>
        <view class="recycle-item__meta">
          <text class="recycle-item__meta-text">删除：{{ formatDateTime(item.deletedAt) }}</text>
          <text class="recycle-item__meta-text">到期：{{ formatDateTime(item.expireAt) }}</text>
        </view>
        <view class="recycle-item__actions">
          <u-button shape="circle" type="primary" color="var(--color-primary-gradient)" size="small" @click="restore(item)">
            恢复
          </u-button>
          <u-button shape="circle" type="error" plain :hair-line="false" size="small" @click="forceDelete(item)">
            彻底删除
          </u-button>
        </view>
      </view>
    </view>
    <!-- 空状态 -->
    <view v-else class="recycle-empty">
      <text class="recycle-empty__icon">📦</text>
      <text class="recycle-empty__title">回收站是空的</text>
      <text class="recycle-empty__desc">还没有进入回收站的内容</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
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
  console.log(items.value,items.value instanceof  Array)
}

async function restore(item: RecycleBinItem) {
  await restoreRecycleBinItem(item.id, item.resourceId)
  uni.$feedback.success('已恢复')
  await loadRecycleBin()
}

async function forceDelete(item: RecycleBinItem) {
  const result = await uni.showModal({ title: '确认彻底删除', content: '彻底删除后不可恢复。' })
  if (!result.confirm) return
  await forceDeleteRecycleBinItem(item.id, item.resourceId)
  uni.$feedback.success('已彻底删除')
  await loadRecycleBin()
}

onMounted(() => { loadRecycleBin().catch(() => undefined) })
</script>

<style scoped lang="scss">
.recycle-page {
  padding-bottom: var(--bottom-padding);
}

/* ========== 空状态 ========== */
.recycle-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx var(--space-6) 40rpx;
}

.recycle-empty__icon {
  font-size: 96rpx;
  line-height: 1;
  margin-bottom: var(--space-5);
}

.recycle-empty__title {
  color: var(--color-text-secondary);
  font-size: var(--font-section);
  line-height: var(--leading-snug);
}

.recycle-empty__desc {
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-caption);
  line-height: var(--leading-relaxed);
  text-align: center;
}

/* ========== 列表 ========== */
.recycle-list {
  margin: var(--space-4) var(--space-4) 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.recycle-item {
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.recycle-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.recycle-item__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recycle-item__type-badge {
  padding: 4rpx 16rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  flex-shrink: 0;
}

.recycle-item__type-text {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.recycle-item__subtitle {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-caption);
  line-height: var(--leading-relaxed);
}

.recycle-item__meta {
  margin-top: var(--space-3);
  display: flex;
  gap: var(--space-4);
}

.recycle-item__meta-text {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.recycle-item__actions {
  margin-top: var(--space-4);
  display: flex;
  gap: var(--space-3);
}
</style>
