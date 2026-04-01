<template>
  <view class="page-shell-safe ai-bill-page">
    <view class="page-head">
      <view class="section-head">
        <view class="section-copy">
          <view class="page-head__eyebrow">AI Ledger</view>
          <view class="page-head__title">{{ pageTitle }}</view>
          <view class="page-head__desc">
            Structured bill analysis for the selected ledger, with summary, risks, suggestions, and samples.
          </view>
        </view>
        <view class="head-actions">
          <u-button size="mini" type="info" plain @tap="goChat">Ask Follow-up</u-button>
          <u-button :loading="loading" size="mini" type="warning" plain @tap="reload">Refresh</u-button>
        </view>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">Range</view>
          <view class="section-copy__desc">Pick a date range before re-running the analysis.</view>
        </view>
      </view>

      <view class="range-grid">
        <picker mode="date" :value="startDate" @change="onStartDateChange">
          <view class="range-chip">
            <view class="range-chip__label">Start</view>
            <view class="range-chip__value">{{ startDate }}</view>
          </view>
        </picker>

        <picker mode="date" :value="endDate" @change="onEndDateChange">
          <view class="range-chip">
            <view class="range-chip__label">End</view>
            <view class="range-chip__value">{{ endDate }}</view>
          </view>
        </picker>
      </view>

      <view class="range-actions">
        <u-button size="mini" type="info" plain @tap="useRecent30Days">Recent 30d</u-button>
        <u-button size="mini" type="warning" plain @tap="reload">Run Analysis</u-button>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">Extra Question</view>
          <view class="section-copy__desc">Ask the model to focus on a specific spending angle.</view>
        </view>
      </view>

      <textarea
        v-model="question"
        class="question-box"
        maxlength="300"
        placeholder="Example: focus on dining and transport expenses"
      />

      <view class="range-actions">
        <u-button size="mini" type="info" plain @tap="fillDefaultQuestion">Use Template</u-button>
        <u-button size="mini" type="warning" plain @tap="reload">Analyze With Question</u-button>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="metric-grid">
        <view class="metric-card">
          <view class="metric-card__label">Income</view>
          <view class="metric-card__value">{{ formatAmount(result?.totalIncome) }}</view>
          <view class="metric-card__hint">{{ dateRangeLabel }}</view>
        </view>
        <view class="metric-card">
          <view class="metric-card__label">Expense</view>
          <view class="metric-card__value">{{ formatAmount(result?.totalExpense) }}</view>
          <view class="metric-card__hint">{{ result?.entryCount || 0 }} entries</view>
        </view>
        <view class="metric-card">
          <view class="metric-card__label">Balance</view>
          <view class="metric-card__value">{{ formatAmount(result?.balance) }}</view>
          <view class="metric-card__hint">Income minus expense</view>
        </view>
      </view>
    </view>

    <view v-if="loading" class="page-section section-shell">
      <view class="loading-copy">Loading AI bill analysis...</view>
    </view>

    <template v-else-if="result">
      <view class="page-section section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">Summary</view>
            <view class="section-copy__desc">Main conclusion returned by the model.</view>
          </view>
        </view>
        <view class="note-card analysis-copy">{{ result.summary }}</view>
      </view>

      <view class="page-section section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">Observations</view>
            <view class="section-copy__desc">What the model noticed from the recent records.</view>
          </view>
        </view>
        <view v-if="result.observations.length" class="block-stack">
          <view v-for="item in result.observations" :key="item" class="note-card list-card">{{ item }}</view>
        </view>
        <view v-else class="note-card">No observations were returned.</view>
      </view>

      <view class="page-section section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">Risks</view>
            <view class="section-copy__desc">Potential issues worth attention.</view>
          </view>
        </view>
        <view v-if="result.risks.length" class="block-stack">
          <view v-for="item in result.risks" :key="item" class="note-card list-card list-card--risk">{{ item }}</view>
        </view>
        <view v-else class="note-card">No obvious risk detected.</view>
      </view>

      <view class="page-section section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">Suggestions</view>
            <view class="section-copy__desc">Actionable recommendations.</view>
          </view>
        </view>
        <view v-if="result.suggestions.length" class="block-stack">
          <view
            v-for="item in result.suggestions"
            :key="item"
            class="note-card list-card list-card--suggestion"
          >
            {{ item }}
          </view>
        </view>
        <view v-else class="note-card">No suggestion was returned.</view>
      </view>

      <view class="page-section section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">Expense Categories</view>
            <view class="section-copy__desc">Top expense groups in the current range.</view>
          </view>
        </view>
        <view v-if="result.expenseCategories.length" class="category-stack">
          <view v-for="item in result.expenseCategories" :key="item.name" class="category-item">
            <view class="category-item__head">
              <text class="category-item__name">{{ item.name }}</text>
              <text class="category-item__amount">{{ formatAmount(item.amount) }}</text>
            </view>
            <view class="category-item__bar">
              <view class="category-item__bar-fill" :style="{ width: `${item.ratio * 100}%` }" />
            </view>
            <view class="category-item__ratio">{{ formatRatio(item.ratio) }}</view>
          </view>
        </view>
        <view v-else class="note-card">No expense category data.</view>
      </view>

      <view class="page-section section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">Large Samples</view>
            <view class="section-copy__desc">High amount records included in the AI prompt.</view>
          </view>
        </view>
        <view v-if="result.samples.length" class="block-stack">
          <view v-for="item in result.samples" :key="sampleKey(item)" class="sample-card">
            <view class="sample-card__top">
              <text class="sample-card__date">{{ item.entryDate }}</text>
              <text class="sample-card__amount">{{ item.type === 'EXPENSE' ? '-' : '+' }}{{ formatAmount(item.amount) }}</text>
            </view>
            <view class="sample-card__meta">
              {{ item.bookName || result.bookName || 'Ledger' }} / {{ item.category || 'Uncategorized' }}
            </view>
            <view class="sample-card__remark">{{ item.remark || 'No remark' }}</view>
          </view>
        </view>
        <view v-else class="note-card">No sample entries were returned.</view>
      </view>

      <view class="page-section section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">History</view>
            <view class="section-copy__desc">Recent bill analysis records for the current account.</view>
          </view>
        </view>
        <view v-if="historyList.length" class="block-stack">
          <view
            v-for="item in historyList"
            :key="item.id"
            class="history-card"
            @tap="useHistoryItem(item)"
          >
            <view class="history-card__top">
              <text class="history-card__title">{{ item.bookName || 'All Ledgers' }}</text>
              <text class="history-card__time">{{ item.createdAt }}</text>
            </view>
            <view class="history-card__range">{{ item.startDate }} to {{ item.endDate }}</view>
            <view class="history-card__summary">{{ item.summary || 'No summary' }}</view>
          </view>
        </view>
        <view v-else class="note-card">No history records yet.</view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { analyzeBill, fetchBillAnalysisHistory, type BillAnalysisResponse, type BillAnalysisHistory } from '@/api/ai'

