<template>
  <view class="glass-panel px-[24rpx] py-[24rpx]">
    <view class="flex items-center justify-between">
      <view>
        <view class="text-[28rpx] font-semibold text-ink">提醒时间</view>
        <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">当天还没写日记时，按设定时间提醒。</view>
      </view>
      <switch :checked="enabled" color="#c47c52" @change="handleSwitch" />
    </view>

    <picker v-if="enabled" mode="time" :value="timeValue || '21:00'" @change="handleTimeChange">
      <view class="mt-[18rpx] rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] py-[18rpx] text-[28rpx] font-semibold text-ink">
        {{ timeValue || '21:00' }}
      </view>
    </picker>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  enabled: boolean
  timeValue?: string
}>()

const emit = defineEmits<{
  'update:enabled': [value: boolean]
  'update:timeValue': [value?: string]
}>()

function handleSwitch(event: any) {
  const enabled = Boolean(event?.detail?.value)
  emit('update:enabled', enabled)
  if (!enabled) {
    emit('update:timeValue', undefined)
  }
}

function handleTimeChange(event: any) {
  emit('update:timeValue', event?.detail?.value)
}
</script>
