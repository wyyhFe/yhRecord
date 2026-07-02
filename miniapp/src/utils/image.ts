import { OSS_BASE_URL } from '@/config/app'

/**
 * 将 OSS 相对路径解析为完整可访问的图片 URL。
 * 入参为相对路径（如 "diary/xxx.jpg"），自动拼接 OSS_BASE_URL。
 * 外部 URL（第三方头像等）原样返回。
 */
export function resolveImage(path?: string | null): string {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${OSS_BASE_URL}/${path}`
}
