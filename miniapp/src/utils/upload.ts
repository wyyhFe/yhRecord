import { fetchUploadPolicy } from '@/api/file'

/**
 * OSS 上传所需的最小参数。
 * 这里只负责文件直传，不负责业务数据入库。
 */
interface UploadToOssOptions {
  filePath: string
  dir?: string
}

/**
 * 从本地文件路径里提取扩展名。
 * 用于生成 OSS key 时保留原始文件后缀。
 */
function getFileExtension(filePath: string) {
  const lastDot = filePath.lastIndexOf('.')
  return lastDot > -1 ? filePath.slice(lastDot) : ''
}

/**
 * 通过后端获取签名后，将图片直接上传到 OSS。
 * 最终只返回对象 key，业务表仍然只保存路径，不保存完整域名。
 */
export async function uploadImageToOss({ filePath, dir = 'diary/' }: UploadToOssOptions) {
  const policy = await fetchUploadPolicy(dir)
  const ext = getFileExtension(filePath)
  const key = `${policy.dir}${Date.now()}-${Math.random().toString(36).slice(2)}${ext}`

  const result = await uni.uploadFile({
    url: policy.host,
    filePath,
    name: 'file',
    formData: {
      key,
      OSSAccessKeyId: policy.accessKeyId,
      policy: policy.policy,
      Signature: policy.signature,
      success_action_status: '200'
    }
  })

  if (result.statusCode !== 200) {
    const message = typeof result.data === 'string' ? result.data : '上传到 OSS 失败'
    throw new Error(message)
  }

  return `${policy.host}/${key}`
}
