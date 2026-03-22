<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">去年今天</view>
          <view class="section-copy__desc">同一天发生过什么，会重新连成线。</view>
        </view>
      </view>

      <view class="note-card">
        <view class="memory-title">春天第一次去植物园</view>
        <view class="memory-body">
          那天太阳很好，拍了很多树影，回去路上还买了面包。
        </view>
        <view class="memory-tags">
          <u-tag text="开心" plain type="success" shape="circle" />
          <u-tag text="2025-03-20" plain type="warning" shape="circle" />
          <u-tag text="2 个打卡 · 1 个纪念日" plain type="info" shape="circle" />
        </view>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">回忆提示</view>
          <view class="section-copy__desc">这里以后会接后端真实的去年今日接口。</view>
        </view>
      </view>

      <EmptyStateCard
        class="page-section"
        title="回忆页已预留联调位置"
        description="等后端去年今日接口联通后，这里会展示同一天的日记、打卡和纪念日。"
        mode="history"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import EmptyStateCard from '@/components/business/empty-state-card'
</script>

<style scoped lang="scss">
.memory-title {
  color: #2b2118;
  font-size: 30rpx;
  font-weight: 600;
}

.memory-body {
  margin-top: 12rpx;
  color: #6b5b4e;
  font-size: 24rpx;
  line-height: 1.8;
}

.memory-tags {
  margin-top: 16rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}
</style>
