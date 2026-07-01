import { request } from '@/utils/request'
import type { Pagination } from '@/types/api'
import type { DiaryItem, Id } from '@/types/domain'
import type { CreateDiaryPayload } from '@/types/diary'

/**
 * 查询日记列表。
 */
export function fetchDiaryList(params?: {
  keyword?: string
  visibility?: 'PRIVATE' | 'SHARED' | 'PUBLIC'
  current?: number
  size?: number
}) {
  // 小程序运行环境没有 URLSearchParams，这里手动拼接查询串。
  const queryEntries: Array<[string, string]> = [
    ['current', String(params?.current ?? 1)],
    ['size', String(params?.size ?? 10)]
  ]
  if (params?.keyword) queryEntries.push(['keyword', params.keyword])
  if (params?.visibility) queryEntries.push(['visibility', params.visibility])
  const query = queryEntries
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')
  return request<Pagination<DiaryItem>>({
    url: `/diaries/list?${query}`,
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
export function updateDiary(id: Id, data: CreateDiaryPayload) {
  return request<DiaryItem>({
    url: `/diaries/update/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 查询日记详情。
 */
export function fetchDiaryDetail(id: Id) {
  return request<DiaryItem>({
    url: `/diaries/detail/${id}`,
    method: 'GET'
  })
}

/**
 * 软删除日记，删除后进入回收站。
 */
export function deleteDiary(id: Id) {
  return request<void>({
    url: `/diaries/delete/${id}`,
    method: 'DELETE'
  })
}

/**
 * 日记大厅：分页查询所有用户的公开日记。
 */
export function fetchDiaryHall(params?: {
  current?: number
  size?: number
}) {
  const query = `current=${params?.current ?? 1}&size=${params?.size ?? 10}`
  return request<Pagination<DiaryItem>>({
    url: `/diaries/hall?${query}`,
    method: 'GET'
  })
}

/**
 * 查询指定用户的公开日记列表。
 */
export function fetchUserPublicDiaries(userId: Id, params?: {
  current?: number
  size?: number
}) {
  const query = `current=${params?.current ?? 1}&size=${params?.size ?? 10}`
  return request<Pagination<DiaryItem>>({
    url: `/diaries/public/${userId}?${query}`,
    method: 'GET'
  })
}

/**
 * 查看公开日记详情。
 */
export function fetchPublicDiaryDetail(id: Id) {
  return request<DiaryItem>({
    url: `/diaries/public-detail/${id}`,
    method: 'GET'
  })
}

// ==================== 互动接口（点赞 + 评论） ====================

/**
 * 切换点赞/取消点赞。
 */
export function toggleLike(diaryId: Id) {
  return request<void>({
    url: `/diaries/${diaryId}/like`,
    method: 'POST'
  })
}

/**
 * 发表评论。
 */
export function addComment(diaryId: Id, data: { content: string; parentId?: Id }) {
  return request<void>({
    url: `/diaries/${diaryId}/comments`,
    method: 'POST',
    data
  })
}

/**
 * 查询评论列表（分页）。
 */
export function fetchComments(diaryId: Id, page: number = 1, size: number = 10) {
  return request<Pagination<DiaryComment>>({
    url: `/diaries/${diaryId}/comments?page=${page}&size=${size}`,
    method: 'GET'
  })
}

/**
 * 删除自己的评论。
 */
export function deleteComment(commentId: Id) {
  return request<void>({
    url: `/diaries/comments/${commentId}`,
    method: 'DELETE'
  })
}
