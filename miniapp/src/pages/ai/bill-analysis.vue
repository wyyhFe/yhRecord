<template>
  <view class="page-shell-safe bill-analysis-page">
    <!-- 页面头部，统一沿用项目里 page-head 风格 -->
    <view class="page-head bill-analysis-head">
      <view class="page-head__eyebrow">AI 账单分析</view>
      <view class="page-head__title">让 AI 帮你看一眼账</view>
      <view class="page-head__desc">
        选择时间范围和账本后开始分析，后端会先聚合统计再交给模型，整体大约需要 5-10 秒。
      </view>
    </view>

    <!-- 筛选条件区：日期、账本、补充问题 -->
    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">分析范围</view>
          <view class="section-copy__desc">默认窗口为最近 30 天，可自行调整。</view>
        </view>
      </view>

      <view class="filter-row">
        <view class="filter-row__label">开始日期</view>
        <picker mode="date" :value="form.startDate" :end="form.endDate" @change="handleStartDateChange">
          <view class="filter-row__value">{{ form.startDate || '请选择' }}</view>
        </picker>
      </view>

      <view class="filter-row">
        <view class="filter-row__label">结束日期</view>
        <picker mode="date" :value="form.endDate" :start="form.startDate" @change="handleEndDateChange">
          <view class="filter-row__value">{{ form.endDate || '请选择' }}</view>
        </picker>
      </view>

      <view class="filter-row">
        <view class="filter-row__label">账本</view>
        <picker
          v-if="bookOptions.length"
          mode="selector"
          :range="bookOptions"
          range-key="label"
          :value="bookIndex"
          @change="handleBookChange"
        >
          <view class="filter-row__value">{{ bookOptions[bookIndex]?.label || '全部账本' }}</view>
        </picker>
        <view v-else class="filter-row__value filter-row__value--muted">暂无账本</view>
      </view>

      <view class="question-block">
        <view class="filter-row__label">补充问题（可选）</view>
        <textarea
          v-model="form.question"
          class="question-input"
          maxlength="120"
          placeholder="例如：重点看餐饮和通勤支出"
        />
      </view>

      <view class="action-row">
        <u-button type="warning" :disabled="analyzing" @click="runAnalysis">
          {{ analyzing ? '分析中...' : '开始分析' }}
        </u-button>
      </view>
    </view>

    <!-- 加载中提示，给用户明确反馈 -->
    <view v-if="analyzing" class="note-card">AI 正在阅读你的账单数据，请稍候。</view>

    <!-- 结果区：只在拿到结果后渲染 -->
    <template v-if="result && !analyzing">
      <!-- 总体统计：账单数 / 收入 / 支出 / 结余 -->
      <view class="page-section section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">概览</view>
            <view class="section-copy__desc">
              {{ result.startDate }} 至 {{ result.endDate }}
              {{ result.bookName ? `· ${result.bookName}` : '· 全部账本' }}
            </view>
          </view>
        </view>

        <view class="metric-grid">
          <view class="metric-card">
            <view class="metric-card__label">账单条数</view>
            <view class="metric-card__value">{{ result.entryCount }}</view>
            <view class="metric-card__hint">参与本次分析</view>
          </view>
          <view class="metric-card">
            <view class="metric-card__label">总收入</view>
            <view class="metric-card__value">{{ formatMoney(result.totalIncome) }}</view>
            <view class="metric-card__hint">单位：元</view>
          </view>
          <view class="metric-card">
            <view class="metric-card__label">总支出</view>
            <view class="metric-card__value">{{ formatMoney(result.totalExpense) }}</view>
            <view class="metric-card__hint">单位：元</view>
          </view>
          <view class="metric-card">
            <view class="metric-card__label">结余</view>
            <view class="metric-card__value" :class="balanceColorClass">
              {{ formatMoney(result.balance) }}
            </view>
            <view class="metric-card__hint">收入减支出</view>
          </view>
        </view>
      </view>

      <!-- AI 总结：summary 必有；observations / risks / suggestions 解析失败时为空 -->
      <view class="page-section section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">AI 总结</view>
            <view class="section-copy__desc">基于上述聚合数据生成。</view>
          </view>
        </view>
        <view class="summary-text">{{ result.summary || '暂无 AI 总结' }}</view>
      </view>

      <view v-if="hasInsights" class="page-section section-shell">
        <view class="insight-block" v-if="result.observations?.length">
          <view class="insight-block__title">关键观察</view>
          <view v-for="(item, index) in result.observations" :key="`o-${index}`" class="insight-item">
            · {{ item }}
          </view>
        </view>
        <view class="insight-block" v-if="result.risks?.length">
          <view class="insight-block__title insight-block__title--warn">风险提示</view>
          <view v-for="(item, index) in result.risks" :key="`r-${index}`" class="insight-item">
            · {{ item }}
          </view>
        </view>
        <view class="insight-block" v-if="result.suggestions?.length">
          <view class="insight-block__title insight-block__title--good">建议</view>
          <view v-for="(item, index) in result.suggestions" :key="`s-${index}`" class="insight-item">
            · {{ item }}
          </view>
        </view>
      </view>

      <!-- 分类占比：支出在前、收入在后；占比按降序，0 占比的不展示 -->
      <view v-if="result.expenseCategories?.length" class="page-section section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">支出分类</view>
            <view class="section-copy__desc">按标签聚合，未打标签会归到「未分类」。</view>
          </view>
        </view>
        <view v-for="item in result.expenseCategories" :key="`e-${item.name}`" class="category-row">
          <view class="category-row__name">{{ item.name }}</view>
          <view class="category-row__amount">{{ formatMoney(item.amount) }}</view>
          <view class="category-row__ratio">{{ formatRatio(item.ratio) }}</view>
        </view>
      </view>

      <view v-if="result.incomeCategories?.length" class="page-section section-shell">
        <view class="section-head">
          <view class="section-copy">
            <view class="section-copy__title">收入分类</view>
          </view>
        </view>
        <view v-for="item in result.incomeCategories" :key="`i-${item.name}`" class="category-row">
          <view class="category-row__name">{{ item.name }}</view>
          <view class="category-row__amount">{{ formatMoney(item.amount) }}</view>
          <view class="category-row__ratio">{{ formatRatio(item.ratio) }}</view>
        </view>
      </view>
    </template>

    <!-- 历史区：始终展示，便于用户回看之前分析过的窗口 -->
    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">分析历史</view>
          <view class="section-copy__desc">最多 50 条，倒序展示。</view>
        </view>
        <u-button size="mini" type="info" plain :disabled="historyLoading" @click="loadHistory">
          刷新
        </u-button>
      </view>

      <view v-if="historyLoading && !history.length" class="note-card">加载历史中...</view>
      <view v-else-if="!history.length" class="note-card">还没有分析记录，先做一次分析吧。</view>

      <view v-else class="list-stack">
        <view v-for="item in history" :key="item.id" class="list-card history-card">
          <view class="list-card__head">
            <view>
              <view class="list-card__title">
                {{ item.startDate }} ~ {{ item.endDate }}
              </view>
              <view class="list-card__meta">
                {{ item.bookName || '全部账本' }} · {{ item.entryCount }} 条
              </view>
            </view>
            <view class="list-card__aside">{{ formatDateTime(item.createdAt) }}</view>
          </view>
          <view v-if="item.question" class="history-card__question">补充问题：{{ item.question }}</view>
          <view class="history-card__summary">{{ item.summary || '（无摘要）' }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  fetchBillAnalysisHistory,
  requestBillAnalysis,
  type BillAnalysisHistoryItem,
  type BillAnalysisResult
} from '@/api/ai'
import { fetchBooks, type LedgerBook } from '@/api/books'
import type { Id } from '@/types/domain'

