import { request } from '@/utils/request'

/**
 * 创建打卡任务提交结构。
 */
export interface CreateCheckinTaskPayload {
  name: string
  description?: string
  startDate: string
}

/**
 * 创建打卡任务。
 */
export function createCheckinTask(data: CreateCheckinTaskPayload) {
  return request({
    url: '/checkin/tasks/create',
    method: 'POST',
    data
  })
}
