import { request } from '@/utils/request'
import type { Id, LedgerEntry, LedgerYearStatistics } from '@/types/domain'

/**
 * 区间统计（周报/月报/年报共用）。
 */
export interface PeriodStatistics {
  startDate: string
  endDate: string
  type: string
  totalAmount: number
  dailyAverage: number
  previousTotal: number
  balance: number
  dailyTrend: Array<{ date: string; amount: number }>
  categories: Array<{ tagId?: Id; tagName: string; amount: number; ratio: number }>
}

/**
 * 获取区间统计。
 */
export function fetchPeriodStatistics(params: {
  startDate: string
  endDate: string
  type?: 'EXPENSE' | 'INCOME'
  bookId?: Id
}) {
  const parts = [
    `startDate=${params.startDate}`,
    `endDate=${params.endDate}`
  ]
  if (params.type) parts.push(`type=${params.type}`)
  if (params.bookId) parts.push(`bookId=${params.bookId}`)
  return request<PeriodStatistics>({
    url: `/ledger/statistics/period?${parts.join('&')}`,
    method: 'GET'
  })
}

/**
 * 获取某年某月的账单明细。
 * 月视图仍然走这个接口，精确按“年 + 月”拉取当前列表。
 */
export function fetchMonthLedger(year: number, month: number, bookId?: Id) {
  return request<LedgerEntry[]>({
    url: `/ledger/entries/month?year=${year}&month=${month}${bookId ? `&bookId=${bookId}` : ''}`,
    method: 'GET'
  })
}

/**
 * 获取某一整年的账单明细。
 * 这里复用后端 `/ledger/entries/month` 接口，但故意不传 `month`，
 * 让后端返回“这一整年”的明细，避免前端为了年视图连续调用 12 次接口。
 */
export function fetchYearLedger(year: number, bookId?: Id) {
  return request<LedgerEntry[]>({
    url: `/ledger/entries/month?year=${year}${bookId ? `&bookId=${bookId}` : ''}`,
    method: 'GET'
  })
}

/**
 * 获取某一年的账单标签统计。
 */
export function fetchYearStatistics(year: number, bookId?: Id) {
  return request<LedgerYearStatistics>({
    url: `/ledger/statistics/year?year=${year}${bookId ? `&bookId=${bookId}` : ''}`,
    method: 'GET'
  })
}
