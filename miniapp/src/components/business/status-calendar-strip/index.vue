<template>
  <scroll-view scroll-x class="whitespace-nowrap">
    <view class="flex gap-[18rpx]">
      <view
        v-for="item in items"
        :key="item.date"
        class="glass-panel min-w-[132rpx] px-[20rpx] py-[20rpx]"
      >
        <view class="text-[22rpx] text-[#7f7366]">{{ item.date.slice(5) }}</view>
        <view class="mt-[12rpx] flex gap-[8rpx]">
          <view :class="item.hasDiary ? 'bg-[#c47c52]' : 'bg-[#d8d0c5]'" class="h-[14rpx] w-[14rpx] rounded-full" />
          <view :class="item.hasCheckin ? 'bg-[#4b6b57]' : 'bg-[#d8d0c5]'" class="h-[14rpx] w-[14rpx] rounded-full" />
        </view>
        <view class="mt-[12rpx] text-[20rpx] text-[#8f7f71]">日记 {{ item.diaryCount }}</view>
      </view>
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
import type { DaySummary } from '@/types/domain'

defineProps<{
  items: DaySummary[]
}>()
</script>
