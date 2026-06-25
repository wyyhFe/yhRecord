<template>
  <view>
    <!-- 展示行：位置名 + 箭头，点击弹出选择 -->
    <view class="lp-row" @tap="showActionSheet = true">
      <view class="lp-row__left">
        <text class="lp-row__label">位置</text>
        <text v-if="modelValue?.locationName" class="lp-row__value">{{ modelValue.locationName }}</text>
      </view>
      <view class="lp-row__right">
        <text v-if="!modelValue?.locationName" class="lp-row__placeholder">添加位置</text>
        <text class="lp-row__arrow">›</text>
      </view>
    </view>

    <!-- 已选地址详情 -->
    <view v-if="modelValue?.address" class="lp-detail">
      <text class="lp-detail__text">{{ modelValue.address }}</text>
      <text class="lp-detail__clear" @tap.stop="clearLocation">✕</text>
    </view>

    <!-- 操作面板 -->
    <u-action-sheet
      v-model="showActionSheet"
      :list="actionItems"
      @click="onActionSelect"
      :cancel-btn="false"
      :border-radius="28"
    />

    <!-- 调试用原始经纬度 -->
    <view v-if="rawCoords" class="lp-debug">
      <text class="lp-debug__text">lat: {{ rawCoords.latitude }}, lng: {{ rawCoords.longitude }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { DiaryLocationInput } from '@/types/diary'
import { pickCurrentLocationPayload, pickManualLocationPayload } from '@/utils/qqmap/location'

const props = defineProps<{
  modelValue?: DiaryLocationInput
}>()

const emit = defineEmits<{
  'update:modelValue': [value: DiaryLocationInput]
}>()

const showActionSheet = ref(false)
const rawCoords = ref<{ latitude: number; longitude: number; accuracy?: number } | null>(null)

const actionItems = [
  { text: '📍 自动获取当前位置', value: 'auto' },
  { text: '🗺️ 微信地图选点', value: 'manual' },
  { text: '🔍 显示原始经纬度（调试）', value: 'debug' }
]

function onActionSelect(index: number) {
  showActionSheet.value = false
  const action = actionItems[index]
  if (!action) return
  if (action.value === 'auto') pickCurrentLocation()
  else if (action.value === 'manual') pickManualLocation()
  else if (action.value === 'debug') debugCoordinates()
}

function clearLocation() {
  rawCoords.value = null
  emit('update:modelValue', undefined as unknown as DiaryLocationInput)
}

async function debugCoordinates() {
  try {
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
    uni.$feedback.success('原始经纬度已展示')
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

defineExpose({ pickCurrentLocation, pickManualLocation })
</script>

<style scoped lang="scss">
.lp-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) 0;
}

.lp-row__left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1;
  min-width: 0;
}

.lp-row__label {
  color: var(--color-text-secondary);
  font-size: var(--font-body);
  flex-shrink: 0;
}

.lp-row__value {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-medium);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lp-row__right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
}

.lp-row__placeholder {
  color: var(--color-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}

.lp-row__arrow {
  color: var(--color-text-muted);
  font-size: 28rpx;
}

.lp-detail {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-small);
  background: var(--color-surface-soft);
  margin-bottom: var(--space-2);
}

.lp-detail__text {
  flex: 1;
  color: var(--color-text-muted);
  font-size: 20rpx;
  line-height: var(--leading-snug);
}

.lp-detail__clear {
  width: 32rpx;
  height: 32rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.lp-debug {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-small);
  background: var(--color-surface-soft);
  margin-top: var(--space-2);
}

.lp-debug__text {
  color: var(--color-text-muted);
  font-size: 20rpx;
  font-family: 'SF Mono', Consolas, monospace;
}
</style>
