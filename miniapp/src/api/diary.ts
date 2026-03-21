import { request } from '@/utils/request'
import type { Pagination } from '@/types/api'
import type { DiaryItem } from '@/types/domain'
import type { CreateDiaryPayload } from '@/types/diary'

/**
 * 查询日记列表。
 */
export function fetchDiaryList(params?: {
  keyword?: string
  visibility?: 'PRIVATE' | 'SHARED' | 'PUBLIC'
}) {
  const query = new URLSearchParams({
    current: '1',
    size: '10'
  })
  if (params?.keyword) query.set('keyword', params.keyword)
  if (params?.visibility) query.set('visibility', params.visibility)
  return request<Pagination<DiaryItem>>({
    url: `/diaries/list?${query.toString()}`,
    method: 'GET'
  })
}

/**
 * 创建日记。
 */
export function createDiary(data: CreateDiaryPayload) {
  return request<DiaryItem>({
    url: '/diaries/create',
    method: 'POST',
    data
  })
}

/**
 * 更新日记。
 */
export function updateDiary(id: number, data: CreateDiaryPayload) {
  return request<DiaryItem>({
    url: `/diaries/update/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 查询日记详情。
 */
export function fetchDiaryDetail(id: number) {
  return request<DiaryItem>({
    url: `/diaries/detail/${id}`,
    method: 'GET'
  })
}

/**
 * 软删除日记，删除后进入回收站。
 */
export function deleteDiary(id: number) {
  return request<void>({
    url: `/diaries/delete/${id}`,
    method: 'DELETE'
  })
}
