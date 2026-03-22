import { request } from '@/utils/request'
import type { LedgerEntry, LedgerYearStatistics } from '@/types/domain'

/**
 * 获取某年某月的账单列表。
 */
export function fetchMonthLedger(year: number, month: number, bookId?: number) {
  return request<LedgerEntry[]>({
    url: `/ledger/entries/month?year=${year}&month=${month}${bookId ? `&bookId=${bookId}` : ''}`,
    method: 'GET'
  })
}

/**
 * 获取某一年的账单标签统计。
 */
export function fetchYearStatistics(year: number, bookId?: number) {
  return request<LedgerYearStatistics>({
    url: `/ledger/statistics/year?year=${year}${bookId ? `&bookId=${bookId}` : ''}`,
    method: 'GET'
  })
}
