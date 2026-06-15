<template>
  <view class="page-shell-safe reminder-page">
    <!-- Hero -->
    <view class="reminder-hero">
      <text class="reminder-hero__icon">🔔</text>
      <text class="reminder-hero__title">提醒设置</text>
      <text class="reminder-hero__sub">管理订阅消息和公众号提醒通道</text>
    </view>

    <!-- 固定提醒时间 -->
    <view class="reminder-card">
      <view class="reminder-card__header">
        <text class="reminder-card__title">⏰ 提醒时间</text>
        <view class="reminder-card__badge">22:00</view>
      </view>
      <text class="reminder-card__desc">当前统一为每天 22:00 提醒</text>
    </view>

    <!-- 小程序订阅消息 -->
    <view class="reminder-card">
      <view class="reminder-card__header">
        <view class="reminder-card__left">
          <text class="reminder-card__title">📱 小程序订阅消息</text>
          <text class="reminder-card__desc">开启后通过微信服务通知提醒</text>
        </view>
        <switch :checked="form.miniProgramReminderEnabled" color="var(--color-primary)" @change="onMiniProgramSwitch" />
      </view>
      <view class="reminder-card__detail">
        <text class="reminder-card__detail-text">包含日记、纪念日、每日记账、记账月报四个模板</text>
      </view>
    </view>

    <!-- 公众号模板消息 -->
    <view class="reminder-card">
      <view class="reminder-card__header">
        <view class="reminder-card__left">
          <text class="reminder-card__title">💬 公众号模板消息</text>
          <text class="reminder-card__desc">扩展提醒通道，依赖公众号绑定</text>
        </view>
        <switch :checked="form.officialAccountReminderEnabled" color="var(--color-primary)" @change="onOfficialSwitch" />
      </view>
    </view>

    <!-- 保存 -->
    <view class="reminder-submit">
      <u-button
        shape="circle"
        type="primary"
        color="var(--color-primary-gradient)"
        :loading="submitting"
        @click="submit"
      >
        {{ submitting ? '保存中...' : '保存设置' }}
      </u-button>
    </view>
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
  if (!granted) uni.$feedback.info('未授权订阅消息')
}

async function submit() {
  submitting.value = true
  try {
    if (form.miniProgramReminderEnabled) {
      const granted = await requestMiniProgramSubscription()
      if (!granted) throw new Error('请先允许小程序订阅消息')
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
  } catch { /* 默认值 */ }
}

async function requestMiniProgramSubscription() {
  const templateIds = [
    MP_DIARY_TEMPLATE_ID,
    MP_LEDGER_TEMPLATE_ID,
    MP_LEDGER_MONTHLY_TEMPLATE_ID,
    MP_MEMORIAL_TEMPLATE_ID
  ].filter(Boolean)
  if (!templateIds.length) throw new Error('请先配置订阅消息模板 ID')
  return new Promise<boolean>((resolve, reject) => {
    // #ifdef MP-WEIXIN
    uni.requestSubscribeMessage({
      tmplIds: templateIds,
      success: (result) => {
        const subscribeResult = result as unknown as Record<string, string>
        resolve(templateIds.some((id) => subscribeResult?.[id] === 'accept'))
      },
      fail: reject
    })
    // #endif
    // #ifndef MP-WEIXIN
    reject(new Error('订阅消息只能在微信小程序中请求'))
    // #endif
  })
}

onMounted(() => { init() })
</script>

<style scoped lang="scss">
.reminder-page {
  padding-bottom: var(--space-10);
}

/* ========== Hero ========== */
.reminder-hero {
  background: var(--color-primary-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-8) var(--space-6) var(--space-7);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  color: #fff;
}

.reminder-hero__icon {
  font-size: 56rpx;
  margin-bottom: var(--space-3);
}

.reminder-hero__title {
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.reminder-hero__sub {
  margin-top: var(--space-2);
  font-size: var(--font-meta);
  opacity: 0.85;
}

/* ========== 卡片 ========== */
.reminder-card {
  margin: var(--space-4) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.reminder-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.reminder-card__left {
  flex: 1;
  min-width: 0;
}

.reminder-card__title {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.reminder-card__desc {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.reminder-card__badge {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

.reminder-card__detail {
  margin-top: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
}

.reminder-card__detail-text {
  color: var(--color-text-secondary);
  font-size: var(--font-tiny);
  line-height: var(--leading-relaxed);
}

/* ========== 提交 ========== */
.reminder-submit {
  margin: var(--space-6) var(--space-4) 0;
}
</style>
