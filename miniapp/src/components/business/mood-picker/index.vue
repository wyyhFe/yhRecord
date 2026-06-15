<template>
  <view class="mood-picker">
    <text class="mood-picker__label">今日心情</text>
    <view class="mood-picker__list">
      <view
        v-for="item in moods"
        :key="item"
        class="mood-picker__item"
        :class="{ 'mood-picker__item--active': modelValue === item }"
        @tap="toggle(item)"
      >
        <text class="mood-picker__emoji">{{ item }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
const moods = ['😊', '😐', '😫', '💪', '🎉', '😴', '🤢', '🔥', '🌧️', '⭐']

const props = defineProps<{
  modelValue?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | undefined]
}>()

function toggle(item: string) {
  emit('update:modelValue', props.modelValue === item ? undefined : item)
}
</script>

<style scoped lang="scss">
.mood-picker {
  margin-bottom: var(--space-3);
}

.mood-picker__label {
  display: block;
  margin-bottom: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
}

.mood-picker__list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.mood-picker__item {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  transition: all var(--motion-fast) var(--ease-standard);
}

.mood-picker__item--active {
  background: var(--color-checkin-soft);
  transform: scale(1.15);
  box-shadow: 0 0 0 3rpx var(--color-checkin);
}

.mood-picker__emoji {
  font-size: 36rpx;
  line-height: 1;
}
</style>
