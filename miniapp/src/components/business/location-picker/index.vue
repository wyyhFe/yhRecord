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
        color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
        @click="pickCurrentLocation"
      >
        当前位置
      </u-button>
      <u-button shape="circle" plain @click="pickManualLocation">微信地图选点</u-button>
    </view>

    <!-- 调试用：只调 wx.getLocation 拿原始经纬度，不打后端逆地址解析。
         用于对比：如果这里显示的坐标和真实地点对得上 → 后端解析有问题；对不上 → wx 本身就给错了 -->
    <view class="primary-action">
      <u-button shape="circle" size="mini" plain :hair-line="false" @click="debugCoordinates">
        🔍 显示原始经纬度（调试）
      </u-button>
      <view v-if="rawCoords" class="location-picker__debug">
        <view>纬度 lat: {{ rawCoords.latitude }}</view>
        <view>经度 lng: {{ rawCoords.longitude }}</view>
        <view>精度（米）: {{ rawCoords.accuracy ?? '未知' }}</view>
        <view class="location-picker__debug-hint">
          复制坐标到 https://lbs.qq.com/getPoint/ 验证位置
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { DiaryLocationInput } from '@/types/diary'
import { pickCurrentLocationPayload, pickManualLocationPayload } from '@/utils/qqmap/location'

defineProps<{
  modelValue?: DiaryLocationInput
}>()

const emit = defineEmits<{
  'update:modelValue': [value: DiaryLocationInput]
}>()

// 调试用：只存 wx.getLocation 拿到的原始经纬度，不传给后端。
const rawCoords = ref<{ latitude: number; longitude: number; accuracy?: number } | null>(null)

async function debugCoordinates() {
  try {
    // 用与正式定位完全一样的参数，确保对比有意义
    const result = await uni.getLocation({
      type: 'gcj02',
      isHighAccuracy: true,
      highAccuracyExpireTime: 3000
    })
    rawCoords.value = {
      latitude: result.latitude,
      longitude: result.longitude,
      accuracy: result.accuracy
    }
    uni.$feedback.success('原始经纬度已展示在下方')
  } catch (error) {
    uni.$feedback.error(error, undefined, '获取经纬度失败')
  }
}

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
  background: var(--color-surface-soft);
  padding: 18rpx 22rpx;
}

.location-picker__name {
  color: var(--color-text-primary);
  font-size: 28rpx;
  font-weight: 600;
}

.location-picker__address {
  margin-top: 10rpx;
  color: var(--color-text-secondary);
  font-size: 22rpx;
}

/* 调试块：用 surface-soft + 等宽字体，显示坐标方便复制核对 */
.location-picker__debug {
  margin-top: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  /* 调试展示的坐标用中性色，跟主题色调隔离开 */
  color: var(--color-text-neutral);
  font-size: var(--font-meta);
  font-family: 'SF Mono', Consolas, monospace;
  line-height: 1.8;
}

.location-picker__debug-hint {
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  font-family: inherit;
}
</style>
