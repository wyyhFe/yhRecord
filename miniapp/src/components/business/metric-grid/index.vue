<template>
  <view class="grid grid-cols-3 gap-[20rpx]">
    <view v-for="item in items" :key="item.label" class="glass-panel px-[24rpx] py-[24rpx]">
      <view class="text-[22rpx] text-[#7f7366]">{{ item.label }}</view>
      <view class="mt-[12rpx] text-[34rpx] font-semibold text-ink">{{ item.value }}</view>
      <view class="mt-[8rpx] text-[22rpx] text-[#a18f7f]">{{ item.hint }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  items: Array<{ label: string; value: string; hint: string }>
}>()
</script>
