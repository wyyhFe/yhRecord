import { request } from '@/utils/request'

/**
 * 登录接口返回的会话信息。
 * 前端拿到它之后会把 accessToken、refreshToken、sessionId 存入本地。
 */
export interface LoginResult {
  userId: number
  openid: string
  accessToken: string
  refreshToken: string
  sessionId: string
}

/**
 * 小程序登录。
 * 前端先调用 uni.login 拿 code，再把 code 传给后端换取业务 token。
 */
export function wxLogin(code: string) {
  return request<LoginResult>({
    url: '/auth/wx-login',
    method: 'POST',
    data: { code },
    withAuth: false
  })
}

/**
 * 手动刷新 token。
 * 正常情况下请求层会自动调用，这里保留给业务层显式使用。
 */
export function refreshToken(refreshTokenValue: string) {
  return request<LoginResult>({
    url: '/auth/refresh',
    method: 'POST',
    data: {
      refreshToken: refreshTokenValue
    },
    withAuth: false
  })
}
