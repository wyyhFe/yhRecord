<template>
  <AppPage>
    <AppHero
      eyebrow="日记详情"
      :title="detail?.title || '正在加载...'"
      :description="detail?.ageLabel || '把当天的内容和照片完整保留下来。'"
      badge="Detail"
    />

    <SectionBlock title="正文">
      <BaseCard>
        <view class="text-[28rpx] leading-[1.8] text-ink">{{ detail?.content || '暂无内容' }}</view>
      </BaseCard>
    </SectionBlock>

    <SectionBlock title="照片" subtitle="保留当天上传的图片内容">
      <view v-if="detail?.mediaPaths?.length" class="grid grid-cols-3 gap-[16rpx]">
        <image
          v-for="path in detail.mediaPaths"
          :key="path"
          :src="resolveImage(path)"
          mode="aspectFill"
          class="h-[180rpx] w-full rounded-[24rpx]"
        />
      </view>
      <EmptyState v-else icon="🖼️" title="这篇日记还没有照片" description="后续可在编辑页里继续补充图片。" />
    </SectionBlock>

    <SectionBlock title="补充信息">
      <SettingList :items="infoItems" />
    </SectionBlock>

    <view class="mt-[32rpx] grid grid-cols-2 gap-[20rpx]">
      <BaseButton @tap="goEdit">编辑这篇日记</BaseButton>
      <BaseButton @tap="removeDiary">删除到回收站</BaseButton>
    </view>
  </AppPage>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/app-hero'
import SectionBlock from '@/components/business/section-block'
import BaseCard from '@/components/base/base-card'
import BaseButton from '@/components/base/base-button'
import EmptyState from '@/components/business/empty-state'
import SettingList from '@/components/business/setting-list'
import { deleteDiary, fetchDiaryDetail } from '@/api/diary'
import { OSS_BASE_URL } from '@/config/app'
import type { DiaryItem } from '@/types/domain'

const detail = ref<DiaryItem | null>(null)
const diaryId = ref<string>('')

const infoItems = computed(() => [
  { title: '日期', description: '记录发生在哪一天', value: detail.value?.recordDate || '--' },
  { title: '天气', description: '当天的天气情况', value: detail.value?.weather || '--' },
  { title: '心情', description: '写下那一刻的状态', value: detail.value?.mood || '--' },
  { title: '位置', description: '如果有位置，会展示在这里', value: detail.value?.locationName || '--' }
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

  await deleteDiary(Number(diaryId.value))
  uni.showToast({ title: '已移入回收站', icon: 'success' })
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
  detail.value = await fetchDiaryDetail(Number(diaryId.value))
}

init().catch(() => undefined)
</script>
