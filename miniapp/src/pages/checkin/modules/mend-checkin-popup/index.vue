<template>
  <BottomSheet
    :model-value="modelValue"
    title="补卡"
    :subtitle="displayDate"
    :show-footer="false"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <view class="mend-list">
      <view class="mend-list__hint">选择一个未完成的任务</view>

      <view
        v-for="task in tasks"
        :key="task.id"
        class="mend-card"
        hover-class="mend-card--pressed"
        @tap="handleSelect(task)"
      >
        <view class="mend-card__icon">○</view>
        <view class="mend-card__body">
          <text class="mend-card__name">{{ task.name }}</text>
          <text v-if="task.description" class="mend-card__desc">{{ task.description }}</text>
        </view>
        <view class="mend-card__arrow">›</view>
      </view>

      <view class="mend-list__footer">
        <text class="mend-list__footer-label">本月剩余补卡</text>
        <text class="mend-list__footer-count">{{ remaining }}</text>
        <text class="mend-list__footer-label">/ 2</text>
      </view>
    </view>
  </BottomSheet>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import BottomSheet from '@/components/business/bottom-sheet/index.vue'
import { mendCheckin } from '@/api/checkin'
import type { CheckinTask } from '@/types/domain'

const props = defineProps<{
  modelValue: boolean
  mendDate: string
  displayDate: string
  tasks: CheckinTask[]
  remaining: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const submitting = ref(false)

async function handleSelect(task: CheckinTask) {
  if (submitting.value) return
  submitting.value = true
  try {
    await mendCheckin(task.id, props.mendDate)
    emit('update:modelValue', false)
    uni.$feedback.success(`${props.displayDate} 补卡成功`)
    emit('success')
  } catch (error) {
    uni.$feedback.error(error, undefined, '补卡失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.mend-list__hint {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
  margin-bottom: var(--space-4);
}

.mend-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--radius-large);
  background: var(--color-surface);
  border: 2rpx solid var(--color-divider);
  transition: all var(--motion-fast) var(--ease-standard);

  & + & {
    margin-top: var(--space-3);
  }
}

.mend-card--pressed {
  transform: scale(0.97);
  border-color: var(--color-checkin);
  background: var(--color-checkin-soft);
}

.mend-card__icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--color-text-muted);
  font-size: 24rpx;
}

.mend-card__body {
  flex: 1;
  min-width: 0;
}

.mend-card__name {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.mend-card__desc {
  display: block;
  margin-top: 4rpx;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mend-card__arrow {
  flex-shrink: 0;
  width: 44rpx;
  height: 44rpx;
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-checkin);
  font-size: 28rpx;
  font-weight: var(--weight-bold);
}

.mend-list__footer {
  margin-top: var(--space-5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
}

.mend-list__footer-label {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.mend-list__footer-count {
  color: var(--color-checkin);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}
</style>
