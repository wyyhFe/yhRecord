
<template>
  <view class="page-shell-safe ledger-page">
    <!-- Hero -->
    <view class="ledger-hero">
      <view class="ledger-hero__top">
        <text class="ledger-hero__title">{{ pageTitle }}</text>
        <view class="ledger-hero__actions">
          <view class="ledger-hero__action" @tap="goAiAnalysis">
            <text class="ledger-hero__action-text">AI</text>
          </view>
          <view class="ledger-hero__action" @tap="goLedgerTags">
            <text class="ledger-hero__action-text">标签</text>
          </view>
          <view class="ledger-hero__action" @tap="goBooks">
            <text class="ledger-hero__action-text">账本</text>
          </view>
        </view>
      </view>
      <view class="ledger-hero__metrics">
        <view v-for="item in headerMetrics" :key="item.label" class="ledger-hero__metric">
          <text class="ledger-hero__metric-value">¥{{ item.value }}</text>
          <text class="ledger-hero__metric-label">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <!-- 工具栏 -->
    <view class="ledger-toolbar-card">
      <view class="ledger-toolbar">
        <view class="ledger-toolbar__left">
          <picker
            mode="multiSelector"
            :range="[yearOptions, monthOptions]"
            :value="yearMonthPickerValue"
            @change="onYearMonthChange"
          >
            <view class="ledger-toolbar__chip">{{ currentMonthLabel }}</view>
          </picker>

          <view class="ledger-toolbar__chip" @tap="openFilterPopup">{{ filterSummaryLabel }}</view>
        </view>

        <u-subsection
          :list="viewModeList"
          :current="viewModeIndex"
          mode="subsection"
          active-color="var(--color-primary)"
          @change="onViewModeChange"
        />
      </view>
    </view>

    <view v-if="viewMode === 'month'" class="page-section">
      <view v-if="groupedEntries.length" class="ledger-group-stack">
        <u-card
          v-for="group in groupedEntries"
          :key="group.date"
          :show-head="false"
          :show-foot="false"
          :border="false"
          margin="0 0 24rpx 0"
          padding="0"
          :body-style="cardBodyStyle"
        >
          <view class="ledger-day-card">
            <view class="ledger-day-card__head">
              <view class="ledger-day-card__date">
                <text class="ledger-day-card__date-main">{{ formatDateTitle(group.date) }}</text>
                <text class="ledger-day-card__date-sub">{{ formatDateHint(group.date) }}</text>
              </view>
              <view class="ledger-day-card__summary">
                <text class="ledger-day-card__summary-item ledger-day-card__summary-item--expense">
                  出 {{ group.expense.toFixed(2) }}
                </text>
                <text class="ledger-day-card__summary-item ledger-day-card__summary-item--income">
                  入 {{ group.income.toFixed(2) }}
                </text>
              </view>
            </view>

            <view class="ledger-day-card__body">
              <view
                v-for="entry in group.entries"
                :key="entry.id"
                class="ledger-entry-row"
                @tap="editEntry(entry)"
              >
                <view class="ledger-entry-row__main">
                  <view class="ledger-entry-row__top">
                    <u-tag
                      v-if="entry.tags?.length"
                      :text="entry.tags[0].name"
                      plain
                      shape="circle"
                      type="warning"
                      size="mini"
                    />
                    <text v-else class="ledger-entry-row__type">{{ entry.type === 'EXPENSE' ? '支出' : '收入' }}</text>
                    <text class="ledger-entry-row__remark">{{ entry.remark || defaultRemark(entry.type) }}</text>
                  </view>

                  <view v-if="entry.tags && entry.tags.length > 1" class="ledger-entry-row__tags">
                    <u-tag
                      v-for="tag in entry.tags.slice(1)"
                      :key="tag.id"
                      :text="tag.name"
                      plain
                      shape="circle"
                      size="mini"
                    />
                  </view>
                </view>

                <view class="ledger-entry-row__aside">
                  <image
                    v-if="entry.imagePath"
                    :src="resolveMediaUrl(entry.imagePath)"
                    mode="aspectFill"
                    class="ledger-entry-row__image"
                  />
                  <text
                    class="ledger-entry-row__amount"
                    :class="entry.type === 'EXPENSE' ? 'ledger-entry-row__amount--expense' : 'ledger-entry-row__amount--income'"
                  >
                    {{ entry.type === 'EXPENSE' ? '-' : '+' }}{{ Number(entry.amount).toFixed(2) }}
                  </text>
                </view>
              </view>
            </view>
          </view>
        </u-card>
      </view>

      <EmptyStateCard
        v-else
        title="当前筛选条件下还没有账单"
        description="切换年月、分类或账本，或者直接新增一笔记录。"
      />
    </view>

    <view v-else class="page-section">
      <view class="section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">{{ currentYear }} 年统计</view>
            <view class="section-copy__desc">按月份查看收支情况，并用图表展示全年走势和分类分布。</view>
          </view>
        </view>

        <view class="metric-grid">
          <view class="metric-card">
            <view class="metric-card__label">月份数</view>
            <view class="metric-card__value">{{ yearOverview.length }}</view>
            <view class="metric-card__hint">有记录的月份</view>
          </view>
          <view class="metric-card">
            <view class="metric-card__label">总额</view>
            <view class="metric-card__value">{{ yearTotalAmount }}</view>
            <view class="metric-card__hint">全年累计</view>
          </view>
          <view class="metric-card">
            <view class="metric-card__label">账本</view>
            <view class="metric-card__value">{{ selectedBookName || '--' }}</view>
            <view class="metric-card__hint">当前视图</view>
          </view>
        </view>
      </view>

      <LedgerYearCharts
        v-if="yearOverview.length"
        :key="`${currentYear}-${selectedBookId || 'no-book'}-${yearOverview.length}`"
        :year="currentYear"
        :items="yearOverview"
      />
      <EmptyStateCard
        v-else
        title="这一年还没有统计数据"
        description="先切回月视图记几笔，年视图会自动生成汇总。"
      />
    </view>

    <view class="ledger-page-tabbar">
      <view class="ledger-page-tabbar__item" @tap="goBooks">
        <u-icon name="list" size="38" color="var(--color-text-secondary)" />
        <view class="ledger-page-tabbar__text">管理账本</view>
      </view>

      <view class="ledger-page-tabbar__center" @tap="openCreateEntryPopup">
        <view class="ledger-page-tabbar__plus">+</view>
        <view class="ledger-page-tabbar__text ledger-page-tabbar__text--active">记一笔</view>
      </view>

      <view class="ledger-page-tabbar__item" @tap="shareBook">
        <u-icon name="share" size="38" color="var(--color-text-secondary)" />
        <view class="ledger-page-tabbar__text">邀请</view>
      </view>
    </view>
    <u-popup v-model="showFilterPopup" mode="bottom" border-radius="28" :safe-area-inset-bottom="true">
      <view class="filter-popup">
        <view class="filter-popup__head">
          <view class="filter-popup__title">筛选账单</view>
        </view>

        <view class="block-stack">
          <view class="field-label">一级分类</view>
          <ChoiceChips v-model="pendingTypeFilter" :items="filterTypeOptions" />
        </view>

        <view v-if="pendingTagOptions.length" class="block-stack">
          <view class="field-label">二级标签</view>
          <ChoiceChips v-model="pendingFilterTagId" :items="pendingTagOptions" />
        </view>

        <view v-else class="note-card filter-popup__empty">
          当前一级分类下还没有可选标签，可以只按一级分类筛选。
        </view>

        <view class="action-grid-2">
          <u-button plain shape="circle" :hair-line="false" @click="resetFilter">重置</u-button>
          <u-button
            type="primary"
            shape="circle"
            color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
            @click="applyFilter"
          >
            应用
          </u-button>
        </view>
      </view>
    </u-popup>

    <u-popup v-model="showEntryPopup" mode="bottom" border-radius="28" :safe-area-inset-bottom="true">
      <view class="entry-popup">
        <scroll-view scroll-y class="entry-popup__body">
          <view class="entry-popup__head">
            <view class="entry-popup__title">{{ editingEntryId ? '修改账单' : '记一笔' }}</view>
            <view class="entry-popup__subtitle">金额、日期、标签和备注都在这里完成。</view>
          </view>

          <view class="entry-popup__section">
            <view class="entry-popup__row entry-popup__row--top">
              <ChoiceChips v-model="entryForm.type" :items="typeOptions" />
              <picker mode="date" :value="entryForm.entryDate" @change="onEntryDateChange">
                <view class="entry-popup__date">{{ entryForm.entryDate }}</view>
              </picker>
            </view>

            <view class="entry-popup__book" @tap="openBookSelector">
              <text class="entry-popup__book-label">所属账本</text>
              <text class="entry-popup__book-value">{{ selectedBookLabel }}</text>
            </view>
          </view>

          <view class="entry-popup__section">
            <view class="entry-popup__amount">
              <text class="entry-popup__amount-symbol">¥</text>
              <text class="entry-popup__amount-value">{{ displayAmount }}</text>
            </view>
          </view>

          <view class="entry-popup__section block-stack">
            <view class="field-label">记账标签</view>
            <ChoiceChips v-model="selectedTagIds" :items="popupTagOptions" :multiple="true" />
          </view>

          <view class="entry-popup__section block-stack">
            <view class="entry-popup__row entry-popup__row--between entry-popup__row--plain">
              <view class="field-label">备注</view>
              <view class="entry-popup__link" @tap="chooseLedgerImage">
                {{ entryImagePath ? '重新选择图片' : '添加图片' }}
              </view>
            </view>

            <view v-if="entryImagePath" class="ledger-entry-image">
              <image :src="resolveMediaUrl(entryImagePath)" mode="aspectFill" class="ledger-entry-image__img" />
            </view>

            <u-input
              v-model="entryForm.remark"
              placeholder="补充这一笔的备注"
              :border="true"
              border-color="var(--color-border-strong)"
              :custom-style="remarkStyle"
            />
          </view>

          <view class="entry-popup__section">
            <view class="entry-keyboard">
              <view v-for="key in keyboardKeys" :key="key" class="entry-keyboard__key" @tap="tapKeyboard(key)">
                {{ key }}
              </view>
            </view>
          </view>

          <view class="entry-popup__actions" :class="editingEntryId ? 'entry-popup__actions--editing' : ''">
            <u-button plain shape="circle" :hair-line="false" @click="closeEntryPopup">取消</u-button>
            <u-button
              v-if="editingEntryId"
              type="error"
              shape="circle"
              plain
              :loading="submitting"
              @click="removeEntry"
            >
              删除
            </u-button>
            <u-button
              type="primary"
              shape="circle"
              color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
              :loading="submitting"
              @click="submitEntry"
            >
              {{ submitting ? '保存中...' : '保存' }}
            </u-button>
          </view>
        </scroll-view>
      </view>
    </u-popup>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import ChoiceChips from '@/components/business/choice-chips'
