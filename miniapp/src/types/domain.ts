export type Id = string

/**
 * 用户个人资料。
 */
export interface UserProfile {
  id: Id
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
  id: Id
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
    id: Id
    name: string
  }>
}

/**
 * 记账流水标签。
 */
export interface LedgerTag {
  id: Id
  name: string
  color?: string
}

/**
 * 记账流水简要信息。
 */
export interface LedgerEntry {
  id: Id
  bookId?: Id
  type: 'INCOME' | 'EXPENSE'
  amount: number
  entryDate: string
  remark?: string
  imagePath?: string
  tagIds?: Id[]
  tags?: LedgerTag[]
}

/**
 * 年度标签统计。
 */
export interface LedgerYearStatisticItem {
  tagId: Id
  tagName?: string
  amount: number
  ratio: number
}

export interface LedgerYearStatistics {
  year: number
  items: LedgerYearStatisticItem[]
}

/**
 * 打卡任务列表项。
 */
export interface CheckinTask {
  id: Id
  name: string
  description?: string
  startDate?: string
  totalCount: number
  latestCheckedAt?: string
}

/**
 * 纪念日信息。
 */
export interface MemorialDay {
  id: Id
  title: string
  type: string
  memorialDate: string
  annualRepeat: boolean
  remark?: string
  remindAt?: string
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

/**
 * 某一天的聚合详情。
 */
export interface CalendarDayDetail {
  date: string
  diaries: DiaryItem[]
  checkins: CheckinTask[]
  memorialDays: MemorialDay[]
}
