<template>
  <u-popup
    :model-value="modelValue"
    mode="bottom"
    border-radius="36"
    :safe-area-inset-bottom="true"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <view class="mend-sheet">
      <!-- 拖拽条 -->
      <view class="mend-sheet__handle" />

      <!-- 头部 -->
      <view class="mend-sheet__header">
        <view class="mend-sheet__title-row">
          <text class="mend-sheet__emoji">📌</text>
          <text class="mend-sheet__title">补卡</text>
        </view>
        <view class="mend-sheet__date-badge">{{ displayDate }}</view>
      </view>

      <view class="mend-sheet__desc">选择一个未完成的任务进行补卡</view>

      <!-- 任务列表 -->
      <view class="mend-sheet__list">
        <view
          v-for="task in tasks"
          :key="task.id"
          class="mend-sheet__task"
          hover-class="mend-sheet__task--pressed"
          @tap="emit('select', task)"
        >
          <view class="mend-sheet__task-icon">
            <text class="mend-sheet__task-check">○</text>
          </view>
          <view class="mend-sheet__task-body">
            <text class="mend-sheet__task-name">{{ task.name }}</text>
            <text v-if="task.description" class="mend-sheet__task-desc">{{ task.description }}</text>
          </view>
          <view class="mend-sheet__task-arrow">
            <text class="mend-sheet__task-arrow-icon">›</text>
          </view>
        </view>
      </view>

      <!-- 底部信息 -->
      <view class="mend-sheet__footer">
        <text class="mend-sheet__footer-text">本月剩余补卡</text>
        <text class="mend-sheet__footer-count">{{ remaining }}</text>
        <text class="mend-sheet__footer-text">/ 2</text>
      </view>
    </view>
  </u-popup>
</template>

<script setup lang="ts">
import type { CheckinTask } from '@/types/domain'

defineProps<{
  modelValue: boolean
  displayDate: string
  tasks: CheckinTask[]
  remaining: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  select: [task: CheckinTask]
}>()
</script>

<style scoped lang="scss">
.mend-sheet {
  padding: var(--space-3) var(--space-5) calc(var(--space-6) + env(safe-area-inset-bottom));
  background: var(--color-bg);
}

/* 拖拽条 */
.mend-sheet__handle {
  width: 72rpx;
  height: 8rpx;
  border-radius: 999rpx;
  background: var(--color-border-strong);
  margin: 0 auto var(--space-4);
}

/* 头部 */
.mend-sheet__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.mend-sheet__title-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.mend-sheet__emoji {
  font-size: 36rpx;
}

.mend-sheet__title {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.mend-sheet__date-badge {
  padding: var(--space-1) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  color: var(--color-checkin);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

.mend-sheet__desc {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
  margin-bottom: var(--space-4);
}

/* 任务列表 */
.mend-sheet__list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  max-height: 560rpx;
  overflow-y: auto;
}

.mend-sheet__task {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--radius-large);
  background: var(--color-surface);
  border: 2rpx solid var(--color-divider);
  transition: all var(--motion-fast) var(--ease-standard);
}

.mend-sheet__task--pressed {
  transform: scale(0.97);
  border-color: var(--color-checkin);
  background: var(--color-checkin-soft);
}

.mend-sheet__task-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.mend-sheet__task-check {
  color: var(--color-text-muted);
  font-size: 24rpx;
}

.mend-sheet__task-body {
  flex: 1;
  min-width: 0;
}

.mend-sheet__task-name {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.mend-sheet__task-desc {
  display: block;
  margin-top: 4rpx;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mend-sheet__task-arrow {
  flex-shrink: 0;
  width: 44rpx;
  height: 44rpx;
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}

.mend-sheet__task-arrow-icon {
  color: var(--color-checkin);
  font-size: 28rpx;
  font-weight: var(--weight-bold);
}

/* 底部 */
.mend-sheet__footer {
  margin-top: var(--space-4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
}

.mend-sheet__footer-text {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.mend-sheet__footer-count {
  color: var(--color-checkin);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}
</style>
