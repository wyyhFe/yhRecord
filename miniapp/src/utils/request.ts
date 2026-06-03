import HttpRequest, { type HttpRequestConfig, type HttpResponse } from 'luch-request'
import { API_BASE_URL } from '@/config/app'
import type { ApiResponse } from '@/types/api'
import { tokenStorage } from './storage'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

interface RequestCustomConfig {
  withAuth?: boolean
  _retry?: boolean
  _retryCount?: number
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

/** 请求失败时自动重试的最大次数 */
const MAX_RETRY_COUNT = 3

/** 重试间的退避延迟基数（毫秒），每次翻倍：1s → 2s → 4s */
const RETRY_BASE_DELAY_MS = 1000

const http = new HttpRequest({
  baseURL: API_BASE_URL,
  timeout: 15000,
  header: {
    'Content-Type': 'application/json'
  }
})

let refreshingPromise: Promise<void> | null = null

/**
 * 登录态彻底失效（refresh 也救不回 + 静默 wx.login 也失败）时调。
 * 清掉本地凭证，跳到登录页让用户手动操作。
 */
function redirectToLogin() {
  tokenStorage.clearAll()
  uni.reLaunch({ url: '/pages/auth/login' })
}

/**
 * 静默无感重登：调微信 wx.login 拿 code，再换 access/refresh token。
 * session.ts 已经实现了这套（带去重 lock），这里走同一份避免逻辑分裂。
 *
 * 用动态 import 避免循环依赖：
 *   request.ts → session.ts → api/auth.ts → request.ts
 * 模块初始化期不解析这条链，运行期 lazy 拿到。
 */
async function silentReLogin(): Promise<boolean> {
  try {
    const { ensureSession } = await import('./session')
    return await ensureSession()
  } catch (error) {
    console.error('[request] silent re-login failed', error)
    return false
  }
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

/**
 * 判断当前错误是否应该重试。
 * 可重试条件：网络错误（无 statusCode）或服务端 5xx 错误。
 * 不可重试：4xx 客户端错误（401/403/404 等）。
 */
function isRetryable(error: HttpResponse<ApiResponse<unknown>>): boolean {
  const statusCode = error?.statusCode
  // 无 statusCode = 网络/超时错误，可重试
  if (statusCode === undefined || statusCode === null) {
    return true
  }
  // 5xx 服务端错误可重试
  if (statusCode >= 500 && statusCode < 600) {
    return true
  }
  return false
}

/**
 * 延迟指定毫秒数。
 */
function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
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

    // ─── 自动重试逻辑（仅对可重试错误生效） ─────────────────────────────
    if (isRetryable(response)) {
      const retryCount = config.custom?._retryCount ?? 0
      if (retryCount < MAX_RETRY_COUNT) {
        const attempt = retryCount + 1
        const delay = RETRY_BASE_DELAY_MS * Math.pow(2, retryCount)
        console.warn('[request] retrying', {
          url: config.url,
          method: config.method ?? 'GET',
          attempt,
          maxRetries: MAX_RETRY_COUNT,
          delayMs: delay,
          statusCode
        })
        config.custom = {
          ...(config.custom ?? {}),
          _retryCount: attempt
        }
        await sleep(delay)
        return http.request(config)
      }
    }

    // ─── 401 鉴权恢复逻辑 ──────────────────────────────────────
    if (statusCode === 401 && !config.custom?._retry) {
      console.warn('[request] got 401, attempting recovery', {
        url: config.url,
        method: config.method ?? 'GET'
      })

      config.custom = {
        ...(config.custom ?? {}),
        _retry: true,
        withAuth: config.custom?.withAuth
      }

      // 三层兜底依次尝试：refresh → 静默 wx.login → 跳登录页
      // 全程对调用方"无感"，能救就救，救不回再跳。
      const refreshValue = tokenStorage.getRefreshToken()

      if (!refreshingPromise) {
        refreshingPromise = (async () => {
          // 第一层：用 refreshToken 换新 access（不需要走微信登录链路）
          if (refreshValue) {
            try {
              await refreshSessionToken(refreshValue)
              return
            } catch (refreshError) {
              console.warn('[request] refresh failed, falling back to silent wx.login', refreshError)
            }
          } else {
            console.warn('[request] no refresh token, falling back to silent wx.login')
          }

          // 第二层：refresh 没救回 → 自动 wx.login + /auth/wx-login（用户无感）
          tokenStorage.clearAll()
          const reLogged = await silentReLogin()
          if (reLogged) {
            return
          }

          // 第三层：彻底失败 → 跳登录页让用户手动登录
          console.error('[request] all recovery failed, redirecting to login')
          uni.$feedback?.info?.('登录已失效，请重新登录')
          redirectToLogin()
          throw new Error('登录已失效')
        })().finally(() => {
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
      message,
      retryAttempts: config.custom?._retryCount ?? 0
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
