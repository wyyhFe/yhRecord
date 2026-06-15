<template>
  <u-popup v-model="visible" mode="center" border-radius="28" :safe-area-inset-bottom="true">
    <view class="unlock-popup">
      <view class="unlock-popup__confetti">🎉</view>
      <view class="unlock-popup__title">恭喜解锁！</view>
      <view class="unlock-popup__icon-wrap">
        <text class="unlock-popup__icon">{{ medal?.icon }}</text>
      </view>
      <view class="unlock-popup__name">{{ medal?.name }}</view>
      <view class="unlock-popup__desc">{{ medal?.description }}</view>
      <u-button
        class="unlock-popup__btn"
        shape="circle"
        type="primary"
        color="linear-gradient(135deg, var(--color-checkin) 0%, var(--color-checkin-gradient) 100%)"
        @click="visible = false"
      >
        太棒了！
      </u-button>
    </view>
  </u-popup>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Medal } from '@/types/domain'

const props = defineProps<{
  modelValue: boolean
  medal: Medal | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = ref(props.modelValue)

watch(() => props.modelValue, (v) => { visible.value = v })
watch(visible, (v) => { emit('update:modelValue', v) })
</script>

<style scoped lang="scss">
.unlock-popup {
  width: 560rpx;
  padding: var(--space-7) var(--space-6);
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--color-surface);
  border-radius: var(--radius-large);
}

.unlock-popup__confetti {
  font-size: 56rpx;
  margin-bottom: var(--space-3);
}

.unlock-popup__title {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
  margin-bottom: var(--space-4);
}

.unlock-popup__icon-wrap {
  width: 128rpx;
  height: 128rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  margin-bottom: var(--space-4);
}

.unlock-popup__icon {
  font-size: 72rpx;
  line-height: 1;
}

.unlock-popup__name {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
  margin-bottom: var(--space-2);
}

.unlock-popup__desc {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  margin-bottom: var(--space-6);
}

.unlock-popup__btn {
  width: 100%;
}
</style>
