<script setup lang="ts">
const collapsed = ref(false)
const route = useRoute()
const color = useColorMode()
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
</script>

<template>
  <div class="flex min-h-screen">
    <!-- Sidebar -->
    <aside class="fixed left-0 top-0 h-full bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-800 flex flex-col transition-all duration-200 z-40"
      :class="collapsed ? 'w-16' : 'w-56'">
      <!-- Logo -->
      <NuxtLink to="/" class="h-14 flex items-center justify-center border-b border-gray-200 dark:border-gray-800 shrink-0">
        <span v-if="!collapsed" class="font-semibold text-gray-800 dark:text-gray-100">📝 生活记录</span>
        <span v-else class="text-lg">📝</span>
      </NuxtLink>

      <!-- Nav -->
      <nav class="flex-1 py-3 px-2 space-y-0.5 overflow-y-auto">
        <NuxtLink v-for="n in nav" :key="n.path" :to="n.path"
          class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors"
          :class="active(n.path) ? 'bg-primary-50 text-primary-600 dark:bg-primary-900/30 dark:text-primary-400 font-medium' : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800'">
          <Icon :name="n.icon" class="w-5 h-5 flex-shrink-0" />
          <span v-if="!collapsed" class="truncate">{{ n.label }}</span>
        </NuxtLink>
      </nav>

      <!-- Footer -->
      <div class="py-3 px-2 border-t border-gray-200 dark:border-gray-800 shrink-0 space-y-0.5">
        <button @click="color.preference = color.value === 'dark' ? 'light' : 'dark'"
          class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
          <Icon :name="color.value === 'dark' ? 'i-heroicons-sun' : 'i-heroicons-moon'" class="w-5 h-5 flex-shrink-0" />
          <span v-if="!collapsed">{{ color.value === 'dark' ? '浅色' : '深色' }}</span>
        </button>
        <button @click="collapsed = !collapsed"
          class="w-full flex items-center justify-center py-2.5 rounded-lg text-sm text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
          <Icon :name="collapsed ? 'i-heroicons-chevron-right' : 'i-heroicons-chevron-left'" class="w-4 h-4" />
        </button>
      </div>
    </aside>

    <!-- Spacer -->
    <div class="shrink-0 transition-all duration-200" :class="collapsed ? 'w-16' : 'w-56'" />

    <!-- Main content -->
    <div class="flex-1 min-w-0">
      <!-- Floating Header -->
      <Teleport to="body">
        <header class="fixed top-0 right-0 z-30 transition-transform duration-300 border-b border-gray-200/50 dark:border-gray-800/50 glass"
          :class="scrolled ? 'translate-y-0' : '-translate-y-full'"
          :style="{ left: collapsed ? '64px' : '224px' }">
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
