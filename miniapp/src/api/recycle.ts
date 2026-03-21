import { request } from '@/utils/request'

/**
 * 回收站条目。
 */
export interface RecycleBinItem {
  id: number
  resourceType: 'DIARY' | string
  resourceId: number
  deletedAt: string
  expireAt: string
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
export function restoreRecycleBinItem(recycleId: number, resourceId: number) {
  return request<void>({
    url: `/recycle-bin/restore/${recycleId}?resourceId=${resourceId}`,
    method: 'POST'
  })
}

/**
 * 彻底删除回收站资源。
 */
export function forceDeleteRecycleBinItem(recycleId: number, resourceId: number) {
  return request<void>({
    url: `/recycle-bin/force-delete/${recycleId}?resourceId=${resourceId}`,
    method: 'DELETE'
  })
}