<script setup lang="ts">
const collapsed = ref(true)
const route = useRoute()
const isDark = useDark()
const toggleDark = useToggle(isDark)
const { scrolled, ratio } = useScroll()

const nav = [
  { path: '/', icon: 'i-heroicons-home', label: '首页' },
  { path: '/diary', icon: 'i-heroicons-book-open', label: '日记' },
  { path: '/ledger', icon: 'i-heroicons-banknotes', label: '记账' },
  { path: '/checkin', icon: 'i-heroicons-check-badge', label: '打卡' },
  { path: '/memorial', icon: 'i-heroicons-calendar-days', label: '纪念日' },
  { path: '/memory', icon: 'i-heroicons-clock', label: '回忆' },
]

function active(p: string) { return p === '/' ? route.path === '/' : route.path.startsWith(p) }

function toggleTheme(e: MouseEvent) {
  const btn = e.currentTarget as HTMLElement
  const rect = btn.getBoundingClientRect()
  const x = rect.left + rect.width / 2
  const y = rect.top + rect.height / 2

  // 降级：不支持 View Transitions API 的浏览器直接切换
  if (!document.startViewTransition) {
    toggleDark()
    return
  }

  // 浏览器原生截图新旧状态 → 文字/边框/背景全部同步过渡，不闪
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
    <aside class="fixed left-0 top-0 h-full flex flex-col z-40 backdrop-blur-sm"
      :style="{ width: collapsed ? '64px' : '192px', transition: 'width 0.2s ease' }">
      <NuxtLink to="/" class="h-14 flex items-center justify-center shrink-0 transition-colors hover:bg-primary-50 dark:hover:bg-primary-900/20">
        <span class="text-xl">📝</span>
      </NuxtLink>

      <nav class="flex-1 flex flex-col justify-center py-2 px-1.5 space-y-1">
        <NuxtLink v-for="n in nav" :key="n.path" :to="n.path"
          class="flex items-center gap-3 px-2.5 py-3 rounded-lg text-sm transition-colors justify-center"
          :class="active(n.path) ? 'bg-primary-50 text-primary-600 dark:bg-primary-900/30 dark:text-primary-400' : 'text-gray-500 dark:text-gray-400 hover:bg-primary-50 dark:hover:bg-primary-900/20'">
          <Icon :name="n.icon" class="w-6 h-6 flex-shrink-0" />
          <span v-if="!collapsed" class="truncate text-xs">{{ n.label }}</span>
        </NuxtLink>
      </nav>

      <div class="py-2 px-1.5 shrink-0">
        <button @click="toggleTheme"
          class="w-full flex items-center justify-center p-2.5 rounded-lg text-gray-500 dark:text-gray-400 hover:bg-primary-50 dark:hover:bg-primary-900/20 transition-colors group">
          <Icon :name="isDark ? 'i-heroicons-sun' : 'i-heroicons-moon'" class="w-5 h-5" />
        </button>
      </div>
    </aside>

    <div class="shrink-0 transition-all duration-200" :class="collapsed ? 'w-16' : 'w-48'" />

    <!-- Main content -->
    <div class="flex-1 min-w-0 relative z-20">
      <!-- Floating Header -->
      <Teleport to="body">
        <header class="fixed top-0 right-0 z-30 transition-transform duration-300 glass"
          :class="scrolled ? 'translate-y-0' : '-translate-y-full'"
          :style="{ left: collapsed ? '64px' : '192px' }">
          <div class="flex items-center h-10 px-5">
            <span class="text-sm text-gray-500 dark:text-gray-400">{{ route.meta.title || route.name }}</span>
          </div>
          <div class="h-0.5 bg-gray-100 dark:bg-gray-800">
            <div class="h-full bg-primary-500 transition-transform origin-left" :style="{ transform: `scaleX(${ratio})` }" />
          </div>
        </header>
      </Teleport>

      <!-- Page -->
      <main class="min-h-[calc(100vh-4rem)]">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.glass { background: rgba(255,255,255,0.72); backdrop-filter: blur(12px); }
.dark .glass { background: rgba(15,23,42,0.72); }
</style>

<style>
/* View Transitions API — 主题切换波纹效果 */
/* 禁用默认 cross-fade 动画，只保留 JS animate() 驱动的 clip-path 扩散 */
::view-transition-old(root),
::view-transition-new(root) {
  animation: none;
  mix-blend-mode: normal;
}
::view-transition-new(root) { z-index: 9999; }
::view-transition-old(root) { z-index: 1; }
</style>
