/**
 * 主题切换 composable。
 *
 * 在页面或组件里这样用：
 *
 * ```ts
 * import { useTheme } from '@/composables/useTheme'
 * const { themeClass, setTheme, active } = useTheme()
 * ```
 *
 * 然后把 themeClass 绑到页面根 view：
 *
 * ```html
 * <view :class="['page-shell-safe', themeClass]">...</view>
 * ```
 *
 * 切换：调 `setTheme('mint')` 即可，整页颜色立刻变。
 *
 * 主题列表与默认值在这里集中维护，store 仅负责持久化和响应式状态，
 * 避免循环依赖（store 引用本文件的常量，本文件用 store 的 action）。
 */

import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'

/**
 * 支持的主题 id。新增主题需要在三处同步加：
 * 1. 这里的 `ALL_THEMES`
 * 2. styles/themes/{id}.scss
 * 3. styles/themes/index.scss 的 import
 */
export const ALL_THEMES = ['clay', 'mint', 'lilac', 'ink'] as const

export type ThemeId = (typeof ALL_THEMES)[number]

/** 默认主题：暖陶（保留项目当前视觉风格）。 */
export const DEFAULT_THEME_ID: ThemeId = 'clay'

/** 主题列表元数据，给"主题切换器"UI 用。 */
export const THEME_META: Record<ThemeId, { label: string; description: string }> = {
  clay: { label: '暖陶', description: '温暖、纸感、复古' },
  mint: { label: '薄荷', description: '清爽、自然、平静' },
  lilac: { label: '紫雾', description: '柔软、梦幻、情感向' },
  ink: { label: '墨夜', description: '沉稳、专注、暗色' }
}

export function useTheme() {
  const store = useThemeStore()

  /** 当前主题 id（响应式）。 */
  const active = computed(() => store.active)

  /**
   * 给页面根 view 用的 class 字符串，例如 "theme-clay"。
   * 注意：clay 主题的 CSS 变量在 SCSS 里已经默认挂在 `page` 上，
   * 但这里仍然加 class，保证切换到其他主题再切回 clay 时能稳定生效。
   */
  const themeClass = computed(() => `theme-${store.active}`)

  /** 是否是暗色主题（页面级布局可以根据这个调整） */
  const isDark = computed(() => store.active === 'ink')

  /** 切到指定主题，会持久化到 storage。 */
  function setTheme(theme: ThemeId) {
    store.setTheme(theme)
  }

  return {
    active,
    themeClass,
    isDark,
    setTheme
  }
}
