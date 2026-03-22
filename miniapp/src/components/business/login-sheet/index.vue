<template>
  <view v-if="modelValue" class="fixed inset-0 z-50">
    <view class="absolute inset-0 bg-black/30" @tap="handleMaskTap" />
    <view class="absolute bottom-0 left-0 right-0 rounded-t-[36rpx] bg-[#fffaf4] px-[32rpx] pb-[48rpx] pt-[36rpx] shadow-[0_-16rpx_40rpx_rgba(0,0,0,0.08)]">
      <view class="mx-auto mb-[24rpx] h-[8rpx] w-[88rpx] rounded-full bg-[#e7d7c7]" />
      <view class="text-[38rpx] font-semibold text-[#2b2118]">微信登录</view>
      <view class="mt-[16rpx] text-[26rpx] leading-[1.7] text-[#6b5b4e]">
        登录后即可同步你的日记、记账、打卡和提醒设置。
      </view>
      <view class="mt-[28rpx] rounded-[28rpx] bg-[#f7efe4] p-[24rpx] text-[24rpx] leading-[1.7] text-[#8a735f]">
        当前使用微信身份登录。前端只负责获取登录 code，业务 token 和会话由后端统一维护。
      </view>

      <BaseButton class="mt-[36rpx]" :disabled="loading" @tap="handleLogin">
        {{ loading ? '登录中...' : '使用微信登录' }}
      </BaseButton>

      <view class="mt-[20rpx] text-center text-[22rpx] text-[#9c8b7b]">
        关闭后仍可浏览首页，但进入日记、记账、打卡等页面时需要先登录。
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import BaseButton from '@/components/base/base-button'
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

/**
 * 蒙层关闭只在允许关闭时生效，避免强制授权场景误关。
 */
function handleMaskTap() {
  if (!props.closable || loading.value) {
    return
  }
  emit('update:modelValue', false)
}

/**
 * 调起微信登录，换取后端业务 token。
 */
async function handleLogin() {
  loading.value = true
  try {
    const loginRes = await uni.login({ provider: 'weixin' })
    const result = await wxLogin(loginRes.code || '')
    tokenStorage.setAccessToken(result.accessToken)
    tokenStorage.setRefreshToken(result.refreshToken)
    emit('update:modelValue', false)
    emit('success')
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })
  } catch (error) {
    uni.showToast({
      title: '登录失败，请稍后重试',
      icon: 'none'
    })
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>
