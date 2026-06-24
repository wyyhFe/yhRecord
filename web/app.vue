<script lang="ts" setup>
const { awesome } = useAppConfig()
useHead({
  title: '生活记录',
  titleTemplate: `%s · 生活记录`,
})
</script>

<template>
  <Body
    class="antialiased duration-300 transition-colors text-gray-800 dark:text-gray-200 bg-white dark:bg-gray-950"
  >
    <NuxtLayout>
      <NuxtLoadingIndicator />
      <NuxtPage />
    </NuxtLayout>
  </Body>
</template>
