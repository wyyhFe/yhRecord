<template>
  <slot />
  <AppLoading :visible="loading.visible.value" :text="loading.text.value" />
</template>

<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { ensureEntryAuth } from '@/utils/auth'
import { useAppStore } from '@/stores/app'
import { ensureSession } from '@/utils/session'
import AppLoading from '@/components/business/app-loading/index.vue'
import { useLoading } from '@/composables/useLoading'
import {
  MP_DIARY_TEMPLATE_ID,
  MP_LEDGER_TEMPLATE_ID,
  MP_LEDGER_MONTHLY_TEMPLATE_ID
} from '@/config/app'

const loading = useLoading()

const appStore = useAppStore()

async function bootstrapSession() {
  const loggedIn = await ensureSession()
  if (loggedIn) {
    await appStore.loadProfile().catch(() => undefined)
  }
}

/** 每日续订：日记+每日记账+月报（最多3个模板，一次弹窗） */
function renewDailySubscription() {
  // #ifdef MP-WEIXIN
  const today = new Date().toDateString()
  if (uni.getStorageSync('last_daily_subscribe_date') === today) return

  const tmplIds = [
    MP_DIARY_TEMPLATE_ID,
    MP_LEDGER_TEMPLATE_ID,
    MP_LEDGER_MONTHLY_TEMPLATE_ID
  ].filter(Boolean)
  if (!tmplIds.length) return

  uni.requestSubscribeMessage({
    tmplIds,
    success: () => uni.setStorageSync('last_daily_subscribe_date', today),
    fail: () => { /* 用户拒绝或微信不弹窗，静默忽略 */ }
  })
  // #endif
}

onLaunch(() => {
  uni.hideTabBar({ animation: false })
  bootstrapSession().catch(() => undefined)
})

onShow(() => {
  uni.hideTabBar({ animation: false })
  ensureEntryAuth()
  renewDailySubscription()
})
</script>

<style lang="scss">
@import 'uview-pro/index.scss';

/**
 * 全局 page 样式。
 *
 * page 元素本身在小程序里无法绑 class，所以 CSS 变量统一挂在 page 选择器上
 * （见 styles/themes/clay.scss）。这里只补极简兜底：背景纯色、字体、min-height。
 */
page {
  min-height: 100%;
  background: var(--color-bg);
  color: var(--color-text-primary);
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

/**
 * 页面安全区容器：尺寸、内外边距由 UnoCSS 同名 shortcut 注入；这里补充：
 *  - 显式 min-height: 100vh —— 防止内容短的页面（如详情页）撑不开，露出 page 的纯色底
 *  - 渐变背景挂在这里 —— 给页面一层柔和的氛围底
 *  - 字体色也走 token —— 部分页面没单独设 color 的话默认继承到这里
 */
.page-shell-safe {
  min-height: 100vh;
  background: var(--color-bg-gradient);
  color: var(--color-text-primary);
}
</style>
