<template>
  <slot />
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app'
import { ensureEntryAuth } from '@/utils/auth'

onShow(() => {
  ensureEntryAuth()
})
</script>

<style lang="scss">
page {
  min-height: 100%;
  background:
    radial-gradient(circle at top left, rgba(216, 124, 134, 0.16), transparent 34%),
    radial-gradient(circle at top right, rgba(215, 166, 72, 0.14), transparent 28%),
    linear-gradient(180deg, #fffaf4 0%, #f6efe5 100%);
  color: #1e293b;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}
</style>