// 表单状态：startDate / endDate 默认给最近 30 天，避免空状态
const form = reactive({
  startDate: '',
  endDate: '',
  bookId: undefined as Id | undefined,
  question: ''
})

// 账本下拉数据。bookId 为空字符串代表"全部账本"，picker 选项 0 默认就是它
const bookOptions = ref<Array<{ label: string; value: Id | '' }>>([{ label: '全部账本', value: '' }])
const bookIndex = ref(0)

const analyzing = ref(false)
const result = ref<BillAnalysisResult | null>(null)

const history = ref<BillAnalysisHistoryItem[]>([])
const historyLoading = ref(false)

// 结余的颜色：正数高亮、负数警告。前端只做颜色，不改文案
const balanceColorClass = computed(() => {
  const value = toNumber(result.value?.balance)
  if (value > 0) return 'metric-card__value--good'
  if (value < 0) return 'metric-card__value--warn'
  return ''
})

// 至少一类有内容才渲染整个 insight 区块，避免出现空标题
const hasInsights = computed(() => {
  if (!result.value) return false
  return Boolean(
    result.value.observations?.length ||
      result.value.risks?.length ||
      result.value.suggestions?.length
  )
})

function pad(value: number) {
  return value < 10 ? `0${value}` : `${value}`
}

function todayString() {
  const now = new Date()
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}

