<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">创建打卡</view>
          <view class="section-copy__desc">任务名称、描述和开始日期直接对应后端建模。</view>
        </view>
      </view>

      <view class="block-stack">
        <u-input
          v-model="form.name"
          placeholder="例如：20 个俯卧撑"
          :border="true"
          border-color="var(--color-border-strong)"
          :custom-style="fieldStyle"
        />
      </view>
      <view class="block-stack">
        <u-textarea
          v-model="form.description"
          placeholder="描述一下任务规则和目标"
          :border="true"
          border-color="var(--color-border-strong)"
          :custom-style="textareaStyle"
          height="180"
        />
      </view>
      <view class="block-stack">
        <view class="field-label">开始日期</view>
        <picker mode="date" :value="form.startDate" @change="onDateChange">
          <view class="picker-row">
            <text class="picker-row__value">{{ form.startDate }}</text>
            <text class="picker-row__arrow">></text>
          </view>
        </picker>
      </view>
    </view>

    <u-button
      class="primary-action"
      type="primary"
      shape="circle"
      color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
      :loading="submitting"
      @click="submit"
    >
      {{ submitting ? '创建中...' : '创建任务' }}
    </u-button>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { createCheckinTask } from '@/api/checkin-form'

const submitting = ref(false)
const fieldStyle = {
  background: 'var(--color-surface-soft)',
  borderRadius: '20rpx',
  padding: '0 22rpx',
  fontSize: '28rpx',
  minHeight: '84rpx'
}
const textareaStyle = {
  background: 'var(--color-surface-soft)',
  borderRadius: '20rpx',
  padding: '18rpx 22rpx',
  fontSize: '26rpx',
  width: '100%',
  boxSizing: 'border-box' as const
}
const form = ref({
  name: '',
  description: '',
  startDate: new Date().toISOString().slice(0, 10)
})

function onDateChange(event: { detail: { value: string } }) {
  form.value.startDate = event.detail.value
}

async function submit() {
  if (!form.value.name.trim()) {
    uni.$feedback.error('请先填写任务名称')
    return
  }

  submitting.value = true
  try {
    await createCheckinTask(form.value)
    uni.$feedback.success('任务已创建')
    setTimeout(() => uni.navigateBack({ delta: 1 }), 500)
  } catch (error) {
    uni.$feedback.error(error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.picker-row {
  min-height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  border-radius: 20rpx;
  background: var(--color-surface-soft);
  padding: 0 22rpx;
}

.picker-row__value {
  color: var(--color-text-primary);
  font-size: 28rpx;
}

.picker-row__arrow {
  color: var(--color-text-muted);
  font-size: 26rpx;
}
</style>
