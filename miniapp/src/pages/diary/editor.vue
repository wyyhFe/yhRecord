<template>
  <view class="page-shell-safe diary-editor-page">
    <!-- Hero -->
    <view class="editor-hero">
      <view class="editor-hero__top">
        <view class="editor-hero__date" @tap="openDatePicker">
          <text class="editor-hero__date-text">{{ displayDate }}</text>
          <text class="editor-hero__date-arrow">›</text>
        </view>
        <u-button
          size="small"
          shape="circle"
          type="primary"
          color="rgba(255,255,255,0.25)"
          :loading="submitting"
          @click="submitDiary"
        >
          {{ submitting ? '保存中...' : '保存' }}
        </u-button>
      </view>

      <!-- 属性选择 -->
      <view class="editor-hero__attrs">
        <view class="editor-hero__attr" @tap="openWeatherDialog">
          <text class="editor-hero__attr-icon">{{ weatherDisplay.icon }}</text>
          <text class="editor-hero__attr-text">{{ weatherDisplay.label }}</text>
        </view>
        <view class="editor-hero__attr" @tap="openMoodDialog">
          <text class="editor-hero__attr-icon">{{ moodDisplay.icon }}</text>
          <text class="editor-hero__attr-text">{{ moodDisplay.label }}</text>
        </view>
        <view class="editor-hero__attr" @tap="openTagDialog">
          <text class="editor-hero__attr-icon">🏷️</text>
          <text class="editor-hero__attr-text">{{ selectedTagSummary }}</text>
        </view>
      </view>
    </view>

    <!-- 标题 + 正文 -->
    <view class="editor-card">
      <input
        v-model="form.title"
        class="editor-card__title-input"
        placeholder="给今天的记录起个标题"
        :maxlength="128"
      />
      <view class="editor-card__divider" />
      <textarea
        v-model="form.content"
        class="editor-card__content-input"
        placeholder="时光易逝，点击记录..."
        :maxlength="3000"
        :auto-height="true"
        :max-height="600"
      />
      <view class="editor-card__footer">
        <view class="editor-card__templates">
          <view class="editor-card__template" @tap="fillDraftTemplate">📝 草稿</view>
          <view class="editor-card__template" @tap="fillWeekendTemplate">💬 佳句</view>
          <view class="editor-card__template" @tap="fillMemoryTemplate">📋 模板</view>
          <view class="editor-card__template" @tap="clearForm">🗑️ 清空</view>
        </view>
        <text class="editor-card__count">{{ form.content.length }}/3000</text>
      </view>
    </view>

    <!-- 照片 -->
    <view class="editor-card">
      <view class="editor-card__header">
        <text class="editor-card__header-title">📷 照片</text>
        <text class="editor-card__header-hint">支持从相册选择或拍照</text>
      </view>
      <PhotoPicker ref="photoPickerRef" v-model="photos" @retry="retryUpload" />
    </view>

    <!-- 位置 -->
    <view class="editor-card">
      <view class="editor-card__header">
        <text class="editor-card__header-title">📍 位置</text>
        <view class="editor-card__header-action" @tap="openLocationSection">
          <text class="editor-card__header-action-text">添加位置</text>
        </view>
      </view>
      <LocationPicker ref="locationPickerRef" v-model="form.location" />
    </view>

    <!-- 设置 -->
    <view class="editor-card">
      <view class="editor-card__header">
        <text class="editor-card__header-title">⚙️ 设置</text>
      </view>
      <view class="editor-settings">
        <picker mode="date" :value="form.recordDate" @change="onDateChange">
          <view class="editor-setting-row">
            <text class="editor-setting-row__label">记录日期</text>
            <view class="editor-setting-row__right">
              <text class="editor-setting-row__value">{{ form.recordDate }}</text>
              <text class="editor-setting-row__arrow">›</text>
            </view>
          </view>
        </picker>
        <picker :range="visibilityOptions" range-key="label" @change="onVisibilityChange">
          <view class="editor-setting-row">
            <text class="editor-setting-row__label">可见范围</text>
            <view class="editor-setting-row__right">
              <view class="editor-setting-row__badge" :class="'editor-setting-row__badge--' + form.visibility?.toLowerCase()">
                <text class="editor-setting-row__badge-text">{{ visibilityLabel }}</text>
              </view>
              <text class="editor-setting-row__arrow">›</text>
            </view>
          </view>
        </picker>
      </view>
    </view>

    <!-- 弹窗 -->
    <SelectorDialog
      v-model="showWeatherDialog"
      title="选择天气"
      :items="weatherOptions"
      mode="grid"
      @select="selectWeather"
    />
    <SelectorDialog
      v-model="showMoodDialog"
      title="选择心情"
      :items="moodOptions"
      mode="grid"
      @select="selectMood"
    />
    <SelectorDialog
      v-model="showTagDialog"
      title="选择标签"
      :items="tagOptions"
      mode="chips"
      action-text="管理"
      :action-primary="true"
      :selected-values="selectedTagIds"
      empty-text="还没有可选标签，先去标签管理页创建。"
      @select="toggleTag"
      @action="goTagManager"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PhotoPicker, { type SelectedPhoto } from '@/components/business/photo-picker/index.vue'
