<template>
  <view class="page-shell-safe diary-editor-page">
    <view class="section-shell diary-editor-topbar">
      <view class="diary-editor-topbar__date" @tap="openDatePicker">{{ displayDate }}</view>

      <view class="diary-editor-topbar__tools">
        <view class="diary-editor-topbar__tool" @tap="openLocationSection">位置</view>
        <view class="diary-editor-topbar__tool" @tap="openPhotoSection">图片</view>
      </view>

      <u-button
        size="medium"
        type="primary"
        shape="circle"
        :hair-line="false"
        color="linear-gradient(135deg, #c47c52 0%, #d7a648 100%)"
        :loading="submitting"
        @click="submitDiary"
      >
        {{ submitting ? '保存中...' : '保存' }}
      </u-button>
    </view>

    <view class="page-section section-shell">
      <view class="diary-editor-tabs">
        <view class="diary-editor-tabs__item diary-editor-tabs__item--active">简洁</view>
        <view class="diary-editor-tabs__item">图文</view>
      </view>

      <view class="diary-editor-filters">
        <view class="diary-editor-filter" @tap="openWeatherDialog">
          <text class="diary-editor-filter__text">{{ form.weather || '天气' }}</text>
        </view>
        <view class="diary-editor-filter" @tap="openMoodDialog">
          <text class="diary-editor-filter__text">{{ form.mood || '心情' }}</text>
        </view>
        <view class="diary-editor-filter" @tap="openTagDialog">
          <text class="diary-editor-filter__text">{{ selectedTagSummary }}</text>
        </view>
      </view>

      <view class="block-stack">
        <u-input
          v-model="form.title"
          placeholder="给今天的记录起个标题"
          :border="false"
          :custom-style="titleStyle"
        />
      </view>

      <view class="block-stack">
        <u-textarea
          v-model="form.content"
          placeholder="时光易逝，点击记录。"
          :border="false"
          :custom-style="contentStyle"
          height="320"
          :maxlength="3000"
        />
      </view>

      <view class="diary-editor-footer">
        <view class="diary-editor-footer__actions">
          <view class="diary-editor-footer__chip" @tap="fillDraftTemplate">草稿</view>
          <view class="diary-editor-footer__chip" @tap="clearForm">清空</view>
          <view class="diary-editor-footer__chip" @tap="fillWeekendTemplate">佳句</view>
          <view class="diary-editor-footer__chip" @tap="fillMemoryTemplate">模板</view>
        </view>
        <view class="diary-editor-footer__count">{{ form.content.length }} / 3000</view>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-copy">
        <view class="section-copy__title">添加素材</view>
        <view class="section-copy__desc">支持上传图片和调用微信位置能力。</view>
      </view>

      <view class="diary-editor-materials">
        <view class="diary-editor-material" @tap="openPhotoSection">上传图片</view>
        <view class="diary-editor-material" @tap="openLocationSection">微信位置</view>
      </view>

      <view class="block-stack">
        <LocationPicker ref="locationPickerRef" v-model="form.location" />
      </view>

      <view class="block-stack">
        <PhotoPicker ref="photoPickerRef" v-model="photos" @retry="retryUpload" />
      </view>

      <view class="block-stack">
        <u-cell-group :border="false">
          <picker mode="date" :value="form.recordDate" @change="onDateChange">
            <u-cell-item title="记录日期" :value="form.recordDate" :border-bottom="true" />
          </picker>
          <picker :range="visibilityOptions" range-key="label" @change="onVisibilityChange">
            <u-cell-item title="可见范围" :value="visibilityLabel" :border-bottom="false" />
          </picker>
        </u-cell-group>
      </view>
    </view>

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
import PhotoPicker, { type SelectedPhoto } from '@/components/business/photo-picker/index.vue'
import LocationPicker from '@/components/business/location-picker/index.vue'
import SelectorDialog from '@/components/business/selector-dialog/index.vue'
import { createDiary, fetchDiaryDetail, updateDiary } from '@/api/diary'
import { fetchUserTags } from '@/api/tag'
import { VISIBILITY_OPTIONS } from '@/config/app'
import { DIARY_MOOD_OPTIONS, DIARY_WEATHER_OPTIONS } from '@/config/diary'
import type { CreateDiaryPayload } from '@/types/diary'
import type { Id } from '@/types/domain'
import { uploadImageToOss } from '@/utils/upload'

type PhotoPickerExpose = {
  openActionSheet: () => void
}

