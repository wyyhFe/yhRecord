<script setup lang="ts">
definePageMeta({ layout: false })
useHead({ title: '登录中...' })

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const error = ref('')

onMounted(async () => {
  const { accessToken, refreshToken, expiresIn, username, provider } =
    route.query

  if (!accessToken || !refreshToken) {
    error.value = '授权失败，未获取到登录信息'
    setTimeout(() => router.push('/login'), 2000)
    return
  }

  try {
    const expiresInSeconds = expiresIn ? Number(expiresIn) : 4 * 60 * 60

    authStore.login({
      accessToken: accessToken as string,
      refreshToken: refreshToken as string,
      expiresIn: expiresInSeconds,
      username: username as string | undefined,
    })

    // 跳回来源页或首页
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    error.value = '登录处理失败'
    setTimeout(() => router.push('/login'), 2000)
  }
})
</script>

<template>
  <div
    class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-950"
  >
    <div v-if="!error" class="text-center">
      <div
        class="inline-flex items-center justify-center w-14 h-14 rounded-full bg-primary-50 dark:bg-primary-900/30 mb-4"
      >
        <span class="text-2xl">🔄</span>
      </div>
      <p class="text-sm text-gray-500 dark:text-gray-400">正在处理登录...</p>
    </div>

    <div v-else class="text-center">
      <div
        class="inline-flex items-center justify-center w-14 h-14 rounded-full bg-red-50 dark:bg-red-900/30 mb-4"
      >
        <span class="text-2xl">❌</span>
      </div>
      <p class="text-sm text-red-500">{{ error }}</p>
      <NuxtLink
        to="/login"
        class="inline-block mt-3 text-sm text-primary-500 hover:underline"
      >
        重新登录
      </NuxtLink>
    </div>
  </div>
</template>
