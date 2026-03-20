<template>
  <AppPage>
    <AppHero
      eyebrow="写日记"
      :title="isEdit ? '修改这篇日记' : '记录这一天的样子'"
      description="正文、照片、天气、心情、标签、提醒和位置一起保存，尽量把记录入口做得顺手。"
      :badge="isEdit ? 'Edit' : 'Create'"
    />

    <SectionBlock title="基础内容" subtitle="标题和正文是最核心的信息">
      <view class="glass-panel px-[24rpx] py-[24rpx]">
        <input
          v-model="form.title"
          class="h-[84rpx] rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] text-[28rpx]"
          placeholder="标题，例如：被晚风修复的一天"
        />
        <textarea
          v-model="form.content"
          class="mt-[18rpx] h-[260rpx] w-full rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] py-[18rpx] text-[26rpx]"
          placeholder="写下今天的感受、细节和想记住的瞬间"
        />
      </view>
    </SectionBlock>

    <SectionBlock title="补充信息" subtitle="先写内容，再慢慢补齐元信息">
      <view class="grid grid-cols-2 gap-[18rpx]">
        <BaseCard>
          <view class="text-[22rpx] text-[#7f7366]">日期</view>
          <picker mode="date" :value="form.recordDate" @change="onDateChange">
            <view class="mt-[12rpx] text-[28rpx] font-semibold text-ink">{{ form.recordDate }}</view>
          </picker>
        </BaseCard>
        <BaseCard>
          <view class="text-[22rpx] text-[#7f7366]">可见范围</view>
          <picker :range="visibilityOptions" range-key="label" @change="onVisibilityChange">
            <view class="mt-[12rpx] text-[28rpx] font-semibold text-ink">{{ visibilityLabel }}</view>
          </picker>
        </BaseCard>
      </view>
    </SectionBlock>

    <SectionBlock title="天气和心情" subtitle="常用项用选择，保留手填空间">
      <view class="glass-panel px-[24rpx] py-[24rpx]">
        <view class="text-[24rpx] text-[#7f7366]">天气</view>
        <ChoiceChips v-model="form.weather" :items="weatherOptions" />
        <view class="mt-[16rpx] text-[24rpx] text-[#7f7366]">心情</view>
        <ChoiceChips v-model="form.mood" :items="moodOptions" />
      </view>
    </SectionBlock>

    <SectionBlock title="标签" subtitle="标签来自后台配置和个人自定义">
      <view v-if="tagOptions.length" class="glass-panel px-[24rpx] py-[24rpx]">
        <ChoiceChips v-model="selectedTagIds" :items="tagOptions" multiple />
      </view>
      <EmptyState
        v-else
        icon="🏷️"
        title="暂时没有可选标签"
        description="等标签接口返回后，这里会展示后台模板和你的自定义标签。"
      />
    </SectionBlock>

    <SectionBlock title="提醒" subtitle="当天未记录时，可按设定时间提醒">
      <ReminderSwitch
        :enabled="reminderEnabled"
        :time-value="reminderTime"
        @update:enabled="reminderEnabled = $event"
        @update:timeValue="reminderTime = $event"
      />
    </SectionBlock>

    <SectionBlock title="位置" subtitle="支持当前位置和手动选点">
      <LocationPicker v-model="form.location" />
    </SectionBlock>

    <SectionBlock title="照片" subtitle="支持本地相册和现拍照片，图片会先直传 OSS">
      <PhotoPicker v-model="photos" @retry="retryUpload" />
    </SectionBlock>

    <view class="mt-[32rpx]">
      <BaseButton :disabled="submitting" @tap="submitDiary">
        {{ submitting ? '保存中...' : isEdit ? '保存修改' : '发布日记' }}
      </BaseButton>
    </view>
  </AppPage>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/AppHero.vue'
import SectionBlock from '@/components/business/SectionBlock.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import PhotoPicker, { type SelectedPhoto } from '@/components/business/PhotoPicker.vue'
import LocationPicker from '@/components/business/LocationPicker.vue'
import ChoiceChips from '@/components/business/ChoiceChips.vue'
import ReminderSwitch from '@/components/business/ReminderSwitch.vue'
import EmptyState from '@/components/business/EmptyState.vue'
import { createDiary, fetchDiaryDetail, updateDiary } from '@/api/diary'
import { fetchDiaryTags } from '@/api/tag'
import { uploadImageToOss } from '@/utils/upload'
import { VISIBILITY_OPTIONS } from '@/config/app'
import type { CreateDiaryPayload } from '@/types/diary'

