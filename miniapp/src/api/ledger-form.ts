import { request } from '@/utils/request'
import type { Id } from '@/types/domain'

export interface LedgerEntryFormPayload {
  bookId: Id
  type: 'INCOME' | 'EXPENSE'
  amount: number
  entryDate: string
  remark?: string
  imagePath?: string
  tagIds?: Id[]
}

export function createLedgerEntry(data: LedgerEntryFormPayload) {
  return request({
    url: '/ledger/entries/create',
    method: 'POST',
    data
  })
}

export function updateLedgerEntry(id: Id, data: LedgerEntryFormPayload) {
  return request({
    url: `/ledger/entries/update/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteLedgerEntry(id: Id) {
  return request<void>({
    url: `/ledger/entries/delete/${id}`,
    method: 'DELETE'
  })
}
