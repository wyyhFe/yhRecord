<template>
  <view class="grid grid-cols-2 gap-[20rpx]">
    <view v-for="item in items" :key="item.title" class="glass-panel px-[24rpx] py-[24rpx]">
      <view class="text-[28rpx] font-semibold text-ink">{{ item.title }}</view>
      <view class="mt-[10rpx] text-[22rpx] leading-[1.6] text-[#7f7366]">{{ item.description }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  items: Array<{ title: string; description: string }>
}>()
</script>
