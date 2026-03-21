<template>
  <view class="glass-panel px-[24rpx] py-[24rpx]">
    <view class="flex items-center justify-between">
      <view>
        <view class="text-[28rpx] font-semibold text-ink">照片</view>
        <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">支持从相册选择，也支持现场拍照。</view>
      </view>
      <view class="text-[22rpx] text-[#a56d4b]">{{ modelValue.length }}/{{ maxCount }}</view>
    </view>

    <view class="mt-[20rpx] grid grid-cols-3 gap-[16rpx]">
      <view
        v-for="(item, index) in modelValue"
        :key="item.localPath"
        class="relative overflow-hidden rounded-[24rpx] bg-[#f2e8db]"
      >
        <image :src="item.localPath" mode="aspectFill" class="h-[180rpx] w-full" />
        <view
          class="absolute left-[10rpx] top-[10rpx] rounded-full bg-[rgba(0,0,0,0.45)] px-[12rpx] py-[6rpx] text-[18rpx] text-white"
        >
          {{ statusLabel(item.status) }}
        </view>
        <view
          class="absolute right-[10rpx] top-[10rpx] rounded-full bg-[rgba(0,0,0,0.45)] px-[12rpx] py-[6rpx] text-[20rpx] text-white"
          @tap="remove(index)"
        >
          删除
        </view>
        <view
          v-if="item.status === 'failed'"
          class="absolute bottom-[10rpx] right-[10rpx] rounded-full bg-[#c15b52] px-[12rpx] py-[6rpx] text-[20rpx] text-white"
          @tap="$emit('retry', index)"
        >
          重试
        </view>
      </view>

      <view
        v-if="modelValue.length < maxCount"
        class="flex h-[180rpx] items-center justify-center rounded-[24rpx] border border-dashed border-[#d0b79f] bg-[#fcf5ec]"
        @tap="openActionSheet"
      >
        <view class="text-center">
          <view class="text-[38rpx] text-[#b57a55]">+</view>
          <view class="mt-[8rpx] text-[22rpx] text-[#8d7d6d]">添加照片</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
/**
 * 前端照片项结构。
 * `localPath` 用于预览，`ossPath` 用于最终提交给后端。
 */
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

/**
 * 把内部状态转换成更适合用户理解的文案。
 */
function statusLabel(status?: SelectedPhoto['status']) {
  if (status === 'uploading') return '上传中'
  if (status === 'done') return '已上传'
  if (status === 'failed') return '失败'
  return '待上传'
}

/**
 * 从当前列表里删除一张图片。
 */
function remove(index: number) {
  const next = props.modelValue.slice()
  next.splice(index, 1)
  emit('update:modelValue', next)
}

/**
 * 从相册或相机选择图片，并先以本地待上传状态放入列表。
 */
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

/**
 * 通过动作面板统一提供“相册”和“拍照”两个入口。
 */
function openActionSheet() {
  uni.showActionSheet({
    itemList: ['从相册选择', '拍一张'],
    success: ({ tapIndex }) => {
      if (tapIndex === 0) {
        chooseFrom(['album']).catch(() => undefined)
      } else {
        chooseFrom(['camera']).catch(() => undefined)
      }
    }
  })
}
</script>
