<template>
  <AppPage>
    <AppHero
      eyebrow="打卡"
      title="把重复的事情做成节律"
      description="任务、次数和最近完成时间放在同一屏，创建页也已经接进来了。"
      badge="Checkin"
    />

    <SectionBlock title="我的任务" subtitle="今天先完成最重要的一件">
      <view class="mb-[18rpx]">
        <BaseButton @tap="goCreate">创建打卡任务</BaseButton>
      </view>

      <view v-if="tasks.length" class="flex flex-col gap-[18rpx]">
        <BaseCard v-for="task in tasks" :key="task.id">
          <view class="flex items-center justify-between">
            <view>
              <view class="text-[28rpx] font-semibold text-ink">{{ task.name }}</view>
              <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">{{ task.description }}</view>
            </view>
            <view class="rounded-full bg-[#edf4ef] px-[20rpx] py-[10rpx] text-[22rpx] text-[#4b6b57]">
              {{ task.totalCount }} 次
            </view>
          </view>
          <view class="mt-[18rpx] text-[22rpx] text-[#8c7f71]">最近完成：{{ task.latestCheckedAt || '今天待打卡' }}</view>
        </BaseCard>
      </view>
      <EmptyState
        v-else
        icon="✅"
        title="还没有打卡任务"
        description="先从最容易开始的一项习惯建立节律。"
      />
    </SectionBlock>
  </AppPage>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/AppHero.vue'
import SectionBlock from '@/components/business/SectionBlock.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import EmptyState from '@/components/business/EmptyState.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import { fetchCheckinTasks } from '@/api/checkin'
import type { CheckinTask } from '@/types/domain'

const tasks = ref<CheckinTask[]>([])

function goCreate() {
  uni.navigateTo({ url: '/pages/checkin/editor' })
}

/**
 * 打卡首页展示的是任务列表，不是具体记录明细。
 */
async function init() {
  try {
    tasks.value = await fetchCheckinTasks()
  } catch {
    tasks.value = [
      { id: 1, name: '20 个俯卧撑', description: '每天看到就做，别拖到晚上。', totalCount: 12, latestCheckedAt: '2026-03-20 07:35' },
      { id: 2, name: '阅读 30 分钟', description: '只要开始，时长自然会出现。', totalCount: 8, latestCheckedAt: '2026-03-19 22:10' }
    ]
  }
}

onShow(() => {
  init()
})
</script>
