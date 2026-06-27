/** 后端返回的原始路由节点 */
interface AsyncRouteVO {
  path: string
  name: string
  redirect?: string
  component?: string
  children?: AsyncRouteVO[]
  meta?: {
    title?: string
    icon?: string
    rank?: number
    roles?: string[]
    showLink?: boolean
  }
}

/** 前端菜单节点（树形，保留 children） */
export interface WebMenuItem {
  path: string
  name: string
  title: string
  icon: string
  rank: number
  requireAuth: boolean
  /** 实际导航链接（Nuxt 文件路由，可能不同于后端 path） */
  link: string
  redirect?: string
  children?: WebMenuItem[]
}

/**
 * 将后端路径映射到 web 端 Nuxt 页面路由。
 * 后端 admin 路径如 /diary/list，web 端页面在 /diary。
 */
function mapLink(from: string): string {
  // 带子路径的 → 去掉最后一段（/diary/list → /diary）
  const segments = from.split('/').filter(Boolean)
  if (segments.length >= 2) {
    const parent = '/' + segments.slice(0, -1).join('/')
    // 如果去掉后至少有两段（如 /ledger/books → /ledger），返回父级；否则保留原路径
    const parentSegments = parent.split('/').filter(Boolean)
    if (parentSegments.length >= 1) return parent
  }
  return from
}

/** 递归构建菜单树 */
function buildMenuTree(routes: AsyncRouteVO[]): WebMenuItem[] {
  return routes
    .filter((r) => r.meta?.showLink !== false)
    .map((r) => {
      const item: WebMenuItem = {
        path: r.path,
        name: r.name || '',
        title: r.meta?.title || '',
        icon: r.meta?.icon || '',
        rank: r.meta?.rank ?? 0,
        requireAuth: !!(r.meta?.roles && r.meta.roles.length > 0),
        link: mapLink(r.redirect || r.path),
        redirect: r.redirect,
        children: r.children
          ? buildMenuTree(r.children.filter((c) => c.meta?.showLink !== false))
          : undefined,
      }
      if (item.children && item.children.length === 0) {
        item.children = undefined
      }
      return item
    })
}

/** 递归收集所有节点路径 */
function collectPaths(nodes: WebMenuItem[]): string[] {
  return nodes.flatMap((n) => {
    const paths = [n.path]
    if (n.children && n.children.length > 0) {
      paths.push(...collectPaths(n.children))
    }
    return paths
  })
}

export const useMenuStore = defineStore('menu', () => {
  const menus = ref<WebMenuItem[]>([])
  const fetched = ref(false)

  async function fetchMenus() {
    if (fetched.value || menus.value.length > 0) return
    try {
      const raw = await $http.get<AsyncRouteVO[]>('/system/menu/get-async-routes?platform=WEB')
      menus.value = buildMenuTree(raw)
      fetched.value = true
    } catch {
      fetched.value = true
    }
  }

  /**
   * 判断路径是否需要登录。
   * 先精确匹配，再前缀降级（子路由继承父路由的 requireAuth）。
   */
  function requireAuth(path: string): boolean {
    if (path === '/') return false
    const segments = path.split('/').filter(Boolean)
    // 前缀降级
    while (segments.length >= 1) {
      const prefix = '/' + segments.join('/')
      if (_authCache.value.has(prefix)) {
        return true
      }
      segments.pop()
    }
    return false
  }

  // 内部缓存：所有 requireAuth === true 的路径（含子节点），供 requireAuth 快速查询
  const _authCache = computed<Set<string>>(() => {
    const set = new Set<string>()
    function walk(nodes: WebMenuItem[]) {
      for (const n of nodes) {
        if (n.requireAuth) {
          set.add(n.path)
          // 子路由继承父路由的 requireAuth
          if (n.children) {
            for (const c of collectPaths(n.children)) set.add(c)
          }
        } else if (n.children) {
          // 父不要求登录，但子节点可能有自己的 requireAuth
          walk(n.children)
        }
      }
    }
    walk(menus.value)
    return set
  })

  return { menus, fetched, fetchMenus, requireAuth }
})
