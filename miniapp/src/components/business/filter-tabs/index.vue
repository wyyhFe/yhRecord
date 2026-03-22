<template>
  <scroll-view scroll-x class="filter-tabs">
    <view class="filter-tabs__track">
      <u-tag
        v-for="item in items"
        :key="item.value"
        :text="item.label"
        shape="circle"
        :plain="modelValue !== item.value"
        :type="modelValue === item.value ? 'warning' : 'info'"
        @click="$emit('update:modelValue', item.value)"
      />
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
defineProps<{
  items: Array<{ label: string; value: string }>
  modelValue: string
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<style scoped lang="scss">
.filter-tabs {
  white-space: nowrap;
}

.filter-tabs__track {
  display: flex;
  gap: 16rpx;
}
</style>
