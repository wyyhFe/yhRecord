<template>
  <slot />
</template>

<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { ensureEntryAuth } from '@/utils/auth'
import { useAppStore } from '@/stores/app'
import { useThemeStore } from '@/stores/theme'
import { ensureSession } from '@/utils/session'

const appStore = useAppStore()
const themeStore = useThemeStore()

async function bootstrapSession() {
  const loggedIn = await ensureSession()
  if (loggedIn) {
    await appStore.loadProfile().catch(() => undefined)
  }
}

onLaunch(() => {
  // 第一时间把上次选的主题从本地存储恢复，避免首屏闪默认主题再切。
  themeStore.bootstrap()
  bootstrapSession().catch(() => undefined)
})

onShow(() => {
  bootstrapSession().catch(() => undefined)
  ensureEntryAuth()
})
</script>

<style lang="scss">
@import 'uview-pro/index.scss';

/**
 * 全局 page 样式。
 *
 * 注意：page 元素本身在小程序里无法绑定 class，所以它只能拿到 page 选择器里挂的
 * "默认主题"变量值。要让背景跟着主题变，必须把渐变背景搬到 .page-shell-safe 上 ——
 * 因为它和 themeClass 一起绑在每个页面的根 view 上，CSS 变量在那一层会被覆盖。
 *
 * 这里 page 只保留极简兜底（背景纯色 + 字体），剩下交给 .page-shell-safe。
 */
page {
  min-height: 100%;
  background: var(--color-bg);
  color: var(--color-text-primary);
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

/**
 * 页面安全区容器。注意这里只给页面级背景；尺寸、内外边距由 UnoCSS 的同名 shortcut
 * 在编译期注入（min-h-screen / box-border / px-6 / pb-40 / pt-6）。两份规则会被
 * CSS 自然合并。
 */
.page-shell-safe {
  background: var(--color-bg-gradient);
  color: var(--color-text-primary);
}
</style>
