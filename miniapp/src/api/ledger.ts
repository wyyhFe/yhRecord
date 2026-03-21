import { request } from '@/utils/request'
import type { LedgerEntry } from '@/types/domain'

/**
 * 获取某年某月的账单列表。
 * 这是页面查询模块，用于查看当前月或指定月份的账单明细。
 */
export function fetchMonthLedger(year: number, month: number) {
  return request<LedgerEntry[]>({
    url: `/ledger/entries/month?year=${year}&month=${month}`,
    method: 'GET'
  })
}