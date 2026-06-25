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
  expiresIn: number
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
function redirectToHome() {
  tokenStorage.clearAll()
  uni.reLaunch({ url: '/pages/home/index' })
}

/**
 * 静默无感重登：调微信 wx.login 拿 code，再调 /auth/wx-login 换 token。
 *
 * 不引用 session.ts 来避免循环依赖：
 *   request.ts → session.ts → api/auth.ts → request.ts
 *
 * 也不使用动态 import()，因为 uni-app 打包器会把
 *   await import('./session')
 * 编译成字符串 "./session.js"（一个无意义的字面量），
 * 导致解构得到 undefined → 调用时抛出 TypeError。
 *
 * 这里直接内联实现，用局部的 http 实例和 tokenStorage 完成。
 */
async function silentReLogin(): Promise<boolean> {
  try {
    const loginRes = await uni.login({ provider: 'weixin' })
    const code = loginRes.code || ''
    if (!code) {
      throw new Error('Missing WeChat login code')
    }

    const response = await http.request<ApiResponse<RefreshTokenResult>>({
      url: '/auth/wx-login',
      method: 'POST',
      data: { code },
      custom: { withAuth: false }
    })

    const result = response.data
    if (!result || result.code !== 0 || !result.data) {
      throw new Error(result?.message || '静默登录失败')
    }

    tokenStorage.setAccessToken(result.data.accessToken)
    tokenStorage.setRefreshToken(result.data.refreshToken)
    tokenStorage.setTokenExpiresIn(result.data.expiresIn)
    return true
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
  tokenStorage.setTokenExpiresIn(result.data.expiresIn)
}

/**
 * accessToken 还有 N 秒过期？
 * 提前 3 分钟（180s）刷新，避免请求发到后端时已过期。
 */
function getTokenRemainingSeconds(): number {
  const expiresAt = tokenStorage.getTokenExpiresAt()
  if (!expiresAt) return 0
  return Math.max(0, Math.floor((expiresAt - Date.now()) / 1000))
}

/** 提前刷新的阈值（秒），accessToken 剩余时间不足此值时主动续签 */
const PROACTIVE_REFRESH_THRESHOLD_SECONDS = 180

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

http.interceptors.request.use(async (config) => {
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

  // ─── 主动续签：accessToken 快过期但 refreshToken 还有效时，提前刷新 ───
  if (
    withAuth &&
    token &&
    getTokenRemainingSeconds() < PROACTIVE_REFRESH_THRESHOLD_SECONDS &&
    !nextConfig.custom?._retry
  ) {
    const refreshValue = tokenStorage.getRefreshToken()
    if (refreshValue) {
      if (!refreshingPromise) {
        refreshingPromise = refreshSessionToken(refreshValue)
          .then(() => {
            // 刷新成功后重新读取最新的 accessToken 写入本次请求 header
            const freshToken = tokenStorage.getAccessToken()
            if (freshToken) {
              nextConfig.header!.Authorization = `Bearer ${freshToken}`
            }
          })
          .catch((err) => {
            console.warn('[request] proactive refresh failed, will retry on 401', err)
          })
          .finally(() => {
            refreshingPromise = null
          })
      }
      await refreshingPromise
    }
  }

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
          console.error('[request] all recovery failed, redirecting to home')
          uni.$feedback?.info?.('登录已失效，正在重新登录')
          redirectToHome()
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
