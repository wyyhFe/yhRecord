<template>
  <u-popup v-model="show" mode="center" :closeable="false" border-radius="28">
    <view class="selector-dialog">
      <view class="selector-dialog__header">
        <view class="selector-dialog__title">{{ title }}</view>
        <view
          v-if="actionText"
          class="selector-dialog__action"
          :class="{ 'selector-dialog__action--primary': actionPrimary }"
          @tap="emit('action')"
        >
          {{ actionText }}
        </view>
        <view v-else class="selector-dialog__action" @tap="show = false">关闭</view>
      </view>

      <view v-if="mode === 'grid'" class="selector-dialog__grid">
        <view
          v-for="item in items"
          :key="String(item.value)"
          class="selector-dialog__option"
          @tap="emit('select', item.value)"
        >
          <view v-if="item.icon" class="selector-dialog__emoji">{{ item.icon }}</view>
          <view class="selector-dialog__label">{{ item.label }}</view>
        </view>
      </view>

      <view v-else-if="items.length" class="selector-dialog__chips">
        <view
          v-for="item in items"
          :key="String(item.value)"
          class="selector-dialog__chip"
          :class="{ 'selector-dialog__chip--active': isSelected(item.value) }"
          @tap="emit('select', item.value)"
        >
          {{ item.label }}
        </view>
      </view>

      <view v-else class="note-card selector-dialog__empty">
        {{ emptyText }}
      </view>
    </view>
  </u-popup>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title: string
    items: Array<{ label: string; value: string | number; icon?: string }>
    mode?: 'grid' | 'chips'
    selectedValues?: Array<string | number>
    actionText?: string
    actionPrimary?: boolean
    emptyText?: string
  }>(),
  {
    mode: 'grid',
    selectedValues: () => [],
    actionText: '',
    actionPrimary: false,
    emptyText: '暂无可选内容'
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  select: [value: string | number]
  action: []
}>()

const show = computed({
  get: () => props.modelValue,
  set: (value: boolean) => {
    console.log('[selector-dialog] update', props.title, value)
    emit('update:modelValue', value)
  }
})

function isSelected(value: string | number) {
  return props.selectedValues.includes(value)
}

watch(
  () => props.modelValue,
  (value) => {
    console.log('[selector-dialog] model value', props.title, value)
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.selector-dialog {
  width: 640rpx;
  background: var(--color-surface);
  border-radius: 28rpx;
  overflow: hidden;
}

.selector-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid var(--color-border-strong);
}

.selector-dialog__title {
  color: var(--color-text-primary);
  font-size: 32rpx;
  font-weight: 700;
}

.selector-dialog__action {
  color: var(--color-text-muted);
  font-size: 24rpx;
}

.selector-dialog__action--primary {
  color: var(--color-primary-strong);
  font-weight: 600;
}

.selector-dialog__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24rpx 12rpx;
  padding: 28rpx 24rpx 32rpx;
}

.selector-dialog__option {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
}

.selector-dialog__emoji {
  font-size: 42rpx;
  line-height: 1;
}

.selector-dialog__label {
  color: var(--color-text-primary);
  font-size: 24rpx;
}

.selector-dialog__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding: 28rpx 24rpx 32rpx;
}

.selector-dialog__chip {
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  border: 1rpx solid var(--color-border-strong);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 24rpx;
}

.selector-dialog__chip--active {
  border-color: var(--color-primary);
  background: var(--color-border-strong);
  color: var(--color-primary-strong);
}

.selector-dialog__empty {
  margin: 24rpx;
}
</style>
