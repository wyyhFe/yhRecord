/**
 * 全局路由守卫 — C 端认证中间件。
 *
 * 逻辑：
 * 1. 无需登录的路由 → 直接放行
 * 2. 需要登录的路由 → 已登录放行，未登录跳转 /login
 *
 * 支持子路由继承父路由的 requireAuth：
 * - /diary/write 继承 /diary 的 requireAuth
 * - /diary/123  继承 /diary 的 requireAuth
 */
export default defineNuxtRouteMiddleware(async (to) => {
  // 服务端跳过鉴权（token 和菜单都在客户端存储）
  if (process.server) return

  // 登录页、回调页直接放行
  if (to.path === '/login' || to.path.startsWith('/auth/')) return

  const authStore = useAuthStore()
  const menuStore = useMenuStore()

  // 加载菜单数据（幂等，只有首次会真正请求）
  await menuStore.fetchMenus()

  // 检查当前路由是否需要登录
  if (!menuStore.requireAuth(to.path)) return

  // 需要登录但未登录 → 跳转登录页
  if (!authStore.logged) {
    return navigateTo({ path: '/login', query: { redirect: to.fullPath } })
  }
})
