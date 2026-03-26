import { request } from '@/utils/request'
import type { Id } from '@/types/domain'

export type TagModuleType = 'DIARY' | 'LEDGER'
export type LedgerTagType = 'EXPENSE' | 'INCOME'

/**
 * 用户标签结构。
 */
export interface TagItem {
  id: Id
  templateId?: Id
  name: string
  color?: string
  icon?: string
  moduleType: TagModuleType
  ledgerType?: LedgerTagType
}

/**
 * 查询当前用户标签。
 */
export function fetchUserTags(moduleType: TagModuleType = 'DIARY', ledgerType?: LedgerTagType) {
  const suffix = ledgerType ? `&ledgerType=${ledgerType}` : ''
  return request<TagItem[]>({
    url: `/tags/list?moduleType=${moduleType}${suffix}`,
    method: 'GET'
  })
}

/**
 * 查询系统标签模板。
 */
export function fetchTagTemplates(moduleType: TagModuleType = 'DIARY', ledgerType?: LedgerTagType) {
  const suffix = ledgerType ? `&ledgerType=${ledgerType}` : ''
  return request<TagItem[]>({
    url: `/tag-templates/list?moduleType=${moduleType}${suffix}`,
    method: 'GET'
  })
}

/**
 * 基于模板创建用户标签。
 */
export function createTagFromTemplate(templateId: Id, moduleType: TagModuleType = 'DIARY') {
  return request<TagItem>({
    url: '/tags/create-from-template',
    method: 'POST',
    data: {
      templateId,
      moduleType
    }
  })
}

/**
 * 创建自定义标签。
 */
export function createTag(data: {
  name: string
  color?: string
  icon?: string
  moduleType: TagModuleType
  ledgerType?: LedgerTagType
}) {
  return request<TagItem>({
    url: '/tags/create',
    method: 'POST',
    data
  })
}

/**
 * 删除标签。
 */
export function deleteTag(id: Id) {
  return request<void>({
    url: `/tags/delete/${id}`,
    method: 'DELETE'
  })
}
