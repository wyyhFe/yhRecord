<template>
  <view v-if="visible" class="app-loading">
    <view class="app-loading__mask" />
    <view class="app-loading__body">
      <view class="app-loading__spinner">
        <view v-for="i in 8" :key="i" class="app-loading__dot" :style="{ animationDelay: `${(i - 1) * 0.1}s` }" />
      </view>
      <text v-if="text" class="app-loading__text">{{ text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
  text?: string
}>()
</script>

<style scoped lang="scss">
.app-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-loading__mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.15);
}

.app-loading__body {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
  padding: 48rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
}

.app-loading__spinner {
  width: 64rpx;
  height: 64rpx;
  position: relative;
}

.app-loading__dot {
  position: absolute;
  top: 0;
  left: 50%;
  width: 8rpx;
  height: 20rpx;
  margin-left: -4rpx;
  border-radius: 4rpx;
  background: var(--color-primary);
  transform-origin: center 32rpx;
  animation: loading-fade 0.8s linear infinite;
}

@for $i from 1 through 8 {
  .app-loading__dot:nth-child(#{$i}) {
    transform: rotate(#{$i * 45}deg);
  }
}

@keyframes loading-fade {
  0% { opacity: 1; }
  100% { opacity: 0.15; }
}

.app-loading__text {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}
</style>
