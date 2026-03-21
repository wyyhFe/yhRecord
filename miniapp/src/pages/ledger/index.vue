<template>
  <AppPage>
    <AppHero
      eyebrow="记账本"
      title="钱花到哪里，一眼就知道"
      description="页面查询负责按月份查看当前账单，模板通知负责单独发送月报提醒，两者是独立模块。"
      badge="Ledger"
    />

    <SectionBlock title="本月概览" subtitle="收入、支出与结余放在同一块展示">
      <MetricGrid :items="metricItems" />
    </SectionBlock>

    <SectionBlock title="最近流水" subtitle="页面查询模块，支持按当前月份查看账单列表">
      <view class="mb-[18rpx]">
        <BaseButton @tap="goEditor">新增一笔账单</BaseButton>
      </view>

      <view v-if="entries.length" class="flex flex-col gap-[18rpx]">
        <BaseCard v-for="item in entries" :key="item.id">
          <view class="flex items-center justify-between">
            <view>
              <view class="text-[28rpx] font-semibold text-ink">{{ item.remark || '未命名账单' }}</view>
              <view class="mt-[8rpx] text-[22rpx] text-[#867868]">{{ item.entryDate }}</view>
            </view>
            <view class="text-[32rpx] font-semibold" :class="item.type === 'EXPENSE' ? 'text-[#c15b52]' : 'text-[#4b6b57]'">
              {{ item.type === 'EXPENSE' ? '-' : '+' }}{{ item.amount }}
            </view>
          </view>
        </BaseCard>
      </view>
      <EmptyState
        v-else
        icon="💵"
        title="账本还没有流水"
        description="先记录第一笔支出或收入，后面的统计才会真正有意义。"
      />
    </SectionBlock>
  </AppPage>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/app-hero'
import SectionBlock from '@/components/business/section-block'
import MetricGrid from '@/components/business/metric-grid'
import BaseCard from '@/components/base/base-card'
import EmptyState from '@/components/business/empty-state'
import BaseButton from '@/components/base/base-button'
import { fetchMonthLedger } from '@/api/ledger'
import type { LedgerEntry } from '@/types/domain'

const entries = ref<LedgerEntry[]>([])

const metricItems = computed(() => {
  const expense = entries.value
    .filter((item) => item.type === 'EXPENSE')
    .reduce((sum, item) => sum + Number(item.amount), 0)
  const income = entries.value
    .filter((item) => item.type === 'INCOME')
    .reduce((sum, item) => sum + Number(item.amount), 0)
  return [
    { label: '支出', value: expense.toFixed(2), hint: '本月总支出' },
    { label: '收入', value: income.toFixed(2), hint: '本月总收入' },
    { label: '结余', value: (income - expense).toFixed(2), hint: '收入减支出' }
  ]
})

function goEditor() {
  uni.navigateTo({ url: '/pages/ledger/editor' })
}

async function init() {
  const now = new Date()
  try {
    entries.value = await fetchMonthLedger(now.getFullYear(), now.getMonth() + 1)
  } catch {
    entries.value = [
      { id: 1, type: 'EXPENSE', amount: 38.5, entryDate: '2026-03-20', remark: '午餐 + 咖啡' },
      { id: 2, type: 'INCOME', amount: 8800, entryDate: '2026-03-15', remark: '工资' }
    ]
  }
}

onShow(() => {
  init()
})
</script>