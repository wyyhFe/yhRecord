<template>
  <view class="glass-panel flex flex-col items-center px-[30rpx] py-[40rpx] text-center">
    <view class="rounded-full bg-[#f2dfcb] px-[24rpx] py-[16rpx] text-[32rpx]">{{ icon }}</view>
    <view class="mt-[18rpx] text-[30rpx] font-semibold text-ink">{{ title }}</view>
    <view class="mt-[10rpx] text-[24rpx] leading-[1.7] text-[#7f7366]">{{ description }}</view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  icon: string
  title: string
  description: string
}>()
</script>
