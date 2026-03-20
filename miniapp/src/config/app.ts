/**
 * 前端统一环境配置。
 * 本地开发读取 `.env.development`，生产构建读取 `.env.production`。
 */
export const APP_NAME = import.meta.env.VITE_APP_NAME || 'Life Record'
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080'
export const OSS_BASE_URL = import.meta.env.VITE_OSS_BASE_URL || 'https://wyhosskey.oss-cn-hangzhou.aliyuncs.com'
// 小程序写日记提醒模板 ID，前端请求订阅授权时会用到。
export const MP_DIARY_TEMPLATE_ID = import.meta.env.VITE_MP_DIARY_TEMPLATE_ID || ''
// 小程序纪念日提醒模板 ID，前端请求订阅授权时会用到。
export const MP_MEMORIAL_TEMPLATE_ID = import.meta.env.VITE_MP_MEMORIAL_TEMPLATE_ID || ''

// 日记可见范围选项，value 需要与后端 VisibilityType 保持一致。
export const VISIBILITY_OPTIONS = [
  { label: '全部', value: 'ALL' },
  { label: '私有', value: 'PRIVATE' },
  { label: '共享', value: 'SHARED' },
  { label: '公开', value: 'PUBLIC' }
]
