<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">{{ detail?.title || '正在加载...' }}</view>
          <view class="section-copy__desc">
            {{ detail?.ageLabel || '把这一天的内容和照片完整保留下来。' }}
          </view>
        </view>
        <u-tag text="日记" type="warning" plain shape="circle" />
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="content-block">
        <view class="content-block__title">正文</view>
        <view class="content-block__body">
          {{ detail?.content || '暂无内容' }}
        </view>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-copy">
        <view class="section-copy__title">照片</view>
        <view class="section-copy__desc">保留当天上传的图片内容。</view>
      </view>
      <view v-if="detail?.mediaPaths?.length" class="detail-media-grid">
        <image
          v-for="path in detail.mediaPaths"
          :key="path"
          :src="resolveImage(path)"
          mode="aspectFill"
          class="detail-media-grid__image"
        />
      </view>
      <view v-else class="note-card">
        这篇日记还没有照片，后续可以在编辑页里继续补充图片。
      </view>
    </view>

    <view class="page-section section-shell overflow-hidden">
      <view class="section-copy">
        <view class="section-copy__title">补充信息</view>
      </view>
      <u-cell-group :border="false">
        <u-cell-item
          v-for="item in infoItems"
          :key="item.title"
          :title="item.title"
          :label="item.description"
          :value="item.value"
        />
      </u-cell-group>
    </view>

    <view class="action-grid-2">
      <u-button type="primary" shape="circle" color="linear-gradient(135deg, #c47c52 0%, #d7a648 100%)" @click="goEdit">
        编辑这篇日记
      </u-button>
      <u-button type="error" shape="circle" plain @click="removeDiary">删除到回收站</u-button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { deleteDiary, fetchDiaryDetail } from '@/api/diary'
import { OSS_BASE_URL } from '@/config/app'
import type { DiaryItem } from '@/types/domain'

const detail = ref<DiaryItem | null>(null)
const diaryId = ref<string>('')

const infoItems = computed(() => [
  { title: '日期', description: '记录发生在哪一天', value: detail.value?.recordDate || '--' },
  { title: '天气', description: '当天的天气情况', value: detail.value?.weather || '--' },
  { title: '心情', description: '写下那一刻的状态', value: detail.value?.mood || '--' },
  { title: '位置', description: '如果有位置，会显示在这里', value: detail.value?.locationName || '--' }
])

function resolveImage(path: string) {
  return path.startsWith('http') ? path : `${OSS_BASE_URL}/${path}`
}

function goEdit() {
  uni.navigateTo({ url: `/pages/diary/editor?id=${diaryId.value}` })
}

async function removeDiary() {
  if (!diaryId.value) return

  const result = await uni.showModal({
    title: '确认删除',
    content: '删除后会进入回收站，并保留 15 天。'
  })
  if (!result.confirm) return

  await deleteDiary(diaryId.value)
  uni.$feedback.success('已移入回收站')
  setTimeout(() => {
    uni.navigateBack()
  }, 600)
}

async function init() {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1] as { options?: Record<string, string> } | undefined
  const options = current?.options
  diaryId.value = options?.id || ''
  if (!diaryId.value) return
  detail.value = await fetchDiaryDetail(diaryId.value)
}

init().catch(() => undefined)
</script>

<style scoped lang="scss">
.detail-media-grid {
  margin-top: 18rpx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.detail-media-grid__image {
  width: 100%;
  height: 180rpx;
  border-radius: 24rpx;
}
</style>