import LedgerYearCharts from '@/components/business/ledger-year-charts/index.vue'
import EmptyStateCard from '@/components/business/empty-state-card'
import { API_BASE_URL, OSS_BASE_URL } from '@/config/app'
import { fetchBooks, type LedgerBook } from '@/api/books'
import { fetchMonthLedger, fetchYearLedger } from '@/api/ledger'
import { createLedgerEntry, deleteLedgerEntry, updateLedgerEntry, type LedgerEntryFormPayload } from '@/api/ledger-form'
import { fetchUserTags, type TagItem } from '@/api/tag'
import type { Id, LedgerEntry } from '@/types/domain'
import { getLastLedgerBook, setLastLedgerBook } from '@/utils/ledger-book'
import { uploadImageToOss } from '@/utils/upload'

type EntryType = 'EXPENSE' | 'INCOME'
type ViewMode = 'month' | 'year'
type TypeFilter = 'ALL' | EntryType

interface YearMonthDistributionItem {
  label: string
  amount: number
  ratio: number
}

interface YearMonthCard {
  month: number
  expense: number
  income: number
  distributions: YearMonthDistributionItem[]
}

const typeOptions = [
  { label: '支出', value: 'EXPENSE' },
  { label: '收入', value: 'INCOME' }
]
const filterTypeOptions = [
  { label: '全部', value: 'ALL' },
  { label: '支出', value: 'EXPENSE' },
  { label: '收入', value: 'INCOME' }
]
const viewModeList = ['月', '年']
const keyboardKeys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '0', '删除']
const cardBodyStyle = {
  padding: '0',
  background: 'var(--color-bg)',
  borderRadius: '28rpx'
}
const remarkStyle = {
  background: 'var(--color-surface-soft)',
  borderRadius: '20rpx',
  padding: '0 22rpx',
  fontSize: '28rpx',
  minHeight: '84rpx'
}

