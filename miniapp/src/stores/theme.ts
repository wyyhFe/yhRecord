/**
 * 主题切换状态管理。
 *
 * 设计思路：
 * - 当前激活的主题 id 保存在 Pinia + 本地存储两份，启动时从 storage 同步进 store
 * - UI 只读 store 状态，不直接操作 storage，避免双向同步出问题
 * - 切换主题 = 改 store 的 active → 触发响应式 → 各页面根 view 上的 themeClass 自动跟着变
 *
 * 主题样式定义在 styles/themes/{id}.scss，本 store 只负责"现在用哪个 id"。
 */

import { defineStore } from 'pinia'
import { ALL_THEMES, DEFAULT_THEME_ID, type ThemeId } from '@/composables/useTheme'

const STORAGE_KEY = 'life-record-theme'

/** 从 storage 安全读取主题 id，无效值兜底为默认主题。 */
function readThemeFromStorage(): ThemeId {
  try {
    const value = uni.getStorageSync(STORAGE_KEY) as string | undefined
    if (value && (ALL_THEMES as ReadonlyArray<string>).includes(value)) {
      return value as ThemeId
    }
  } catch (error) {
    console.warn('[theme-store] 读取本地存储失败', error)
  }
  return DEFAULT_THEME_ID
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    /** 当前激活的主题。启动后立即从 storage 同步。 */
    active: DEFAULT_THEME_ID as ThemeId
  }),
  actions: {
    /**
     * 应用启动时调用一次：把 storage 里的偏好写入 store。
     * 没存过任何主题的用户保持默认（暖陶）。
     */
    bootstrap() {
      this.active = readThemeFromStorage()
    },
    /**
     * 切换主题并写入本地存储。失败也不让 UI 崩。
     */
    setTheme(theme: ThemeId) {
      if (this.active === theme) return
      this.active = theme
      try {
        uni.setStorageSync(STORAGE_KEY, theme)
      } catch (error) {
        console.warn('[theme-store] 写入本地存储失败', error)
      }
    }
  }
})
