import type { App } from 'vue'

type ToastIcon = 'none' | 'success'
type FeedbackInput = unknown

export interface FeedbackApi {
  success(input: FeedbackInput, duration?: number): void
  error(input: FeedbackInput, duration?: number, fallback?: string): void
  info(input: FeedbackInput, duration?: number): void
}

function openToast(title: string, icon: ToastIcon, duration = 2000) {
  uni.showToast({
    title,
    icon,
    duration
  })
}

function normalizeMessage(input: FeedbackInput, fallback = '操作失败') {
  if (typeof input === 'string') {
    return input
  }
  if (input instanceof Error) {
    return input.message || fallback
  }
  if (input && typeof input === 'object') {
    const maybeMessage = (input as { message?: string; errMsg?: string }).message
      || (input as { message?: string; errMsg?: string }).errMsg
    if (typeof maybeMessage === 'string' && maybeMessage.trim()) {
      return maybeMessage
    }
  }
  return fallback
}

export const feedback: FeedbackApi = {
  success(input, duration) {
    openToast(normalizeMessage(input, '操作成功'), 'success', duration)
  },
  error(input, duration, fallback = '操作失败') {
    openToast(normalizeMessage(input, fallback), 'none', duration)
  },
  info(input, duration) {
    openToast(normalizeMessage(input, '请稍后重试'), 'none', duration)
  }
}

/**
 * 把轻提示能力挂到全局，页面和工具函数里统一走 `uni.$feedback`。
 */
export default {
  install(app: App) {
    ;(app.config.globalProperties as Record<string, unknown>).$feedback = feedback
    ;(uni as typeof uni & { $feedback: FeedbackApi }).$feedback = feedback
  }
}
