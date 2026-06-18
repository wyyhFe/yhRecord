<template>
  <BottomSheet
    :model-value="modelValue"
    title="补卡"
    :subtitle="displayDate"
    :show-footer="false"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <view class="mend-list">
      <view class="mend-list__hint">
        <template v-if="mendableCount > 0">未打卡 <text class="mend-list__hint-num">{{ mendableCount }}</text> 个，已完成 <text class="mend-list__hint-num">{{ doneIds.size }}</text> 个</template>
        <template v-else>全部任务已完成</template>
      </view>

      <view
        v-for="task in pendingTasks"
        :key="task.id"
        class="mend-card"
        :class="{ 'mend-card--done': doneIds.has(task.id) }"
        hover-class="mend-card--pressed"
        @tap="handleSelect(task)"
      >
        <view class="mend-card__icon" :class="{ 'mend-card__icon--done': doneIds.has(task.id) }">
          <text v-if="doneIds.has(task.id)">✓</text>
          <text v-else>○</text>
        </view>
        <view class="mend-card__body">
          <text class="mend-card__name">{{ task.name }}</text>
          <text v-if="task.description" class="mend-card__desc">{{ task.description }}</text>
        </view>
        <view v-if="doneIds.has(task.id)" class="mend-card__done-badge">已完成</view>
        <view v-else class="mend-card__arrow">›</view>
      </view>

      <view class="mend-list__footer">
        <text class="mend-list__footer-label">本月剩余补卡</text>
        <text class="mend-list__footer-count">{{ localRemaining }}</text>
        <text class="mend-list__footer-label">/ 2</text>
      </view>
    </view>
  </BottomSheet>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import BottomSheet from '@/components/business/bottom-sheet/index.vue'
import { mendCheckin } from '@/api/checkin'
import type { CheckinTask } from '@/types/domain'

const props = defineProps<{
  modelValue: boolean
  mendDate: string
  displayDate: string
  tasks: CheckinTask[]
  completedIds: Set<number>
  remaining: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const pendingTasks = reactive<CheckinTask[]>([])
const doneIds = reactive(new Set<number>())
const submitting = ref(false)
const localRemaining = ref(0)

const mendableCount = computed(() => pendingTasks.length - doneIds.size)

// 弹窗打开时同步任务列表（含已完成的）
watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      pendingTasks.splice(0, pendingTasks.length, ...props.tasks)
      // 初始化 doneIds：来自外部的已打卡任务 + 可能已补过的
      doneIds.clear()
      props.completedIds.forEach((id) => doneIds.add(id))
      localRemaining.value = props.remaining
    }
  }
)

async function handleSelect(task: CheckinTask) {
  if (submitting.value) return
  if (localRemaining.value <= 0) {
    uni.$feedback.info('本月补卡次数已用完')
    return
  }
  submitting.value = true
  try {
    await mendCheckin(task.id, props.mendDate)
    // 标记为已完成，不关弹窗
    doneIds.add(task.id)
    localRemaining.value--
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

.mend-list__hint-num {
  color: var(--color-text-primary);
  font-weight: var(--weight-semibold);
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

.mend-card__icon--done {
  background: var(--color-checkin-soft);
  color: var(--color-checkin);
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

.mend-card--done {
  opacity: 0.6;
  pointer-events: none;
}

.mend-card__done-badge {
  flex-shrink: 0;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  color: var(--color-checkin);
  font-size: var(--font-tiny);
  font-weight: var(--weight-semibold);
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
