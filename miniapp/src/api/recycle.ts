import { request } from '@/utils/request'
import type { Id } from '@/types/domain'

/**
 * 回收站条目。
 */
export interface RecycleBinItem {
  id: Id
  resourceType: 'DIARY' | 'LEDGER_ENTRY' | string
  resourceId: Id
  deletedAt: string
  expireAt: string
  title?: string
  subtitle?: string
}

/**
 * 查询回收站列表。
 */
export function fetchRecycleBinList() {
  return request<RecycleBinItem[]>({
    url: '/recycle-bin/list',
    method: 'GET'
  })
}

/**
 * 恢复回收站资源。
 */
export function restoreRecycleBinItem(recycleId: Id, resourceId: Id) {
  return request<void>({
    url: `/recycle-bin/restore/${recycleId}?resourceId=${resourceId}`,
    method: 'POST'
  })
}

/**
 * 彻底删除回收站资源。
 */
export function forceDeleteRecycleBinItem(recycleId: Id, resourceId: Id) {
  return request<void>({
    url: `/recycle-bin/force-delete/${recycleId}?resourceId=${resourceId}`,
    method: 'DELETE'
  })
}
