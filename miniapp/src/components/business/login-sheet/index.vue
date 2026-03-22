<template>
  <u-popup
    :model-value="modelValue"
    mode="bottom"
    :mask-close-able="closable && !loading"
    border-radius="36"
    @update:model-value="handlePopupChange"
  >
    <view class="px-[32rpx] pb-[48rpx] pt-[36rpx]">
      <view class="mx-auto h-[8rpx] w-[88rpx] rounded-full bg-[#e7d7c7]" />

      <view class="mt-[24rpx] text-[38rpx] font-semibold text-[#2b2118]">微信登录</view>
      <view class="mt-[16rpx] text-[26rpx] leading-[1.7] text-[#6b5b4e]">
        登录后即可同步你的日记、记账、打卡和提醒设置，后端会统一维护当前设备的会话状态。
      </view>

      <view class="mt-[24rpx] rounded-[28rpx] bg-[#f7efe4] px-[24rpx] py-[24rpx]">
        <view class="flex items-start gap-[12rpx]">
          <u-icon name="weixin-fill" size="36" color="#4b6b57" />
          <view class="flex-1 text-[24rpx] leading-[1.8] text-[#8a735f]">
            当前通过微信身份完成登录，前端只负责获取临时 code，真正的 accessToken、refreshToken
            和单设备会话都由后端统一签发。
          </view>
        </view>
      </view>

      <u-button
        class="mt-[36rpx]"
        type="primary"
        shape="circle"
        color="linear-gradient(135deg, #c47c52 0%, #d7a648 100%)"
        :loading="loading"
        @click="handleLogin"
      >
        {{ loading ? '登录中...' : '使用微信登录' }}
      </u-button>

      <view class="mt-[20rpx] text-center text-[22rpx] text-[#9c8b7b]">
        关闭后仍可浏览首页，但进入日记、记账、打卡等页面时需要先登录。
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
  if (!props.closable || loading.value) {
    return
  }
  emit('update:modelValue', value)
}

async function handleLogin() {
  loading.value = true
  try {
    const loginRes = await uni.login({ provider: 'weixin' })
    const result = await wxLogin(loginRes.code || '')
    tokenStorage.setAccessToken(result.accessToken)
    tokenStorage.setRefreshToken(result.refreshToken)
    emit('update:modelValue', false)
    emit('success')
    uni.$feedback.success('登录成功')
  } catch (error) {
    uni.$feedback.error(error, undefined, '登录失败，请稍后重试')
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>
