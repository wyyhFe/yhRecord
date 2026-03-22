/**
 * 用户个人资料。
 */
export interface UserProfile {
  id: number
  nickname: string
  avatarPath?: string
  gender?: 'UNKNOWN' | 'MALE' | 'FEMALE'
  officialAccountOpenid?: string
  signature?: string
  birthday?: string
  diaryCount: number
}

/**
 * 更新用户资料请求体。
 */
export interface UserProfileUpdatePayload {
  nickname?: string
  avatarPath?: string
  gender?: 'UNKNOWN' | 'MALE' | 'FEMALE'
  officialAccountOpenid?: string
  birthday?: string
  signature?: string
}

/**
 * 日记列表页和详情页复用的数据结构。
 */
export interface DiaryItem {
  id: number
  title: string
  content: string
  recordDate: string
  weather?: string
  mood?: string
  visibility: 'PRIVATE' | 'SHARED' | 'PUBLIC'
  likeCount: number
  commentCount: number
  mediaPaths: string[]
  ageLabel?: string
  locationName?: string
  address?: string
  province?: string
  city?: string
  district?: string
  latitude?: number
  longitude?: number
  locationSourceType?: 'CURRENT' | 'MANUAL'
  tags?: Array<{
    id: number
    name: string
  }>
}

/**
 * 记账流水标签。
 */
export interface LedgerTag {
  id: number
  name: string
  color?: string
}

/**
 * 记账流水简要信息。
 */
export interface LedgerEntry {
  id: number
  bookId?: number
  type: 'INCOME' | 'EXPENSE'
  amount: number
  entryDate: string
  remark?: string
  imagePath?: string
  tagIds?: number[]
  tags?: LedgerTag[]
}

/**
 * 年度标签统计。
 */
export interface LedgerYearStatisticItem {
  tagId: number
  tagName?: string
  amount: number
  ratio: number
}

export interface LedgerYearStatistics {
  year: number
  items: LedgerYearStatisticItem[]
}

/**
 * 打卡任务列表页。
 */
export interface CheckinTask {
  id: number
  name: string
  description?: string
  totalCount: number
  latestCheckedAt?: string
}

/**
 * 日历某一天的摘要状态。
 */
export interface DaySummary {
  date: string
  hasDiary: boolean
  diaryCount: number
  hasCheckin: boolean
  checkinCount: number
  memorialCount: number
}