const loading = ref(false)
const result = ref<BillAnalysisResponse>()
const bookId = ref('')
const bookName = ref('')
const startDate = ref('')
const endDate = ref('')
const historyList = ref<BillAnalysisHistory[]>([])
const question = ref('')

const pageTitle = computed(() => bookName.value || result.value?.bookName || 'Ledger Analysis')
const dateRangeLabel = computed(() => {
  if (!result.value) return 'Recent range'
  return `${result.value.startDate} to ${result.value.endDate}`
})

function formatAmount(value?: number | null) {
  return Number(value || 0).toFixed(2)
}

function formatRatio(value: number) {
  return `${(value * 100).toFixed(1)}%`
}

function sampleKey(item: BillAnalysisResponse['samples'][number]) {
  return `${item.entryDate}-${item.type}-${item.amount}-${item.category || 'none'}`
}

async function reload() {
  if (startDate.value && endDate.value && startDate.value > endDate.value) {
    uni.$feedback.error('Start date must be earlier than end date')
    return
  }

  loading.value = true
  try {
    result.value = await analyzeBill({
      bookId: bookId.value || undefined,
      startDate: startDate.value || undefined,
      endDate: endDate.value || undefined,
      question: question.value.trim() || (
        bookName.value
          ? `Focus on recent records in ${bookName.value}, explain spending habits, risks, and suggestions.`
          : 'Explain recent spending habits, risks, and suggestions.'
      )
    })
  } catch (error) {
    uni.$feedback.error(error, undefined, 'Failed to load AI bill analysis')
  } finally {
    loading.value = false
  }
}

async function loadHistory() {
  try {
    historyList.value = await fetchBillAnalysisHistory(10)
  } catch {
    historyList.value = []
  }
}

function onStartDateChange(event: { detail: { value: string } }) {
  startDate.value = event.detail.value
}

function onEndDateChange(event: { detail: { value: string } }) {
  endDate.value = event.detail.value
}

function useRecent30Days() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 29)
  endDate.value = end.toISOString().slice(0, 10)
  startDate.value = start.toISOString().slice(0, 10)
}

