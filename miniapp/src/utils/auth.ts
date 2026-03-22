import { tokenStorage } from './storage'

// 这些页面允许未登录直接进入。
const whiteList = ['/pages/home/index', '/pages/auth/login']

/**
 * 判断目标页面是否需要登录。
 */
function requiresAuth(url: string) {
  return !whiteList.some((path) => url.startsWith(path))
}

/**
 * 统一跳转到首页，由首页承接底部登录弹层。
 */
function redirectToHome() {
  uni.reLaunch({ url: '/pages/home/index' })
}

/**
 * 为页面跳转 API 生成统一的登录守卫拦截器。
 */
function buildInterceptor() {
  return {
    invoke(args: { url: string }) {
      if (requiresAuth(args.url) && !tokenStorage.getAccessToken()) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
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
