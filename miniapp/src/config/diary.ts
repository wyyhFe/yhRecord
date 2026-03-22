export interface DiarySelectorOption {
  label: string
  value: string
  icon?: string
}

export const DIARY_WEATHER_OPTIONS: DiarySelectorOption[] = [
  { label: '晴天', value: '晴天', icon: '☀️' },
  { label: '多云', value: '多云', icon: '☁️' },
  { label: '阴天', value: '阴天', icon: '🌥️' },
  { label: '小雨', value: '小雨', icon: '🌦️' },
  { label: '中雨', value: '中雨', icon: '🌧️' },
  { label: '大雨', value: '大雨', icon: '⛈️' },
  { label: '雷雨', value: '雷雨', icon: '🌩️' },
  { label: '小雪', value: '小雪', icon: '❄️' },
  { label: '大雪', value: '大雪', icon: '☃️' },
  { label: '大风', value: '大风', icon: '💨' },
  { label: '雾', value: '雾', icon: '🌫️' },
  { label: '雨夹雪', value: '雨夹雪', icon: '🌨️' }
]

export const DIARY_MOOD_OPTIONS: DiarySelectorOption[] = [
  { label: '开心', value: '开心', icon: '😊' },
  { label: '大笑', value: '大笑', icon: '😄' },
  { label: '平静', value: '平静', icon: '😌' },
  { label: '疲惫', value: '疲惫', icon: '🥱' },
  { label: '伤心', value: '伤心', icon: '😢' },
  { label: '生气', value: '生气', icon: '😠' },
  { label: '惊讶', value: '惊讶', icon: '😲' },
  { label: '期待', value: '期待', icon: '🤗' },
  { label: '感动', value: '感动', icon: '🥹' },
  { label: '悠闲', value: '悠闲', icon: '😎' },
  { label: '郁闷', value: '郁闷', icon: '😔' },
  { label: '幸福', value: '幸福', icon: '🥳' }
]
