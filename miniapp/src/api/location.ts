import { request } from '@/utils/request'
import type { DiaryLocationInput } from '@/types/diary'

/**
 * 根据经纬度换取结构化地址。
 * 写日记时如果前端只拿到坐标，会调用这个接口补齐地址字段。
 */
export function reverseGeocode(latitude: number, longitude: number) {
  return request<DiaryLocationInput>({
    url: '/locations/reverse-geocode',
    method: 'POST',
    data: {
      latitude,
      longitude
    }
  })
}
