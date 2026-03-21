import type { DiaryLocationInput } from '@/types/diary'

/**
 * 腾讯地图地点搜索结果。
 * 这里是在日记定位场景下裁剪过的结构，避免页面直接依赖原始 SDK 字段。
 */
export interface QqMapSearchResult extends DiaryLocationInput {
  title: string
  id?: string
}
