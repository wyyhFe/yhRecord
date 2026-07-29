import { request } from '@/utils/request'

/**
 * OSS 上传签名返回结构。
 * 前端会用它拼出表单后直传 OSS。
 */
export interface UploadPolicyResult {
  accessKeyId: string
  policy: string
  signature: string
  dir: string
  host: string
  expire: number
}

/** 内容安全检测结果 */
export interface ContentCheckResult {
  safe: boolean
  message?: string
}

/**
 * 获取 OSS 上传签名。
 */
export function fetchUploadPolicy(dir: string, expireSeconds = 300) {
  return request<UploadPolicyResult>({
    url: '/files/upload-policy',
    method: 'POST',
    data: {
      dir,
      expireSeconds
    }
  })
}

/**
 * 检测图片内容是否安全。
 * 调用后端微信 imgSecCheck 接口，传入 OSS 图片完整 URL。
 */
export function checkImageSecurity(mediaUrl: string) {
  return request<ContentCheckResult>({
    url: '/security/check-image',
    method: 'POST',
    data: { mediaUrl }
  })
}
