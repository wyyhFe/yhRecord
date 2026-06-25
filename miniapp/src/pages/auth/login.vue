<template>
  <view class="page-shell-safe login-page">
    <!-- Flex 撑满全屏，top 居中 + bottom 固定在底部 -->
    <view class="login-main">
      <!-- Logo + 品牌区 -->
      <view class="login-brand">
        <view class="login-logo">
          <text class="login-logo__emoji">📖</text>
        </view>
        <text class="login-brand__name">生活记录</text>
        <text class="login-brand__tagline">记录生活的每个瞬间</text>
      </view>
    </view>

    <!-- 底部操作区 -->
    <view class="login-footer">
      <!-- 微信登录按钮 -->
      <button
        class="login-btn"
        :loading="loading"
        :disabled="loading"
        hover-class="login-btn--pressed"
        :hover-stay-time="120"
        @click="handleLogin"
      >
        <text class="login-btn__icon">💬</text>
        <text class="login-btn__label">{{ loading ? '登录中...' : '微信一键登录' }}</text>
      </button>

      <!-- 协议 -->
      <view class="login-agreement">
        <text class="login-agreement__text">登录即代表同意《</text>
        <text class="login-agreement__link" @click="handleOpenAgreement('user')">用户协议</text>
        <text class="login-agreement__text">》和《</text>
        <text class="login-agreement__link" @click="handleOpenAgreement('privacy')">隐私政策</text>
        <text class="login-agreement__text">》</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { wxLogin } from '@/api/auth'
import { tokenStorage } from '@/utils/storage'

const loading = ref(false)

async function handleLogin() {
  if (loading.value) return
  loading.value = true
  try {
    const loginRes = await uni.login({ provider: 'weixin' })
    const result = await wxLogin(loginRes.code || '')
    tokenStorage.setAccessToken(result.accessToken)
    tokenStorage.setRefreshToken(result.refreshToken)
    uni.$feedback.success('登录成功')
    // 登录成功后返回上一页；若已是首页则切到 tab（home 在 tabBar 中）
    const pages = getCurrentPages()
    if (pages.length > 1) {
      uni.navigateBack()
    } else {
      uni.switchTab({ url: '/pages/home/index' })
    }
  } catch (error) {
    uni.$feedback.error(error, undefined, '登录失败，请稍后重试')
    console.error('[Login]', error)
  } finally {
    loading.value = false
  }
}

function handleOpenAgreement(type: 'user' | 'privacy') {
  // TODO: 打开协议页面
  uni.showToast({ title: type === 'user' ? '用户协议' : '隐私政策', icon: 'none' })
}
</script>

<style scoped lang="scss">
.login-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--color-bg-gradient, linear-gradient(180deg, #F8F5F1 0%, #F0EDEA 100%));
}

/* ---------- 主区域：垂直居中 ---------- */
.login-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 var(--space-8);
}

.login-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
}

.login-logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: var(--radius-xlarge);
  background: var(--color-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-1);
}

.login-logo__emoji {
  font-size: 72rpx;
  line-height: 1;
}

.login-brand__name {
  color: var(--color-text-primary);
  font-size: var(--font-hero);
  font-weight: var(--weight-bold);
  line-height: var(--leading-tight);
  letter-spacing: -1rpx;
}

.login-brand__tagline {
  color: var(--color-text-secondary);
  font-size: var(--font-section);
  line-height: var(--leading-relaxed);
  margin-top: var(--space-1);
}

/* ---------- 底部操作区 ---------- */
.login-footer {
  padding: 0 var(--space-8) var(--space-10);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
}

.login-btn {
  width: 560rpx;
  height: 96rpx;
  border-radius: var(--radius-large);
  background: #07C160;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border: none;
  outline: none;
  padding: 0;
  position: relative;
  transition: opacity var(--motion-fast) var(--ease-standard);
  /* 重置 button 默认样式 */
  line-height: 1;
  box-shadow: 0 6rpx 20rpx rgba(7, 193, 96, 0.3);
}

.login-btn::after {
  display: none; /* 清除 uni-app button 伪元素边框 */
}

.login-btn--pressed {
  opacity: 0.85;
  transform: scale(0.97);
}

.login-btn[disabled] {
  opacity: 0.6;
}

.login-btn__icon {
  font-size: 36rpx;
  line-height: 1;
}

.login-btn__label {
  color: #ffffff;
  font-size: var(--font-section);
  font-weight: var(--weight-semibold);
}

/* ---------- 协议文本 ---------- */
.login-agreement {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  padding-bottom: var(--space-5);
}

.login-agreement__text {
  color: var(--color-text-muted);
  font-size: var(--font-caption);
  line-height: var(--leading-snug);
}

.login-agreement__link {
  color: #4a90d9;
  font-size: var(--font-caption);
  line-height: var(--leading-snug);
}
</style>
