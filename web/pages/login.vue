<script setup lang="ts">
definePageMeta({ layout: false })
useHead({ title: '登录' })

function goHome() {
  navigateTo('/')
}

const providers = [
  {
    id: 'github',
    label: 'GitHub',
    icon: 'i-ri-github-fill',
    color: 'hover:text-gray-900 dark:hover:text-white',
  },
  {
    id: 'google',
    label: 'Google',
    icon: 'i-ri-google-fill',
    color: 'hover:text-blue-500',
  },
]

function handleOAuth(provider: string) {
  window.location.href = `/api/auth/${provider}/authorize`
}
</script>

<template>
  <div
    class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-950 px-4"
  >
    <div class="w-full max-w-sm">
      <div class="text-center mb-8">
        <span class="text-4xl">📝</span>
        <h1 class="mt-3 text-xl font-semibold text-gray-900 dark:text-white">
          登录生活记录
        </h1>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
          选择一种方式登录
        </p>
      </div>

      <!-- OAuth 登录按钮 -->
      <div class="space-y-3">
        <!-- 微信登录 -->
        <button
          class="w-full flex items-center justify-center gap-3 py-3 px-4 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 hover:bg-green-50 dark:hover:bg-green-900/20 hover:border-green-300 transition-colors text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-green-600"
          @click="handleOAuth('wechat')"
        >
          <span class="text-xl text-green-500">💬</span>
          微信登录
        </button>

        <!-- GitHub -->
        <button
          v-for="p in providers"
          :key="p.id"
          class="w-full flex items-center justify-center gap-3 py-3 px-4 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors text-sm font-medium text-gray-700 dark:text-gray-300"
          :class="p.color"
          @click="handleOAuth(p.id)"
        >
          <Icon :name="p.icon" class="w-5 h-5" />
          {{ p.label }} 登录
        </button>
      </div>

      <div class="mt-6 text-center">
        <button
          class="text-sm text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
          @click="goHome"
        >
          暂不登录，返回首页
        </button>
      </div>
    </div>
  </div>
</template>
