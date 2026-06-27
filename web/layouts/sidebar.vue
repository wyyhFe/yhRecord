<script setup lang="ts">
const collapsed = ref(true)
const route = useRoute()
const isDark = useDark()
const toggleDark = useToggle(isDark)
const { scrolled, ratio } = useScroll()

const authStore = useAuthStore()
const menuStore = useMenuStore()

/** 当前 hover 的菜单 path */
const hovered = ref<string | null>(null)
let closeTimer: ReturnType<typeof setTimeout> | null = null

/** 延迟关闭子菜单（给鼠标移动到子面板留时间） */
function scheduleClose() {
  closeTimer = setTimeout(() => {
    hovered.value = null
  }, 150)
}
function cancelClose() {
  if (closeTimer) {
    clearTimeout(closeTimer)
    closeTimer = null
  }
}
function openMenu(path: string) {
  cancelClose()
  hovered.value = path
}
function closeMenu() {
  cancelClose()
  hovered.value = null
}

onMounted(() => {
  menuStore.fetchMenus()
})

function active(p: string) {
  return p === '/' ? route.path === '/' : route.path.startsWith(p)
}

const showLogin = ref(false)
function handleLogin() {
  showLogin.value = true
}
function handleLogout() {
  authStore.logout()
}

function toggleTheme(e: MouseEvent) {
  const btn = e.currentTarget as HTMLElement
  const rect = btn.getBoundingClientRect()
  const x = rect.left + rect.width / 2
  const y = rect.top + rect.height / 2
  if (!document.startViewTransition) {
    toggleDark()
    return
  }
  const transition = document.startViewTransition(() => {
    toggleDark()
  })
  transition.ready.then(() => {
    document.documentElement.animate(
      {
        clipPath: [
          `circle(0px at ${x}px ${y}px)`,
          `circle(${Math.hypot(Math.max(x, innerWidth - x), Math.max(y, innerHeight - y))}px at ${x}px ${y}px)`,
        ],
      },
      {
        duration: 300,
        easing: 'ease-out',
        pseudoElement: '::view-transition-new(root)',
      },
    )
  })
}
</script>

