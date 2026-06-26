<template>
  <u-popup
    :model-value="modelValue"
    mode="bottom"
    :mask-close-able="closable && !loading"
    border-radius="32"
    :safe-area-inset-bottom="true"
    @update:model-value="handlePopupChange"
  >
    <view class="login-sheet">
      <!-- 拖拽条 -->
      <view class="login-sheet__handle" />

      <!-- 关闭按钮 -->
      <view v-if="closable" class="login-sheet__close" @click="handleClose">
        <u-icon name="close" size="28" color="var(--color-text-muted)" />
      </view>

      <!-- 内容区 -->
      <view class="login-sheet__body">
        <!-- 图标 -->
        <view class="login-sheet__icon">
          <text class="login-sheet__icon-emoji">📖</text>
        </view>

        <!-- 标题 -->
        <text class="login-sheet__title">微信登录</text>

        <!-- 描述 -->
        <text class="login-sheet__desc">
          登录后即可同步日记、记账、打卡数据，随时随地记录生活
        </text>

        <!-- 登录按钮 -->
        <button
          class="login-sheet__btn"
          :loading="loading"
          :disabled="loading"
          hover-class="login-sheet__btn--pressed"
          :hover-stay-time="120"
          @click="handleLogin"
        >
          <text class="login-sheet__btn-label">
            {{ loading ? '登录中...' : '使用微信一键登录' }}
          </text>
        </button>

        <!-- 协议 -->
        <view class="login-sheet__agreement">
          <text class="login-sheet__agreement-text">登录即代表同意《</text>
          <text class="login-sheet__agreement-link" @click="handleOpenAgreement('user')">用户协议</text>
          <text class="login-sheet__agreement-text">》和《</text>
          <text class="login-sheet__agreement-link" @click="handleOpenAgreement('privacy')">隐私政策</text>
          <text class="login-sheet__agreement-text">》</text>
        </view>
      </view>
    </view>
  </u-popup>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { wxLogin } from '@/api/auth'
import { tokenStorage } from '@/utils/storage'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    closable?: boolean
  }>(),
  {
    closable: true
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const loading = ref(false)

function handlePopupChange(value: boolean) {
  if (!props.closable || loading.value) return
  emit('update:modelValue', value)
}

function handleClose() {
  if (loading.value) return
  emit('update:modelValue', false)
}

async function handleLogin() {
  if (loading.value) return
  loading.value = true
  try {
    const loginRes = await uni.login({ provider: 'weixin' })
    const result = await wxLogin(loginRes.code || '')
    tokenStorage.setAccessToken(result.accessToken)
    tokenStorage.setRefreshToken(result.refreshToken)
    tokenStorage.setTokenExpiresIn(result.expiresIn)
    emit('update:modelValue', false)
    emit('success')
    uni.$feedback.success('登录成功')
  } catch (error) {
    uni.$feedback.error(error, undefined, '登录失败，请稍后重试')
    console.error('[LoginSheet]', error)
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
.login-sheet {
  position: relative;
  padding: 0 40rpx calc(40rpx + env(safe-area-inset-bottom));
  background: var(--color-bg);
}

/* ── 拖拽条 ── */
.login-sheet__handle {
  width: 72rpx;
  height: 8rpx;
  border-radius: var(--radius-full);
  background: var(--color-border-strong);
  margin: 16rpx auto 0;
}

/* ── 关闭按钮 ── */
.login-sheet__close {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  z-index: 1;
}

/* ── 内容区 ── */
.login-sheet__body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20rpx;
}

/* ── 图标 ── */
.login-sheet__icon {
  width: 128rpx;
  height: 128rpx;
  border-radius: var(--radius-xlarge);
  background: var(--color-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-sheet__icon-emoji {
  font-size: 64rpx;
  line-height: 1;
}

/* ── 标题 ── */
.login-sheet__title {
  margin-top: 24rpx;
  color: var(--color-text-primary);
  font-size: var(--font-hero);
  font-weight: var(--weight-bold);
  line-height: var(--leading-tight);
  letter-spacing: -1rpx;
}

/* ── 描述 ── */
.login-sheet__desc {
  margin-top: 16rpx;
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  line-height: var(--leading-relaxed);
  text-align: center;
  max-width: 480rpx;
}

/* ── 微信登录按钮 ── */
.login-sheet__btn {
  width: 100%;
  height: 96rpx;
  margin-top: 40rpx;
  border-radius: var(--radius-large);
  background: #07C160;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  outline: none;
  padding: 0;
  line-height: 1;
  box-shadow: 0 6rpx 24rpx rgba(7, 193, 96, 0.35);
  transition: opacity var(--motion-fast) var(--ease-standard);
}

.login-sheet__btn::after {
  display: none;
}

.login-sheet__btn--pressed {
  opacity: 0.85;
  transform: scale(0.98);
}

.login-sheet__btn[disabled] {
  opacity: 0.6;
}

.login-sheet__btn-label {
  color: #ffffff;
  font-size: var(--font-section);
  font-weight: var(--weight-semibold);
}

/* ── 协议 ── */
.login-sheet__agreement {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 32rpx;
  padding-bottom: 8rpx;
}

.login-sheet__agreement-text {
  color: var(--color-text-muted);
  font-size: var(--font-caption);
  line-height: var(--leading-snug);
}

.login-sheet__agreement-link {
  color: #4a90d9;
  font-size: var(--font-caption);
  line-height: var(--leading-snug);
}
</style>
