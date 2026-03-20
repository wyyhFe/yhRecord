/**
 * 后端统一返回体结构。
 * request 工具会自动解包其中的 data。
 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

/**
 * 通用分页结构。
 */
export interface Pagination<T> {
  records: T[]
  total: number
  size: number
  current: number
}
