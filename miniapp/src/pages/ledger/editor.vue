<template>
  <AppPage>
    <AppHero
      eyebrow="新增账单"
      title="把这笔钱记下来"
      description="收支类型、账本、日期和备注直接对齐后端字段。"
      badge="Ledger Form"
    />

    <SectionBlock title="基础信息" subtitle="先把必要字段录进去">
      <view class="glass-panel px-[24rpx] py-[24rpx]">
        <view class="text-[24rpx] text-[#7f7366]">收支类型</view>
        <ChoiceChips v-model="form.type" :items="typeOptions" />

        <view class="mt-[18rpx] text-[24rpx] text-[#7f7366]">账本</view>
        <picker :range="books" range-key="name" @change="onBookChange">
          <view class="mt-[12rpx] rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] py-[18rpx] text-[28rpx] font-semibold text-ink">
            {{ selectedBookLabel }}
          </view>
        </picker>

        <view class="mt-[18rpx] text-[24rpx] text-[#7f7366]">金额</view>
        <input
          v-model="amountText"
          class="mt-[12rpx] rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] py-[18rpx] text-[28rpx] font-semibold text-ink"
          placeholder="0.00"
          type="digit"
        />

        <view class="mt-[18rpx] text-[24rpx] text-[#7f7366]">日期</view>
        <picker mode="date" :value="form.entryDate" @change="onDateChange">
          <view class="mt-[12rpx] rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] py-[18rpx] text-[28rpx] font-semibold text-ink">
            {{ form.entryDate }}
          </view>
        </picker>

        <view class="mt-[18rpx] text-[24rpx] text-[#7f7366]">备注</view>
        <textarea
          v-model="form.remark"
          class="mt-[12rpx] h-[180rpx] rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] py-[18rpx] text-[26rpx]"
          placeholder="补充这一笔的说明"
        />
      </view>
    </SectionBlock>

    <view class="mt-[32rpx]">
      <BaseButton :disabled="submitting" @tap="submit">保存账单</BaseButton>
    </view>
  </AppPage>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/app-hero'
import SectionBlock from '@/components/business/section-block'
import ChoiceChips from '@/components/business/choice-chips'
import BaseButton from '@/components/base/base-button'
import { createLedgerEntry } from '@/api/ledger-form'
import { fetchBooks, type LedgerBook } from '@/api/books'

const typeOptions = [
  { label: '支出', value: 'EXPENSE' },
  { label: '收入', value: 'INCOME' }
]

const books = ref<LedgerBook[]>([])
const amountText = ref('')
const submitting = ref(false)
const form = ref({
  bookId: 0,
  type: 'EXPENSE' as 'EXPENSE' | 'INCOME',
  entryDate: new Date().toISOString().slice(0, 10),
  remark: ''
})

const selectedBookLabel = computed(() => books.value.find((item) => item.id === form.value.bookId)?.name || '请选择账本')

function onBookChange(event: { detail: { value: string } }) {
  const book = books.value[Number(event.detail.value)]
  if (book) form.value.bookId = book.id
}

function onDateChange(event: { detail: { value: string } }) {
  form.value.entryDate = event.detail.value
}

/**
 * 账单表单在提交前会先做账本必填和金额格式校验。
 */
async function submit() {
  if (!form.value.bookId || !amountText.value) {
    uni.showToast({ title: '请先选择账本并填写金额', icon: 'none' })
    return
  }

  const amount = Number(amountText.value)
  if (Number.isNaN(amount) || !/^\d+(\.\d{1,2})?$/.test(amountText.value)) {
    uni.showToast({ title: '金额最多保留两位小数', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    await createLedgerEntry({
      ...form.value,
      amount
    })
    uni.showToast({ title: '账单已保存', icon: 'success' })
    setTimeout(() => uni.navigateBack({ delta: 1 }), 500)
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '保存失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

/**
 * 初始化账本列表，并默认选中第一本账本。
 */
async function init() {
  try {
    books.value = await fetchBooks()
    if (books.value[0]) form.value.bookId = books.value[0].id
  } catch {
    books.value = []
  }
}

init()
</script>