import LocationPicker from '@/components/business/location-picker/index.vue'
import SelectorDialog from '@/components/business/selector-dialog/index.vue'
import { createDiary, fetchDiaryDetail, updateDiary } from '@/api/diary'
import { fetchUserTags } from '@/api/tag'
import { VISIBILITY_OPTIONS } from '@/config/app'
import { DIARY_MOOD_OPTIONS, DIARY_WEATHER_OPTIONS } from '@/config/diary'
import type { CreateDiaryPayload } from '@/types/diary'
import type { DiaryItem, Id } from '@/types/domain'
import { uploadImageToOss } from '@/utils/upload'

type PhotoPickerExpose = { openActionSheet: () => void }
type LocationPickerExpose = {
  pickCurrentLocation: () => Promise<void> | void
  pickManualLocation: () => Promise<void> | void
}

const visibilityOptions = VISIBILITY_OPTIONS.filter((item) => item.value !== 'ALL')
const weatherOptions = DIARY_WEATHER_OPTIONS
const moodOptions = DIARY_MOOD_OPTIONS

const WEATHER_MAP: Record<string, { icon: string; label: string }> = {
  SUNNY: { icon: '☀️', label: '晴' },
  CLOUDY: { icon: '☁️', label: '多云' },
  RAINY: { icon: '🌧️', label: '雨' },
  SNOWY: { icon: '❄️', label: '雪' },
  WINDY: { icon: '💨', label: '风' },
  FOGGY: { icon: '🌫️', label: '雾' }
}
const MOOD_MAP: Record<string, { icon: string; label: string }> = {
  HAPPY: { icon: '😊', label: '开心' },
  CALM: { icon: '😌', label: '平静' },
  SAD: { icon: '😢', label: '难过' },
  ANGRY: { icon: '😠', label: '生气' },
  ANXIOUS: { icon: '😰', label: '焦虑' },
  EXCITED: { icon: '🤩', label: '兴奋' }
}

const diaryId = ref('')
const isEdit = computed(() => Boolean(diaryId.value))

const form = ref<CreateDiaryPayload>(createEmptyForm())
const photoPickerRef = ref<PhotoPickerExpose | null>(null)
const locationPickerRef = ref<LocationPickerExpose | null>(null)
const photos = ref<SelectedPhoto[]>([])
const submitting = ref(false)
const selectedTagIds = ref<Id[]>([])
const tagOptions = ref<Array<{ label: string; value: Id }>>([])
const showWeatherDialog = ref(false)
const showMoodDialog = ref(false)
const showTagDialog = ref(false)

const displayDate = computed(() => {
  const d = form.value.recordDate
  const month = Number(d.slice(5, 7))
  const day = Number(d.slice(8, 10))
  return `${month}月${day}日`
})

const weatherDisplay = computed(() => WEATHER_MAP[form.value.weather || ''] || { icon: '🌤️', label: '天气' })
const moodDisplay = computed(() => MOOD_MAP[form.value.mood || ''] || { icon: '😊', label: '心情' })

const visibilityLabel = computed(
  () => visibilityOptions.find((item) => item.value === form.value.visibility)?.label || '私有'
)

