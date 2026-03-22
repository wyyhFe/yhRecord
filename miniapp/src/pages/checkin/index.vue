<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">我的任务</view>
          <view class="section-copy__desc">今天先完成最重要的一项。</view>
        </view>
      </view>

      <view class="block-stack">
        <u-button type="primary" shape="circle" color="linear-gradient(135deg, #c47c52 0%, #d7a648 100%)" @click="goCreate">
          创建打卡任务
        </u-button>
      </view>

      <view v-if="tasks.length" class="list-stack">
        <view v-for="task in tasks" :key="task.id" class="list-card">
          <view class="list-card__head">
            <view>
              <view class="list-card__title">{{ task.name }}</view>
              <view class="list-card__meta">{{ task.description }}</view>
            </view>
            <view class="checkin-count">{{ task.totalCount }} 次</view>
          </view>
          <view class="list-card__meta">最近完成：{{ task.latestCheckedAt || '今天待打卡' }}</view>
        </view>
      </view>
      <EmptyStateCard
        v-else
        class="page-section"
        title="还没有打卡任务"
        description="先从最容易开始的一项习惯建立节律。"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import { fetchCheckinTasks } from '@/api/checkin'
import type { CheckinTask } from '@/types/domain'

const tasks = ref<CheckinTask[]>([])

function goCreate() {
  uni.navigateTo({ url: '/pages/checkin/editor' })
}

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

<style scoped lang="scss">
.checkin-count {
  border-radius: 999rpx;
  background: #edf4ef;
  padding: 10rpx 20rpx;
  color: #4b6b57;
  font-size: 22rpx;
}
</style>
