import { request } from '@/utils/request'
import type { DaySummary } from '@/types/domain'

/**
 * 月历摘要接口返回结构。
 */
export interface CalendarSummaryResult {
  year: number
  month: number
  days: DaySummary[]
}

/**
 * 获取某年某月的日历摘要。
 * 首页和日历状态条都会依赖这组数据。
 */
export function fetchCalendarSummary(year: number, month: number) {
  return request<CalendarSummaryResult>({
    url: `/calendar/summary?year=${year}&month=${month}`,
    method: 'GET'
  })
}