const selectedTagSummary = computed(() => {
  if (!selectedTagIds.value.length) return '标签'
  const names = tagOptions.value
    .filter((item) => selectedTagIds.value.includes(item.value))
    .map((item) => item.label)
  return names.length > 2 ? `${names.slice(0, 2).join(' ')} +${names.length - 2}` : names.join(' ')
})

function createEmptyForm(): CreateDiaryPayload {
  return {
    title: '',
    content: '',
    recordDate: new Date().toISOString().slice(0, 10),
    weather: '',
    mood: '',
    visibility: 'PRIVATE',
    mediaList: [],
    location: undefined
  }
}

function openWeatherDialog() { showWeatherDialog.value = true }
function openMoodDialog() { showMoodDialog.value = true }
function openTagDialog() { showTagDialog.value = true }

function selectWeather(value: string | number) {
  form.value.weather = String(value)
  showWeatherDialog.value = false
}
function selectMood(value: string | number) {
  form.value.mood = String(value)
  showMoodDialog.value = false
}
function toggleTag(value: string | number) {
  const tagId = String(value)
  const index = selectedTagIds.value.indexOf(tagId)
  if (index >= 0) selectedTagIds.value.splice(index, 1)
  else selectedTagIds.value.push(tagId)
}
function goTagManager() {
  showTagDialog.value = false
  uni.navigateTo({ url: '/pages/profile/tags/index' })
}
function openDatePicker() { uni.$feedback.info('日期可在下方设置里修改') }
function openPhotoSection() { photoPickerRef.value?.openActionSheet() }
function openLocationSection() { locationPickerRef.value?.pickManualLocation?.() }

function fillDraftTemplate() {
  if (!form.value.title) form.value.title = '今天的小记'
  if (!form.value.content) form.value.content = '今天发生了几件想记住的小事。'
}
function fillWeekendTemplate() {
  form.value.content = form.value.content || '今天适合慢一点，把注意力留给自己。'
}
function fillMemoryTemplate() {
  form.value.content = '今天最想记住的是：\n1. \n2. \n3. '
}
function clearForm() {
  form.value = createEmptyForm()
  photos.value = []
  selectedTagIds.value = []
}
function onDateChange(event: { detail: { value: string } }) { form.value.recordDate = event.detail.value }
function onVisibilityChange(event: { detail: { value: string } }) {
  form.value.visibility = visibilityOptions[Number(event.detail.value)].value as CreateDiaryPayload['visibility']
}

async function uploadOne(index: number) {
  const current = photos.value[index]
  if (!current) return
  current.status = 'uploading'
  try {
    const ossPath = current.ossPath || (await uploadImageToOss({ filePath: current.localPath, dir: 'diary/' }))
    current.ossPath = ossPath
    current.status = 'done'
  } catch {
    current.status = 'failed'
    throw new Error('图片上传失败')
  }
}
async function retryUpload(index: number) {
  try { await uploadOne(index); uni.$feedback.success('重试成功') }
  catch (error) { uni.$feedback.error(error, undefined, '重试失败') }
}

async function submitDiary() {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    uni.$feedback.error('请先填写标题和正文')
    return
  }
  submitting.value = true
  try {
    for (let i = 0; i < photos.value.length; i++) {
      if (photos.value[i].status !== 'done') await uploadOne(i)
    }
    const mediaList = photos.value.filter((item) => item.ossPath).map((item, i) => ({
      mediaType: 'IMAGE' as const, filePath: item.ossPath as string, sortOrder: i + 1
    }))
    const payload: CreateDiaryPayload = { ...form.value, mediaList, tagIds: selectedTagIds.value }
    if (isEdit.value) await updateDiary(diaryId.value, payload)
    else await createDiary(payload)
    uni.$feedback.success(isEdit.value ? '修改已保存' : '日记已保存')
    setTimeout(() => uni.navigateBack({ delta: 1 }), 500)
  } catch (error) {
    uni.$feedback.error(error, undefined, '保存失败')
  } finally {
    submitting.value = false
  }
}

