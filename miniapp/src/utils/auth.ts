import { tokenStorage } from './storage'

const whiteList = ['/pages/home/index', '/pages/auth/login']

function requiresAuth(url: string) {
  return !whiteList.some((path) => url.startsWith(path))
}

function redirectToHome() {
  uni.reLaunch({ url: '/pages/home/index' })
}

function buildInterceptor() {
  return {
    invoke(args: { url: string }) {
      if (requiresAuth(args.url) && !tokenStorage.getAccessToken()) {
        uni.$feedback.info('请先登录')
        redirectToHome()
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
 * 首屏进入时兜底检查一次当前页面是否需要登录。
 */
export function ensureEntryAuth() {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1] as { route?: string } | undefined
  const route = current?.route ? `/${current.route}` : '/pages/home/index'
  if (requiresAuth(route) && !tokenStorage.getAccessToken()) {
    redirectToHome()
  }
}
