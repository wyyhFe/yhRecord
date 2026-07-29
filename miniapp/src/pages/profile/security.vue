<template>
  <view class="page-shell-safe security-page">
    <!-- 状态卡片 -->
    <view class="security-card">
      <view class="security-card__header">
        <text class="security-card__title">手机号绑定</text>
      </view>

      <!-- 已绑定 -->
      <view v-if="profile?.phone" class="security-card__body">
        <view class="security-card__status security-card__status--bound">
          <text class="security-card__status-icon">✅</text>
          <text class="security-card__status-text">已绑定</text>
        </view>
        <text class="security-card__phone">{{ maskedPhone }}</text>
        <text class="security-card__hint">
          可使用手机号 + 密码在网页管理后台登录
        </text>
      </view>

      <!-- 未绑定 -->
      <view v-else class="security-card__body">
        <!-- TODO: 个人主体小程序暂不可用 getPhoneNumber，代码保留待主体升级后启用 -->
        <view class="security-card__status security-card__status--unbound">
          <text class="security-card__status-icon">⚠️</text>
          <text class="security-card__status-text">未绑定（暂不可用）</text>
        </view>
        <text class="security-card__desc">
          绑定手机号后，可使用手机号 + 密码在网页管理后台登录。初始密码为手机号后 6 位。
        </text>

        <button
          class="security-card__phone-btn"
          open-type="getPhoneNumber"
          :loading="loading"
          :disabled="loading"
          @getphonenumber="onGetPhoneNumber"
        >
          {{ loading ? '绑定中...' : '微信授权获取手机号' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useAppStore } from '@/stores/app'
import { bindPhone } from '@/api/user'

const appStore = useAppStore()
const profile = computed(() => appStore.profile)

const loading = ref(false)

const maskedPhone = computed(() => {
  const p = profile.value?.phone
  if (!p) return ''
  return p.slice(0, 3) + '****' + p.slice(-4)
})

async function onGetPhoneNumber(event: { detail: { code?: string; errMsg?: string } }) {
  const code = event.detail?.code
  if (!code) {
    uni.showToast({ title: '授权已取消', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await bindPhone({ code })
    await appStore.loadProfile()
    uni.showToast({ title: '绑定成功', icon: 'success' })
  } catch (error) {
    uni.$feedback.error(error, undefined, '绑定失败，请稍后重试')
    console.error('[Security]', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.security-page {
  padding-bottom: var(--bottom-padding-with-tabbar);
}

.security-card {
  margin: var(--space-6);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.security-card__header {
  margin-bottom: var(--space-4);
}

.security-card__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.security-card__body {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.security-card__status {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.security-card__status-icon {
  font-size: 36rpx;
}

.security-card__status-text {
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.security-card__status--bound .security-card__status-text {
  color: #07C160;
}

.security-card__status--unbound .security-card__status-text {
  color: var(--color-warning);
}

.security-card__phone {
  color: var(--color-text-primary);
  font-size: var(--font-hero);
  font-weight: var(--weight-bold);
  letter-spacing: 2rpx;
  margin-bottom: var(--space-3);
}

.security-card__hint {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
  text-align: center;
  line-height: var(--leading-relaxed);
}

.security-card__desc {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  text-align: center;
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-5);
  max-width: 500rpx;
}

.security-card__phone-btn {
  width: 100%;
  height: 96rpx;
  border-radius: var(--radius-large);
  background: #07C160;
  color: #ffffff;
  font-size: var(--font-section);
  font-weight: var(--weight-semibold);
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  outline: none;
  padding: 0;
  line-height: 1;
  box-shadow: 0 6rpx 24rpx rgba(7, 193, 96, 0.35);
}

.security-card__phone-btn::after {
  display: none;
}

.security-card__phone-btn[disabled] {
  opacity: 0.6;
}
</style>
