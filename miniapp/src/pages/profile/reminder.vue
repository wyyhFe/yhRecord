<template>
  <AppPage>
    <AppHero
      eyebrow="提醒设置"
      title="统一管理提醒通道"
      description="全局日记提醒固定为每天晚上 22:00。这里只负责保存开关和申请订阅授权。"
      badge="Reminder"
    />

    <SectionBlock title="日记提醒" subtitle="每天晚上 22:00 检查，当天未写日记时才发送提醒">
      <view class="glass-panel px-[24rpx] py-[24rpx]">
        <view class="flex items-center justify-between">
          <view class="pr-[24rpx]">
            <view class="text-[28rpx] font-semibold text-ink">固定提醒时间</view>
            <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">当前统一为每天 22:00，不再支持单独修改时间。</view>
          </view>
          <view class="rounded-full bg-[#f2dfcb] px-[22rpx] py-[10rpx] text-[22rpx] text-[#8d5a3e]">22:00</view>
        </view>
      </view>
    </SectionBlock>

    <SectionBlock title="小程序订阅消息" subtitle="开启时会请求微信订阅授权，消息会进入微信服务通知">
      <view class="glass-panel px-[24rpx] py-[24rpx]">
        <view class="flex items-center justify-between">
          <view class="pr-[24rpx]">
            <view class="text-[28rpx] font-semibold text-ink">小程序提醒</view>
            <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">
              会一次性申请日记提醒、纪念日提醒、每日记账提醒和记账月报通知四个模板的订阅授权。
            </view>
          </view>
          <switch :checked="form.miniProgramReminderEnabled" color="#c47c52" @change="onMiniProgramSwitch" />
        </view>
      </view>
    </SectionBlock>

    <SectionBlock title="公众号模板消息" subtitle="扩展提醒通道，依赖公众号 openid 绑定能力">
      <view class="glass-panel px-[24rpx] py-[24rpx]">
        <view class="flex items-center justify-between">
          <view class="pr-[24rpx]">
            <view class="text-[28rpx] font-semibold text-ink">公众号提醒</view>
            <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">
              首发以小程序订阅消息为主，公众号模板消息作为补充通道保留。
            </view>
          </view>
          <switch :checked="form.officialAccountReminderEnabled" color="#c47c52" @change="onOfficialSwitch" />
        </view>
      </view>
    </SectionBlock>

    <view class="mt-[32rpx]">
      <BaseButton :disabled="submitting" @tap="submit">保存提醒设置</BaseButton>
    </view>
  </AppPage>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/app-hero'
import SectionBlock from '@/components/business/section-block'
import BaseButton from '@/components/base/base-button'
import {
  MP_DIARY_TEMPLATE_ID,
  MP_LEDGER_MONTHLY_TEMPLATE_ID,
  MP_LEDGER_TEMPLATE_ID,
  MP_MEMORIAL_TEMPLATE_ID
} from '@/config/app'
import { fetchReminderSettings, saveReminderSettings } from '@/api/reminder'

const submitting = ref(false)
const form = reactive({
  diaryReminderEnabled: false,
  miniProgramReminderEnabled: false,
  officialAccountReminderEnabled: false
})

function onOfficialSwitch(event: any) {
  form.officialAccountReminderEnabled = Boolean(event?.detail?.value)
}

async function onMiniProgramSwitch(event: any) {
  const nextEnabled = Boolean(event?.detail?.value)
  if (!nextEnabled) {
    form.miniProgramReminderEnabled = false
    return
  }

  const granted = await requestMiniProgramSubscription()
  form.miniProgramReminderEnabled = granted
  if (!granted) {
    uni.showToast({ title: '未授权订阅消息', icon: 'none' })
  }
}

async function submit() {
  submitting.value = true
  try {
    if (form.miniProgramReminderEnabled) {
      const granted = await requestMiniProgramSubscription()
      if (!granted) {
        throw new Error('请先允许小程序订阅消息')
      }
    }

    await saveReminderSettings({
      diaryReminderEnabled: form.diaryReminderEnabled,
      miniProgramReminderEnabled: form.miniProgramReminderEnabled,
      officialAccountReminderEnabled: form.officialAccountReminderEnabled
    })
    uni.showToast({ title: '提醒设置已保存', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '保存失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

async function init() {
  try {
    const result = await fetchReminderSettings()
    form.diaryReminderEnabled = result.diaryReminderEnabled
    form.miniProgramReminderEnabled = result.miniProgramReminderEnabled
    form.officialAccountReminderEnabled = result.officialAccountReminderEnabled
  } catch {
    // 初始化失败时保留默认值即可。
  }
}

async function requestMiniProgramSubscription() {
  const templateIds = [
    MP_DIARY_TEMPLATE_ID,
    MP_LEDGER_TEMPLATE_ID,
    MP_LEDGER_MONTHLY_TEMPLATE_ID,
    MP_MEMORIAL_TEMPLATE_ID
  ].filter(Boolean)

  if (!templateIds.length) {
    throw new Error('请先在前端环境变量中配置订阅消息模板 ID')
  }

  return new Promise<boolean>((resolve, reject) => {
    // #ifdef MP-WEIXIN
    uni.requestSubscribeMessage({
      tmplIds: templateIds,
      success: (result) => {
        const subscribeResult = result as unknown as Record<string, string>
        const accepted = templateIds.some((templateId) => subscribeResult?.[templateId] === 'accept')
        resolve(accepted)
      },
      fail: (error) => {
        reject(error)
      }
    })
    // #endif

    // #ifndef MP-WEIXIN
    reject(new Error('订阅消息只能在微信小程序环境中请求授权'))
    // #endif
  })
}

init()
</script>
