import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import mpAdapter from 'axios-miniprogram-adapter'
import { API_BASE_URL } from '@/config/app'
import type { ApiResponse } from '@/types/api'
import { tokenStorage } from './storage'

/**
 * 项目统一请求参数。
 * 默认所有接口都走登录态，只有显式传 `withAuth: false` 才跳过 token 注入。
 */
interface RequestOptions<T = unknown> extends AxiosRequestConfig<T> {
  withAuth?: boolean
}

/**
 * 刷新 token 接口返回的最小结构。
 * 请求层只关心 accessToken 和 refreshToken。
 */
interface RefreshTokenResult {
  accessToken: string
  refreshToken: string
}

/**
 * uni-app 小程序环境下的 axios 实例。
 */
const service: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  adapter: mpAdapter as never
})

// 用一个共享 Promise 串起刷新流程，避免多个 401 同时触发多次 refresh。
let refreshingPromise: Promise<void> | null = null

/**
 * 跳回登录页前统一清空本地会话。
 */
function redirectToLogin() {
  tokenStorage.clearAll()
  uni.reLaunch({ url: '/pages/auth/login' })
}

/**
 * 这里不走 `@/api/auth`，而是直接发原始请求。
 * 目的是切断 `request -> auth api -> request` 的循环依赖。
 */
async function refreshSessionToken(refreshTokenValue: string) {
  const response = await axios.request<ApiResponse<RefreshTokenResult>>({
    baseURL: API_BASE_URL,
    url: '/auth/refresh',
    method: 'POST',
    timeout: 15000,
    adapter: mpAdapter as never,
    data: {
      refreshToken: refreshTokenValue
    }
  })

  const result = response.data
  if (!result || result.code !== 0 || !result.data) {
    throw new Error(result?.message || '刷新登录态失败')
  }

  tokenStorage.setAccessToken(result.data.accessToken)
  tokenStorage.setRefreshToken(result.data.refreshToken)
}

service.interceptors.request.use((config) => {
  const nextConfig = { ...config }
  nextConfig.headers = nextConfig.headers ?? {}

  // 默认所有接口都带 token，只有明确声明 withAuth=false 才跳过。
  const withAuth = (nextConfig as RequestOptions).withAuth !== false
  const token = tokenStorage.getAccessToken()
  if (withAuth && token) {
    nextConfig.headers.Authorization = `Bearer ${token}`
  }
  return nextConfig
})

service.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error?.response?.status
    const originalRequest = error?.config as (RequestOptions & { _retry?: boolean }) | undefined

    // 401 时尝试刷新 token，并在成功后自动重放原请求。
    if (status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true
      const refreshValue = tokenStorage.getRefreshToken()
      if (!refreshValue) {
        redirectToLogin()
        return Promise.reject(new Error('登录已失效'))
      }

      if (!refreshingPromise) {
        refreshingPromise = refreshSessionToken(refreshValue)
          .catch((refreshError) => {
            // refresh 失败时直接清理会话，避免前端继续拿失效状态请求。
            redirectToLogin()
            throw refreshError
          })
          .finally(() => {
            refreshingPromise = null
          })
      }

      await refreshingPromise
      return service.request(originalRequest)
    }

    const message = error?.response?.data?.message || error?.message || '网络异常'
    return Promise.reject(new Error(message))
  }
)

/**
 * 项目统一请求入口。
 * 这里会自动解包后端统一返回体，只把真正业务数据返回给调用方。
 */
export async function request<T = unknown>(options: RequestOptions) {
  const response = await service.request<ApiResponse<T>, AxiosResponse<ApiResponse<T>>>(options)
  const result = response.data
  if (!result) {
    throw new Error('Empty response')
  }
  if (result.code !== 0) {
    throw new Error(result.message || 'Request failed')
  }
  return result.data
}
