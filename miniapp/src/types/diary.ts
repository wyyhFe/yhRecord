/**
 * 日记附件提交结构。
 * 与后端 mediaList 字段保持一致。
 */
export interface DiaryMediaInput {
  mediaType: 'IMAGE' | 'VIDEO'
  filePath: string
  sortOrder: number
}

/**
 * 日记定位信息提交结构。
 * sourceType 需要与后端 LocationSourceType 枚举保持一致。
 */
export interface DiaryLocationInput {
  locationName?: string
  address?: string
  province?: string
  city?: string
  district?: string
  latitude?: number
  longitude?: number
  sourceType?: 'CURRENT' | 'MANUAL'
}

/**
 * 新建或编辑日记时提交给后端的请求体。
 */
export interface CreateDiaryPayload {
  title: string
  content: string
  recordDate: string
  weather?: string
  mood?: string
  visibility: 'PRIVATE' | 'SHARED' | 'PUBLIC'
  remindAt?: string
  location?: DiaryLocationInput
  mediaList: DiaryMediaInput[]
  tagIds?: number[]
}