const now = new Date()
const entries = ref<LedgerEntry[]>([])
const books = ref<LedgerBook[]>([])
const allTags = ref<TagItem[]>([])
const yearOverview = ref<YearMonthCard[]>([])
const currentYear = ref(now.getFullYear())
const currentMonth = ref(now.getMonth() + 1)
const viewMode = ref<ViewMode>('month')
const typeFilter = ref<TypeFilter>('ALL')
const selectedFilterTagId = ref<Id>()
const pendingTypeFilter = ref<TypeFilter>('ALL')
const pendingFilterTagId = ref<Id>()
const selectedBookId = ref<Id>()
const selectedBookName = ref('')
const selectedTagIds = ref<Id[]>([])
const amountText = ref('')
const entryImagePath = ref('')
const showFilterPopup = ref(false)
const showEntryPopup = ref(false)
const submitting = ref(false)
const editingEntryId = ref<Id>()
const pendingOpenEntry = ref(false)

const entryForm = ref({
  type: 'EXPENSE' as EntryType,
  entryDate: new Date().toISOString().slice(0, 10),
  remark: ''
})

const yearOptions = computed(() => {
  const current = now.getFullYear()
  return Array.from({ length: 8 }, (_, index) => String(current - 5 + index))
})
const monthOptions = computed(() => Array.from({ length: 12 }, (_, index) => `${index + 1}月`))
const yearMonthPickerValue = computed(() => {
  const yearIndex = Math.max(yearOptions.value.findIndex((item) => Number(item) === currentYear.value), 0)
  const monthIndex = Math.max(currentMonth.value - 1, 0)
  return [yearIndex, monthIndex]
})
const currentMonthLabel = computed(() => `${currentYear.value}年${String(currentMonth.value).padStart(2, '0')}月`)
const pageTitle = computed(() => selectedBookName.value || '记账本')
const displayAmount = computed(() => amountText.value || '0')
const selectedBookLabel = computed(() => {
  return books.value.find((item) => item.id === selectedBookId.value)?.name || '请选择账本'
})
const viewModeIndex = computed(() => (viewMode.value === 'month' ? 0 : 1))
const filterTagOptions = computed(() => {
  if (typeFilter.value === 'ALL') return []
  return allTags.value.filter((item) => item.ledgerType === typeFilter.value)
})
const pendingTagOptions = computed(() => {
  if (pendingTypeFilter.value === 'ALL') return []
  return allTags.value
    .filter((item) => item.ledgerType === pendingTypeFilter.value)
    .map((item) => ({
      label: item.name,
      value: item.id
    }))
})
const popupTagOptions = computed(() => {
  return allTags.value
    .filter((item) => item.ledgerType === entryForm.value.type)
    .map((item) => ({
      label: item.name,
      value: item.id
    }))
})
const filterSummaryLabel = computed(() => {
  if (typeFilter.value === 'ALL') return '全部分类'
  const typeLabel = typeFilter.value === 'EXPENSE' ? '支出' : '收入'
  if (!selectedFilterTagId.value) return `${typeLabel} · 全部标签`
  const tagName = filterTagOptions.value.find((item) => item.id === selectedFilterTagId.value)?.name
  return `${typeLabel} · ${tagName || '全部标签'}`
})

