import { request } from '@/utils/request'
import type { Pagination } from '@/types/api'
import type { Id, MemorialDay } from '@/types/domain'

export interface MemorialDayPayload {
  title: string
  type: string
  memorialDate: string
  annualRepeat?: boolean
  remark?: string
  remindAt?: string
}

/**
 * 获取纪念日列表。
 */
export function fetchMemorialDays() {
  return request<Pagination<MemorialDay>>({
    url: '/memorial-days/list',
    method: 'GET'
  })
}

/**
 * 创建纪念日。
 */
export function createMemorialDay(data: MemorialDayPayload) {
  return request<MemorialDay>({
    url: '/memorial-days/create',
    method: 'POST',
    data
  })
}

/**
 * 更新纪念日。
 */
export function updateMemorialDay(id: Id, data: MemorialDayPayload) {
  return request<MemorialDay>({
    url: `/memorial-days/update/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除纪念日。
 */
export function deleteMemorialDay(id: Id) {
  return request<void>({
    url: `/memorial-days/delete/${id}`,
    method: 'DELETE'
  })
}
