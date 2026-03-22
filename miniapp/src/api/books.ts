import { request } from '@/utils/request'

/**
 * 账本列表项。
 */
export interface LedgerBook {
  id: number
  name: string
  description?: string
}

/**
 * 创建账本请求体。
 */
export interface CreateLedgerBookPayload {
  name: string
  description?: string
}

/**
 * 获取当前用户的账本列表。
 */
export function fetchBooks() {
  return request<LedgerBook[]>({
    url: '/books/list',
    method: 'GET'
  })
}

/**
 * 创建账本。
 */
export function createBook(data: CreateLedgerBookPayload) {
  return request<LedgerBook>({
    url: '/books/create',
    method: 'POST',
    data
  })
}