type LocationPickerExpose = {
  pickCurrentLocation: () => Promise<void> | void
  pickManualLocation: () => Promise<void> | void
}

const visibilityOptions = VISIBILITY_OPTIONS.filter((item) => item.value !== 'ALL')
const weatherOptions = DIARY_WEATHER_OPTIONS
const moodOptions = DIARY_MOOD_OPTIONS

const diaryId = ref<string | undefined>()
const isEdit = computed(() => Boolean(diaryId.value))

const form = ref<CreateDiaryPayload>({
  title: '',
  content: '',
  recordDate: new Date().toISOString().slice(0, 10),
  weather: '',
  mood: '',
  visibility: 'PRIVATE',
  mediaList: [],
  location: undefined
})

const photoPickerRef = ref<PhotoPickerExpose | null>(null)
const locationPickerRef = ref<LocationPickerExpose | null>(null)
const photos = ref<SelectedPhoto[]>([])
const submitting = ref(false)
const selectedTagIds = ref<Id[]>([])
const tagOptions = ref<Array<{ label: string; value: Id }>>([])
const showWeatherDialog = ref(false)
const showMoodDialog = ref(false)
const showTagDialog = ref(false)

const titleStyle = {
  background: '#ffffff',
  borderRadius: '0',
  padding: '0',
  fontSize: '40rpx',
  fontWeight: '600',
  minHeight: '72rpx',
  color: '#2b2118'
}

const contentStyle = {
  background: '#ffffff',
  borderRadius: '0',
  padding: '0',
  fontSize: '30rpx',
  width: '100%',
  boxSizing: 'border-box' as const,
  lineHeight: '1.8',
  color: '#2b2118'
}

const displayDate = computed(() => form.value.recordDate.slice(5))
const visibilityLabel = computed(
  () => visibilityOptions.find((item) => item.value === form.value.visibility)?.label || '私有'
)
const selectedTagSummary = computed(() => {
  if (!selectedTagIds.value.length) return '标签'
  const names = tagOptions.value
    .filter((item) => selectedTagIds.value.includes(item.value))
    .map((item) => item.label)
  return names.length > 2 ? `${names.slice(0, 2).join(' / ')} +${names.length - 2}` : names.join(' / ')
})

function openWeatherDialog() {
  console.log('[diary-editor] open weather dialog')
  showWeatherDialog.value = true
}

function openMoodDialog() {
  console.log('[diary-editor] open mood dialog')
  showMoodDialog.value = true
}

function openTagDialog() {
  console.log('[diary-editor] open tag dialog', selectedTagIds.value)
  showTagDialog.value = true
}

function selectWeather(value: string | number) {
  console.log('[diary-editor] select weather', value)
  form.value.weather = String(value)
  showWeatherDialog.value = false
}

function selectMood(value: string | number) {
  console.log('[diary-editor] select mood', value)
  form.value.mood = String(value)
  showMoodDialog.value = false
}

function toggleTag(value: string | number) {
  const tagId = String(value)
  console.log('[diary-editor] toggle tag', tagId)
  const index = selectedTagIds.value.indexOf(tagId)
  if (index >= 0) {
    selectedTagIds.value.splice(index, 1)
  } else {
    selectedTagIds.value.push(tagId)
  }
}

function goTagManager() {
  showTagDialog.value = false
  uni.navigateTo({ url: '/pages/profile/tags/index' })
}

function openDatePicker() {
  uni.$feedback.info('日期可在下方记录设置里修改')
}

function openPhotoSection() {
  console.log('[diary-editor] open photo action')
  photoPickerRef.value?.openActionSheet()
}

function openLocationSection() {
  console.log('[diary-editor] open location action')
  locationPickerRef.value?.pickManualLocation?.()
}

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
  form.value.title = ''
  form.value.content = ''
}

function onDateChange(event: { detail: { value: string } }) {
  form.value.recordDate = event.detail.value
}

function onVisibilityChange(event: { detail: { value: string } }) {
  form.value.visibility =
    visibilityOptions[Number(event.detail.value)].value as CreateDiaryPayload['visibility']
}

async function uploadOne(index: number) {
  const current = photos.value[index]
  if (!current) return

  current.status = 'uploading'
  try {
    const ossPath =
      current.ossPath || (await uploadImageToOss({ filePath: current.localPath, dir: 'diary/' }))
    current.ossPath = ossPath
    current.status = 'done'
  } catch {
    current.status = 'failed'
    throw new Error('图片上传失败')
  }
}

