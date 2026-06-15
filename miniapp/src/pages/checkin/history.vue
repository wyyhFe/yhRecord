<template>
  <view class="page-shell-safe">
    <!-- 日期选择 -->
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">{{ displayDate }} 打卡记录</view>
          <view class="section-copy__desc">查看当天完成了哪些任务。</view>
        </view>
      </view>

      <view class="history-toolbar">
        <picker mode="date" :value="date" @change="onDateChange">
          <view class="history-toolbar__date">{{ date }}</view>
        </picker>
        <view class="history-toolbar__nav">
          <view class="history-toolbar__arrow" @tap="prevDay">‹</view>
          <u-button shape="circle" plain size="mini" :hair-line="false" @tap="goToday">今天</u-button>
          <view class="history-toolbar__arrow" @tap="nextDay">›</view>
        </view>
      </view>
    </view>

    <!-- 打卡列表 -->
    <view v-if="items.length" class="list-stack">
      <view v-for="item in items" :key="item.id" class="section-shell list-card">
        <view class="list-card__head">
          <view>
            <view class="list-card__title">{{ item.name }}</view>
            <view class="list-card__meta">{{ item.description || '未填写任务描述' }}</view>
          </view>
          <view class="history-tag">已完成</view>
        </view>
        <view v-if="item.mood || item.tagNames?.length" class="checkin-meta-row">
          <text v-if="item.mood" class="checkin-meta-mood">{{ item.mood }}</text>
          <text v-for="tag in item.tagNames" :key="tag" class="checkin-meta-tag">#{{ tag }}</text>
        </view>
        <view v-if="item.remark" class="checkin-remark">{{ item.remark }}</view>
        <view v-if="item.mediaPaths?.length" class="checkin-media-grid">
          <image
            v-for="(path, idx) in item.mediaPaths"
            :key="idx"
            :src="path"
            mode="aspectFill"
            class="checkin-media-grid__image"
            @tap="previewImage(item.mediaPaths!, idx)"
          />
        </view>
      </view>
    </view>
    <EmptyStateCard
      v-else
      title="这一天还没有打卡记录"
      description="切换日期看看过去哪一天完成过任务。"
      mode="history"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import { fetchCheckinDayDetail } from '@/api/checkin'
import type { CheckinTask } from '@/types/domain'

const items = ref<CheckinTask[]>([])
const date = ref(new Date().toISOString().slice(0, 10))

const displayDate = computed(() => {
  const d = new Date(date.value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

async function loadHistory() {
  try {
    items.value = await fetchCheckinDayDetail(date.value)
  } catch {
    items.value = []
  }
}

function onDateChange(e: { detail: { value: string } }) {
  date.value = e.detail.value
  loadHistory()
}

function prevDay() {
  const d = new Date(date.value)
  d.setDate(d.getDate() - 1)
  date.value = d.toISOString().slice(0, 10)
  loadHistory()
}

function nextDay() {
  const d = new Date(date.value)
  d.setDate(d.getDate() + 1)
  date.value = d.toISOString().slice(0, 10)
  loadHistory()
}

function goToday() {
  date.value = new Date().toISOString().slice(0, 10)
  loadHistory()
}

function previewImage(urls: string[], current: number) {
  uni.previewImage({ urls, current })
}

onLoad((query) => {
  if (query?.date) {
    date.value = query.date
  }
  loadHistory()
})
</script>

<style scoped lang="scss">
.history-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.history-toolbar__date {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 68rpx;
  padding: 0 var(--space-5);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

.history-toolbar__nav {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.history-toolbar__arrow {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
  font-size: 32rpx;
}

.history-tag {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
}

.checkin-meta-row {
  margin-top: var(--space-2);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8rpx;
}

.checkin-meta-mood {
  font-size: 28rpx;
}

.checkin-meta-tag {
  padding: 4rpx 14rpx;
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  color: var(--color-checkin);
  font-size: var(--font-tiny);
}

.checkin-remark {
  margin-top: var(--space-3);
  padding: var(--space-3);
  background: var(--color-surface-soft);
  border-radius: var(--radius-medium);
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  line-height: 1.6;
}

.checkin-media-grid {
  margin-top: var(--space-3);
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12rpx;
}

.checkin-media-grid__image {
  width: 100%;
  height: 180rpx;
  border-radius: var(--radius-medium);
}
</style>
