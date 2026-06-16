<template>
  <view class="page-shell-safe detail-page">
    <!-- Hero -->
    <view class="detail-hero">
      <view class="detail-hero__meta">
        <view v-if="detail?.recordDate" class="detail-hero__chip">
          <text class="detail-hero__chip-text">📅 {{ detail.recordDate }}</text>
        </view>
        <view v-if="detail?.weather" class="detail-hero__chip">
          <text class="detail-hero__chip-text">{{ resolveDiaryWeatherLabel(detail.weather) }}</text>
        </view>
        <view v-if="detail?.mood" class="detail-hero__chip">
          <text class="detail-hero__chip-text">{{ resolveDiaryMoodLabel(detail.mood) }}</text>
        </view>
      </view>
      <text class="detail-hero__title">{{ detail?.title || '正在加载...' }}</text>
      <text v-if="detail?.ageLabel" class="detail-hero__age">{{ detail.ageLabel }}</text>
    </view>

    <!-- 正文 -->
    <view class="detail-card">
      <text class="detail-card__text">{{ detail?.content || '暂无内容' }}</text>
    </view>

    <!-- 照片 -->
    <view v-if="detail?.mediaPaths?.length" class="detail-card">
      <view class="detail-card__header">
        <text class="detail-card__title">📷 照片</text>
        <text class="detail-card__badge">{{ detail.mediaPaths.length }}</text>
      </view>
      <view class="detail-media">
        <image
          v-for="(path, idx) in detail.mediaPaths"
          :key="idx"
          :src="resolveImage(path)"
          mode="aspectFill"
          class="detail-media__item"
          @tap="previewImage(idx)"
        />
      </view>
    </view>

    <!-- 标签 -->
    <view v-if="detail?.tags?.length" class="detail-card">
      <view class="detail-card__header">
        <text class="detail-card__title">🏷️ 标签</text>
      </view>
      <view class="detail-tags">
        <view v-for="tag in detail.tags" :key="tag.id" class="detail-tag">
          <text class="detail-tag__name">{{ tag.name }}</text>
        </view>
      </view>
    </view>

    <!-- 位置信息 -->
    <view v-if="detail?.address" class="detail-card">
      <view class="detail-card__header">
        <text class="detail-card__title">📍 位置</text>
      </view>
      <text class="detail-card__text detail-card__text--secondary">{{ detail.address }}</text>
    </view>

    <!-- 可见范围 -->
    <view v-if="detail?.visibility" class="detail-card">
      <view class="detail-card__row">
        <text class="detail-card__row-label">可见范围</text>
        <view class="detail-card__row-badge" :class="'detail-card__row-badge--' + detail.visibility?.toLowerCase()">
          <text class="detail-card__row-badge-text">{{ visibilityText }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="detail-actions">
      <u-button
        shape="circle"
        type="primary"
        color="var(--color-diary-gradient)"
        @click="goEdit"
      >
        编辑日记
      </u-button>
      <u-button
        shape="circle"
        plain
        type="error"
        :hair-line="false"
        @click="removeDiary"
      >
        删除
      </u-button>
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

const visibilityText = computed(() => {
  const v = detail.value?.visibility
  if (v === 'PUBLIC') return '公开'
  if (v === 'SHARED') return '仅分享可见'
  return '仅自己可见'
})

function resolveImage(path: string) {
  return path.startsWith('http') ? path : `${OSS_BASE_URL}/${path}`
}

function previewImage(idx: number) {
  if (!detail.value?.mediaPaths?.length) return
  const urls = detail.value.mediaPaths.map((p) => resolveImage(p))
  uni.previewImage({ urls, current: idx })
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
.detail-page {
  padding-bottom: var(--space-10);
}

/* ========== Hero ========== */
.detail-hero {
  background: var(--color-diary-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-5) var(--space-6);
  min-height: 200rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
}

.detail-hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.detail-hero__chip {
  padding: 4rpx 14rpx;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.2);
}

.detail-hero__chip-text {
  color: #fff;
  font-size: var(--font-tiny);
}

.detail-hero__title {
  display: block;
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
  line-height: var(--leading-tight);
}

.detail-hero__age {
  display: block;
  margin-top: var(--space-2);
  font-size: var(--font-tiny);
  opacity: 0.8;
}

/* ========== 通用卡片 ========== */
.detail-card {
  margin: var(--space-4) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.detail-card__header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.detail-card__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.detail-card__badge {
  padding: 2rpx 14rpx;
  border-radius: var(--radius-full);
  background: var(--color-diary-soft);
  color: var(--color-diary);
  font-size: var(--font-tiny);
  font-weight: var(--weight-semibold);
}

.detail-card__text {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  line-height: var(--leading-loose);
}

.detail-card__text--secondary {
  color: var(--color-text-secondary);
  font-size: var(--font-caption);
}

.detail-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-card__row-label {
  color: var(--color-text-secondary);
  font-size: var(--font-body);
}

.detail-card__row-badge {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
}

.detail-card__row-badge--public {
  background: rgba(52, 199, 89, 0.12);
}

.detail-card__row-badge--shared {
  background: rgba(90, 200, 250, 0.12);
}

.detail-card__row-badge--private {
  background: var(--color-surface-soft);
}

.detail-card__row-badge-text {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}

.detail-card__row-badge--public .detail-card__row-badge-text {
  color: var(--color-success);
}

/* ========== 照片 ========== */
.detail-media {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12rpx;
}

.detail-media__item {
  width: 100%;
  height: 200rpx;
  border-radius: var(--radius-medium);
}

/* ========== 标签 ========== */
.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.detail-tag {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--color-diary-soft);
}

.detail-tag__name {
  color: var(--color-diary);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}

/* ========== 操作按钮 ========== */
.detail-actions {
  margin: var(--space-6) var(--space-4) 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
</style>
