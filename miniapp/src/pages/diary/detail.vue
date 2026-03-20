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

    <view class="mt-[32rpx]">
      <BaseButton @tap="goEdit">编辑这篇日记</BaseButton>
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
import EmptyState from '@/components/business/EmptyState.vue'
import SettingList from '@/components/business/SettingList.vue'
import { fetchDiaryDetail } from '@/api/diary'
import { OSS_BASE_URL } from '@/config/app'
import type { DiaryItem } from '@/types/domain'

const detail = ref<DiaryItem | null>(null)
const diaryId = ref<string>('')

/**
 * 详情页把零散字段整理成统一列表，更适合直接展示。
 */
const infoItems = computed(() => [
  { title: '日期', description: '记录发生在哪一天', value: detail.value?.recordDate || '--' },
  { title: '天气', description: '当天的天气情况', value: detail.value?.weather || '--' },
  { title: '心情', description: '写下那一刻的状态', value: detail.value?.mood || '--' },
  { title: '位置', description: '如果有位置，会展示在这里', value: detail.value?.locationName || '--' }
])

/**
 * OSS 只保存路径，因此详情页预览时需要补成完整访问地址。
 * 访问域名走环境变量，避免本地和生产写死同一个域名。
 */
function resolveImage(path: string) {
  return path.startsWith('http') ? path : `${OSS_BASE_URL}/${path}`
}

function goEdit() {
  uni.navigateTo({ url: `/pages/diary/editor?id=${diaryId.value}` })
}

/**
 * 从路由参数里拿到日记 id，并请求详情数据。
 */
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
