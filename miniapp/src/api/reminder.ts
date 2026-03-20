import { request } from '@/utils/request'

/**
 * 提醒设置响应结构。
 * 字段和后端 ReminderSettingVO 保持一致。
 */
export interface ReminderSetting {
  id: number
  diaryReminderEnabled: boolean
  diaryReminderTime?: string
  miniProgramReminderEnabled: boolean
  officialAccountReminderEnabled: boolean
}

/**
 * 保存提醒设置时提交给后端的参数结构。
 */
export interface SaveReminderSettingPayload {
  diaryReminderEnabled: boolean
  diaryReminderTime?: string
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
