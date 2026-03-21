import { reverseGeocode } from '@/api/location'
import type { DiaryLocationInput } from '@/types/diary'
import { createQqMapClient } from './client'
import type { QqMapSearchResult } from './types'

/**
 * 尝试把位置数据补齐成后端需要的结构。
 * 如果前端只有经纬度，这里会再调用后端逆地理编码接口补全地址信息。
 */
export async function buildLocationPayload(payload: DiaryLocationInput): Promise<DiaryLocationInput> {
  if (payload.address && payload.province && payload.city) return payload
  if (!payload.latitude || !payload.longitude) return payload

  try {
    const resolved = await reverseGeocode(payload.latitude, payload.longitude)
    return {
      ...resolved,
      locationName: payload.locationName || resolved.locationName || resolved.address || '已选位置',
      sourceType: payload.sourceType
    }
  } catch {
    return payload
  }
}

/**
 * 获取当前位置，并组装成统一的位置结构。
 */
export async function pickCurrentLocationPayload(): Promise<DiaryLocationInput> {
  const result = await uni.getLocation({ type: 'gcj02' })
  return buildLocationPayload({
    latitude: result.latitude,
    longitude: result.longitude,
    sourceType: 'CURRENT',
    locationName: '当前位置'
  })
}

/**
 * 使用微信原生选点能力，返回统一的位置结构。
 */
export async function pickManualLocationPayload(): Promise<DiaryLocationInput> {
  const result = await uni.chooseLocation({})
  return buildLocationPayload({
    latitude: result.latitude,
    longitude: result.longitude,
    sourceType: 'MANUAL',
    locationName: result.name || '手动选择位置',
    address: result.address
  })
}

/**
 * 使用腾讯地图 SDK 搜索地点。
 * 返回值已经做过裁剪，页面不需要直接处理原始 SDK 数据结构。
 */
export function searchLocations(keyword: string): Promise<QqMapSearchResult[]> {
  const client = createQqMapClient()

  return new Promise((resolve, reject) => {
    client.search({
      keyword,
      success(res: any) {
        const data = Array.isArray(res?.data) ? res.data : []
        resolve(
          data.map((item: any) => ({
            id: item.id,
            title: item.title || item.name || '',
            locationName: item.title || item.name || '',
            address: item.address || '',
            latitude: item.location?.lat,
            longitude: item.location?.lng,
            sourceType: 'MANUAL',
            province: item.ad_info?.province,
            city: item.ad_info?.city,
            district: item.ad_info?.district
          }))
        )
      },
      fail(error: unknown) {
        reject(error)
      }
    })
  })
}
