<template>
  <view class="section-shell">
    <view class="section-head">
      <view class="section-copy">
        <view class="section-copy__title">图片</view>
        <view class="section-copy__desc">支持从相册选择，也支持直接拍照。</view>
      </view>
      <view class="photo-picker__count">{{ modelValue.length }}/{{ maxCount }}</view>
    </view>

    <view class="photo-picker__grid">
      <view
        v-for="(item, index) in modelValue"
        :key="item.localPath"
        class="photo-picker__card"
      >
        <image :src="item.localPath" mode="aspectFill" class="photo-picker__image" />
        <view class="photo-picker__badge">{{ statusLabel(item.status) }}</view>
        <view class="photo-picker__action photo-picker__action--top" @tap="remove(index)">
          删除
        </view>
        <view
          v-if="item.status === 'failed'"
          class="photo-picker__action photo-picker__action--retry"
          @tap="$emit('retry', index)"
        >
          重试
        </view>
      </view>

      <view
        v-if="modelValue.length < maxCount"
        class="photo-picker__add"
        @tap="openActionSheet"
      >
        <view class="photo-picker__add-content">
          <view class="photo-picker__add-icon">+</view>
          <view class="photo-picker__add-text">添加图片</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
export interface SelectedPhoto {
  localPath: string
  ossPath?: string
  status?: 'pending' | 'uploading' | 'done' | 'failed'
}

const props = withDefaults(defineProps<{
  modelValue: SelectedPhoto[]
  maxCount?: number
}>(), {
  maxCount: 9
})

const emit = defineEmits<{
  'update:modelValue': [value: SelectedPhoto[]]
  retry: [index: number]
}>()

function statusLabel(status?: SelectedPhoto['status']) {
  if (status === 'uploading') return '上传中'
  if (status === 'done') return '已上传'
  if (status === 'failed') return '失败'
  return '待上传'
}

function remove(index: number) {
  const next = props.modelValue.slice()
  next.splice(index, 1)
  emit('update:modelValue', next)
}

async function chooseFrom(sourceType: Array<'album' | 'camera'>) {
  const result = await uni.chooseImage({
    count: props.maxCount - props.modelValue.length,
    sizeType: ['compressed'],
    sourceType
  })
  const paths = Array.isArray(result.tempFilePaths) ? result.tempFilePaths : [result.tempFilePaths]
  const next = [
    ...props.modelValue,
    ...paths.map((path: string) => ({ localPath: path, status: 'pending' as const }))
  ]
  emit('update:modelValue', next)
}

function openActionSheet() {
  console.log('[photo-picker] open action sheet')
  uni.showActionSheet({
    itemList: ['从相册选择', '拍一张'],
    success: ({ tapIndex }) => {
      console.log('[photo-picker] choose source', tapIndex)
      if (tapIndex === 0) {
        chooseFrom(['album']).catch(() => undefined)
      } else {
        chooseFrom(['camera']).catch(() => undefined)
      }
    }
  })
}

defineExpose({
  openActionSheet
})
</script>

<style scoped lang="scss">
.photo-picker__count {
  color: #a56d4b;
  font-size: 22rpx;
}

.photo-picker__grid {
  margin-top: 20rpx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.photo-picker__card {
  position: relative;
  overflow: hidden;
  border-radius: 24rpx;
  background: #f2e8db;
}

.photo-picker__image {
  width: 100%;
  height: 180rpx;
}

.photo-picker__badge {
  position: absolute;
  top: 10rpx;
  left: 10rpx;
  border-radius: 999rpx;
  background: rgba(0, 0, 0, 0.45);
  padding: 6rpx 12rpx;
  color: #fff;
  font-size: 18rpx;
}

.photo-picker__action {
  position: absolute;
  border-radius: 999rpx;
  padding: 6rpx 12rpx;
  color: #fff;
  font-size: 20rpx;
}

.photo-picker__action--top {
  top: 10rpx;
  right: 10rpx;
  background: rgba(0, 0, 0, 0.45);
}

.photo-picker__action--retry {
  right: 10rpx;
  bottom: 10rpx;
  background: #c15b52;
}

.photo-picker__add {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 180rpx;
  border: 1rpx dashed #d0b79f;
  border-radius: 24rpx;
  background: #fcf5ec;
}

.photo-picker__add-content {
  text-align: center;
}

.photo-picker__add-icon {
  color: #b57a55;
  font-size: 38rpx;
}

.photo-picker__add-text {
  margin-top: 8rpx;
  color: #8d7d6d;
  font-size: 22rpx;
}
</style>
