<template>
  <view class="section-shell">
    <view class="section-head">
      <view class="section-copy">
        <view class="section-copy__title">位置</view>
        <view class="section-copy__desc">可以直接获取当前位置，也可以调用微信地图选点。</view>
      </view>
      <u-tag v-if="modelValue?.locationName" text="已选择" plain shape="circle" type="warning" />
    </view>

    <view v-if="modelValue?.locationName" class="location-picker__result">
      <view class="location-picker__name">{{ modelValue.locationName }}</view>
      <view class="location-picker__address">
        {{ modelValue.address || '已获取坐标，保存时会继续补全地址信息' }}
      </view>
    </view>

    <view class="action-grid-2">
      <u-button
        type="primary"
        shape="circle"
        color="linear-gradient(135deg, #c47c52 0%, #d7a648 100%)"
        @click="pickCurrentLocation"
      >
        当前位置
      </u-button>
      <u-button shape="circle" plain @click="pickManualLocation">微信地图选点</u-button>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { DiaryLocationInput } from '@/types/diary'
import { pickCurrentLocationPayload, pickManualLocationPayload } from '@/utils/qqmap/location'

defineProps<{
  modelValue?: DiaryLocationInput
}>()

const emit = defineEmits<{
  'update:modelValue': [value: DiaryLocationInput]
}>()

async function pickCurrentLocation() {
  try {
    const result = await pickCurrentLocationPayload()
    emit('update:modelValue', result.payload)

    if (result.reverseGeocodeError) {
      uni.$feedback.error(result.reverseGeocodeError, undefined, '已获取坐标，但地址解析失败')
      return
    }

    uni.$feedback.success(result.payload.address ? '已获取当前位置' : '已获取当前位置坐标')
  } catch (error) {
    uni.$feedback.error(error, undefined, '定位失败，请检查定位权限')
  }
}

async function pickManualLocation() {
  // #ifdef MP-WEIXIN
  try {
    const payload = await pickManualLocationPayload()
    emit('update:modelValue', payload)
  } catch {
    uni.$feedback.info('已取消选点')
  }
  // #endif

  // #ifndef MP-WEIXIN
  uni.$feedback.info('当前环境不支持微信地图选点')
  // #endif
}

defineExpose({
  pickCurrentLocation,
  pickManualLocation
})
</script>

<style scoped lang="scss">
.location-picker__result {
  margin-top: 18rpx;
  border-radius: 20rpx;
  background: #fcf5ec;
  padding: 18rpx 22rpx;
}

.location-picker__name {
  color: #2b2118;
  font-size: 28rpx;
  font-weight: 600;
}

.location-picker__address {
  margin-top: 10rpx;
  color: #7f7366;
  font-size: 22rpx;
}
</style>
