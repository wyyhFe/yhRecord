<template>
  <view class="page-shell flex min-h-screen flex-col justify-between">
    <view class="pt-[80rpx]">
      <view class="text-[24rpx] uppercase tracking-[6rpx] text-[#9b866d]">Life Record</view>
      <view class="mt-[16rpx] text-[56rpx] font-bold leading-[1.15] text-ink">让记录变成一种长期关系</view>
      <view class="mt-[18rpx] text-[26rpx] leading-[1.7] text-[#72675c]">
        使用微信身份进入，不打断后端 OpenID 登录链路。前端只负责拿到 code，再交给后端管理登录会话。
      </view>
    </view>

    <view class="mb-[80rpx]">
      <BaseButton :disabled="loading" @tap="handleLogin">
        {{ loading ? '登录中...' : '微信登录' }}
      </BaseButton>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import BaseButton from '@/components/base/base-button'
import { wxLogin } from '@/api/auth'
import { tokenStorage } from '@/utils/storage'

const loading = ref(false)

/**
 * 登录页只做一件事：调用 `uni.login` 拿到 code，再交给后端换取业务 token。
 */
async function handleLogin() {
  loading.value = true
  try {
    const loginRes = await uni.login({ provider: 'weixin' })
    const result = await wxLogin(loginRes.code || '')
    tokenStorage.setAccessToken(result.accessToken)
    tokenStorage.setRefreshToken(result.refreshToken)
    uni.switchTab({ url: '/pages/home/index' })
  } catch (error) {
    uni.showToast({
      title: '登录失败',
      icon: 'none'
    })
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>
