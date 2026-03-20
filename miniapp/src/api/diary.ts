import { request } from '@/utils/request'
import type { Pagination } from '@/types/api'
import type { DiaryItem } from '@/types/domain'
import type { CreateDiaryPayload } from '@/types/diary'

/**
 * 日记列表查询。
 * 当前支持关键字和可见范围两个常用过滤条件。
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
 * 更新指定日记。
 */
export function updateDiary(id: number, data: CreateDiaryPayload) {
  return request<DiaryItem>({
    url: `/diaries/update/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 获取单篇日记详情。
 */
export function fetchDiaryDetail(id: number) {
  return request<DiaryItem>({
    url: `/diaries/detail/${id}`,
    method: 'GET'
  })
}
