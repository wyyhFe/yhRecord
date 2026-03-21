declare const require: (path: string) => any

import { TENCENT_MAP_KEY } from '@/config/app'

/**
 * 创建腾讯地图小程序 SDK 实例。
 * 统一从这里初始化，避免页面和组件直接依赖 SDK 文件路径。
 */
export function createQqMapClient() {
  // 官方 SDK 是 CommonJS 风格，在 uni-app 小程序环境下直接 require 最稳定。
  const QQMapWX = require('./sdk/qqmap-wx-jssdk.js')

  return new QQMapWX({
    key: TENCENT_MAP_KEY
  })
}
