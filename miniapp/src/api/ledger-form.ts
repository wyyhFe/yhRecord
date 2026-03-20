import { request } from '@/utils/request'

/**
 * 创建记账流水提交结构。
 */
export interface CreateLedgerEntryPayload {
  bookId: number
  type: 'INCOME' | 'EXPENSE'
  amount: number
  entryDate: string
  remark?: string
  imagePath?: string
  tagIds?: number[]
}

/**
 * 创建记账流水。
 */
export function createLedgerEntry(data: CreateLedgerEntryPayload) {
  return request({
    url: '/ledger/entries/create',
    method: 'POST',
    data
  })
}
