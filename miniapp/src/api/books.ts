import { request } from '@/utils/request'
import type { Pagination } from '@/types/api'
import type { Id } from '@/types/domain'

/**
 * 账本列表项。
 */
export interface LedgerBook {
  id: Id
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
  return request<Pagination<LedgerBook>>({
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
