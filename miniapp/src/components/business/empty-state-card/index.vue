<template>
  <view class="empty-state-card">
    <text class="empty-state-card__icon">{{ iconMap[mode] }}</text>
    <text class="empty-state-card__title">{{ title }}</text>
    <text v-if="description" class="empty-state-card__desc">{{ description }}</text>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title: string
  description?: string
  mode?: string
}>(), {
  description: '',
  mode: 'data'
})

const iconMap: Record<string, string> = {
  car: '🚗',
  page: '📄',
  search: '🔍',
  address: '📍',
  wifi: '📶',
  order: '📋',
  coupon: '🎫',
  favor: '❤️',
  permission: '🔒',
  history: '🕰️',
  news: '📰',
  message: '💬',
  list: '📝',
  data: '📦'
}
</script>

<style scoped lang="scss">
.empty-state-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx var(--space-6) 40rpx;
}

.empty-state-card__icon {
  font-size: 96rpx;
  line-height: 1;
  margin-bottom: var(--space-5);
}

.empty-state-card__title {
  color: var(--color-text-secondary);
  font-size: var(--font-section);
  line-height: var(--leading-snug);
}

.empty-state-card__desc {
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-caption);
  line-height: var(--leading-relaxed);
  text-align: center;
}
</style>
