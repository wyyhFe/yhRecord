<template>
  <AppPage>
    <AppHero
      eyebrow="提醒设置"
      title="把提醒时间和通道配置好"
      description="小程序订阅消息是主提醒方式，公众号模板消息作为扩展通道保留。"
      badge="Reminder"
    />

    <SectionBlock title="写日记提醒" subtitle="当天还没记录时，按设定时间检查并发送提醒">
      <ReminderSwitch
        :enabled="form.diaryReminderEnabled"
        :time-value="form.diaryReminderTime"
        @update:enabled="form.diaryReminderEnabled = $event"
        @update:timeValue="form.diaryReminderTime = $event"
      />
    </SectionBlock>

    <SectionBlock title="小程序订阅消息" subtitle="开启时会请求微信订阅授权，消息会进入微信服务通知">
      <view class="glass-panel px-[24rpx] py-[24rpx]">
        <view class="flex items-center justify-between">
          <view class="pr-[24rpx]">
            <view class="text-[28rpx] font-semibold text-ink">小程序提醒</view>
            <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">
              支持日记提醒和纪念日提醒，前提是你已经在小程序后台配置好模板 ID。
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
              这条链路需要你后续补公众号用户绑定，否则后端没有目标 openid 可发送。
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
import AppHero from '@/components/business/AppHero.vue'
import SectionBlock from '@/components/business/SectionBlock.vue'
import ReminderSwitch from '@/components/business/ReminderSwitch.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import { MP_DIARY_TEMPLATE_ID, MP_MEMORIAL_TEMPLATE_ID } from '@/config/app'
import { fetchReminderSettings, saveReminderSettings } from '@/api/reminder'

const submitting = ref(false)
const form = reactive({
  diaryReminderEnabled: false,
  diaryReminderTime: undefined as string | undefined,
  miniProgramReminderEnabled: false,
  officialAccountReminderEnabled: false
})

/**
 * switch 事件在模板层会返回运行时事件对象，这里统一做宽松解析。
 */
function onOfficialSwitch(event: any) {
  form.officialAccountReminderEnabled = Boolean(event?.detail?.value)
}

/**
 * 开启小程序提醒时先拉起微信订阅授权，再更新表单状态。
 */
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

/**
 * 保存提醒设置前再做一次订阅校验，避免用户跳过授权直接保存开启状态。
 */
async function submit() {
  submitting.value = true
  try {
    if (form.miniProgramReminderEnabled) {
      const granted = await requestMiniProgramSubscription()
      if (!granted) {
        throw new Error('请先允许小程序订阅消息')
      }
    }

    await saveReminderSettings({ ...form })
    uni.showToast({ title: '提醒设置已保存', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '保存失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

/**
 * 页面初始化时拉取后端保存的提醒配置，用于表单回填。
 */
async function init() {
  try {
    const result = await fetchReminderSettings()
    form.diaryReminderEnabled = result.diaryReminderEnabled
    form.diaryReminderTime = result.diaryReminderTime
    form.miniProgramReminderEnabled = result.miniProgramReminderEnabled
    form.officialAccountReminderEnabled = result.officialAccountReminderEnabled
  } catch {
    // keep defaults
  }
}

/**
 * 请求微信小程序订阅消息授权。
 * 模板 ID 由前端环境变量提供，后续你只需要把真实模板 ID 配进去。
 */
async function requestMiniProgramSubscription() {
  const templateIds = [MP_DIARY_TEMPLATE_ID, MP_MEMORIAL_TEMPLATE_ID].filter(Boolean)
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
    reject(new Error('订阅消息仅支持在微信小程序环境中请求授权'))
    // #endif
  })
}

init()
</script>
