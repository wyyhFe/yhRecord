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

      <view v-if="metaItems.length" class="detail-meta">
        <view v-for="item in metaItems" :key="item.key" class="detail-meta__item">
          <text class="detail-meta__label">{{ item.label }}</text>
          <text class="detail-meta__value">{{ item.value }}</text>
        </view>
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

    <view v-if="detail?.address || detail?.visibility" class="page-section section-shell">
      <view class="section-copy">
        <view class="section-copy__title">补充信息</view>
      </view>
      <view class="detail-extra">
        <view v-if="detail?.address" class="detail-extra__card">
          <view class="detail-extra__label">地址</view>
          <view class="detail-extra__value">{{ detail.address }}</view>
        </view>
        <view v-if="detail?.visibility" class="detail-extra__card">
          <view class="detail-extra__label">可见范围</view>
          <view class="detail-extra__value">{{ visibilityText }}</view>
        </view>
      </view>
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
import { onLoad } from '@dcloudio/uni-app'
import { deleteDiary, fetchDiaryDetail } from '@/api/diary'
import { OSS_BASE_URL } from '@/config/app'
import { resolveDiaryMoodLabel, resolveDiaryWeatherLabel } from '@/utils/diary-display'
import type { DiaryItem } from '@/types/domain'

const detail = ref<DiaryItem | null>(null)
const diaryId = ref('')

const metaItems = computed(() => {
  const items: Array<{ key: string; label: string; value: string }> = []
  if (detail.value?.recordDate) {
    items.push({ key: 'recordDate', label: '日期', value: detail.value.recordDate })
  }
  if (detail.value?.weather) {
    items.push({
      key: 'weather',
      label: '天气',
      value: resolveDiaryWeatherLabel(detail.value.weather)
    })
  }
  if (detail.value?.mood) {
    items.push({
      key: 'mood',
      label: '心情',
      value: resolveDiaryMoodLabel(detail.value.mood)
    })
  }
  if (detail.value?.locationName) {
    items.push({ key: 'locationName', label: '位置', value: detail.value.locationName })
  }
  return items
})

const visibilityText = computed(() => {
  const visibility = detail.value?.visibility
  if (visibility === 'PUBLIC') return '公开'
  if (visibility === 'SHARED') return '仅分享可见'
  return '仅自己可见'
})

function resolveImage(path: string) {
  return path.startsWith('http') ? path : `${OSS_BASE_URL}/${path}`
}

function goEdit() {
  if (!diaryId.value) return
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

async function loadDiaryDetail() {
  if (!diaryId.value) return
  detail.value = await fetchDiaryDetail(diaryId.value)
}

onLoad((options) => {
  diaryId.value = options?.id || ''
  loadDiaryDetail().catch((error) => {
    uni.$feedback.error(error, undefined, '加载日记详情失败')
  })
})
</script>

<style scoped lang="scss">
.detail-meta {
  margin-top: 20rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.detail-meta__item {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  min-width: 0;
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: #fcf5ec;
  border: 1rpx solid rgba(196, 124, 82, 0.12);
}

.detail-meta__label {
  color: #8c735c;
  font-size: 22rpx;
}

.detail-meta__value {
  max-width: 280rpx;
  color: #2b2118;
  font-size: 24rpx;
  font-weight: 600;
}

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

.detail-extra {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.detail-extra__card {
  padding: 20rpx 22rpx;
  border-radius: 22rpx;
  background: #fffaf4;
  border: 1rpx solid rgba(196, 124, 82, 0.1);
}

.detail-extra__label {
  color: #8a735f;
  font-size: 22rpx;
}

.detail-extra__value {
  margin-top: 10rpx;
  color: #2b2118;
  font-size: 26rpx;
  line-height: 1.6;
}
</style>
