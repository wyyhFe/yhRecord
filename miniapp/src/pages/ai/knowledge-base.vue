<template>
  <view class="page-shell-safe simple-ai-page">
    <view class="section-shell">
      <view class="simple-ai-page__title">知识库页面已下线</view>
      <view class="simple-ai-page__desc">
        当前项目先只保留基础聊天和会话能力，知识库相关内容后续再单独接入。
      </view>
      <u-button type="warning" @tap="goChat">进入基础聊天</u-button>
    </view>
  </view>
</template>

<script setup lang="ts">
function goChat() {
  uni.navigateTo({ url: '/pages/ai/index' })
}
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
