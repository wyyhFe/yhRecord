/**
 * 用户个人资料。
 */
export interface UserProfile {
  id: number
  nickname: string
  avatarPath?: string
  signature?: string
  birthday?: string
  diaryCount: number
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
  remindAt?: string
  tags?: Array<{
    id: number
    name: string
  }>
}

/**
 * 记账流水简要信息。
 */
export interface LedgerEntry {
  id: number
  type: 'INCOME' | 'EXPENSE'
  amount: number
  entryDate: string
  remark?: string
}

/**
 * 打卡任务列表项。
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