const visibilityOptions = VISIBILITY_OPTIONS.filter((item) => item.value !== 'ALL')
const weatherOptions = [
  { label: '晴', value: '晴' },
  { label: '多云', value: '多云' },
  { label: '阴', value: '阴' },
  { label: '小雨', value: '小雨' },
  { label: '大雨', value: '大雨' },
  { label: '雪', value: '雪' }
]
const moodOptions = [
  { label: '放松', value: '放松' },
  { label: '开心', value: '开心' },
  { label: '平静', value: '平静' },
  { label: '满足', value: '满足' },
  { label: '疲惫', value: '疲惫' },
  { label: '焦虑', value: '焦虑' }
]

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
const photos = ref<SelectedPhoto[]>([])
const submitting = ref(false)
const reminderEnabled = ref(false)
const reminderTime = ref<string>()
const selectedTagIds = ref<Array<string | number>>([])
const tagOptions = ref<Array<{ label: string; value: number }>>([])

const visibilityLabel = computed(
  () => visibilityOptions.find((item) => item.value === form.value.visibility)?.label || '私有'
)

function onDateChange(event: { detail: { value: string } }) {
  form.value.recordDate = event.detail.value
}

function onVisibilityChange(event: { detail: { value: string } }) {
  form.value.visibility = visibilityOptions[Number(event.detail.value)].value as CreateDiaryPayload['visibility']
}

/**
 * 上传单张图片，并把上传状态同步回本地列表。
 * 这样失败时可以精准重试，不会影响其他图片。
 */
async function uploadOne(index: number) {
  const current = photos.value[index]
  if (!current) return
  current.status = 'uploading'
  try {
    const ossPath = current.ossPath || await uploadImageToOss({ filePath: current.localPath, dir: 'diary/' })
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
    uni.showToast({ title: '重试成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '重试失败', icon: 'none' })
  }
}

/**
 * 日记提醒时间最终按后端需要的完整日期时间字符串提交。
 */
function buildRemindAt() {
  if (!reminderEnabled.value || !reminderTime.value) return undefined
  return `${form.value.recordDate}T${reminderTime.value}:00`
}

async function submitDiary() {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    uni.showToast({ title: '请先填写标题和正文', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    // 提交前确保所有待上传图片都已经拿到 OSS 路径。
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
      remindAt: buildRemindAt(),
      mediaList,
      tagIds: selectedTagIds.value.map((item) => Number(item))
    }

    if (isEdit.value && diaryId.value) {
      await updateDiary(Number(diaryId.value), payload)
    } else {
      await createDiary(payload)
    }

    uni.showToast({ title: isEdit.value ? '修改已保存' : '日记已保存', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack({ delta: 1 })
    }, 500)
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '保存失败',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

/**
 * 拉取当前用户可用的日记标签，供表单多选。
 */
async function initTags() {
  try {
    const tags = await fetchDiaryTags()
    tagOptions.value = tags.map((item) => ({
      label: item.name,
      value: item.id
    }))
  } catch {
    tagOptions.value = []
  }
}

/**
 * 编辑模式下用详情接口回填表单和图片列表。
 * 这样新增和编辑可以共用同一套页面结构。
 */
async function initDiary() {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1] as { options?: Record<string, string> } | undefined
  const options = current?.options
  if (!options?.id) return

  diaryId.value = options.id
  const detail = await fetchDiaryDetail(Number(options.id))
  form.value = {
    title: detail.title,
    content: detail.content,
    recordDate: detail.recordDate,
    weather: detail.weather,
    mood: detail.mood,
    visibility: detail.visibility,
    mediaList: [],
    remindAt: detail.remindAt,
    location: detail.latitude || detail.longitude
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
  reminderEnabled.value = Boolean(detail.remindAt)
  reminderTime.value = detail.remindAt ? detail.remindAt.slice(11, 16) : undefined
  photos.value = detail.mediaPaths.map((path) => ({
    localPath: path,
    ossPath: path,
    status: 'done'
  }))
}

Promise.allSettled([initTags(), initDiary()])
</script>
