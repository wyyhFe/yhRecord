<template>
  <AppPage>
    <AppHero
      :eyebrow="greeting"
      title="把一天认真记下来"
      description="日记、记账、打卡和回忆被组织到同一套时间轴里，信息会更安静，也更清楚。"
      badge="V1"
    />

    <SectionBlock title="今日概览" subtitle="先看今天记录状态，再决定从哪里开始">
      <MetricGrid :items="metrics" />
    </SectionBlock>

    <SectionBlock title="最近七天" subtitle="日记和打卡会共用同一条日历状态">
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
import AppHero from '@/components/business/AppHero.vue'
import SectionBlock from '@/components/business/SectionBlock.vue'
import MetricGrid from '@/components/business/MetricGrid.vue'
import StatusCalendarStrip from '@/components/business/StatusCalendarStrip.vue'
import ActionGrid from '@/components/business/ActionGrid.vue'
import { useGreeting } from '@/composables/useGreeting'
import { fetchCalendarSummary } from '@/api/calendar'
import type { DaySummary } from '@/types/domain'

const greeting = useGreeting()
const calendarItems = ref<DaySummary[]>([])

/**
 * 首页入口保持轻量，只展示最常用的四类操作。
 */
const quickActions = [
  { title: '写日记', description: '记录天气、心情、图片与位置。' },
  { title: '记一笔', description: '收入支出与标签统计保持同步。' },
  { title: '打卡任务', description: '把重复动作固定成习惯节律。' },
  { title: '去年今日', description: '快速回看同一天的内容。' }
]

/**
 * 根据最近日历摘要推导出首页的三个核心指标。
 */
const metrics = computed(() => {
  const today = calendarItems.value.at(-1)
  return [
    { label: '日记', value: today?.hasDiary ? '已记录' : '待记录', hint: `数量 ${today?.diaryCount ?? 0}` },
    { label: '打卡', value: today?.hasCheckin ? '已完成' : '待打卡', hint: `次数 ${today?.checkinCount ?? 0}` },
    { label: '回忆', value: `${calendarItems.value.filter((item) => item.hasDiary).length}`, hint: '近七天有日记' }
  ]
})

/**
 * 首页只取当前月份摘要，再截取最近七天用于展示。
 */
async function loadSummary() {
  const now = new Date()
  try {
    const result = await fetchCalendarSummary(now.getFullYear(), now.getMonth() + 1)
    calendarItems.value = result.days.slice(-7)
  } catch {
    // 联调失败时保留一组可预览的降级数据，避免首页完全空白。
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
