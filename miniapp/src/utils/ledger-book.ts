import type { Id } from '@/types/domain'

const LAST_LEDGER_BOOK_KEY = 'life-record:last-ledger-book'

export interface LastLedgerBook {
  id: Id
  name: string
}

export function getLastLedgerBook(): LastLedgerBook | null {
  const value = uni.getStorageSync(LAST_LEDGER_BOOK_KEY)
  if (!value || typeof value !== 'object') return null

  const id = String((value as Record<string, unknown>).id || '')
  const name = String((value as Record<string, unknown>).name || '')
  if (!id || !name) return null

  return { id, name }
}

export function setLastLedgerBook(book: LastLedgerBook) {
  uni.setStorageSync(LAST_LEDGER_BOOK_KEY, book)
}

export function clearLastLedgerBook() {
  uni.removeStorageSync(LAST_LEDGER_BOOK_KEY)
}
