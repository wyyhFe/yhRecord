import { request } from '@/utils/request'
import type { CheckinTask } from '@/types/domain'

/**
 * 获取当前用户的打卡任务列表。
 */
export function fetchCheckinTasks() {
  return request<CheckinTask[]>({
    url: '/checkin/tasks/list',
    method: 'GET'
  })
}
