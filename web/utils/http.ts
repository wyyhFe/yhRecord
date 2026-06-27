import { getToken, fmtToken, removeToken } from './auth'

export const $http = {
  async request<T>(url: string, opts: RequestInit = {}): Promise<T> {
    const token = getToken()
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...((opts.headers as Record<string, string>) || {}),
    }
    if (token) headers.Authorization = fmtToken(token.accessToken)

    const res = await fetch(`/api${url}`, { ...opts, headers })

    if (res.status === 401) {
      removeToken()
      if (process.client) navigateTo('/login')
    }
    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    const json = await res.json()
    if (json.code && json.code !== 0)
      throw new Error(json.message || '请求失败')
    return json.data as T
  },
  get: <T>(url: string, params?: Record<string, any>) =>
    $http.request<T>(
      url + (params ? '?' + new URLSearchParams(params).toString() : ''),
    ),
  post: <T>(url: string, body?: any) =>
    $http.request<T>(url, {
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    }),
  put: <T>(url: string, body?: any) =>
    $http.request<T>(url, {
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    }),
  delete: <T>(url: string) => $http.request<T>(url, { method: 'DELETE' }),
}
