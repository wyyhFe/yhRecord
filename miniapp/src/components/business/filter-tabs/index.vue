<template>
  <scroll-view scroll-x class="whitespace-nowrap">
    <view class="flex gap-[16rpx]">
      <view
        v-for="item in items"
        :key="item.value"
        class="rounded-full px-[22rpx] py-[14rpx] text-[24rpx]"
        :class="modelValue === item.value ? 'bg-[#c47c52] text-white' : 'glass-panel text-[#7c6d5d]'"
        @tap="$emit('update:modelValue', item.value)"
      >
        {{ item.label }}
      </view>
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
defineProps<{
  items: Array<{ label: string; value: string }>
  modelValue: string
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>