<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <aside
      class="fixed left-0 top-0 h-full flex flex-col z-40 backdrop-blur-sm"
      :style="{
        width: collapsed ? '64px' : '192px',
        transition: 'width 0.2s ease',
      }"
    >
      <!-- Logo -->
      <NuxtLink
        to="/"
        class="h-14 flex items-center justify-center shrink-0 transition-colors hover:bg-primary-50 dark:hover:bg-primary-900/20"
      >
        <span class="text-xl">📝</span>
      </NuxtLink>

      <!-- 导航菜单 -->
      <nav class="flex-1 flex flex-col justify-center py-2 px-1.5 space-y-1">
        <div
          v-for="m in menuStore.menus"
          :key="m.path"
          class="relative"
          @mouseenter="openMenu(m.path)"
          @mouseleave="scheduleClose"
        >
          <NuxtLink
            :to="m.link"
            class="flex items-center gap-3 px-2.5 py-3 rounded-lg text-sm transition-colors justify-center"
            :title="m.title"
            :class="
              active(m.path)
                ? 'bg-primary-50 text-primary-600 dark:bg-primary-900/30 dark:text-primary-400'
                : 'text-gray-500 dark:text-gray-400 hover:bg-primary-50 dark:hover:bg-primary-900/20'
            "
          >
            <Icon :name="m.icon" class="w-6 h-6 flex-shrink-0" />
            <span v-if="!collapsed" class="truncate text-xs">{{
              m.title
            }}</span>
          </NuxtLink>

          <!-- 子菜单 fly-out -->
          <div
            v-if="m.children?.length"
            class="absolute top-1/2 -translate-y-1/2 z-50 min-w-[140px] rounded-xl bg-white/95 dark:bg-gray-900/95 backdrop-blur-md border border-gray-100 dark:border-gray-800 shadow-lg shadow-gray-200/50 dark:shadow-black/30 py-1.5 transition-all duration-150"
            :style="{ left: 'calc(100% + 4px)' }"
            :class="
              hovered === m.path
                ? 'opacity-100 pointer-events-auto translate-x-0'
                : 'opacity-0 pointer-events-none -translate-x-1'
            "
            @mouseenter="cancelClose()"
            @mouseleave="closeMenu()"
          >
            <NuxtLink
              v-for="c in m.children"
              :key="c.path"
              :to="c.link"
              class="flex items-center gap-2 px-3.5 py-2 text-sm transition-colors rounded-lg mx-1 hover:bg-primary-50 dark:hover:bg-primary-900/20 hover:text-primary-600 dark:hover:text-primary-400"
              :class="
                active(c.path)
                  ? 'text-primary-600 dark:text-primary-400 bg-primary-50/50 dark:bg-primary-900/10'
                  : 'text-gray-600 dark:text-gray-300'
              "
            >
              <span
                class="w-1.5 h-1.5 rounded-full flex-shrink-0"
                :class="
                  active(c.path)
                    ? 'bg-primary-500'
                    : 'bg-gray-300 dark:bg-gray-600'
                "
              />
              <span class="truncate">{{ c.title }}</span>
            </NuxtLink>
          </div>
        </div>
      </nav>

      <!-- 底部操作区 -->
      <div class="py-2 px-1.5 shrink-0 space-y-1">
        <template v-if="authStore.logged">
          <button
            class="w-full flex items-center justify-center p-2.5 rounded-lg text-gray-500 dark:text-gray-400 hover:bg-primary-50 dark:hover:bg-primary-900/20 transition-colors group"
            :title="authStore.username || '已登录'"
          >
            <Icon
              name="i-heroicons-user-circle"
              class="w-5 h-5 text-primary-500"
            />
            <span
              v-if="!collapsed"
              class="ml-2 text-xs truncate max-w-[80px]"
              >{{ authStore.username || '已登录' }}</span
            >
          </button>
          <button
            class="w-full flex items-center justify-center p-2 rounded-lg text-gray-400 dark:text-gray-500 hover:text-red-500 dark:hover:text-red-400 transition-colors text-xs"
            title="退出登录"
            @click="handleLogout"
          >
            <Icon name="i-heroicons-arrow-right-on-rectangle" class="w-4 h-4" />
            <span v-if="!collapsed" class="ml-1">退出</span>
          </button>
        </template>
        <button
          v-else
          class="w-full flex items-center justify-center p-2.5 rounded-lg text-gray-400 dark:text-gray-500 hover:text-primary-600 dark:hover:text-primary-400 hover:bg-primary-50 dark:hover:bg-primary-900/20 transition-colors group"
          title="登录"
          @click="handleLogin"
        >
          <Icon name="i-heroicons-arrow-left-on-rectangle" class="w-5 h-5" />
          <span v-if="!collapsed" class="ml-2 text-xs">登录</span>
        </button>

        <button
          class="w-full flex items-center justify-center p-2.5 rounded-lg text-gray-500 dark:text-gray-400 hover:bg-primary-50 dark:hover:bg-primary-900/20 transition-colors group"
          @click="toggleTheme"
        >
          <Icon
            :name="isDark ? 'i-heroicons-sun' : 'i-heroicons-moon'"
            class="w-5 h-5"
          />
        </button>
      </div>
    </aside>

    <!-- 占位 -->
    <div
      class="shrink-0 transition-all duration-200"
      :class="collapsed ? 'w-16' : 'w-48'"
    />

    <!-- 主内容 -->
    <div class="flex-1 min-w-0 relative z-20">
      <Teleport to="body">
        <header
          class="fixed top-0 right-0 z-30 transition-transform duration-300 glass"
          :class="scrolled ? 'translate-y-0' : '-translate-y-full'"
          :style="{ left: collapsed ? '64px' : '192px' }"
        >
          <div class="flex items-center h-10 px-5">
            <span class="text-sm text-gray-500 dark:text-gray-400">{{
              route.meta.title || route.name
            }}</span>
          </div>
          <div class="h-0.5 bg-gray-100 dark:bg-gray-800">
            <div
              class="h-full bg-primary-500 transition-transform origin-left"
              :style="{ transform: `scaleX(${ratio})` }"
            />
          </div>
        </header>
      </Teleport>

      <main class="min-h-[calc(100vh-4rem)]">
        <slot />
      </main>
    </div>
  </div>

  <!-- 登录弹窗 -->
  <LoginDialog v-if="showLogin" @close="showLogin = false" />
</template>

<style scoped>
.glass {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
}
.dark .glass {
  background: rgba(15, 23, 42, 0.72);
}
</style>

<style>
::view-transition-old(root),
::view-transition-new(root) {
  animation: none;
  mix-blend-mode: normal;
}
::view-transition-new(root) {
  z-index: 9999;
}
::view-transition-old(root) {
  z-index: 1;
}
</style>
