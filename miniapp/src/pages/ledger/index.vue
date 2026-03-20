<template>
  <AppPage>
    <AppHero
      eyebrow="记账本"
      title="钱花到哪里，一眼就知道"
      description="按账本、月份和标签做汇总，账单列表和新增入口都已经准备好联调。"
      badge="Ledger"
    />

    <SectionBlock title="本月概览" subtitle="收入支出与结余放在同一块">
      <MetricGrid :items="metricItems" />
    </SectionBlock>

    <SectionBlock title="最近流水" subtitle="保留备注与图片入口">
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
        icon="💰"
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
import AppHero from '@/components/business/AppHero.vue'
import SectionBlock from '@/components/business/SectionBlock.vue'
import MetricGrid from '@/components/business/MetricGrid.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import EmptyState from '@/components/business/EmptyState.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import { fetchMonthLedger } from '@/api/ledger'
import type { LedgerEntry } from '@/types/domain'

const entries = ref<LedgerEntry[]>([])

/**
 * 记账首页把流水推导成三块最常看的统计信息。
 */
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

/**
 * 每次页面回显时重新请求本月账单，确保新增后能立即看到结果。
 */
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
