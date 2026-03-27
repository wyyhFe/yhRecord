import { DIARY_MOOD_OPTIONS, DIARY_WEATHER_OPTIONS, type DiarySelectorOption } from '@/config/diary'

function resolveDiaryOptionLabel(options: DiarySelectorOption[], value?: string, fallback = '') {
  if (!value) return fallback
  return options.find((item) => item.value === value)?.label || value
}

export function resolveDiaryMoodLabel(value?: string, fallback = '') {
  return resolveDiaryOptionLabel(DIARY_MOOD_OPTIONS, value, fallback)
}

export function resolveDiaryWeatherLabel(value?: string, fallback = '') {
  return resolveDiaryOptionLabel(DIARY_WEATHER_OPTIONS, value, fallback)
}
