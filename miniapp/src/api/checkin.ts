import { request } from '@/utils/request'
import type { Pagination } from '@/types/api'
import type { CheckinTag, CheckinTask, HeatmapData, Id, Medal } from '@/types/domain'

/**
 * 获取当前用户的打卡任务列表。
 */
export function fetchCheckinTasks() {
  return request<Pagination<CheckinTask>>({
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
 * 打卡附件项。
 */
export interface CheckinMediaItem {
  mediaType: string
  filePath: string
  sortOrder?: number
}

/**
 * 完成某个任务的打卡。
 */
export function submitCheckin(
  taskId: Id,
  data?: { checkinDate?: string; remark?: string; mediaList?: CheckinMediaItem[]; mood?: string; tagIds?: Id[] }
) {
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

/**
 * 获取热力图数据。
 */
export function fetchCheckinHeatmap(year: number, month: number) {
  return request<HeatmapData>({
    url: `/checkin/heatmap?year=${year}&month=${month}`,
    method: 'GET'
  })
}

/**
 * 获取打卡标签列表。
 */
export function fetchCheckinTags() {
  return request<CheckinTag[]>({
    url: '/checkin/tags',
    method: 'GET'
  })
}

/**
 * 创建自定义打卡标签。
 */
export function createCheckinTag(data: { name: string; icon?: string }) {
  return request<CheckinTag>({
    url: '/checkin/tags',
    method: 'POST',
    data
  })
}

export function deleteCheckinTag(id: Id) {
  return request<void>({
    url: `/checkin/tags/${id}`,
    method: 'DELETE'
  })
}

/**
 * 获取勋章列表。
 */
export function fetchMedals() {
  return request<Medal[]>({
    url: '/checkin/medals',
    method: 'GET'
  })
}

/**
 * 补卡。
 */
export function mendCheckin(taskId: Id, mendDate: string) {
  return request<void>({
    url: '/checkin/records/mend',
    method: 'POST',
    data: { taskId, mendDate }
  })
}

/**
 * 查询当月剩余补卡次数。
 */
export function fetchMendRemaining() {
  return request<number>({
    url: '/checkin/mend-remaining',
    method: 'GET'
  })
}
