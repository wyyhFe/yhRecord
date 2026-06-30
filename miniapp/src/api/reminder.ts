import { request } from '@/utils/request'
import type { Id } from '@/types/domain'

/**
 * 提醒设置响应结构。
 */
export interface ReminderSetting {
  id: Id
  diaryReminderEnabled: boolean
  miniProgramReminderEnabled: boolean
  officialAccountReminderEnabled: boolean
}

/**
 * 保存提醒设置请求体。
 */
export interface SaveReminderSettingPayload {
  diaryReminderEnabled: boolean
  miniProgramReminderEnabled: boolean
  officialAccountReminderEnabled: boolean
}

/**
 * 获取当前用户提醒设置。
 */
export function fetchReminderSettings() {
  return request<ReminderSetting>({
    url: '/reminders/settings',
    method: 'GET'
  })
}

/**
 * 保存当前用户提醒设置。
 */
export function saveReminderSettings(data: SaveReminderSettingPayload) {
  return request<ReminderSetting>({
    url: '/reminders/settings',
    method: 'PUT',
    data
  })
}

/**
 * 手动触发日记提醒。
 */
export function triggerDiaryReminder() {
  return request<void>({ url: '/reminders/trigger/diary', method: 'POST' })
}

/**
 * 手动触发每日记账提醒。
 */
export function triggerLedgerReminder() {
  return request<void>({ url: '/reminders/trigger/ledger', method: 'POST' })
}

/**
 * 手动触发记账月报提醒。
 */
export function triggerMonthlyReminder() {
  return request<void>({ url: '/reminders/trigger/monthly', method: 'POST' })
}

/**
 * 手动触发纪念日提醒。
 */
export function triggerMemorialReminder() {
  return request<void>({ url: '/reminders/trigger/memorial', method: 'POST' })
}
