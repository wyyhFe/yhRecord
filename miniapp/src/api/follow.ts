import { request } from '@/utils/request'
import type { Id } from '@/types/domain'

/**
 * 关注用户。
 */
export function followUser(targetUserId: Id) {
  return request<void>({
    url: `/follow/${targetUserId}`,
    method: 'POST'
  })
}

/**
 * 取消关注。
 */
export function unfollowUser(targetUserId: Id) {
  return request<void>({
    url: `/follow/${targetUserId}`,
    method: 'DELETE'
  })
}

/**
 * 查询是否已关注。
 */
export function fetchFollowStatus(targetUserId: Id) {
  return request<{ following: boolean }>({
    url: `/follow/status/${targetUserId}`,
    method: 'GET'
  })
}

/**
 * 获取关注/粉丝数。
 */
export function fetchFollowCounts(userId: Id) {
  return request<{ following: number; followers: number }>({
    url: `/follow/counts/${userId}`,
    method: 'GET'
  })
}
