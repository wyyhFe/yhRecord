const TOKEN_KEY = 'life-record-token'
const REFRESH_TOKEN_KEY = 'life-record-refresh-token'

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
   * 一次性清空全部登录态。
   */
  clearAll: () => {
    uni.removeStorageSync(TOKEN_KEY)
    uni.removeStorageSync(REFRESH_TOKEN_KEY)
  }
}