const filteredEntries = computed(() => {
  return entries.value.filter((item) => {
    if (typeFilter.value !== 'ALL' && item.type !== typeFilter.value) return false
    if (selectedFilterTagId.value && !item.tagIds?.includes(selectedFilterTagId.value)) return false
    return true
  })
})

const headerMetrics = computed(() => {
  const expense = filteredEntries.value
    .filter((item) => item.type === 'EXPENSE')
    .reduce((sum, item) => sum + Number(item.amount), 0)
  const income = filteredEntries.value
    .filter((item) => item.type === 'INCOME')
    .reduce((sum, item) => sum + Number(item.amount), 0)

  return [
    {
      label: '支出',
      value: expense.toFixed(2),
      hint: viewMode.value === 'month' ? '当前月份支出' : '当前筛选结果'
    },
    {
      label: '收入',
      value: income.toFixed(2),
      hint: viewMode.value === 'month' ? '当前月份收入' : '当前筛选结果'
    },
    {
      label: '结余',
      value: (income - expense).toFixed(2),
      hint: '收入减支出'
    }
  ]
})

const groupedEntries = computed(() => {
  const map = new Map<string, LedgerEntry[]>()
  for (const entry of filteredEntries.value) {
    const list = map.get(entry.entryDate) || []
    list.push(entry)
    map.set(entry.entryDate, list)
  }
  return Array.from(map.entries())
    .map(([date, dayEntries]) => ({
      date,
      entries: dayEntries,
      expense: dayEntries.filter((item) => item.type === 'EXPENSE').reduce((sum, item) => sum + Number(item.amount), 0),
      income: dayEntries.filter((item) => item.type === 'INCOME').reduce((sum, item) => sum + Number(item.amount), 0)
    }))
    .sort((a, b) => (a.date < b.date ? 1 : -1))
})

const yearTotalAmount = computed(() => {
  return yearOverview.value
    .reduce((sum, item) => sum + Number(item.expense) + Number(item.income), 0)
    .toFixed(2)
})

