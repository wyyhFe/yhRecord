<template>
  <AppPage>
    <AppHero
      eyebrow="创建打卡"
      title="给习惯一个明确的起点"
      description="任务名称、描述和开始日期直接对应后端建模。"
      badge="Checkin Form"
    />

    <SectionBlock title="任务信息" subtitle="先把打卡任务定义清楚">
      <view class="glass-panel px-[24rpx] py-[24rpx]">
        <input
          v-model="form.name"
          class="h-[84rpx] rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] text-[28rpx]"
          placeholder="例如：20 个俯卧撑"
        />
        <textarea
          v-model="form.description"
          class="mt-[18rpx] h-[180rpx] rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] py-[18rpx] text-[26rpx]"
          placeholder="描述一下任务规则和目标"
        />
        <view class="mt-[18rpx] text-[24rpx] text-[#7f7366]">开始日期</view>
        <picker mode="date" :value="form.startDate" @change="onDateChange">
          <view class="mt-[12rpx] rounded-[20rpx] bg-[#fcf5ec] px-[22rpx] py-[18rpx] text-[28rpx] font-semibold text-ink">
            {{ form.startDate }}
          </view>
        </picker>
      </view>
    </SectionBlock>

    <view class="mt-[32rpx]">
      <BaseButton :disabled="submitting" @tap="submit">创建任务</BaseButton>
    </view>
  </AppPage>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/app-hero'
import SectionBlock from '@/components/business/section-block'
import BaseButton from '@/components/base/base-button'
import { createCheckinTask } from '@/api/checkin-form'

const submitting = ref(false)
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
    uni.showToast({ title: '请先填写任务名称', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    await createCheckinTask(form.value)
    uni.showToast({ title: '任务已创建', icon: 'success' })
    setTimeout(() => uni.navigateBack({ delta: 1 }), 500)
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '创建失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>
