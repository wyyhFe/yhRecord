import { tokenStorage } from './storage'

/**
 * 无需登录态即可访问的页面白名单。
 * 静默登录模式下，首页承担了登录入口的职责，
 * 所以白名单只保留首页本身。
 */
const whiteList = ['/pages/home/index']

function requiresAuth(url: string) {
  return !whiteList.some((path) => url.startsWith(path))
}

function buildInterceptor() {
  return {
    invoke(args: { url: string }) {
      if (requiresAuth(args.url) && !tokenStorage.getAccessToken()) {
        // 静默登录尚未完成，跳回首页等待 ensureSession 完成
        uni.reLaunch({ url: '/pages/home/index' })
        return false
      }
      return args
    }
  }
}

/**
 * 在应用启动时注册路由守卫，拦截未登录访问。
 */
export function registerRouteGuard() {
  uni.addInterceptor('navigateTo', buildInterceptor())
  uni.addInterceptor('redirectTo', buildInterceptor())
  uni.addInterceptor('reLaunch', buildInterceptor())
  uni.addInterceptor('switchTab', buildInterceptor())
}

/**
 * 首屏进入时兜底检查一次当前页面是否需要登录态。
 * 如果没有 token 则跳回首页（首页会触发 ensureSession 静默登录）。
 */
export function ensureEntryAuth() {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1] as { route?: string } | undefined
  const route = current?.route ? `/${current.route}` : '/pages/home/index'
  if (requiresAuth(route) && !tokenStorage.getAccessToken()) {
    uni.reLaunch({ url: '/pages/home/index' })
  }
}