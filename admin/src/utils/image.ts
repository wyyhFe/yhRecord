/** OSS 图片基础域名 */
const OSS_BASE_URL = (import.meta as any).env?.VITE_OSS_BASE_URL || ''

/**
 * 将 OSS 相对路径解析为完整可访问的图片 URL。
 * 相对路径（如 "avatar/xxx.jpg"）自动拼接 OSS 域名；
 * 已是完整 HTTP URL 的直接返回。
 */
export function resolveImage(path?: string | null): string {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${OSS_BASE_URL}/${path}`
}
