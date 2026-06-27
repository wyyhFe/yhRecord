<script setup lang="ts">
definePageMeta({ layout: 'sidebar' })
useHead({ title: '首页' })

const modules = [
  { icon: '📝', label: '日记', desc: '记录每一天', path: '/diary' },
  { icon: '💰', label: '记账', desc: '收支管理', path: '/ledger' },
  { icon: '✅', label: '打卡', desc: '坚持习惯', path: '/checkin' },
  { icon: '📅', label: '纪念日', desc: '重要日子', path: '/memorial' },
  { icon: '📖', label: '回忆', desc: '那年今日', path: '/memory' },
]
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-10">
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
        👋 下午好
      </h1>
      <p class="text-gray-500 dark:text-gray-400 mt-1">
        今天是个值得记录的日子
      </p>
    </div>

    <!-- Module cards -->
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4 mb-10">
      <NuxtLink
        v-for="m in modules"
        :key="m.path"
        :to="m.path"
        class="backdrop-blur-sm rounded-xl p-5 transition-all hover:bg-primary-50 dark:hover:bg-primary-900/20 hover:shadow-md hover:-translate-y-0.5"
      >
        <span class="text-3xl">{{ m.icon }}</span>
        <h3 class="font-semibold text-gray-800 dark:text-gray-100 mt-3">
          {{ m.label }}
        </h3>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
          {{ m.desc }}
        </p>
      </NuxtLink>
    </div>

    <!-- Recent placeholder -->
    <div
      class="backdrop-blur-sm rounded-xl p-8 text-center text-gray-400 dark:text-gray-500 transition-colors hover:bg-primary-50 dark:hover:bg-primary-900/20"
    >
      最近日记 — 登录后显示
    </div>
  </div>
</template>
