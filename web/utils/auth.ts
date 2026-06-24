export interface TokenData {
  accessToken: string; refreshToken: string; expires: number
  username?: string; roles?: string[]
}
const KEY = 'lr-token'
export const getToken = (): TokenData | null => {
  if (process.server) return null
  try { const r = localStorage.getItem(KEY); return r ? JSON.parse(r) : null } catch { return null }
}
export const setToken = (d: { accessToken: string; refreshToken: string; expiresIn: number; username?: string }) => {
  if (process.server) return
  localStorage.setItem(KEY, JSON.stringify({ ...d, expires: Date.now() + d.expiresIn * 1000 }))
}
export const removeToken = () => { if (process.client) localStorage.removeItem(KEY) }
export const fmtToken = (t: string) => `Bearer ${t}`
