<template>
  <view class="glass-panel px-[28rpx] py-[26rpx]" @tap="$emit('click')">
    <view class="flex items-center justify-between">
      <view class="max-w-[72%] text-[30rpx] font-semibold text-ink">{{ title }}</view>
      <view class="rounded-full bg-[#f4e4da] px-[16rpx] py-[8rpx] text-[20rpx] text-[#9b6547]">{{ mood }}</view>
    </view>
    <view class="mt-[12rpx] line-clamp-2 text-[24rpx] leading-[1.7] text-[#6b6359]">{{ content }}</view>
    <view class="mt-[18rpx] flex items-center justify-between text-[22rpx] text-[#918577]">
      <view>{{ recordDate }}</view>
      <view>{{ footer }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  content: string
  mood: string
  recordDate: string
  footer: string
}>()

defineEmits<{
  click: []
}>()
</script>
