<template>
  <view class="page-shell-safe detail-page">
    <!-- 顶栏 -->
    <view class="detail-header">
      <text class="detail-header__title">{{ detail?.title || '正在加载...' }}</text>
    </view>

    <!-- 元信息行 -->
    <view v-if="detail" class="detail-meta">
      <text class="detail-meta__item">📅 {{ detail.recordDate }}</text>
      <text v-if="detail.weather" class="detail-meta__item">{{ resolveDiaryWeatherLabel(detail.weather) }}</text>
      <text v-if="detail.mood" class="detail-meta__item">{{ resolveDiaryMoodLabel(detail.mood) }}</text>
      <text v-if="detail.ageLabel" class="detail-meta__item detail-meta__item--muted">{{ detail.ageLabel }}</text>
    </view>

    <!-- 正文卡片 -->
    <view class="detail-card">
      <text class="detail-card__text">{{ detail?.content || '暂无内容' }}</text>

      <!-- 标签（附在正文下方） -->
      <view v-if="detail?.tags?.length" class="detail-card__tags">
        <view v-for="tag in detail.tags" :key="tag.id" class="detail-tag">
          <text class="detail-tag__name">{{ tag.name }}</text>
        </view>
      </view>
    </view>

    <!-- 照片 -->
    <view v-if="detail?.mediaPaths?.length" class="detail-photos">
      <image
        v-for="(path, idx) in detail.mediaPaths"
        :key="idx"
        :src="resolveImage(path)"
        mode="aspectFill"
        class="detail-photos__item"
        @tap="previewImage(idx)"
      />
    </view>

    <!-- 位置 + 可见范围（合并成一行卡片） -->
    <view v-if="detail" class="detail-info">
      <view v-if="detail.address" class="detail-info__row">
        <text class="detail-info__label">📍</text>
        <text class="detail-info__value">{{ detail.address }}</text>
      </view>
      <view class="detail-info__row">
        <text class="detail-info__label">👁️</text>
        <text class="detail-info__value">{{ visibilityText }}</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view v-if="!isPublicView" class="detail-actions">
      <view class="detail-actions__btn detail-actions__btn--edit" hover-class="detail-actions__btn--pressed" @click="goEdit">
        <text>✏️ 编辑</text>
      </view>
      <view class="detail-actions__btn detail-actions__btn--delete" hover-class="detail-actions__btn--pressed" @click="removeDiary">
        <text>🗑️ 删除</text>
      </view>
    </view>

    <!-- 公开日记模式：显示作者信息 -->
    <view v-if="isPublicView && detail?.authorId" class="detail-author" @tap="goUserProfile">
      <view class="detail-author__avatar">
        <image
          v-if="detail?.authorAvatar"
          :src="resolveImage(detail.authorAvatar)"
          mode="aspectFill"
          class="detail-author__avatar-img"
        />
        <text v-else class="detail-author__avatar-text">{{ (detail?.authorNickname || '?').charAt(0) }}</text>
      </view>
      <view class="detail-author__info">
        <text class="detail-author__name">{{ detail?.authorNickname || '匿名用户' }}</text>
        <text class="detail-author__hint">查看主页 ›</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { deleteDiary, fetchDiaryDetail, fetchPublicDiaryDetail } from '@/api/diary'
import { OSS_BASE_URL } from '@/config/app'
import { resolveDiaryMoodLabel, resolveDiaryWeatherLabel } from '@/utils/diary-display'
import type { DiaryItem } from '@/types/domain'

const detail = ref<DiaryItem | null>(null)
const diaryId = ref('')
const isPublicView = ref(false)

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
  setTimeout(() => uni.navigateBack(), 600)
}

async function loadDiaryDetail() {
  if (!diaryId.value) return
  if (isPublicView.value) {
    detail.value = await fetchPublicDiaryDetail(diaryId.value)
  } else {
    detail.value = await fetchDiaryDetail(diaryId.value)
  }
}

function goUserProfile() {
  if (!detail.value?.authorId) return
  uni.navigateTo({ url: `/pages/discover/user-profile?userId=${detail.value.authorId}` })
}

onLoad((options) => {
  diaryId.value = options?.id || ''
  isPublicView.value = options?.public === '1'
  loadDiaryDetail().catch((error) => {
    uni.$feedback.error(error, undefined, '加载日记详情失败')
  })
})
</script>

<style scoped lang="scss">
.detail-page {
  padding-bottom: var(--bottom-padding);
}

/* ========== 顶栏 ========== */
.detail-header {
  padding: var(--space-5) var(--space-6) var(--space-1);
}

.detail-header__title {
  color: var(--color-text-primary);
  font-size: var(--font-display);
  font-weight: var(--weight-bold);
  line-height: var(--leading-tight);
}

/* ========== 元信息行 ========== */
.detail-meta {
  padding: 0 var(--space-6) var(--space-5);
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.detail-meta__item {
  padding: 4rpx 16rpx;
  border-radius: var(--radius-full);
  background: var(--color-diary-soft);
  color: var(--color-diary);
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
}

.detail-meta__item--muted {
  background: var(--color-surface-soft);
  color: var(--color-text-muted);
}

/* ========== 正文卡片 ========== */
.detail-card {
  margin: 0 var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding:  var(--space-6);
}

.detail-card__text {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  line-height: var(--leading-loose);
  white-space: pre-wrap;
}

.detail-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: var(--space-5);
  padding-top: var(--space-4);
  border-top: 1rpx solid var(--color-divider);
}

.detail-tag {
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-diary-soft);
}

.detail-tag__name {
  color: var(--color-diary);
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
}

/* ========== 照片 ========== */
.detail-photos {
  margin: var(--space-3) var(--space-4) 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8rpx;
}

.detail-photos__item {
  width: 100%;
  height: 200rpx;
  border-radius: var(--radius-medium);
}

/* ========== 信息行（位置+可见范围） ========== */
.detail-info {
  margin: var(--space-3) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-4) var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.detail-info__row {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
}

.detail-info__label {
  font-size: 28rpx;
  flex-shrink: 0;
  line-height: 1.6;
}

.detail-info__value {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  line-height: 1.6;
}

/* ========== 操作按钮 ========== */
.detail-actions {
  margin: var(--space-6) var(--space-4) 0;
  display: flex;
  gap: var(--space-3);
}

.detail-actions__btn {
  flex: 1;
  text-align: center;
  padding: var(--space-3) 0;
  border-radius: var(--radius-full);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
  transition: all var(--motion-fast) var(--ease-standard);
}

.detail-actions__btn--edit {
  background: var(--color-diary-gradient);
  color: #fff;
}

.detail-actions__btn--delete {
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
}

.detail-actions__btn--pressed {
  transform: scale(0.95);
  opacity: 0.85;
}

/* ========== 公开日记作者信息 ========== */
.detail-author {
  margin: var(--space-6) var(--space-4) 0;
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
}

.detail-author__avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.detail-author__avatar-img {
  width: 100%;
  height: 100%;
}

.detail-author__avatar-text {
  color: var(--color-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-bold);
}

.detail-author__info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-author__name {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.detail-author__hint {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}
</style>
