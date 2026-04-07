<template>
  <view class="knowledge-chat-panel">
    <view class="knowledge-chat-panel__title">知识库能力已暂时移除</view>
    <view class="knowledge-chat-panel__desc">
      当前只保留基础聊天和会话管理，知识库聊天后续再单独接回。
    </view>
    <u-button type="warning" plain @tap="goChat">进入基础聊天</u-button>
  </view>
</template>

<script setup lang="ts">
function goChat() {
  uni.navigateTo({ url: '/pages/ai/index' })
}
</script>

<style scoped lang="scss">
.knowledge-chat-panel {
  padding: 32rpx 24rpx;
}

.knowledge-chat-panel__title {
  color: #2b2118;
  font-size: 34rpx;
  font-weight: 700;
}

.knowledge-chat-panel__desc {
  margin: 18rpx 0 28rpx;
  color: #6b5b4e;
  font-size: 26rpx;
  line-height: 1.8;
}
</style>
