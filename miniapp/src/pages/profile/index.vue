<template>
  <view :class="['page-shell-safe', themeClass]">
    <view class="section-shell profile-hero">
      <view class="profile-hero__main">
        <view class="profile-hero__avatar">
          <image
            v-if="profile?.avatarPath"
            :src="profile.avatarPath"
            mode="aspectFill"
            class="profile-hero__avatar-image"
          />
          <view v-else class="profile-hero__avatar-text">{{ avatarText }}</view>
        </view>

        <view class="profile-hero__copy">
          <view class="profile-hero__name">{{ profile?.nickname || '还没有填写昵称' }}</view>
          <view class="profile-hero__signature">
            {{ profile?.signature || '把生活慢慢记下来，时间会替你整理它。' }}
          </view>
          <view class="profile-hero__meta">
            <view class="profile-hero__meta-item">生日 {{ profile?.birthday || '未设置' }}</view>
            <view class="profile-hero__meta-item">性别 {{ genderLabel }}</view>
          </view>
        </view>
      </view>

      <view class="profile-hero__actions">
        <u-button plain shape="circle" :hair-line="false" @click="goEditProfile">编辑个人信息</u-button>
        <u-button
          type="primary"
          shape="circle"
          :hair-line="false"
          color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
          @click="goDiaryEditor"
        >
          去写日记
        </u-button>
        <u-button
          class="profile-hero__ai-trigger"
          plain
          shape="circle"
          :hair-line="false"
          @click="openAiPopup"
        >
          AI 助手
        </u-button>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-copy">
        <view class="section-copy__title">我的记录</view>
        <view class="section-copy__desc">把当前账号最常用的数据和状态集中放在同一个页面里。</view>
      </view>

      <view class="metric-grid">
        <view v-for="item in metrics" :key="item.label" class="metric-card">
          <view class="metric-card__label">{{ item.label }}</view>
          <view class="metric-card__value">{{ item.value }}</view>
          <view class="metric-card__hint">{{ item.hint }}</view>
        </view>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-copy">
        <view class="section-copy__title">外观主题</view>
        <view class="section-copy__desc">选择喜欢的视觉风格，整页颜色立即生效。</view>
      </view>

      <view class="theme-picker-grid">
        <view
          v-for="item in themeOptions"
          :key="item.id"
          :class="['theme-picker-card', `theme-${item.id}`, { 'theme-picker-card--active': item.id === active }]"
          @tap="handleThemeChange(item.id)"
        >
          <!-- 用 var(--color-primary) 等变量取该卡片自己的主题色，预览即所见 -->
          <view class="theme-picker-card__swatches">
            <view class="theme-picker-card__swatch theme-picker-card__swatch--primary" />
            <view class="theme-picker-card__swatch theme-picker-card__swatch--accent" />
            <view class="theme-picker-card__swatch theme-picker-card__swatch--surface" />
          </view>
          <view class="theme-picker-card__label">{{ item.label }}</view>
          <view class="theme-picker-card__desc">{{ item.description }}</view>
          <view v-if="item.id === active" class="theme-picker-card__badge">已启用</view>
        </view>
      </view>
    </view>

    <view class="page-section section-shell overflow-hidden">
      <view class="section-copy">
        <view class="section-copy__title">常用入口</view>
        <view class="section-copy__desc">提醒、纪念日、标签和回收站都集中放在这里。</view>
      </view>

      <view class="profile-entry-list">
        <view
          v-for="item in settings"
          :key="item.key"
          class="profile-entry-card"
          @tap="handleSelect(item.key)"
        >
          <view class="profile-entry-card__main">
            <view class="profile-entry-card__title">{{ item.title }}</view>
            <view class="profile-entry-card__desc">{{ item.description }}</view>
          </view>
          <view class="profile-entry-card__value">{{ item.value }}</view>
        </view>
      </view>
    </view>

    <u-popup
      :model-value="showAiPopup"
      mode="bottom"
      border-radius="36"
      @update:model-value="handleAiPopupChange"
    >
      <view class="ai-entry-popup">
        <view class="ai-entry-popup__handle" />

        <view class="ai-entry-popup__title">AI 聊天</view>
        <view class="ai-entry-popup__desc">当前只保留基础聊天入口，方便单独学习和调试流式会话能力。</view>

        <view class="ai-entry-popup__list">
          <view class="ai-entry-card" @tap="goAiChat">
            <view class="ai-entry-card__main">
              <view class="ai-entry-card__title">基础聊天</view>
              <view class="ai-entry-card__desc">只保留会话列表、消息窗口和流式发送，其他能力暂时移除。</view>
            </view>
            <view class="ai-entry-card__value">进入</view>
          </view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAppStore } from '@/stores/app'