watch(pendingTypeFilter, (nextType) => {
  if (nextType === 'ALL') {
    pendingFilterTagId.value = undefined
    return
  }
  const validIds = new Set(
    allTags.value.filter((item) => item.ledgerType === nextType).map((item) => item.id)
  )
  if (pendingFilterTagId.value && !validIds.has(pendingFilterTagId.value)) {
    pendingFilterTagId.value = undefined
  }
})

watch(
  () => entryForm.value.type,
  (nextType) => {
    const validIds = new Set(allTags.value.filter((item) => item.ledgerType === nextType).map((item) => item.id))
    selectedTagIds.value = selectedTagIds.value.filter((id) => validIds.has(id))
  }
)

function resolveMediaUrl(path?: string) {
  if (!path) return ''
  if (/^https?:\/\//.test(path)) return path
  const normalizedBase = API_BASE_URL.replace(/\/$/, '')
  if (path.startsWith('/')) {
    return `${normalizedBase}${path}`
  }
  if (/^(diary|ledger|avatar)\//.test(path)) {
    return `${OSS_BASE_URL.replace(/\/$/, '')}/${path}`
  }
  return `${normalizedBase}/${path}`
}

function defaultRemark(type: EntryType) {
  return type === 'EXPENSE' ? '未命名支出' : '未命名收入'
}

function formatDateTitle(dateText: string) {
  const date = new Date(`${dateText}T00:00:00`)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

function formatDateHint(dateText: string) {
  const target = new Date(`${dateText}T00:00:00`)
  const today = new Date()
  const todayOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime()
  const targetOnly = new Date(target.getFullYear(), target.getMonth(), target.getDate()).getTime()
  const diffDays = Math.round((todayOnly - targetOnly) / 86400000)
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  return ''
}

function buildYearMonthCard(month: number, monthEntries: LedgerEntry[]): YearMonthCard {
  const expenseEntries = monthEntries.filter((item) => item.type === 'EXPENSE')
  const incomeEntries = monthEntries.filter((item) => item.type === 'INCOME')
  const expense = expenseEntries.reduce((sum, item) => sum + Number(item.amount), 0)
  const income = incomeEntries.reduce((sum, item) => sum + Number(item.amount), 0)

  const distributionMap = new Map<string, number>()
  for (const entry of expenseEntries) {
    const label = entry.tags?.[0]?.name || '未分类'
    distributionMap.set(label, (distributionMap.get(label) || 0) + Number(entry.amount))
  }

  const safeExpense = expense > 0 ? expense : 1
  const distributions = Array.from(distributionMap.entries())
    .map(([label, amount]) => ({
      label,
      amount,
      ratio: amount / safeExpense
    }))
    .sort((a, b) => b.amount - a.amount)

  return {
    month,
    expense,
    income,
    distributions
  }
}

function rememberCurrentBook() {
  if (!selectedBookId.value || !selectedBookName.value) return
  setLastLedgerBook({ id: selectedBookId.value, name: selectedBookName.value })
}

function syncPendingFilter() {
  pendingTypeFilter.value = typeFilter.value
  pendingFilterTagId.value = selectedFilterTagId.value
}

function goBooks() {
  uni.navigateTo({ url: '/pages/ledger/books' })
}

function goAiAnalysis() {
  // 直接进 AI 账单分析页，把当前账本名带过去预填补充问题。
  // 之前是跳到聊天页只给一段 prompt scene，现在改成跳真正的账单分析页。
  const bookName = selectedBookName.value || ''
  const query = bookName ? `?bookName=${encodeURIComponent(bookName)}` : ''
  uni.navigateTo({ url: `/pages/ai/bill-analysis${query}` })
}

function goLedgerTags() {
  uni.navigateTo({ url: '/pages/profile/tags/index?moduleType=LEDGER' })
}

function shareBook() {
  uni.$feedback.info('账本邀请功能下一步再接入')
}

function onYearMonthChange(event: { detail: { value: number[] } }) {
  const [yearIndex, monthIndex] = event.detail.value
  currentYear.value = Number(yearOptions.value[yearIndex])
  currentMonth.value = monthIndex + 1
  reloadByView()
}

function openFilterPopup() {
  syncPendingFilter()
  showFilterPopup.value = true
}

function resetFilter() {
  pendingTypeFilter.value = 'ALL'
  pendingFilterTagId.value = undefined
  typeFilter.value = 'ALL'
  selectedFilterTagId.value = undefined
  showFilterPopup.value = false
}

function applyFilter() {
  typeFilter.value = pendingTypeFilter.value
  selectedFilterTagId.value = pendingFilterTagId.value
  showFilterPopup.value = false
}

function onViewModeChange(index: number) {
  viewMode.value = index === 0 ? 'month' : 'year'
  reloadByView()
}

function resetEntryForm() {
  editingEntryId.value = undefined
  entryForm.value = {
    type: 'EXPENSE',
    entryDate: new Date().toISOString().slice(0, 10),
    remark: ''
  }
  amountText.value = ''
  selectedTagIds.value = []
  entryImagePath.value = ''
}

function openCreateEntryPopup() {
  if (!selectedBookId.value) {
    uni.navigateTo({ url: '/pages/ledger/books' })
    return
  }
  rememberCurrentBook()
  resetEntryForm()
  showEntryPopup.value = true
}

function editEntry(entry: LedgerEntry) {
  editingEntryId.value = entry.id
  entryForm.value = {
    type: entry.type,
    entryDate: entry.entryDate,
    remark: entry.remark || ''
  }
  amountText.value = Number(entry.amount).toFixed(2).replace(/\.00$/, '')
  selectedTagIds.value = entry.tagIds?.length
    ? [...entry.tagIds]
    : (entry.tags || []).map((item) => item.id)
  entryImagePath.value = entry.imagePath || ''
  if (entry.bookId) {
    selectedBookId.value = entry.bookId
  }
  showEntryPopup.value = true
}

function closeEntryPopup() {
  showEntryPopup.value = false
  resetEntryForm()
}

function normalizeAmount(next: string) {
  const cleaned = next.replace(/[^\d.]/g, '')
  const parts = cleaned.split('.')
  if (parts.length > 2) return amountText.value
  if (parts[1] && parts[1].length > 2) return `${parts[0]}.${parts[1].slice(0, 2)}`
  return cleaned
}

function tapKeyboard(key: string) {
  if (key === '删除') {
    amountText.value = amountText.value.slice(0, -1)
    return
  }
  amountText.value = normalizeAmount(`${amountText.value}${key}`)
}

function onEntryDateChange(event: { detail: { value: string } }) {
  entryForm.value.entryDate = event.detail.value
}

function openBookSelector() {
  if (!books.value.length) {
    uni.$feedback.info('还没有账本，请先创建账本')
    return
  }
  uni.showActionSheet({
    itemList: books.value.map((item) => item.name),
    success: ({ tapIndex }) => {
      const book = books.value[tapIndex]
      if (!book) return
      selectedBookId.value = book.id
      selectedBookName.value = book.name
      rememberCurrentBook()
      reloadByView()
    }
  })
}

async function chooseLedgerImage() {
  try {
    const result = await uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera']
    })
    const filePath = result.tempFilePaths?.[0]
    if (!filePath) return
    entryImagePath.value = await uploadImageToOss({
      filePath,
      dir: 'ledger/'
    })
    uni.$feedback.success('图片已上传')
  } catch (error) {
    uni.$feedback.error(error, undefined, '图片上传失败')
  }
}

