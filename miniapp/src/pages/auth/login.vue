<template>
  <view :class="['page-shell-safe login-page', themeClass]">
    <view class="login-page__hero">
      <view class="login-page__brand">Life Record</view>
      <view class="login-page__title">让记录变成一种长期关系</view>
      <view class="login-page__desc">
        使用微信身份进入，不打断后端 OpenID 登录链路。前端只负责拿到 code，再交给后端管理登录会话。
      </view>
    </view>

    <view class="login-page__actions">
      <u-button
        type="primary"
        shape="circle"
        color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
        :loading="loading"
        @click="handleLogin"
      >
        {{ loading ? '登录中...' : '微信登录' }}
      </u-button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
const { themeClass } = useTheme()

import { ref } from 'vue'
import { wxLogin } from '@/api/auth'
import { tokenStorage } from '@/utils/storage'

const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    const loginRes = await uni.login({ provider: 'weixin' })
    const result = await wxLogin(loginRes.code || '')
    tokenStorage.setAccessToken(result.accessToken)
    tokenStorage.setRefreshToken(result.refreshToken)
    uni.$feedback.success('登录成功')
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/home/index' })
    }, 300)
  } catch (error) {
    uni.$feedback.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.login-page__hero {
  padding-top: 80rpx;
}

.login-page__brand {
  color: var(--color-text-muted);
  font-size: 24rpx;
  letter-spacing: 6rpx;
  text-transform: uppercase;
}

.login-page__title {
  margin-top: 16rpx;
  color: var(--color-text-primary);
  font-size: 56rpx;
  font-weight: 700;
  line-height: 1.15;
}

.login-page__desc {
  margin-top: 18rpx;
  color: #72675c;
  font-size: 26rpx;
  line-height: 1.7;
}

.login-page__actions {
  margin-bottom: 80rpx;
}
</style>
