import { request } from '@/utils/request'
import type { CalendarDayDetail, DaySummary } from '@/types/domain'

/**
 * 月历摘要接口返回结构。
 */
export interface CalendarSummaryResult {
  year: number
  month: number
  days: DaySummary[]
}

/**
 * 获取最近 N 天的日历摘要（后端按日期范围直接返回，高效）。
 * 首页「近七天」时间轴专用。
 */
export function fetchCalendarSummaryRecent(days: number = 7) {
  return request<CalendarSummaryResult>({
    url: `/calendar/summary-recent?days=${days}`,
    method: 'GET'
  })
}

/**
 * 获取某一天的聚合详情。
 */
export function fetchCalendarDayDetail(date: string) {
  return request<CalendarDayDetail>({
    url: `/calendar/day-detail?date=${encodeURIComponent(date)}`,
    method: 'GET'
  })
}

/**
 * 获取去年今日数据。
 */
export function fetchOnThisDay(date: string) {
  return request<CalendarDayDetail>({
    url: `/memories/on-this-day?date=${encodeURIComponent(date)}`,
    method: 'GET'
  })
}
