<template>
  <view class="glass-panel overflow-hidden">
    <view
      v-for="item in items"
      :key="item.key || item.title"
      class="flex items-center justify-between border-b border-[rgba(196,124,82,0.08)] px-[26rpx] py-[24rpx] last:border-b-0"
      hover-class="bg-[#fbf3ea]"
      @tap="emit('select', item)"
    >
      <view>
        <view class="text-[28rpx] font-semibold text-ink">{{ item.title }}</view>
        <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">{{ item.description }}</view>
      </view>
      <view class="text-[22rpx] text-[#a56d4b]">{{ item.value }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
export interface SettingItem {
  key?: string
  title: string
  description: string
  value: string
}

defineProps<{
  items: SettingItem[]
}>()

const emit = defineEmits<{
  select: [item: SettingItem]
}>()
</script>
