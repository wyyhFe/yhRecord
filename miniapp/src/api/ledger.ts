import { request } from '@/utils/request'
import type { LedgerEntry } from '@/types/domain'

/**
 * 获取某年某月的账单列表。
 * 当前页面先按月拉取简版流水数据。
 */
export function fetchMonthLedger(year: number, month: number) {
  return request<LedgerEntry[]>({
    url: `/ledger/entries/month?year=${year}&month=${month}`,
    method: 'GET'
  })
}