async function submitEntry() {
  if (!selectedBookId.value) {
    uni.$feedback.error('请先选择账本')
    return
  }
  if (!amountText.value) {
    uni.$feedback.error('请先输入金额')
    return
  }

  const amount = Number(amountText.value)
  if (Number.isNaN(amount) || amount <= 0) {
    uni.$feedback.error('金额必须大于 0')
    return
  }

  const payload: LedgerEntryFormPayload = {
    bookId: selectedBookId.value,
    type: entryForm.value.type,
    amount,
    entryDate: entryForm.value.entryDate,
    remark: entryForm.value.remark.trim() || undefined,
    imagePath: entryImagePath.value || undefined,
    tagIds: selectedTagIds.value
  }

  submitting.value = true
  try {
    if (editingEntryId.value) {
      await updateLedgerEntry(editingEntryId.value, payload)
      uni.$feedback.success('账单已更新')
    } else {
      await createLedgerEntry(payload)
      uni.$feedback.success('账单已保存')
    }
    rememberCurrentBook()
    closeEntryPopup()
    await Promise.all([loadMonthEntries(), viewMode.value === 'year' ? loadYearStats() : Promise.resolve()])
  } catch (error) {
    uni.$feedback.error(error, undefined, editingEntryId.value ? '更新失败' : '保存失败')
  } finally {
    submitting.value = false
  }
}

async function removeEntry() {
  if (!editingEntryId.value) return

  const result = await uni.showModal({
    title: '确认删除账单',
    content: '删除后会进入回收站，可在 15 天内恢复。'
  })
  if (!result.confirm) return

  submitting.value = true
  try {
    await deleteLedgerEntry(editingEntryId.value)
    uni.$feedback.success('账单已移入回收站')
    closeEntryPopup()
    await Promise.all([loadMonthEntries(), viewMode.value === 'year' ? loadYearStats() : Promise.resolve()])
  } catch (error) {
    uni.$feedback.error(error, undefined, '删除失败')
  } finally {
    submitting.value = false
  }
}

