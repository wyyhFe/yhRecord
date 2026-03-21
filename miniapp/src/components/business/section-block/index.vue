<template>
  <view class="mt-[28rpx]">
    <view class="mb-[18rpx] flex items-end justify-between">
      <view>
        <view class="section-title">{{ title }}</view>
        <view v-if="subtitle" class="section-subtitle mt-[8rpx]">{{ subtitle }}</view>
      </view>
      <view v-if="actionText" class="text-[22rpx] font-medium text-[#8d5a3e]">{{ actionText }}</view>
    </view>
    <slot />
  </view>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  actionText?: string
}>()
</script>
