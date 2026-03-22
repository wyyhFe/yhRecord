import HttpRequest, { type HttpRequestConfig, type HttpResponse } from 'luch-request'
import { API_BASE_URL } from '@/config/app'
import type { ApiResponse } from '@/types/api'
import { tokenStorage } from './storage'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

interface RequestCustomConfig {
  withAuth?: boolean
  _retry?: boolean
}

/**
 * 项目统一请求参数。
 * 默认所有接口都带登录态，只有显式传入 withAuth: false 才跳过 token 注入。
 */
interface RequestOptions<T = unknown> {
  url: string
  method?: HttpMethod
  data?: T
  header?: Record<string, string>
  withAuth?: boolean
  custom?: RequestCustomConfig
}

/**
 * 刷新 token 接口最小返回结构。
 */
interface RefreshTokenResult {
  accessToken: string
  refreshToken: string
}

const http = new HttpRequest({
  baseURL: API_BASE_URL,
  timeout: 15000,
  header: {
    'Content-Type': 'application/json'
  }
})

let refreshingPromise: Promise<void> | null = null

/**
 * 登录态失效后统一清空本地会话，并回到首页，由首页承接登录弹层。
 */
function redirectToLogin() {
  tokenStorage.clearAll()
  uni.reLaunch({ url: '/pages/home/index' })
}

/**
 * 刷新 accessToken。
 */
async function refreshSessionToken(refreshTokenValue: string) {
  const response = await http.request<ApiResponse<RefreshTokenResult>>({
    url: '/auth/refresh',
    method: 'POST',
    data: {
      refreshToken: refreshTokenValue
    },
    custom: {
      withAuth: false
    }
  })

  const result = response.data
  if (!result || result.code !== 0 || !result.data) {
    throw new Error(result?.message || '刷新登录状态失败')
  }

  tokenStorage.setAccessToken(result.data.accessToken)
  tokenStorage.setRefreshToken(result.data.refreshToken)
}

http.interceptors.request.use((config) => {
  const nextConfig = config as HttpRequestConfig & {
    custom?: RequestCustomConfig
  }

  nextConfig.header = {
    'Content-Type': 'application/json',
    ...(nextConfig.header ?? {})
  }

  const withAuth = nextConfig.custom?.withAuth !== false
  const token = tokenStorage.getAccessToken()
  if (withAuth && token) {
    nextConfig.header.Authorization = `Bearer ${token}`
  }

  console.log('[request] prepare', {
    url: nextConfig.url,
    method: nextConfig.method ?? 'GET',
    withAuth,
    hasToken: Boolean(token),
    authorization: nextConfig.header.Authorization
  })

  return nextConfig
})

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const response = error as HttpResponse<ApiResponse<unknown>>
    const statusCode = response?.statusCode
    const config = (response?.config ?? {}) as HttpRequestConfig & {
      custom?: RequestCustomConfig
    }

    if (statusCode === 401 && !config.custom?._retry) {
      console.warn('[request] got 401, try refresh token', {
        url: config.url,
        method: config.method ?? 'GET'
      })

      config.custom = {
        ...(config.custom ?? {}),
        _retry: true,
        withAuth: config.custom?.withAuth
      }

      const refreshValue = tokenStorage.getRefreshToken()
      if (!refreshValue) {
        console.warn('[request] no refresh token, redirect to login')
        redirectToLogin()
        throw new Error('登录已失效')
      }

      if (!refreshingPromise) {
        refreshingPromise = refreshSessionToken(refreshValue)
          .catch((refreshError) => {
            console.error('[request] refresh token failed', refreshError)
            redirectToLogin()
            throw refreshError
          })
          .finally(() => {
            refreshingPromise = null
          })
      }

      await refreshingPromise

      console.log('[request] replay original request after refresh', {
        url: config.url,
        method: config.method ?? 'GET'
      })

      return http.request(config)
    }

    const message = response?.data?.message || response?.errMsg || '网络异常'
    console.error('[request] request failed', {
      url: config.url,
      method: config.method ?? 'GET',
      statusCode,
      message
    })
    throw new Error(message)
  }
)

/**
 * 项目统一请求入口。
 * 这里只返回后端 data 字段，业务码非 0 时直接抛错。
 */
export async function request<T = unknown>(options: RequestOptions) {
  const response = await http.request<ApiResponse<T>>({
    url: options.url,
    method: options.method,
    data: options.data as Record<string, unknown> | undefined,
    header: options.header,
    custom: {
      ...(options.custom ?? {}),
      withAuth: options.withAuth
    }
  })

  const result = response.data
  if (!result) {
    throw new Error('响应为空')
  }

  if (result.code !== 0) {
    console.error('[request] business failed', {
      url: options.url,
      method: options.method ?? 'GET',
      code: result.code,
      message: result.message
    })
    throw new Error(result.message || '请求失败')
  }

  return result.data
}
