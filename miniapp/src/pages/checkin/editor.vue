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
          border-color="#eadfd0"
          :custom-style="fieldStyle"
        />
      </view>
      <view class="block-stack">
        <u-textarea
          v-model="form.description"
          placeholder="描述一下任务规则和目标"
          :border="true"
          border-color="#eadfd0"
          :custom-style="textareaStyle"
          height="180"
        />
      </view>
      <view class="block-stack">
        <u-cell-group :border="false">
          <picker mode="date" :value="form.startDate" @change="onDateChange">
            <u-cell-item title="开始日期" :value="form.startDate" :border-bottom="false" />
          </picker>
        </u-cell-group>
      </view>
    </view>

    <u-button
      class="primary-action"
      type="primary"
      shape="circle"
      color="linear-gradient(135deg, #c47c52 0%, #d7a648 100%)"
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
  background: '#fcf5ec',
  borderRadius: '20rpx',
  padding: '0 22rpx',
  fontSize: '28rpx',
  minHeight: '84rpx'
}
const textareaStyle = {
  background: '#fcf5ec',
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
