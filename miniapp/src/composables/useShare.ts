import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'

/**
 * 统一分享配置。
 * 每个页面在 setup 中调用一次即可注册「发送给朋友」和「朋友圈」。
 */
export function useShare(options: {
  title: string
  path?: string
  imageUrl?: string
}) {
  onShareAppMessage(() => ({
    title: options.title,
    path: options.path || undefined,
    imageUrl: options.imageUrl
  }))

  onShareTimeline(() => ({
    title: options.title
  }))
}
