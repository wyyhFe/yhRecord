<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">固定提醒时间</view>
          <view class="section-copy__desc">当前统一为每天 22:00，不再支持单独修改全局提醒时间。</view>
        </view>
        <u-tag text="22:00" plain shape="circle" type="warning" />
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">小程序订阅消息</view>
          <view class="section-copy__desc">开启时会请求微信订阅授权，消息会进入微信服务通知。</view>
          <view class="list-card__meta">
            会一次性申请日记提醒、纪念日提醒、每日记账提醒和记账月报提醒四个模板。
          </view>
        </view>
        <switch :checked="form.miniProgramReminderEnabled" color="#c47c52" @change="onMiniProgramSwitch" />
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">公众号模板消息</view>
          <view class="section-copy__desc">作为扩展提醒通道保留，依赖公众号 openid 绑定能力。</view>
        </view>
        <switch :checked="form.officialAccountReminderEnabled" color="#c47c52" @change="onOfficialSwitch" />
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
      {{ submitting ? '保存中...' : '保存提醒设置' }}
    </u-button>
  </view>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
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

function onOfficialSwitch(event: Event) {
  const payload = event as Event & { detail?: { value?: boolean } }
  form.officialAccountReminderEnabled = Boolean(payload.detail?.value)
}

async function onMiniProgramSwitch(event: Event) {
  const payload = event as Event & { detail?: { value?: boolean } }
  const nextEnabled = Boolean(payload.detail?.value)
  if (!nextEnabled) {
    form.miniProgramReminderEnabled = false
    return
  }

  const granted = await requestMiniProgramSubscription()
  form.miniProgramReminderEnabled = granted
  if (!granted) {
    uni.$feedback.info('未授权订阅消息')
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
    uni.$feedback.success('提醒设置已保存')
  } catch (error) {
    uni.$feedback.error(error)
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

onMounted(() => {
  init()
})
</script>
