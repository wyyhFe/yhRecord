/**
 * 前端环境配置。
 * 开发环境读取 .env.development，生产环境读取 .env.production。
 */
export const APP_NAME = import.meta.env.VITE_APP_NAME || 'Life Record'
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080'
export const OSS_BASE_URL = import.meta.env.VITE_OSS_BASE_URL || 'https://wyhosskey.oss-cn-hangzhou.aliyuncs.com'

/** 小程序侧腾讯地图 Key，供 qqmap SDK 使用。 */
export const TENCENT_MAP_KEY = import.meta.env.VITE_TENCENT_MAP_KEY || ''

/** 前端在请求订阅授权时会一次性申请这些模板。 */
export const MP_DIARY_TEMPLATE_ID = import.meta.env.VITE_MP_DIARY_TEMPLATE_ID || ''
export const MP_LEDGER_TEMPLATE_ID = import.meta.env.VITE_MP_LEDGER_TEMPLATE_ID || ''
export const MP_LEDGER_MONTHLY_TEMPLATE_ID = import.meta.env.VITE_MP_LEDGER_MONTHLY_TEMPLATE_ID || ''
export const MP_MEMORIAL_TEMPLATE_ID = import.meta.env.VITE_MP_MEMORIAL_TEMPLATE_ID || ''

/** 日记可见范围选项，取值需要和后端 VisibilityType 保持一致。 */
export const VISIBILITY_OPTIONS = [
  { label: '全部', value: 'ALL' },
  { label: '私有', value: 'PRIVATE' },
  { label: '共享', value: 'SHARED' },
  { label: '公开', value: 'PUBLIC' }
]
