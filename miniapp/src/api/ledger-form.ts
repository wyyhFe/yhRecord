import { request } from '@/utils/request'

export interface LedgerEntryFormPayload {
  bookId: number
  type: 'INCOME' | 'EXPENSE'
  amount: number
  entryDate: string
  remark?: string
  imagePath?: string
  tagIds?: number[]
}

export function createLedgerEntry(data: LedgerEntryFormPayload) {
  return request({
    url: '/ledger/entries/create',
    method: 'POST',
    data
  })
}

export function updateLedgerEntry(id: number, data: LedgerEntryFormPayload) {
  return request({
    url: `/ledger/entries/update/${id}`,
    method: 'PUT',
    data
  })
}
