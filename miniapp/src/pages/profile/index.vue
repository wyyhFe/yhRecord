<template>
  <view class="page-shell-safe profile-page">
    <!-- Hero -->
    <view class="profile-hero">
      <view class="profile-hero__main">
        <view class="profile-hero__avatar">
          <image v-if="profile?.avatarPath" :src="profile.avatarPath" mode="aspectFill" class="profile-hero__avatar-img" />
          <text v-else class="profile-hero__avatar-text">{{ avatarText }}</text>
        </view>
        <view class="profile-hero__info">
          <text class="profile-hero__name">{{ profile?.nickname || '未设置昵称' }}</text>
          <text class="profile-hero__sign">{{ profile?.signature || '把生活慢慢记下来' }}</text>
        </view>
      </view>
      <view class="profile-hero__stats">
        <view class="profile-hero__stat">
          <text class="profile-hero__stat-value">{{ profile?.diaryCount ?? 0 }}</text>
          <text class="profile-hero__stat-label">日记</text>
        </view>
        <view class="profile-hero__stat">
          <text class="profile-hero__stat-value">{{ profile?.birthday || '--' }}</text>
          <text class="profile-hero__stat-label">生日</text>
        </view>
        <view class="profile-hero__stat">
          <text class="profile-hero__stat-value">{{ genderLabel }}</text>
          <text class="profile-hero__stat-label">性别</text>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="profile-card">
      <view class="profile-shortcuts">
        <view class="profile-shortcut" hover-class="profile-shortcut--pressed" @click="goEditProfile">
          <text class="profile-shortcut__icon">✏️</text>
          <text class="profile-shortcut__label">编辑资料</text>
        </view>
        <view class="profile-shortcut" hover-class="profile-shortcut--pressed" @click="goDiaryEditor">
          <text class="profile-shortcut__icon">📝</text>
          <text class="profile-shortcut__label">写日记</text>
        </view>
        <view class="profile-shortcut" hover-class="profile-shortcut--pressed" @click="goMemorial">
          <text class="profile-shortcut__icon">📅</text>
          <text class="profile-shortcut__label">纪念日</text>
        </view>
        <view class="profile-shortcut" hover-class="profile-shortcut--pressed" @click="goRecycle">
          <text class="profile-shortcut__icon">🗑️</text>
          <text class="profile-shortcut__label">回收站</text>
        </view>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="profile-card">
      <view class="profile-card__header">
        <text class="profile-card__title">设置</text>
      </view>
      <view class="profile-menu">
        <view v-for="item in menuItems" :key="item.key" class="profile-menu__item" @tap="handleSelect(item.key)">
          <view class="profile-menu__left">
            <text class="profile-menu__icon">{{ item.icon }}</text>
            <view class="profile-menu__info">
              <text class="profile-menu__name">{{ item.title }}</text>
              <text class="profile-menu__desc">{{ item.description }}</text>
            </view>
          </view>
          <text class="profile-menu__arrow">›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const profile = computed(() => appStore.profile)

const avatarText = computed(() => {
  const name = profile.value?.nickname?.trim()
  return name ? name.slice(0, 1) : '我'
})

const genderLabel = computed(() => {
  if (profile.value?.gender === 'MALE') return '男'
  if (profile.value?.gender === 'FEMALE') return '女'
  return '未设置'
})

const menuItems = [
  { key: 'profile', icon: '👤', title: '编辑个人信息', description: '昵称、头像、生日、签名' },
  { key: 'reminder', icon: '🔔', title: '提醒设置', description: '订阅消息和公众号提醒' },
  { key: 'memorial', icon: '📅', title: '纪念日管理', description: '重要日期和提醒配置' },
  { key: 'tag', icon: '🏷️', title: '标签管理', description: '日记和记账标签' },
  { key: 'recycle', icon: '🗑️', title: '回收站', description: '已删除内容可恢复' }
]

function goEditProfile() { uni.navigateTo({ url: '/pages/profile/edit' }) }
function goDiaryEditor() { uni.navigateTo({ url: '/pages/diary/editor' }) }
function goMemorial() { uni.navigateTo({ url: '/pages/memorialPage/index' }) }
function goRecycle() { uni.navigateTo({ url: '/pages/profile/recycle/index' }) }

function handleSelect(key: string) {
  const routes: Record<string, string> = {
    profile: '/pages/profile/edit',
    reminder: '/pages/profile/reminder',
    memorial: '/pages/memorialPage/index',
    tag: '/pages/profile/tags/index',
    recycle: '/pages/profile/recycle/index'
  }
  if (routes[key]) uni.navigateTo({ url: routes[key] })
}

onMounted(() => {
  appStore.loadProfile().catch(() => undefined)
})
</script>

<style scoped lang="scss">
.profile-page {
  padding-bottom: var(--space-10);
}

/* ========== Hero ========== */
.profile-hero {
  background: var(--color-primary-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-7) var(--space-6) var(--space-6);
  color: #fff;
}

.profile-hero__main {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.profile-hero__avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.profile-hero__avatar-img {
  width: 100%;
  height: 100%;
}

.profile-hero__avatar-text {
  font-size: 48rpx;
  font-weight: var(--weight-bold);
}

.profile-hero__info {
  flex: 1;
  min-width: 0;
}

.profile-hero__name {
  display: block;
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.profile-hero__sign {
  display: block;
  margin-top: var(--space-2);
  font-size: var(--font-meta);
  opacity: 0.85;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-hero__stats {
  display: flex;
  background: rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-medium);
  padding: var(--space-3) 0;
}

.profile-hero__stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.profile-hero__stat-value {
  font-size: var(--font-body);
  font-weight: var(--weight-bold);
}

.profile-hero__stat-label {
  font-size: var(--font-tiny);
  opacity: 0.8;
}

/* ========== 卡片 ========== */
.profile-card {
  margin: var(--space-4) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.profile-card__header {
  margin-bottom: var(--space-4);
}

.profile-card__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

/* ========== 快捷操作 ========== */
.profile-shortcuts {
  display: flex;
  justify-content: space-around;
}

.profile-shortcut {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3);
  transition: all var(--motion-fast) var(--ease-standard);
}

.profile-shortcut--pressed {
  transform: scale(0.92);
}

.profile-shortcut__icon {
  font-size: 44rpx;
}

.profile-shortcut__label {
  color: var(--color-text-secondary);
  font-size: var(--font-tiny);
}

/* ========== 菜单列表 ========== */
.profile-menu {
  display: flex;
  flex-direction: column;
}

.profile-menu__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) 0;
  border-bottom: 1rpx solid var(--color-divider);

  &:last-child {
    border-bottom: none;
  }
}

.profile-menu__left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1;
  min-width: 0;
}

.profile-menu__icon {
  font-size: 36rpx;
  flex-shrink: 0;
}

.profile-menu__info {
  flex: 1;
  min-width: 0;
}

.profile-menu__name {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.profile-menu__desc {
  display: block;
  margin-top: 4rpx;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.profile-menu__arrow {
  color: var(--color-text-muted);
  font-size: 32rpx;
  flex-shrink: 0;
}
</style>