import { ALL_THEMES, THEME_META, useTheme, type ThemeId } from '@/composables/useTheme'

const appStore = useAppStore()
const profile = computed(() => appStore.profile)
const showAiPopup = ref(false)

// 主题切换：themeClass 绑到根 view 上保证整页随激活主题变色。
const { themeClass, active, setTheme } = useTheme()

// 给 UI 用的主题选项列表，顺序与 ALL_THEMES 保持一致便于以后增减。
const themeOptions = ALL_THEMES.map((id) => ({
  id,
  label: THEME_META[id].label,
  description: THEME_META[id].description
}))

function handleThemeChange(theme: ThemeId) {
  setTheme(theme)
}

const avatarText = computed(() => {
  const name = profile.value?.nickname?.trim()
  return name ? name.slice(0, 1) : '我'
})

const genderLabel = computed(() => {
  if (profile.value?.gender === 'MALE') return '男'
  if (profile.value?.gender === 'FEMALE') return '女'
  return '未设置'
})

const metrics = computed(() => [
  {
    label: '记录天数',
    value: String(profile.value?.diaryCount ?? 0),
    hint: '累计写下的日子'
  },
  {
    label: '生日',
    value: profile.value?.birthday || '--',
    hint: '用于展示个人资料'
  },
  {
    label: '签名',
    value: profile.value?.signature ? '已设置' : '未设置',
    hint: '表达现在的状态'
  }
])

const settings = [
  {
    key: 'profile',
    title: '编辑个人信息',
    description: '修改昵称、生日、性别和个性签名。',
    value: '前往'
  },
  {
    key: 'reminder',
    title: '提醒设置',
    description: '统一管理小程序订阅提醒和公众号提醒。',
    value: '配置'
  },
  {
    key: 'memorial',
    title: '纪念日管理',
    description: '维护重要日期，提醒、首页和去年今日都会复用这里的数据。',
    value: '进入'
  },
  {
    key: 'tag',
    title: '标签管理',
    description: '基于系统模板扩展自己的日记和记账标签。',
    value: '管理'
  },
  {
    key: 'recycle',
    title: '回收站',
    description: '删除后的内容会暂存到这里，可恢复或彻底删除。',
    value: '进入'
  }
]

function goEditProfile() {
  uni.navigateTo({ url: '/pages/profile/edit' })
}

function goDiaryEditor() {
  uni.navigateTo({ url: '/pages/diary/editor' })
}

function openAiPopup() {
  showAiPopup.value = true
}

function handleAiPopupChange(value: boolean) {
  showAiPopup.value = value
}

function goAiChat() {
  showAiPopup.value = false
  uni.navigateTo({ url: '/pages/ai/index' })
}

function handleSelect(key: string) {
  if (key === 'profile') {
    goEditProfile()
    return
  }

  if (key === 'recycle') {
    uni.navigateTo({ url: '/pages/profile/recycle/index' })
    return
  }

  if (key === 'reminder') {
    uni.navigateTo({ url: '/pages/profile/reminder' })
    return
  }

  if (key === 'memorial') {
    uni.navigateTo({ url: '/pages/memorialPage/index' })
    return
  }

  if (key === 'tag') {
    uni.navigateTo({ url: '/pages/profile/tags/index' })
  }
}

onMounted(() => {
  appStore.loadProfile().catch(() => undefined)
})
</script>

<style scoped lang="scss">
.profile-hero {
  background:
    radial-gradient(circle at top right, rgba(234, 201, 160, 0.24), transparent 34%),
    linear-gradient(135deg, var(--color-surface) 0%, rgba(252, 244, 234, 0.96) 100%);
}

.profile-hero__main {
  display: flex;
  align-items: center;
  gap: 22rpx;
}

.profile-hero__avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 14rpx 28rpx rgba(144, 88, 49, 0.18);
}

.profile-hero__avatar-image {
  width: 100%;
  height: 100%;
}

.profile-hero__avatar-text {
  color: var(--color-bg);
  font-size: 42rpx;
  font-weight: 700;
}

.profile-hero__copy {
  min-width: 0;
  flex: 1;
}

.profile-hero__name {
  color: var(--color-text-primary);
  font-size: 38rpx;
  font-weight: 700;
}

.profile-hero__signature {
  margin-top: 10rpx;
  color: #6d5b4c;
  font-size: 24rpx;
  line-height: 1.7;
}

