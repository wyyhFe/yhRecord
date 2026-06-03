<template>
  <view class="page-shell-safe">
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
        <view class="btn-secondary" @click="goEditProfile">编辑个人信息</view>
        <view class="btn-primary" @click="goDiaryEditor">去写日记</view>
        <!-- 个人开发者主体暂不能上线 AI 聊天类目，入口先屏蔽；想放开把 aiChatEntryEnabled 改为 true -->
        <view
          v-if="aiChatEntryEnabled"
          class="btn-ghost profile-hero__ai-trigger"
          @click="openAiPopup"
        >
          AI 助手
        </view>
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

const appStore = useAppStore()
const profile = computed(() => appStore.profile)
const showAiPopup = ref(false)
// 个人开发者主体不能上线 AI 聊天，先把入口屏蔽；恢复时改成 true 即可，弹层和跳转逻辑保留
const aiChatEntryEnabled = false

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
/*
 * 个人中心 Hero：渐变背景 + 圆角底部
 * 使用主色渐变营造温暖氛围
 */
.profile-hero {
  background:
    radial-gradient(circle at top right, rgba(191, 123, 94, 0.15), transparent 50%),
    linear-gradient(160deg, #E8D5C4 0%, var(--color-surface) 50%);
  border-radius: var(--radius-xlarge);
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
  color: var(--color-text-secondary);
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
  background: var(--color-surface);
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
  background: var(--color-border-strong);
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
    radial-gradient(circle at top right, var(--color-primary-soft), transparent 40%),
    linear-gradient(180deg, var(--color-surface) 0%, var(--color-surface-soft) 100%);
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

</style>