function fillDefaultQuestion() {
  question.value = bookName.value
    ? `Focus on recent records in ${bookName.value}, explain spending habits, risks, and suggestions.`
    : 'Explain recent spending habits, risks, and suggestions.'
}

function goChat() {
  const query = [
    bookId.value ? `bookId=${encodeURIComponent(bookId.value)}` : '',
    bookName.value ? `bookName=${encodeURIComponent(bookName.value)}` : ''
  ]
    .filter(Boolean)
    .join('&')

  uni.navigateTo({
    url: `/pages/ai/index?scene=${encodeURIComponent(bookName.value || '账单分析追问')}${query ? `&${query}` : ''}`
  })
}

function useHistoryItem(item: BillAnalysisHistory) {
  if (item.bookId) {
    bookId.value = String(item.bookId)
  } else {
    bookId.value = ''
  }
  bookName.value = item.bookName || ''
  startDate.value = item.startDate
  endDate.value = item.endDate
  question.value = item.question || ''
  reload()
}

onLoad((query) => {
  if (query?.bookId) {
    bookId.value = String(query.bookId)
  }
  if (query?.bookName) {
    bookName.value = decodeURIComponent(String(query.bookName))
  }
  useRecent30Days()
  fillDefaultQuestion()
  loadHistory()
  reload()
})
</script>

<style scoped lang="scss">
.ai-bill-page {
  padding-bottom: 40rpx;
}

.loading-copy {
  color: #8a735f;
  font-size: 28rpx;
}

.head-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12rpx;
}

.range-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.range-chip {
  padding: 20rpx 22rpx;
  border-radius: 22rpx;
  background: #fffdf8;
  border: 1rpx solid rgba(196, 124, 82, 0.1);
}

.range-chip__label {
  color: #8a735f;
  font-size: 22rpx;
}

.range-chip__value {
  margin-top: 10rpx;
  color: #2d241c;
  font-size: 28rpx;
  font-weight: 600;
}

.range-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  margin-top: 18rpx;
}

.question-box {
  width: 100%;
  min-height: 180rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: #fffdf9;
  border: 1rpx solid rgba(196, 124, 82, 0.12);
  box-sizing: border-box;
  color: #2d241c;
  font-size: 28rpx;
  line-height: 1.7;
}

.analysis-copy {
  line-height: 1.8;
  font-size: 28rpx;
}

.list-card {
  line-height: 1.8;
}

.list-card--risk {
  background: #fff3ef;
}

.list-card--suggestion {
  background: #f4f9ef;
}

.category-stack {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.category-item {
  padding: 18rpx 20rpx;
  border-radius: 22rpx;
  background: #fffdf8;
  border: 1rpx solid rgba(196, 124, 82, 0.1);
}

.category-item__head {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.category-item__name,
.category-item__amount {
  color: #2d241c;
  font-size: 26rpx;
  font-weight: 600;
}

.category-item__bar {
  margin-top: 14rpx;
  height: 14rpx;
  border-radius: 999rpx;
  background: #f2e7d9;
  overflow: hidden;
}

.category-item__bar-fill {
  height: 100%;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #c47c52 0%, #d7a648 100%);
}

.category-item__ratio {
  margin-top: 10rpx;
  color: #8a735f;
  font-size: 22rpx;
}

.sample-card {
  padding: 20rpx 22rpx;
  border-radius: 22rpx;
  background: #fffdf8;
  border: 1rpx solid rgba(196, 124, 82, 0.1);
}

.sample-card__top {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.sample-card__date,
.sample-card__amount {
  color: #2d241c;
  font-size: 26rpx;
  font-weight: 600;
}

.sample-card__meta {
  margin-top: 10rpx;
  color: #8a735f;
  font-size: 22rpx;
}

.sample-card__remark {
  margin-top: 10rpx;
  color: #4f3f31;
  font-size: 24rpx;
  line-height: 1.7;
}

.history-card {
  padding: 20rpx 22rpx;
  border-radius: 22rpx;
  background: #fffdf8;
  border: 1rpx solid rgba(196, 124, 82, 0.1);
}

.history-card__top {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.history-card__title {
  color: #2d241c;
  font-size: 26rpx;
  font-weight: 600;
}

.history-card__time {
  color: #8a735f;
  font-size: 20rpx;
}

.history-card__range {
  margin-top: 10rpx;
  color: #8a735f;
  font-size: 22rpx;
}

.history-card__summary {
  margin-top: 10rpx;
  color: #4f3f31;
  font-size: 24rpx;
  line-height: 1.7;
}
</style>
