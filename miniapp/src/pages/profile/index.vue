<template>
  <view class="page-shell-safe profile-page">
    <!-- 用户信息区 -->
    <view class="profile-user">
      <view class="profile-user__avatar">
        <image v-if="profile?.avatarPath" :src="profile.avatarPath" mode="aspectFill" class="profile-user__avatar-img" />
        <text v-else class="profile-user__avatar-text">{{ avatarText }}</text>
      </view>
      <text class="profile-user__name">{{ profile?.nickname || '未设置昵称' }}</text>
      <text class="profile-user__sign">{{ profile?.signature || '把生活慢慢记下来' }}</text>
    </view>

    <!-- 统计三列 -->
    <view class="profile-stats">
      <view class="profile-stats__item">
        <text class="profile-stats__value">{{ profile?.diaryCount ?? 0 }}</text>
        <text class="profile-stats__label">日记</text>
      </view>
      <view class="profile-stats__divider" />
      <view class="profile-stats__item">
        <text class="profile-stats__value">{{ joinDate }}</text>
        <text class="profile-stats__label">加入</text>
      </view>
      <view class="profile-stats__divider" />
      <view class="profile-stats__item">
        <text class="profile-stats__value">{{ genderLabel }}</text>
        <text class="profile-stats__label">性别</text>
      </view>
    </view>

    <!-- 设置菜单 -->
    <view class="profile-card">
      <view class="profile-card__header">
        <text class="profile-card__title">设置</text>
      </view>
      <view class="profile-menu">
        <view v-for="item in menuItems" :key="item.key" class="profile-menu__item" hover-class="profile-menu__item--pressed" @tap="handleSelect(item.key)">
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

const joinDate = computed(() => {
  if (!profile.value?.createdAt) return '--'
  const d = new Date(profile.value.createdAt)
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}`
})

const menuItems = [
  { key: 'profile', icon: '👤', title: '编辑个人信息', description: '昵称、头像、生日、签名' },
  { key: 'reminder', icon: '🔔', title: '提醒设置', description: '订阅消息和公众号提醒' },
  { key: 'memorial', icon: '📅', title: '纪念日管理', description: '重要日期和提醒配置' },
  { key: 'tag', icon: '🏷️', title: '标签管理', description: '日记、记账和打卡标签' },
  { key: 'recycle', icon: '🗑️', title: '回收站', description: '已删除内容可恢复' }
]

function handleSelect(key: string) {
  const routes: Record<string, string> = {
    profile: '/pages/profile/edit',
    reminder: '/pages/profile/reminder',
    memorial: '/pages/memorialPage/index',
    recycle: '/pages/profile/recycle/index'
  }
  if (routes[key]) {
    uni.navigateTo({ url: routes[key] })
    return
  }
  if (key === 'tag') {
    uni.showActionSheet({
      itemList: ['📝 日记标签', '💰 记账标签', '✅ 打卡标签'],
      success: (res) => {
        const type = ['DIARY', 'LEDGER', 'CHECKIN'][res.tapIndex]
        uni.navigateTo({ url: `/pages/profile/tags/index?moduleType=${type}` })
      }
    })
  }
}

onMounted(() => {
  appStore.loadProfile().catch(() => undefined)
})
</script>

<style scoped lang="scss">
.profile-page {
  padding-bottom: var(--bottom-padding);
}

/* ========== 用户信息 ========== */
.profile-user {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-8) var(--space-6) var(--space-5);
}

.profile-user__avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: var(--radius-full);
  background: var(--color-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin-bottom: var(--space-4);
  box-shadow: 0 0 0 6rpx var(--color-primary-soft);
}

.profile-user__avatar-img {
  width: 100%;
  height: 100%;
}

.profile-user__avatar-text {
  font-size: 52rpx;
  font-weight: var(--weight-bold);
  color: var(--color-primary);
}

.profile-user__name {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
  line-height: var(--leading-tight);
}

.profile-user__sign {
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

/* ========== 统计 ========== */
.profile-stats {
  margin: 0 var(--space-6) var(--space-4);
  display: flex;
  align-items: center;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-4) 0;
}

.profile-stats__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.profile-stats__value {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-bold);
}

.profile-stats__label {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.profile-stats__divider {
  width: 1rpx;
  height: 48rpx;
  background: var(--color-divider);
}

/* ========== 卡片 ========== */
.profile-card {
  margin: 0 var(--space-4) var(--space-3);
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
  transition: all var(--motion-fast) var(--ease-standard);

  &:last-child {
    border-bottom: none;
  }
}

.profile-menu__item--pressed {
  opacity: 0.6;
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
