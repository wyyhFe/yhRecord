import { request } from '@/utils/request'

/**
 * 用户标签结构。
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
 * 查询当前用户标签。
 */
export function fetchUserTags(moduleType: 'DIARY' | 'LEDGER' = 'DIARY') {
  return request<TagItem[]>({
    url: `/tags/list?moduleType=${moduleType}`,
    method: 'GET'
  })
}

/**
 * 查询系统标签模板。
 */
export function fetchTagTemplates(moduleType: 'DIARY' | 'LEDGER' = 'DIARY') {
  return request<TagItem[]>({
    url: `/tag-templates/list?moduleType=${moduleType}`,
    method: 'GET'
  })
}

/**
 * 基于模板创建用户标签。
 */
export function createTagFromTemplate(templateId: number, moduleType: 'DIARY' | 'LEDGER' = 'DIARY') {
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
  moduleType: 'DIARY' | 'LEDGER'
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
export function deleteTag(id: number) {
  return request<void>({
    url: `/tags/delete/${id}`,
    method: 'DELETE'
  })
}
