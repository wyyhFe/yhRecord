<template>
  <view class="glass-panel px-[24rpx] py-[24rpx]">
    <view class="flex items-center justify-between">
      <view>
        <view class="text-[28rpx] font-semibold text-ink">位置</view>
        <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">
          支持当前位置和手动选点，字段会尽量补齐到后端 `diary.location` 结构。
        </view>
      </view>
      <view v-if="modelValue?.locationName" class="text-[22rpx] text-[#a56d4b]">已选择</view>
    </view>

    <view
      v-if="modelValue?.locationName"
      class="mt-[18rpx] rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] py-[18rpx]"
    >
      <view class="text-[28rpx] font-semibold text-ink">{{ modelValue.locationName }}</view>
      <view class="mt-[10rpx] text-[22rpx] text-[#7f7366]">
        {{ modelValue.address || '已获取经纬度' }}
      </view>
    </view>

    <view class="mt-[18rpx] flex gap-[16rpx]">
      <view class="brand-button flex-1 text-[24rpx]" @tap="pickCurrentLocation">当前位置</view>
      <view
        class="glass-panel flex flex-1 items-center justify-center px-[18rpx] text-[24rpx] text-[#8d6a4f]"
        @tap="pickManualLocation"
      >
        手动选择
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { DiaryLocationInput } from '@/types/diary'
import { pickCurrentLocationPayload, pickManualLocationPayload } from '@/utils/qqmap'

defineProps<{
  modelValue?: DiaryLocationInput
}>()

const emit = defineEmits<{
  'update:modelValue': [value: DiaryLocationInput]
}>()

/**
 * 获取当前位置并同步给父组件。
 */
async function pickCurrentLocation() {
  try {
    const payload = await pickCurrentLocationPayload()
    emit('update:modelValue', payload)
  } catch {
    uni.showToast({ title: '定位失败，请检查权限', icon: 'none' })
  }
}

/**
 * 使用微信原生选点能力，选中后同步给父组件。
 */
async function pickManualLocation() {
  // #ifdef MP-WEIXIN
  try {
    const payload = await pickManualLocationPayload()
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
