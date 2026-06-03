<template>
  <view class="page-shell-safe detail-page">
    <!-- Hero 头部 -->
    <view class="detail-hero">
      <view class="detail-hero__tags">
        <view v-for="item in metaItems" :key="item.key" class="detail-hero__tag">
          <text class="detail-hero__tag-label">{{ item.label }}</text>
          <text class="detail-hero__tag-value">{{ item.value }}</text>
        </view>
      </view>
      <text class="detail-hero__title">{{ detail?.title || '正在加载...' }}</text>
      <text class="detail-hero__subtitle">
        {{ detail?.ageLabel || '把这一天的内容完整保留下来。' }}
      </text>
    </view>

    <!-- 正文 -->
    <view class="detail-section">
      <text class="detail-section__text">{{ detail?.content || '暂无内容' }}</text>
    </view>

    <!-- 照片 -->
    <view v-if="detail?.mediaPaths?.length" class="detail-section">
      <view class="detail-media">
        <image
          v-for="path in detail.mediaPaths"
          :key="path"
          :src="resolveImage(path)"
          mode="aspectFill"
          class="detail-media__item"
        />
      </view>
    </view>

    <!-- 补充信息 -->
    <view v-if="detail?.address || detail?.visibility" class="detail-section">
      <view class="detail-extra">
        <view v-if="detail?.address" class="detail-extra__item">
          <text class="detail-extra__label">地址</text>
          <text class="detail-extra__value">{{ detail.address }}</text>
        </view>
        <view v-if="detail?.visibility" class="detail-extra__item">
          <text class="detail-extra__label">可见范围</text>
          <text class="detail-extra__value">{{ visibilityText }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="detail-actions">
      <view class="btn-secondary" @click="goEdit">
        <text>编辑日记</text>
      </view>
      <view class="btn-ghost btn-ghost--danger" @click="removeDiary">
        <text>删除</text>
      </view>
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
/* ============================================================
 * Diary Detail v4 · Warm Modern
 * ========================================================= */

.detail-page {
  padding-bottom: var(--space-10);
}

/* Hero 渐变头部 */
.detail-hero {
  background: var(--hero-diary-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-7) var(--space-6) var(--space-6);
}

.detail-hero__tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.detail-hero__tag {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: 8rpx 18rpx;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
}

.detail-hero__tag-label {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.detail-hero__tag-value {
  color: var(--color-text-primary);
  font-size: var(--font-tiny);
  font-weight: var(--weight-semibold);
}

.detail-hero__title {
  margin-top: var(--space-4);
  color: var(--color-text-primary);
  font-size: var(--font-display);
  font-weight: var(--weight-bold);
  line-height: var(--leading-tight);
}

.detail-hero__subtitle {
  margin-top: var(--space-3);
  color: var(--color-text-secondary);
  font-size: var(--font-body);
  line-height: var(--leading-relaxed);
}

/* 内容区块 */
.detail-section {
  margin-top: var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-6);
}

.detail-section__text {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  line-height: var(--leading-loose);
}

/* 照片网格 */
.detail-media {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.detail-media__item {
  width: 100%;
  height: 200rpx;
  border-radius: var(--radius-medium);
}

/* 补充信息 */
.detail-extra {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.detail-extra__item {
  padding: var(--space-4) var(--space-5);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  border-left: 6rpx solid var(--color-primary);
}

.detail-extra__label {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.detail-extra__value {
  margin-top: var(--space-2);
  color: var(--color-text-primary);
  font-size: var(--font-caption);
  line-height: var(--leading-relaxed);
}

/* 操作按钮 */
.detail-actions {
  margin-top: var(--space-7);
  display: flex;
  gap: var(--space-4);
}

.detail-actions .btn-secondary {
  flex: 1;
}

.btn-ghost--danger {
  color: var(--color-danger) !important;
}
</style>
