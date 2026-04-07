<template>
  <view class="page-shell-safe simple-ai-page">
    <view class="section-shell">
      <view class="simple-ai-page__title">账单分析页面已下线</view>
      <view class="simple-ai-page__desc">
        当前项目先只保留基础聊天能力。你可以进入聊天页，结合账本场景继续提问。
      </view>
      <u-button type="warning" @tap="goChat">进入基础聊天</u-button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app'

const scene = { value: '' }

function goChat() {
  const suffix = scene.value ? `?scene=${encodeURIComponent(scene.value)}` : ''
  uni.navigateTo({ url: `/pages/ai/index${suffix}` })
}

onLoad((query) => {
  const bookName = query?.bookName ? decodeURIComponent(String(query.bookName)) : ''
  scene.value = bookName ? `请结合账本 ${bookName} 帮我整理一下分析思路。` : '请帮我整理一下账单分析思路。'
})
</script>

<style scoped lang="scss">
.simple-ai-page {
  padding: 24rpx;
}

.simple-ai-page__title {
  color: #2b2118;
  font-size: 36rpx;
  font-weight: 700;
}

.simple-ai-page__desc {
  margin: 20rpx 0 32rpx;
  color: #6b5b4e;
  font-size: 28rpx;
  line-height: 1.8;
}
</style>