async function loadBooks() {
  books.value = await fetchBooks()
  if (selectedBookId.value) {
    const target = books.value.find((item) => item.id === selectedBookId.value)
    if (target) {
      selectedBookName.value = target.name
      rememberCurrentBook()
    }
  }
}

async function loadTags() {
  allTags.value = await fetchUserTags('LEDGER')
}

async function loadMonthEntries() {
  entries.value = await fetchMonthLedger(currentYear.value, currentMonth.value, selectedBookId.value)
}

async function loadYearStats() {
  // 年视图改成一次性拉全年明细，避免前端按 1~12 月连续触发 12 次接口请求。
  const yearEntries = await fetchYearLedger(currentYear.value, selectedBookId.value)
  const monthMap = new Map<number, LedgerEntry[]>()
  for (const entry of yearEntries) {
    // 把全年明细按月份分桶，后面图表会直接消费这个月度聚合结果。
    const month = new Date(`${entry.entryDate}T00:00:00`).getMonth() + 1
    const bucket = monthMap.get(month) || []
    bucket.push(entry)
    monthMap.set(month, bucket)
  }

  // 固定生成 1~12 月的数据结构，保证图表顺序稳定；没有数据的月份最后统一过滤掉。
  yearOverview.value = Array.from({ length: 12 }, (_, index) => {
    const month = index + 1
    return buildYearMonthCard(month, monthMap.get(month) || [])
  })
    .filter((item) => item.expense > 0 || item.income > 0)
}

async function reloadByView() {
  if (viewMode.value === 'month') {
    await loadMonthEntries()
    return
  }
  // 切到年视图时，走“全年一次请求 + 前端聚合”的分支。
  await loadYearStats()
}

onLoad((query) => {
  if (query?.bookId) selectedBookId.value = String(query.bookId)
  if (query?.bookName) selectedBookName.value = decodeURIComponent(String(query.bookName))

  if (!selectedBookId.value) {
    const lastBook = getLastLedgerBook()
    if (lastBook) {
      selectedBookId.value = lastBook.id
      selectedBookName.value = lastBook.name
    }
  }

  if (selectedBookName.value) {
    rememberCurrentBook()
    uni.setNavigationBarTitle({ title: selectedBookName.value })
  }

  if (query?.openEntry === '1') {
    pendingOpenEntry.value = true
  }
})

onShow(() => {
  Promise.all([loadBooks(), loadTags()])
    .then(async () => {
      await reloadByView()
      if (pendingOpenEntry.value && selectedBookId.value) {
        pendingOpenEntry.value = false
        openCreateEntryPopup()
      }
    })
    .catch((error) => {
      uni.$feedback.error(error, undefined, '加载账本数据失败')
    })
})
</script>

<style scoped lang="scss">
.ledger-page {
  padding-bottom: calc(180rpx + env(safe-area-inset-bottom));
}

/* ========== Hero ========== */
.ledger-hero {
  background: var(--color-ledger-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-7) var(--space-6) var(--space-6);
  color: #fff;
}

.ledger-hero__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
}

.ledger-hero__title {
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.ledger-hero__actions {
  display: flex;
  gap: var(--space-2);
}

.ledger-hero__action {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.18);
}

.ledger-hero__action-text {
  font-size: var(--font-tiny);
  font-weight: var(--weight-semibold);
}

.ledger-hero__metrics {
  display: flex;
  background: rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-medium);
  padding: var(--space-4) 0;
}

.ledger-hero__metric {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.ledger-hero__metric-value {
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.ledger-hero__metric-label {
  font-size: var(--font-tiny);
  opacity: 0.8;
}

/* ========== 工具栏卡片 ========== */
.ledger-toolbar-card {
  margin: var(--space-4) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-4) var(--space-5);
}

.ledger-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.ledger-toolbar__left {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  flex: 1;
}

.ledger-toolbar__chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 68rpx;
  padding: 0 var(--space-5);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-caption);
  font-weight: var(--weight-semibold);
}

.ledger-group-stack {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.ledger-day-card {
  overflow: hidden;
  border-radius: var(--radius-large);
}

.ledger-day-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-6);
  border-bottom: 1rpx solid var(--color-surface-soft);
}

.ledger-day-card__date {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
}

