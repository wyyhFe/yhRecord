<template>
  <view class="section-shell empty-state-card">
    <u-empty :mode="mode" :text="title" :margin-top="0" />
    <view v-if="description" class="empty-state-card__desc">
      {{ description }}
    </view>
    <view v-if="$slots.action" class="empty-state-card__action">
      <slot name="action" />
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title: string
  description?: string
  mode?: string
}>(), {
  description: '',
  mode: 'data'
})
</script>

<style scoped lang="scss">
.empty-state-card {
  padding-top: 32rpx;
  padding-bottom: 32rpx;
  text-align: center;
}

.empty-state-card__desc {
  margin-top: 10rpx;
  color: #7f7366;
  font-size: 22rpx;
  line-height: 1.7;
}

.empty-state-card__action {
  margin-top: 20rpx;
  display: flex;
  justify-content: center;
}
</style>
