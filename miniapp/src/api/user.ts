import { request } from '@/utils/request'
import type { UserProfile } from '@/types/domain'

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
