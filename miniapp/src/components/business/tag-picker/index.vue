<template>
  <view class="tag-picker">
    <text class="tag-picker__label">选择标签（可多选，最多 3 个）</text>
    <view class="tag-picker__list">
      <view
        v-for="tag in tags"
        :key="tag.id"
        class="tag-picker__item"
        :class="{ 'tag-picker__item--active': selectedIds.has(tag.id) }"
        @tap="toggle(tag.id)"
      >
        <text v-if="tag.icon" class="tag-picker__icon">{{ tag.icon }}</text>
        <text class="tag-picker__name">{{ tag.name }}</text>
      </view>
      <view class="tag-picker__item tag-picker__item--add" @tap="showCustomInput = true">
        <text class="tag-picker__name">+ 自定义</text>
      </view>
    </view>

    <!-- 自定义标签输入 -->
    <view v-if="showCustomInput" class="tag-picker__custom">
      <input
        v-model="customName"
        class="tag-picker__custom-input"
        placeholder="输入标签名称"
        :maxlength="32"
        @confirm="addCustomTag"
      />
      <view class="tag-picker__custom-actions">
        <u-button size="mini" plain :hair-line="false" @click="showCustomInput = false">取消</u-button>
        <u-button size="mini" type="primary" @click="addCustomTag">添加</u-button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { CheckinTag, Id } from '@/types/domain'

const props = defineProps<{
  tags: CheckinTag[]
  modelValue: Id[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: Id[]]
  'create-tag': [name: string]
}>()

const showCustomInput = ref(false)
const customName = ref('')

const selectedIds = computed(() => new Set(props.modelValue))

function toggle(tagId: Id) {
  const next = [...props.modelValue]
  const idx = next.indexOf(tagId)
  if (idx >= 0) {
    next.splice(idx, 1)
  } else if (next.length < 3) {
    next.push(tagId)
  } else {
    uni.$feedback.info('最多选择 3 个标签')
    return
  }
  emit('update:modelValue', next)
}

function addCustomTag() {
  const name = customName.value.trim()
  if (!name) return
  emit('create-tag', name)
  customName.value = ''
  showCustomInput.value = false
}
</script>

<style scoped lang="scss">
.tag-picker {
  margin-bottom: var(--space-3);
}

.tag-picker__label {
  display: block;
  margin-bottom: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
}

.tag-picker__list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag-picker__item {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  transition: all var(--motion-fast) var(--ease-standard);
}

.tag-picker__item--active {
  background: var(--color-checkin-soft);
  box-shadow: 0 0 0 2rpx var(--color-checkin);
}

.tag-picker__item--add {
  border: 2rpx dashed var(--color-border-strong);
  background: transparent;
}

.tag-picker__icon {
  font-size: 24rpx;
}

.tag-picker__name {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
}

.tag-picker__item--active .tag-picker__name {
  color: var(--color-checkin);
  font-weight: var(--weight-semibold);
}

.tag-picker__custom {
  margin-top: var(--space-3);
  display: flex;
  gap: var(--space-2);
  align-items: center;
}

.tag-picker__custom-input {
  flex: 1;
  height: 64rpx;
  padding: 0 var(--space-3);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  font-size: var(--font-meta);
}

.tag-picker__custom-actions {
  display: flex;
  gap: var(--space-2);
}
</style>
