<template>
  <view v-if="state !== 'hidden'" class="load-more">
    <text v-if="state === 'loading'" class="load-more__text load-more__text--loading">加载中...</text>
    <text v-else-if="state === 'noMore'" class="load-more__text load-more__text--muted">没有更多了</text>
    <text v-else-if="state === 'failed'" class="load-more__text load-more__text--failed">加载失败，点击重试</text>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  state?: 'loading' | 'noMore' | 'failed' | 'hidden'
}>(), {
  state: 'hidden'
})
</script>

<style scoped lang="scss">
.load-more {
  padding: var(--space-4) 0 var(--space-6);
  text-align: center;
}

.load-more__text {
  font-size: var(--font-meta);
}

.load-more__text--loading {
  color: var(--color-primary);
  font-weight: var(--weight-medium);
}

.load-more__text--muted {
  color: var(--color-text-muted);
}

.load-more__text--failed {
  color: var(--color-danger);
}
</style>