async function initTags() {
  try {
    const tags = await fetchUserTags('DIARY')
    tagOptions.value = tags.map((item) => ({ label: item.name, value: item.id }))
  } catch { tagOptions.value = [] }
}
async function initDiary() {
  if (!diaryId.value) return
  const detail = await fetchDiaryDetail(diaryId.value)
  applyDiaryDetail(detail)
}
function applyDiaryDetail(detail: DiaryItem & { tagIds?: Id[] }) {
  form.value = {
    title: detail.title, content: detail.content, recordDate: detail.recordDate,
    weather: detail.weather || '', mood: detail.mood || '', visibility: detail.visibility,
    mediaList: [],
    location: detail.latitude != null || detail.longitude != null || detail.locationName || detail.address
      ? { locationName: detail.locationName, address: detail.address, province: detail.province,
          city: detail.city, district: detail.district, latitude: detail.latitude,
          longitude: detail.longitude, sourceType: detail.locationSourceType } : undefined
  }
  selectedTagIds.value = detail.tags?.map((item) => item.id) || detail.tagIds || []
  photos.value = (detail.mediaPaths || []).map((path) => ({ localPath: path, ossPath: path, status: 'done' }))
}

onLoad((options) => {
  diaryId.value = options?.id || ''
  Promise.allSettled([initTags(), initDiary()]).catch(() => undefined)
})
</script>

<style scoped lang="scss">
.diary-editor-page {
  padding-bottom: var(--space-10);
}

/* ========== Hero ========== */
.editor-hero {
  background: var(--color-diary-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-6) var(--space-6) var(--space-7);
  color: #fff;
}

.editor-hero__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
}

.editor-hero__date {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.editor-hero__date-text {
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.editor-hero__date-arrow {
  font-size: 32rpx;
  opacity: 0.7;
}

.editor-hero__attrs {
  display: flex;
  gap: var(--space-2);
}

.editor-hero__attr {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  padding: var(--space-3) 0;
  border-radius: var(--radius-medium);
  background: rgba(255, 255, 255, 0.18);
  transition: all var(--motion-fast) var(--ease-standard);
}

.editor-hero__attr:active {
  background: rgba(255, 255, 255, 0.3);
}

.editor-hero__attr-icon {
  font-size: 32rpx;
  line-height: 1;
}

.editor-hero__attr-text {
  font-size: var(--font-tiny);
  opacity: 0.9;
}

/* ========== 编辑卡片 ========== */
.editor-card {
  margin: var(--space-4) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.editor-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.editor-card__header-title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.editor-card__header-hint {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.editor-card__header-action {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-diary-soft);
}

.editor-card__header-action-text {
  color: var(--color-diary);
  font-size: var(--font-tiny);
  font-weight: var(--weight-semibold);
}

.editor-card__title-input {
  width: 100%;
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
  color: var(--color-text-primary);
  min-height: 80rpx;
}

.editor-card__divider {
  height: 1rpx;
  background: var(--color-divider);
  margin: var(--space-3) 0;
}

.editor-card__content-input {
  width: 100%;
  min-height: 240rpx;
  font-size: var(--font-body);
  color: var(--color-text-primary);
  line-height: var(--leading-loose);
}

.editor-card__footer {
  margin-top: var(--space-4);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.editor-card__templates {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.editor-card__template {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
  font-size: var(--font-tiny);
  transition: all var(--motion-fast) var(--ease-standard);
}

.editor-card__template:active {
  transform: scale(0.95);
}

.editor-card__count {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  flex-shrink: 0;
}

/* ========== 设置 ========== */
.editor-settings {
  display: flex;
  flex-direction: column;
}

.editor-setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) 0;
  border-bottom: 1rpx solid var(--color-divider);

  &:last-child {
    border-bottom: none;
  }
}

.editor-setting-row__label {
  color: var(--color-text-secondary);
  font-size: var(--font-body);
}

.editor-setting-row__right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.editor-setting-row__value {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-medium);
}

.editor-setting-row__arrow {
  color: var(--color-text-muted);
  font-size: 28rpx;
}

.editor-setting-row__badge {
  padding: 4rpx 16rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
}

.editor-setting-row__badge--public {
  background: rgba(52, 199, 89, 0.12);
}

.editor-setting-row__badge--shared {
  background: rgba(90, 200, 250, 0.12);
}

.editor-setting-row__badge-text {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}

.editor-setting-row__badge--public .editor-setting-row__badge-text {
  color: var(--color-success);
}
</style>
