<template>
  <AppPage>
    <AppHero
      :eyebrow="greeting"
      title="把一天认真记下来"
      description="日记、记账、打卡和回忆被组织到同一条时间轴里，信息会更安静，也更清楚。"
      badge="V1"
    />

    <SectionBlock title="今日概览" subtitle="先看今天记录状态，再决定从哪里开始">
      <MetricGrid :items="metrics" />
    </SectionBlock>

    <SectionBlock title="最近七天" subtitle="日记和打卡共用一条日历状态条">
      <StatusCalendarStrip :items="calendarItems" />
    </SectionBlock>

    <SectionBlock title="快速入口" subtitle="常用动作尽量一步完成">
      <ActionGrid :items="quickActions" />
    </SectionBlock>
  </AppPage>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/app-hero'
import SectionBlock from '@/components/business/section-block'
import MetricGrid from '@/components/business/metric-grid'
import StatusCalendarStrip from '@/components/business/status-calendar-strip'
import ActionGrid from '@/components/business/action-grid'
import { useGreeting } from '@/composables/useGreeting'
import { fetchCalendarSummary } from '@/api/calendar'
import type { DaySummary } from '@/types/domain'

const greeting = useGreeting()
const calendarItems = ref<DaySummary[]>([])

const quickActions = [
  { title: '写日记', description: '记录天气、心情、图片与位置。' },
  { title: '记一笔', description: '把收入支出和标签统计同步起来。' },
  { title: '打卡任务', description: '把重复动作固定成习惯节律。' },
  { title: '去年今日', description: '快速回看同一天的内容。' }
]

const metrics = computed(() => {
  const today = calendarItems.value.at(-1)
  return [
    { label: '日记', value: today?.hasDiary ? '已记录' : '待记录', hint: `数量 ${today?.diaryCount ?? 0}` },
    { label: '打卡', value: today?.hasCheckin ? '已完成' : '待打卡', hint: `次数 ${today?.checkinCount ?? 0}` },
    { label: '回忆', value: `${calendarItems.value.filter((item) => item.hasDiary).length}`, hint: '近七天有日记' }
  ]
})

async function loadSummary() {
  const now = new Date()
  try {
    const result = await fetchCalendarSummary(now.getFullYear(), now.getMonth() + 1)
    calendarItems.value = result.days.slice(-7)
  } catch {
    calendarItems.value = Array.from({ length: 7 }).map((_, index) => ({
      date: `2026-03-${String(index + 14).padStart(2, '0')}`,
      hasDiary: index % 2 === 0,
      diaryCount: index % 2 === 0 ? 1 : 0,
      hasCheckin: index % 3 !== 0,
      checkinCount: index % 3 !== 0 ? 1 : 0,
      memorialCount: 0
    }))
  }
}

loadSummary()
</script>