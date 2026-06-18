const TOKEN_KEY = 'life-record-token'
const REFRESH_TOKEN_KEY = 'life-record-refresh-token'
const TOKEN_EXPIRES_AT_KEY = 'life-record-token-expires-at'

/**
 * 登录态本地存储工具。
 * accessToken 和 refreshToken 分开管理，便于请求层单独读取和清理。
 */
export const tokenStorage = {
  /**
   * 读取 accessToken。
   */
  getAccessToken: () => uni.getStorageSync(TOKEN_KEY) as string,

  /**
   * 保存 accessToken。
   */
  setAccessToken: (token: string) => uni.setStorageSync(TOKEN_KEY, token),

  /**
   * 清理 accessToken。
   */
  clearAccessToken: () => uni.removeStorageSync(TOKEN_KEY),

  /**
   * 读取 refreshToken。
   */
  getRefreshToken: () => uni.getStorageSync(REFRESH_TOKEN_KEY) as string,

  /**
   * 保存 refreshToken。
   */
  setRefreshToken: (token: string) => uni.setStorageSync(REFRESH_TOKEN_KEY, token),

  /**
   * 清理 refreshToken。
   */
  clearRefreshToken: () => uni.removeStorageSync(REFRESH_TOKEN_KEY),

  /**
   * 读取 accessToken 过期时间戳（毫秒）。
   */
  getTokenExpiresAt: () => {
    const val = uni.getStorageSync(TOKEN_EXPIRES_AT_KEY)
    return typeof val === 'number' ? val : 0
  },

  /**
   * 保存 accessToken 过期时间戳（毫秒）。
   * 传入 expiresIn（秒），自动换算为绝对时间戳。
   */
  setTokenExpiresIn: (expiresIn: number) => {
    const expiresAt = Date.now() + expiresIn * 1000
    uni.setStorageSync(TOKEN_EXPIRES_AT_KEY, expiresAt)
  },

  /**
   * 一次性清空全部登录态。
   */
  clearAll: () => {
    uni.removeStorageSync(TOKEN_KEY)
    uni.removeStorageSync(REFRESH_TOKEN_KEY)
    uni.removeStorageSync(TOKEN_EXPIRES_AT_KEY)
  }
}
