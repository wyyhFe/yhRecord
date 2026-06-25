/**
 * 格式化后端返回的 ISO 时间（含 T）为可读短格式。
 *
 * "2026-06-24T15:59:50" → "06-24 15:59"
 * null / undefined / ""   → "暂无记录"
 */
export function formatLatestTime(value: string | null | undefined): string {
  if (!value) return '暂无记录'
  const d = value.replace('T', ' ')
  return d.length >= 16 ? d.slice(5, 16) : d
}

/**
 * 格式化 ISO 时间为完整日期时间。
 *
 * "2026-06-24T15:59:50" → "2026-06-24 15:59"
 */
export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '--'
  return value.replace('T', ' ').slice(0, 16)
}
