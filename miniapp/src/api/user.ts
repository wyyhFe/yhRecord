import { request } from '@/utils/request'
import type { UserProfile, UserProfileUpdatePayload, PublicUserProfile, Id } from '@/types/domain'

/**
 * 获取当前登录用户资料。
 * 首页和个人中心会复用这份数据。
 */
export function fetchUserProfile() {
  return request<UserProfile>({
    url: '/users/profile',
    method: 'GET'
  })
}

/**
 * 更新当前登录用户资料。
 */
export function updateUserProfile(data: UserProfileUpdatePayload) {
  return request<UserProfile>({
    url: '/users/profile/update',
    method: 'PUT',
    data
  })
}

/**
 * 绑定手机号（微信小程序端，需已登录）。
 * code 来自 button open-type="getPhoneNumber" 回调。
 */
export function bindPhone(data: { code: string }) {
  return request<void>({
    url: '/auth/bind-phone',
    method: 'POST',
    data
  })
}

/**
 * 获取指定用户的公开资料（供他人主页展示）。
 */
export function fetchPublicUserProfile(userId: Id) {
  return request<PublicUserProfile>({
    url: `/users/public/${userId}`,
    method: 'GET'
  })
}