async function retryUpload(index: number) {
  try {
    await uploadOne(index)
    uni.$feedback.success('重试成功')
  } catch (error) {
    uni.$feedback.error(error, undefined, '重试失败')
  }
}

async function submitDiary() {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    uni.$feedback.error('请先填写标题和正文')
    return
  }

  submitting.value = true
  try {
    for (let index = 0; index < photos.value.length; index += 1) {
      if (photos.value[index].status !== 'done') {
        await uploadOne(index)
      }
    }

    const mediaList = photos.value
      .filter((item) => item.ossPath)
      .map((item, index) => ({
        mediaType: 'IMAGE' as const,
        filePath: item.ossPath as string,
        sortOrder: index + 1
      }))

    const payload: CreateDiaryPayload = {
      ...form.value,
      mediaList,
      tagIds: selectedTagIds.value
    }

    if (isEdit.value && diaryId.value) {
      await updateDiary(diaryId.value, payload)
    } else {
      await createDiary(payload)
    }

    uni.$feedback.success(isEdit.value ? '修改已保存' : '日记已保存')
    setTimeout(() => {
      uni.navigateBack({ delta: 1 })
    }, 500)
  } catch (error) {
    uni.$feedback.error(error, undefined, '保存失败')
  } finally {
    submitting.value = false
  }
}

async function initTags() {
  try {
    const tags = await fetchUserTags('DIARY')
    tagOptions.value = tags.map((item) => ({
      label: item.name,
      value: item.id
    }))
  } catch {
    tagOptions.value = []
  }
}

async function initDiary() {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1] as { options?: Record<string, string> } | undefined
  const options = current?.options
  if (!options?.id) return

  diaryId.value = options.id
  const detail = await fetchDiaryDetail(options.id)
  form.value = {
    title: detail.title,
    content: detail.content,
    recordDate: detail.recordDate,
    weather: detail.weather,
    mood: detail.mood,
    visibility: detail.visibility,
    mediaList: [],
    location:
      detail.latitude || detail.longitude
        ? {
            locationName: detail.locationName,
            address: detail.address,
            province: detail.province,
            city: detail.city,
            district: detail.district,
            latitude: detail.latitude,
            longitude: detail.longitude,
            sourceType: detail.locationSourceType
          }
        : undefined
  }
  selectedTagIds.value = detail.tags?.map((item) => item.id) || []
  photos.value = detail.mediaPaths.map((path) => ({
    localPath: path,
    ossPath: path,
    status: 'done'
  }))
}

Promise.allSettled([initTags(), initDiary()])
</script>

<style scoped lang="scss">
.diary-editor-page {
  padding-bottom: 40rpx;
}

.diary-editor-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.diary-editor-topbar__date {
  min-width: 136rpx;
  color: #2b2118;
  font-size: 32rpx;
  font-weight: 700;
}

.diary-editor-topbar__tools {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 14rpx;
}

.diary-editor-topbar__tool {
  padding: 14rpx 24rpx;
  border-radius: 999rpx;
  border: 1rpx solid #ead9c7;
  background: #fff8ef;
  color: #a56d4b;
  font-size: 22rpx;
}

.diary-editor-tabs {
  display: flex;
  align-items: center;
  gap: 28rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid rgba(196, 124, 82, 0.12);
}

.diary-editor-tabs__item {
  color: #9b866d;
  font-size: 26rpx;
}

.diary-editor-tabs__item--active {
  color: #a56d4b;
  font-weight: 600;
}

.diary-editor-filters {
  margin-top: 22rpx;
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.diary-editor-filter {
  padding: 10rpx 22rpx;
  border-radius: 999rpx;
  border: 1rpx solid #ead9c7;
  background: #fff8ef;
}

.diary-editor-filter__text {
  color: #a56d4b;
  font-size: 24rpx;
}

.diary-editor-footer {
  margin-top: 22rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.diary-editor-footer__actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14rpx;
}

.diary-editor-footer__chip {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  border: 1rpx solid #ead9c7;
  color: #a56d4b;
  font-size: 22rpx;
  background: #fffdf8;
}

.diary-editor-footer__count {
  flex-shrink: 0;
  color: #8a735f;
  font-size: 22rpx;
}

.diary-editor-materials {
  margin-top: 22rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.diary-editor-material {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18rpx 12rpx;
  border-radius: 999rpx;
  border: 1rpx solid #ead9c7;
  background: #fff8ef;
  color: #a56d4b;
  font-size: 24rpx;
}
</style>