function daysAgoString(days: number) {
  const date = new Date()
  date.setDate(date.getDate() - days)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function toNumber(value: number | string | null | undefined) {
  if (value === null || value === undefined || value === '') return 0
  const num = typeof value === 'number' ? value : Number(value)
  return Number.isFinite(num) ? num : 0
}

function formatMoney(value: number | string | null | undefined) {
  return toNumber(value).toFixed(2)
}

function formatRatio(value: number | string | null | undefined) {
  const num = toNumber(value)
  // 后端给的是 0-1 的小数，乘 100 转成百分比
  return `${(num * 100).toFixed(1)}%`
}

function formatDateTime(value?: string) {
  if (!value) return ''
  const date = new Date(value.replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function handleStartDateChange(event: { detail: { value: string } }) {
  form.startDate = event.detail.value
}

function handleEndDateChange(event: { detail: { value: string } }) {
  form.endDate = event.detail.value
}

function handleBookChange(event: { detail: { value: number } }) {
  bookIndex.value = event.detail.value
  const target = bookOptions.value[bookIndex.value]
  // 空字符串表示"全部账本"，需要还原成 undefined 给请求体
  form.bookId = target?.value === '' ? undefined : target?.value
}

async function loadBooks() {
  try {
    const { list: books } = await fetchBooks()
    bookOptions.value = [
      { label: '全部账本', value: '' },
      ...books.map((book) => ({ label: book.name, value: book.id }))
    ]
  } catch (error) {
    // 账本拉取失败不阻断分析功能，仅保留"全部账本"
    console.warn('账本列表加载失败', error)
  }
}

async function runAnalysis() {
  if (analyzing.value) return
  if (!form.startDate || !form.endDate) {
    uni.showToast({ title: '请先选择日期范围', icon: 'none' })
    return
  }

  analyzing.value = true
  try {
    result.value = await requestBillAnalysis({
      startDate: form.startDate,
      endDate: form.endDate,
      bookId: form.bookId,
      question: form.question.trim() || undefined
    })
    // 分析完顺手刷新一次历史，把新记录展示出来
    void loadHistory()
  } catch (error) {
    const message = error instanceof Error ? error.message : '分析失败，请稍后重试'
    uni.showToast({ title: message, icon: 'none' })
  } finally {
    analyzing.value = false
  }
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const page = await fetchBillAnalysisHistory({ current: 1, size: 10 })
    history.value = page.list || []
  } catch (error) {
    console.warn('历史加载失败', error)
  } finally {
    historyLoading.value = false
  }
}

onLoad((query) => {
  // 默认值：最近 30 天
  form.endDate = todayString()
  form.startDate = daysAgoString(30)

  // 从入口跳转过来时可以带账本名预填补充问题，方便用户接着想
  const bookName = query?.bookName ? decodeURIComponent(String(query.bookName)) : ''
  if (bookName) {
    form.question = `请重点分析账本「${bookName}」的支出结构。`
  }

  void loadBooks()
  void loadHistory()
})
</script>

<style scoped lang="scss">
.bill-analysis-page {
  padding-bottom: 60rpx;
}

.bill-analysis-head {
  background:
    radial-gradient(circle at top right, var(--color-primary-soft), transparent 34%),
    linear-gradient(135deg, var(--color-surface) 0%, var(--color-surface) 100%);
}

.filter-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18rpx 0;
  border-bottom: 1rpx solid var(--color-border);
}

.filter-row__label {
  color: var(--color-text-secondary);
  font-size: 26rpx;
}

.filter-row__value {
  color: var(--color-text-primary);
  font-size: 28rpx;
  font-weight: 600;
}

.filter-row__value--muted {
  color: var(--color-text-muted);
  font-weight: 400;
}

.question-block {
  margin-top: 18rpx;
}

.question-input {
  width: 100%;
  min-height: 140rpx;
  margin-top: 12rpx;
  padding: 20rpx;
  border-radius: 18rpx;
  background: var(--color-surface);
  border: 1rpx solid var(--color-border-strong);
  box-sizing: border-box;
  /* 用户输入内容 → 中性色，不带主题色调 */
  color: var(--color-text-neutral);
  font-size: 26rpx;
  line-height: 1.6;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 24rpx;
}

.summary-text {
  color: var(--color-text-primary);
  font-size: 28rpx;
  line-height: 1.8;
  white-space: pre-wrap;
}

.insight-block {
  margin-top: 18rpx;

  &:first-child {
    margin-top: 0;
  }
}

.insight-block__title {
  color: var(--color-text-primary);
  font-size: 28rpx;
  font-weight: 700;
  margin-bottom: 10rpx;
}

.insight-block__title--warn {
  color: var(--color-danger);
}

.insight-block__title--good {
  color: var(--color-success);
}

.insight-item {
  color: var(--color-text-primary);
  font-size: 26rpx;
  line-height: 1.8;
}

.category-row {
  display: flex;
  align-items: center;
  padding: 14rpx 0;
  border-bottom: 1rpx solid var(--color-border);

  &:last-child {
    border-bottom: none;
  }
}

.category-row__name {
  flex: 1;
  color: var(--color-text-primary);
  font-size: 26rpx;
}

.category-row__amount {
  width: 180rpx;
  text-align: right;
  color: var(--color-text-primary);
  font-size: 26rpx;
}

.category-row__ratio {
  width: 120rpx;
  text-align: right;
  color: var(--color-primary-strong);
  font-size: 24rpx;
  font-weight: 600;
}

.history-card {
  padding: 22rpx;
}

.history-card__question {
  margin-top: 12rpx;
  color: var(--color-text-secondary);
  font-size: 22rpx;
  line-height: 1.7;
}

.history-card__summary {
  margin-top: 12rpx;
  color: var(--color-text-primary);
  font-size: 26rpx;
  line-height: 1.7;
  // 历史里的 summary 可能很长，截两行；用户想看全的去重新分析
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

.metric-card__value--good {
  color: var(--color-success) !important;
}

.metric-card__value--warn {
  color: var(--color-danger) !important;
}
</style>
