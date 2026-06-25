import { tokenStorage } from './storage'

/**
 * 无需登录态即可访问的页面白名单。
 */
const whiteList = ['/pages/home/index']

function requiresAuth(url: string) {
  return !whiteList.some((path) => url.startsWith(path))
}

function buildInterceptor() {
  return {
    invoke(args: { url: string }) {
      if (requiresAuth(args.url) && !tokenStorage.getAccessToken()) {
        uni.reLaunch({ url: '/pages/home/index' })
        return false
      }
      return args
    }
  }
}

/**
 * 注册路由守卫拦截器。
 */
export function registerRouteGuard() {
  uni.addInterceptor('navigateTo', buildInterceptor())
  uni.addInterceptor('redirectTo', buildInterceptor())
  uni.addInterceptor('reLaunch', buildInterceptor())
  uni.addInterceptor('switchTab', buildInterceptor())
}

/**
 * 首屏进入时兜底检查。
 */
export function ensureEntryAuth() {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1] as { route?: string } | undefined
  const route = current?.route ? `/${current.route}` : '/pages/home/index'
  if (requiresAuth(route) && !tokenStorage.getAccessToken()) {
    uni.reLaunch({ url: '/pages/home/index' })
  }
}
