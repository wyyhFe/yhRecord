import { wxLogin } from '@/api/auth'
import { tokenStorage } from './storage'

let loginPromise: Promise<boolean> | null = null

export async function ensureSession() {
  if (tokenStorage.getAccessToken()) {
    return true
  }

  if (!loginPromise) {
    loginPromise = uni
      .login({ provider: 'weixin' })
      .then((loginRes) => {
        const code = loginRes.code || ''
        if (!code) {
          throw new Error('Missing WeChat login code')
        }
        return wxLogin(code)
      })
      .then((result) => {
        tokenStorage.setAccessToken(result.accessToken)
        tokenStorage.setRefreshToken(result.refreshToken)
        return true
      })
      .catch((error) => {
        console.error('[session] ensure session failed', error)
        return false
      })
      .finally(() => {
        loginPromise = null
      })
  }

  return loginPromise
}
