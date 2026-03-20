import { request } from '@/utils/request'

/**
 * 标签项。
 */
export interface TagItem {
  id: number
  templateId?: number
  name: string
  color?: string
  icon?: string
  moduleType: 'DIARY' | 'LEDGER'
}

/**
 * 获取日记模块可选标签。
 */
export function fetchDiaryTags() {
  return request<TagItem[]>({
    url: '/tags/list?moduleType=DIARY',
    method: 'GET'
  })
}