.profile-hero__meta {
  margin-top: 14rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.profile-hero__meta-item {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.78);
  color: var(--color-text-muted);
  font-size: 22rpx;
}

.profile-hero__actions {
  margin-top: 24rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.profile-hero__ai-trigger {
  grid-column: 1 / -1;
}

.profile-entry-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.profile-entry-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  padding: 22rpx 20rpx;
  border-radius: 24rpx;
  background: var(--color-bg);
  border: 1rpx solid var(--color-border);
}

.profile-entry-card__main {
  min-width: 0;
  flex: 1;
}

.profile-entry-card__title {
  color: var(--color-text-primary);
  font-size: 28rpx;
  font-weight: 700;
}

.profile-entry-card__desc {
  margin-top: 10rpx;
  color: var(--color-text-secondary);
  font-size: 23rpx;
  line-height: 1.6;
}

.profile-entry-card__value {
  flex-shrink: 0;
  color: var(--color-primary-strong);
  font-size: 24rpx;
  font-weight: 600;
}

.ai-entry-popup {
  padding: 30rpx 28rpx 48rpx;
}

.ai-entry-popup__handle {
  width: 88rpx;
  height: 8rpx;
  margin: 0 auto;
  border-radius: 999rpx;
  background: #e7d7c7;
}

.ai-entry-popup__title {
  margin-top: 24rpx;
  color: var(--color-text-primary);
  font-size: 36rpx;
  font-weight: 700;
}

.ai-entry-popup__desc {
  margin-top: 14rpx;
  color: var(--color-text-secondary);
  font-size: 24rpx;
  line-height: 1.7;
}

.ai-entry-popup__list {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.ai-entry-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  padding: 24rpx 22rpx;
  border-radius: 24rpx;
  background:
    radial-gradient(circle at top right, rgba(215, 166, 72, 0.12), transparent 40%),
    linear-gradient(180deg, #fffdf8 0%, #fcf4ea 100%);
  border: 1rpx solid var(--color-border);
}

.ai-entry-card__main {
  min-width: 0;
  flex: 1;
}

.ai-entry-card__title {
  color: var(--color-text-primary);
  font-size: 28rpx;
  font-weight: 700;
}

.ai-entry-card__desc {
  margin-top: 10rpx;
  color: var(--color-text-secondary);
  font-size: 23rpx;
  line-height: 1.6;
}

.ai-entry-card__value {
  flex-shrink: 0;
  color: var(--color-primary-strong);
  font-size: 24rpx;
  font-weight: 600;
}

/* ============== 外观主题切换器 ============== */
/* 网格在主题切换时不需要动，所以放外层（不在 .theme-{id} 卡片内）。 */
.theme-picker-grid {
  margin-top: var(--space-4);
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

/*
 * 每张卡片自己带 .theme-{id} class，让 CSS 变量在卡内被该主题覆盖。
 * 这样卡片的 swatches 直接 var(--color-primary) 就能预览真实色，
 * 用户当前用什么主题不影响这些预览。
 */
.theme-picker-card {
  position: relative;
  padding: var(--space-4) var(--space-3);
  border-radius: var(--radius-large);
  background: var(--color-surface);
  border: 2rpx solid var(--color-border);
  transition: border-color var(--motion-fast) var(--ease-standard),
    box-shadow var(--motion-fast) var(--ease-standard);
}

.theme-picker-card--active {
  /* 激活态用稍强的边框色 + 阴影，向用户明示当前选中 */
  border-color: var(--color-primary);
  box-shadow: var(--shadow-card);
}

.theme-picker-card__swatches {
  display: flex;
  gap: var(--space-1);
  margin-bottom: var(--space-3);
}

.theme-picker-card__swatch {
  width: 36rpx;
  height: 36rpx;
  border-radius: var(--radius-full);
  border: 1rpx solid var(--color-border);
}

.theme-picker-card__swatch--primary {
  background: var(--color-primary);
}

.theme-picker-card__swatch--accent {
  background: var(--color-accent);
}

.theme-picker-card__swatch--surface {
  background: var(--color-surface-soft);
}

.theme-picker-card__label {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-semibold);
}

.theme-picker-card__desc {
  margin-top: var(--space-1);
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  line-height: var(--leading-snug);
}

.theme-picker-card__badge {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
  padding: 4rpx var(--space-2);
  border-radius: var(--radius-full);
  background: var(--color-primary);
  color: var(--color-text-inverse);
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
}
</style>
