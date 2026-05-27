/**
 * 主题色调色盘 composable，专给 canvas 类组件（ECharts、QRCode、Canvas API 等）用。
 *
 * 为什么单独搞这个：
 * - canvas 渲染不解析 CSS 变量，所以 `var(--color-primary)` 写到 chart options 里会失效
 * - 必须传具体颜色字符串（hex / rgba）
 *
 * 历史：早期支持 clay/mint/lilac/ink 四套主题切换，返回 ComputedRef 跟随主题变化。
 * 撤回多主题后简化为只返 clay 调色板的常量；接口保留 ComputedRef 是为了消费方
 * （比如 ledger-year-charts）不用改用法，未来重新接入主题切换也能无缝替换。
 *
 * ⚠️ 色值必须与 styles/themes/clay.scss 保持一致，改色时两处都要动。
 */

import { computed } from 'vue'

export interface ThemePalette {
  /** 主色（强调、激活态） */
  primary: string
  /** 主色的弱化背景，~12% 透明 */
  primarySoft: string
  /** 主色加深，用于 hover/active */
  primaryStrong: string
  /** 次要强调色 */
  accent: string
  /** 整页背景纯色 */
  bg: string
  /** 卡片表面 */
  surface: string
  /** 弱化表面 */
  surfaceSoft: string
  /** 边框 */
  border: string
  /** 标题、正文主文字 */
  textPrimary: string
  /** 说明、次级文字 */
  textSecondary: string
  /** 时间戳、辅助标签 */
  textMuted: string
  /** 中性文字（用户输入、代码等"功能性"文字），不带主题色调 */
  textNeutral: string
  /** 收入、成功 */
  success: string
  /** 警告 */
  warning: string
  /** 支出、错误、删除 */
  danger: string
}

/** 当前唯一主题（clay）的色板。值与 styles/themes/clay.scss 完全对应。 */
const CLAY_PALETTE: ThemePalette = {
  primary: '#c47c52',
  primarySoft: 'rgba(196, 124, 82, 0.12)',
  primaryStrong: '#a56d4b',
  accent: '#d7a648',
  bg: '#fffaf4',
  surface: 'rgba(255, 250, 244, 0.94)',
  surfaceSoft: '#fcf5ec',
  border: 'rgba(196, 124, 82, 0.08)',
  textPrimary: '#2b2118',
  textSecondary: '#6b5b4e',
  textMuted: '#9b866d',
  textNeutral: '#1d1d1f',
  success: '#4f7a4a',
  warning: '#d7a648',
  danger: '#b94a3b'
}

/**
 * 获取当前主题调色盘。返回 `ComputedRef<ThemePalette>` 是为了向前兼容
 * 早期多主题写法（消费方都用 `.value` 拿色值），现在 .value 永远是 clay。
 *
 * 用法：
 * ```ts
 * const colors = useThemeColors()
 * const chartOption = computed(() => ({
 *   textStyle: { color: colors.value.textPrimary },
 *   color: [colors.value.danger, colors.value.success]
 * }))
 * ```
 */
export function useThemeColors() {
  return computed(() => CLAY_PALETTE)
}
