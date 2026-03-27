import { request } from '@/utils/request'
import type { CheckinTask, Id } from '@/types/domain'

/**
 * 获取当前用户的打卡任务列表。
 */
export function fetchCheckinTasks() {
  return request<CheckinTask[]>({
    url: '/checkin/tasks/list',
    method: 'GET'
  })
}

export function deleteCheckinTask(taskId: Id) {
  return request<void>({
    url: `/checkin/tasks/delete/${taskId}`,
    method: 'DELETE'
  })
}

/**
 * 完成某个任务的打卡。
 */
export function submitCheckin(taskId: Id, data?: { checkinDate?: string; remark?: string }) {
  return request<void>({
    url: `/checkin/records/check/${taskId}`,
    method: 'POST',
    data
  })
}

/**
 * 查询某一天的打卡记录。
 */
export function fetchCheckinDayDetail(date: string) {
  return request<CheckinTask[]>({
    url: `/checkin/day-detail?date=${encodeURIComponent(date)}`,
    method: 'GET'
  })
}