.ledger-day-card__date-main {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.ledger-day-card__date-sub {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.ledger-day-card__summary {
  display: flex;
  gap: var(--space-4);
}

.ledger-day-card__summary-item {
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.ledger-day-card__summary-item--expense {
  color: var(--color-danger);
}

.ledger-day-card__summary-item--income {
  color: var(--color-success);
}

.ledger-day-card__body {
  display: flex;
  flex-direction: column;
}

.ledger-entry-row {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-6);
  border-bottom: 1rpx solid var(--color-surface-soft);
}

.ledger-entry-row:last-child {
  border-bottom: none;
}

.ledger-entry-row__main {
  min-width: 0;
  flex: 1;
}

.ledger-entry-row__top {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.ledger-entry-row__type {
  color: var(--color-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

.ledger-entry-row__remark {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ledger-entry-row__tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: var(--space-3);
}

.ledger-entry-row__aside {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.ledger-entry-row__image {
  width: 92rpx;
  height: 92rpx;
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
}

.ledger-entry-row__amount {
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.ledger-entry-row__amount--expense {
  color: var(--color-text-primary);
}

.ledger-entry-row__amount--income {
  color: var(--color-success);
}

.ledger-page-tabbar {
  position: fixed;
  left: var(--space-4);
  right: var(--space-4);
  bottom: var(--space-5);
  z-index: 20;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: var(--space-4) var(--space-6) var(--space-3);
  border-radius: var(--radius-xlarge);
  background: var(--color-surface);
  box-shadow: 0 18rpx 42rpx rgba(67, 41, 26, 0.12);
}

.ledger-page-tabbar__item,
.ledger-page-tabbar__center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
}

.ledger-page-tabbar__center {
  transform: translateY(-18rpx);
}

.ledger-page-tabbar__plus {
  width: 92rpx;
  height: 92rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary-gradient);
  color: var(--color-bg);
  font-size: 52rpx;
  line-height: 1;
  box-shadow: 0 16rpx 30rpx rgba(144, 88, 49, 0.2);
}

.ledger-page-tabbar__text {
  color: var(--color-text-secondary);
  font-size: var(--font-tiny);
}

.ledger-page-tabbar__text--active {
  color: var(--color-primary);
  font-weight: var(--weight-bold);
}

.filter-popup,
.entry-popup {
  background: var(--color-bg);
}

.filter-popup {
  padding: var(--space-6) var(--space-5) calc(var(--space-6) + env(safe-area-inset-bottom));
}

.filter-popup__head,
.entry-popup__head {
  text-align: center;
}

.filter-popup__title,
.entry-popup__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.entry-popup {
  max-height: 70vh;
  padding: 24rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
}

.entry-popup__subtitle {
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.entry-popup__body {
  max-height: calc(70vh - 48rpx);
}

.entry-popup__section {
  margin-bottom: var(--space-4);
  padding: var(--space-5) 0;
  border-bottom: 1rpx solid var(--color-surface-soft);
}

.entry-popup__section:last-child {
  margin-bottom: 0;
}

.entry-popup__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.entry-popup__row--top {
  align-items: flex-start;
}

.entry-popup__row--between {
  justify-content: space-between;
}

.entry-popup__row--plain {
  margin-bottom: 12rpx;
}

.entry-popup__date,
.entry-popup__book {
  min-height: 76rpx;
  border-radius: 20rpx;
  background: var(--color-surface-soft);
}

.entry-popup__date {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 220rpx;
  padding: 0 var(--space-5);
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.entry-popup__book {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--space-4);
  padding: 0 var(--space-5);
}

.entry-popup__book-label {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.entry-popup__book-value {
  color: var(--color-text-primary);
  font-size: 28rpx;
  font-weight: 600;
}

.entry-popup__amount {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.entry-popup__amount-symbol {
  color: var(--color-text-secondary);
  font-size: 40rpx;
  font-weight: 700;
}

.entry-popup__amount-value {
  color: var(--color-text-primary);
  font-size: 72rpx;
  font-weight: 700;
  line-height: 1;
}

.entry-popup__link {
  color: var(--color-primary);
  font-size: 26rpx;
  font-weight: 600;
}

.ledger-entry-image {
  margin-bottom: 12rpx;
}

.ledger-entry-image__img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 20rpx;
  background: var(--color-surface-soft);
}

.entry-keyboard {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
}

.entry-keyboard__key {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 92rpx;
  border-radius: 20rpx;
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: 40rpx;
  font-weight: 600;
}

.entry-popup__actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 20rpx;
  padding-top: 12rpx;
}

.entry-popup__actions--editing {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.filter-popup__empty {
  font-size: 24rpx;
  line-height: 1.7;
}

</style>
