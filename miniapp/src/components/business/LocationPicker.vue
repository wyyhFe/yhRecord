<template>
  <view class="glass-panel px-[24rpx] py-[24rpx]">
    <view class="flex items-center justify-between">
      <view>
        <view class="text-[28rpx] font-semibold text-ink">位置</view>
        <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">支持当前位置和手动选点，字段会尽量补齐到后端结构。</view>
      </view>
      <view v-if="modelValue?.locationName" class="text-[22rpx] text-[#a56d4b]">已选择</view>
    </view>

    <view v-if="modelValue?.locationName" class="mt-[18rpx] rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] py-[18rpx]">
      <view class="text-[28rpx] font-semibold text-ink">{{ modelValue.locationName }}</view>
      <view class="mt-[10rpx] text-[22rpx] text-[#7f7366]">{{ modelValue.address || '已获取经纬度' }}</view>
    </view>

    <view class="mt-[18rpx] flex gap-[16rpx]">
      <view class="brand-button flex-1 text-[24rpx]" @tap="pickCurrentLocation">当前位置</view>
      <view class="glass-panel flex flex-1 items-center justify-center px-[18rpx] text-[24rpx] text-[#8d6a4f]" @tap="pickManualLocation">手动选择</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reverseGeocode } from '@/api/location'
import type { DiaryLocationInput } from '@/types/diary'

defineProps<{
  modelValue?: DiaryLocationInput
}>()

const emit = defineEmits<{
  'update:modelValue': [value: DiaryLocationInput]
}>()

async function buildLocationPayload(payload: DiaryLocationInput) {
  if (payload.address && payload.province && payload.city) return payload
  if (!payload.latitude || !payload.longitude) return payload

  try {
    const resolved = await reverseGeocode(payload.latitude, payload.longitude)
    return {
      ...resolved,
      locationName: payload.locationName || resolved.locationName || resolved.address || '已选位置',
      sourceType: payload.sourceType
    }
  } catch {
    return payload
  }
}

// 位置字段需要尽量和后端 diary.location 的结构对齐，因此选点后会尝试补齐地址信息。
async function pickCurrentLocation() {
  try {
    const result = await uni.getLocation({ type: 'gcj02' })
    const payload = await buildLocationPayload({
      latitude: result.latitude,
      longitude: result.longitude,
      sourceType: 'CURRENT',
      locationName: '当前位置'
    })
    emit('update:modelValue', payload)
  } catch {
    uni.showToast({ title: '定位失败，请检查权限', icon: 'none' })
  }
}

async function pickManualLocation() {
  // #ifdef MP-WEIXIN
  try {
    const result = await uni.chooseLocation({})
    const payload = await buildLocationPayload({
      latitude: result.latitude,
      longitude: result.longitude,
      sourceType: 'MANUAL',
      locationName: result.name || '手动选择位置',
      address: result.address
    })
    emit('update:modelValue', payload)
  } catch {
    uni.showToast({ title: '已取消选点', icon: 'none' })
  }
  // #endif
  // #ifndef MP-WEIXIN
  uni.showToast({ title: '当前环境不支持手动选点', icon: 'none' })
  // #endif
}
</script>
