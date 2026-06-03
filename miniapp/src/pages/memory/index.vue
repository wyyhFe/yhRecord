<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">去年今日</view>
          <view class="section-copy__desc">
            用真实接口回看去年同一天，把日记、打卡和纪念日放到同一页展示。
          </view>
        </view>
        <view class="memory-date-chip">{{ targetDate }}</view>
      </view>

      <view class="metric-grid">
        <view v-for="item in metrics" :key="item.label" class="metric-card">
          <view class="metric-card__label">{{ item.label }}</view>
          <view class="metric-card__value">{{ item.value }}</view>
          <view class="metric-card__hint">{{ item.hint }}</view>
        </view>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">日记</view>
          <view class="section-copy__desc">去年这一天写下过什么。</view>
        </view>
      </view>

      <view v-if="detail.diaries.length" class="list-stack">
        <view
          v-for="item in detail.diaries"
          :key="item.id"
          class="list-card memory-diary-card"
          @tap="goDiaryDetail(item.id)"
        >
          <view class="list-card__head">
            <view>
              <view class="list-card__title">{{ item.title }}</view>
              <view class="list-card__meta">{{ item.recordDate }} {{ resolveDiaryMoodLabel(item.mood, '平静') }}</view>
            </view>
            <view class="list-card__aside">{{ resolveDiaryWeatherLabel(item.weather, '未记录天气') }}</view>
          </view>
          <view class="memory-diary-card__content">{{ item.content }}</view>
        </view>
      </view>
      <EmptyStateCard
        v-else
        title="去年今天没有日记"
        description="这一部分已经接通真实接口，等你后续留下更多内容后会自动展示。"
        mode="history"
      />
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">打卡</view>
          <view class="section-copy__desc">那一天完成过哪些任务。</view>
        </view>
      </view>

      <view v-if="detail.checkins.length" class="list-stack">
        <view v-for="item in detail.checkins" :key="item.id" class="list-card">
          <view class="list-card__head">
            <view>
              <view class="list-card__title">{{ item.name }}</view>
              <view class="list-card__meta">{{ item.description || '未填写任务描述' }}</view>
            </view>
            <view class="memory-checkin-count">{{ item.totalCount }} 次</view>
          </view>
          <view class="list-card__meta">最近一次完成：{{ item.latestCheckedAt || '无记录' }}</view>
        </view>
      </view>
      <EmptyStateCard
        v-else
        title="去年今天没有打卡"
        description="如果那一天完成过任务，这里会直接聚合展示。"
        mode="history"
      />
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">纪念日</view>
          <view class="section-copy__desc">那一天命中了哪些纪念提醒。</view>
        </view>
        <u-button plain shape="circle" size="mini" :hair-line="false" @click="goMemorialPage">管理纪念日</u-button>
      </view>

      <view v-if="detail.memorialDays.length" class="list-stack">
        <view v-for="item in detail.memorialDays" :key="item.id" class="list-card">
          <view class="list-card__head">
            <view>
              <view class="list-card__title">{{ item.title }}</view>
              <view class="list-card__meta">{{ item.type || '纪念日' }} / {{ item.memorialDate }}</view>
            </view>
            <view class="memory-tag">{{ item.annualRepeat ? '每年重复' : '单次' }}</view>
          </view>
          <view v-if="item.remark" class="list-card__meta">{{ item.remark }}</view>
        </view>
      </view>
      <EmptyStateCard
        v-else
        title="去年今天没有纪念日"
        description="如果命中了纪念日规则，这里会和日记、打卡一起展示。"
        mode="history"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import { fetchOnThisDay } from '@/api/calendar'
import type { CalendarDayDetail, Id } from '@/types/domain'
import { resolveDiaryMoodLabel, resolveDiaryWeatherLabel } from '@/utils/diary-display'

const defaultDate = new Date()
const queryDate = reactive({
  value: defaultDate.toISOString().slice(0, 10)
})
const detail = reactive<CalendarDayDetail>({
  date: '',
  diaries: [],
  checkins: [],
  memorialDays: []
})

const targetDate = computed(() => detail.date || fallbackDateLabel())
const metrics = computed(() => [
  {
    label: '日记',
    value: String(detail.diaries.length),
    hint: '去年同日记录数'
  },
  {
    label: '打卡',
    value: String(detail.checkins.length),
    hint: '去年同日任务数'
  },
  {
    label: '纪念日',
    value: String(detail.memorialDays.length),
    hint: '去年同日命中数'
  }
])

function fallbackDateLabel() {
  const date = new Date(`${queryDate.value}T00:00:00`)
  date.setFullYear(date.getFullYear() - 1)
  return date.toISOString().slice(0, 10)
}

function goDiaryDetail(id: Id) {
  uni.navigateTo({ url: `/pages/diary/detail?id=${id}` })
}

function goMemorialPage() {
  uni.navigateTo({ url: '/pages/memorialPage/index' })
}

async function loadOnThisDay() {
  try {
    const result = await fetchOnThisDay(queryDate.value)
    detail.date = result.date
    detail.diaries = result.diaries || []
    detail.checkins = result.checkins || []
    detail.memorialDays = result.memorialDays || []
  } catch (error) {
    detail.date = fallbackDateLabel()
    detail.diaries = []
    detail.checkins = []
    detail.memorialDays = []
    uni.$feedback.error(error, undefined, '加载去年今日失败')
  }
}

onLoad((query) => {
  if (query?.date) {
    queryDate.value = String(query.date)
  }
})

onShow(() => {
  loadOnThisDay()
})
</script>

<style scoped lang="scss">
.memory-date-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 60rpx;
  padding: 0 var(--space-5);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

.memory-diary-card__content {
  margin-top: var(--space-3);
  color: var(--color-text-secondary);
  font-size: var(--font-caption);
  line-height: var(--leading-loose);
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.memory-checkin-count,
.memory-tag {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
  font-size: var(--font-tiny);
}
</style>
