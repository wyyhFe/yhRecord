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
 * 后端 Spring Boot MyBatis-Plus 分页返回字段：
 *   list / total / pageNum / pageSize
 */
export interface Pagination<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}
