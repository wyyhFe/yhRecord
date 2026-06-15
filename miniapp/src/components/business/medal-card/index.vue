<template>
  <view
    class="medal-card"
    :class="{ 'medal-card--locked': !medal.unlocked }"
    @tap="$emit('tap', medal)"
  >
    <text class="medal-card__icon">{{ medal.unlocked ? medal.icon : '🔒' }}</text>
    <text class="medal-card__name">{{ medal.name }}</text>
    <view v-if="medal.unlocked" class="medal-card__stars">
      <text v-for="i in medal.difficulty" :key="i" class="medal-card__star">⭐</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { Medal } from '@/types/domain'

defineProps<{
  medal: Medal
}>()

defineEmits<{
  tap: [medal: Medal]
}>()
</script>

<style scoped lang="scss">
.medal-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-2);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  transition: all var(--motion-fast) var(--ease-standard);
}

.medal-card--locked {
  opacity: 0.45;
}

.medal-card__icon {
  font-size: 48rpx;
  line-height: 1;
}

.medal-card__name {
  color: var(--color-text-primary);
  font-size: var(--font-tiny);
  font-weight: var(--weight-semibold);
  text-align: center;
}

.medal-card__stars {
  display: flex;
  gap: 2rpx;
}

.medal-card__star {
  font-size: 14rpx;
}
</style>
