<template>
  <u-popup
    :model-value="modelValue"
    mode="bottom"
    border-radius="28"
    :safe-area-inset-bottom="true"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <view class="sheet">
      <!-- 拖拽条 -->
      <view v-if="showHandle" class="sheet__handle" />

      <!-- 标题 -->
      <view v-if="title" class="sheet__header">
        <text class="sheet__title">{{ title }}</text>
        <text v-if="subtitle" class="sheet__subtitle">{{ subtitle }}</text>
      </view>

      <!-- 内容 -->
      <view class="sheet__body">
        <slot />
      </view>

      <!-- 底部 -->
      <view v-if="showFooter" class="sheet__footer">
        <slot name="footer">
          <u-button
            v-if="cancelText"
            shape="circle"
            plain
            :hair-line="false"
            @click="handleCancel"
          >
            {{ cancelText }}
          </u-button>
          <u-button
            v-if="confirmText"
            shape="circle"
            type="primary"
            :loading="confirmLoading"
            @click="emit('confirm')"
          >
            {{ confirmText }}
          </u-button>
        </slot>
      </view>
    </view>
  </u-popup>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    subtitle?: string
    confirmText?: string
    cancelText?: string
    confirmLoading?: boolean
    showHandle?: boolean
    showFooter?: boolean
  }>(),
  {
    title: '',
    subtitle: '',
    confirmText: '',
    cancelText: '取消',
    confirmLoading: false,
    showHandle: true,
    showFooter: true
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
  cancel: []
}>()

function handleCancel() {
  emit('cancel')
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
.sheet {
  padding: var(--space-6) var(--space-5) calc(var(--space-6) + env(safe-area-inset-bottom));
  background: var(--color-bg);
}

.sheet__handle {
  width: 72rpx;
  height: 8rpx;
  border-radius: var(--radius-full);
  background: var(--color-border-strong);
  margin: 0 auto var(--space-4);
}

.sheet__header {
  text-align: center;
  margin-bottom: var(--space-4);
}

.sheet__title {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.sheet__subtitle {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.sheet__footer {
  margin-top: var(--space-5);
  display: flex;
  gap: var(--space-3);

  .u-button {
    flex: 1;
  }
}
</style>
