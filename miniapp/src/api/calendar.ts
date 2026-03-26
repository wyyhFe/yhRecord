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
 * 获取某年某月的日历摘要。
 * 首页和日历状态条都会依赖这组数据。
 */
export function fetchCalendarSummary(year: number, month: number) {
  return request<CalendarSummaryResult>({
    url: `/calendar/summary?year=${year}&month=${month}`,
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
