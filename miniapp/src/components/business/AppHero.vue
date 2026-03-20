<template>
  <view class="glass-panel overflow-hidden px-[30rpx] py-[32rpx]">
    <view class="flex items-start justify-between">
      <view class="max-w-[72%]">
        <view class="text-[24rpx] text-[#7b6c5a]">{{ eyebrow }}</view>
        <view class="mt-[10rpx] text-[42rpx] font-bold text-ink">{{ title }}</view>
        <view class="mt-[12rpx] text-[24rpx] leading-[1.6] text-[#6f6457]">{{ description }}</view>
      </view>
      <view class="rounded-full bg-[#f2dfcb] px-[20rpx] py-[10rpx] text-[22rpx] text-[#8d5a3e]">{{ badge }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  eyebrow: string
  title: string
  description: string
  badge: string
}>()
</script>
