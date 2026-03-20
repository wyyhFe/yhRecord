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
