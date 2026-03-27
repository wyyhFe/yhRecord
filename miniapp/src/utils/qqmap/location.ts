import { reverseGeocode } from '@/api/location'
import type { DiaryLocationInput } from '@/types/diary'
import { createQqMapClient } from './client'
import type { QqMapSearchResult } from './types'

const CURRENT_LOCATION_CACHE_KEY = 'current-location-cache'
const CURRENT_LOCATION_CACHE_TTL = 5 * 60 * 1000

interface CurrentLocationCache {
  latitude: number
  longitude: number
  expiresAt: number
  location: DiaryLocationInput
}

export interface CurrentLocationResult {
  payload: DiaryLocationInput
  reverseGeocodeError?: unknown
}

export async function pickCurrentLocationPayload(): Promise<CurrentLocationResult> {
  const result = await uni.getLocation({ type: 'gcj02' })
  const payload: DiaryLocationInput = {
    latitude: result.latitude,
    longitude: result.longitude,
    sourceType: 'CURRENT',
    locationName: '当前位置'
  }

  const cachedLocation = getCachedCurrentLocation(result.latitude, result.longitude)
  if (cachedLocation) {
    return {
      payload: {
        ...payload,
        ...cachedLocation,
        latitude: result.latitude,
        longitude: result.longitude,
        sourceType: 'CURRENT',
        locationName: cachedLocation.locationName || cachedLocation.address || payload.locationName
      }
    }
  }

  try {
    const location = await reverseGeocode(result.latitude, result.longitude)
    setCachedCurrentLocation(result.latitude, result.longitude, location)
    return {
      payload: {
        ...payload,
        ...location,
        latitude: result.latitude,
        longitude: result.longitude,
        sourceType: 'CURRENT',
        locationName: location.locationName || location.address || payload.locationName
      }
    }
  } catch (error) {
    return {
      payload,
      reverseGeocodeError: error
    }
  }
}

export async function pickManualLocationPayload(): Promise<DiaryLocationInput> {
  const result = await uni.chooseLocation({})
  return {
    latitude: result.latitude,
    longitude: result.longitude,
    sourceType: 'MANUAL',
    locationName: result.name || '手动选择位置',
    address: result.address
  }
}

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

function getCachedCurrentLocation(latitude: number, longitude: number): DiaryLocationInput | null {
  const cache = uni.getStorageSync(CURRENT_LOCATION_CACHE_KEY) as CurrentLocationCache | null
  if (!cache || cache.expiresAt <= Date.now()) {
    uni.removeStorageSync(CURRENT_LOCATION_CACHE_KEY)
    return null
  }

  if (!isSameCoordinate(cache.latitude, latitude) || !isSameCoordinate(cache.longitude, longitude)) {
    return null
  }

  return cache.location
}

function setCachedCurrentLocation(latitude: number, longitude: number, location: DiaryLocationInput) {
  const cache: CurrentLocationCache = {
    latitude,
    longitude,
    expiresAt: Date.now() + CURRENT_LOCATION_CACHE_TTL,
    location: {
      locationName: location.locationName,
      address: location.address,
      province: location.province,
      city: location.city,
      district: location.district
    }
  }

  uni.setStorageSync(CURRENT_LOCATION_CACHE_KEY, cache)
}

function isSameCoordinate(left: number, right: number) {
  return Math.abs(left - right) < 0.0001
}
