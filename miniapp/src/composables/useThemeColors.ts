/**
 * 主题色调色盘 composable，专给 canvas 类组件（ECharts、QRCode、Canvas API 等）用。
 *
 * 为什么单独搞这个：
 * - canvas 渲染不解析 CSS 变量，所以 `var(--color-primary)` 写到 chart options 里会失效
 * - 必须传具体颜色字符串（hex / rgba），主题切换时要重新计算
 * - 这里把每套主题的实际颜色值集中维护，与 styles/themes/*.scss 里的一一对应
 *
 * ⚠️ 跨两份维护是必要的代价：
 *   - SCSS 用 CSS 变量给纯 CSS 场景用
 *   - TS 用字面量给 canvas 场景用
 *   两边的色值必须保持一致，新增/调整主题色时**两处都要改**
 */

import { computed } from 'vue'
import { useTheme, type ThemeId } from './useTheme'

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

/**
 * 4 套主题的色板。值与 styles/themes/{id}.scss 完全对应。
 * 排序：暖陶（默认）→ 薄荷 → 紫雾 → 墨夜（暗色）。
 */
const PALETTES: Record<ThemeId, ThemePalette> = {
  clay: {
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
  },
  mint: {
    primary: '#5cab86',
    primarySoft: 'rgba(92, 171, 134, 0.12)',
    primaryStrong: '#4a8e6e',
    accent: '#7bb89a',
    bg: '#f5f9f5',
    surface: 'rgba(255, 255, 255, 0.94)',
    surfaceSoft: '#ecf3ec',
    border: 'rgba(92, 171, 134, 0.1)',
    textPrimary: '#1f2a23',
    textSecondary: '#4f5f54',
    textMuted: '#7a8b80',
    textNeutral: '#1d1d1f',
    success: '#4f8a5c',
    warning: '#c89a3a',
    danger: '#c45a4d'
  },
  lilac: {
    primary: '#9b7cb6',
    primarySoft: 'rgba(155, 124, 182, 0.12)',
    primaryStrong: '#7e63a0',
    accent: '#c89cc8',
    bg: '#faf6fb',
    surface: 'rgba(255, 255, 255, 0.94)',
    surfaceSoft: '#f3edf5',
    border: 'rgba(155, 124, 182, 0.1)',
    textPrimary: '#2a1f33',
    textSecondary: '#5a4d65',
    textMuted: '#8b7d99',
    textNeutral: '#1d1d1f',
    success: '#6f9c66',
    warning: '#d09848',
    danger: '#b85a78'
  },
  ink: {
    primary: '#7c93a8',
    primarySoft: 'rgba(124, 147, 168, 0.18)',
    primaryStrong: '#94a8bc',
    accent: '#c8a674',
    bg: '#1d2128',
    surface: 'rgba(38, 44, 53, 0.94)',
    surfaceSoft: '#2a313b',
    border: 'rgba(255, 255, 255, 0.06)',
    textPrimary: '#e8ecf1',
    textSecondary: '#a8b2bf',
    textMuted: '#6f7886',
    textNeutral: '#f5f5f7',
    success: '#7ab07f',
    warning: '#d6b06a',
    danger: '#d77a6b'
  }
}

/**
 * 获取当前激活主题的调色盘。返回 `ComputedRef<ThemePalette>`，主题切换时 .value 会自动更新。
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
  const { active } = useTheme()
  return computed(() => PALETTES[active.value])
}
