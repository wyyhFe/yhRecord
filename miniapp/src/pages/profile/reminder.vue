<template>
  <view class="page-shell-safe reminder-page">
    <!-- 顶栏 -->
    <view class="reminder-header">
      <text class="reminder-header__title">提醒设置</text>
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
      <view class="reminder-card__tip">
        <text class="reminder-card__tip-text">如需重新授权，请到微信「发现 → 小程序」进入本小程序 → 右上角 "···" → 设置 → 订阅消息 管理</text>
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

    <!-- 手动触发测试 -->
    <view class="reminder-card">
      <view class="reminder-card__header">
        <view class="reminder-card__left">
          <text class="reminder-card__title">🧪 手动触发测试</text>
          <text class="reminder-card__desc">立即发送提醒消息，不受定时任务限制</text>
        </view>
      </view>
      <view class="reminder-trigger-grid">
        <u-button
          shape="circle"
          type="primary"
          size="mini"
          color="var(--color-primary)"
          :loading="triggering.diary"
          :disabled="triggering.diary"
          @click="triggerDiary"
        >
          日记提醒
        </u-button>
        <u-button
          shape="circle"
          type="primary"
          size="mini"
          color="var(--color-primary)"
          :loading="triggering.ledger"
          :disabled="triggering.ledger"
          @click="triggerLedger"
        >
          记账提醒
        </u-button>
        <u-button
          shape="circle"
          type="primary"
          size="mini"
          color="var(--color-primary)"
          :loading="triggering.monthly"
          :disabled="triggering.monthly"
          @click="triggerMonthly"
        >
          记账月报
        </u-button>
        <u-button
          shape="circle"
          type="primary"
          size="mini"
          color="var(--color-primary)"
          :loading="triggering.memorial"
          :disabled="triggering.memorial"
          @click="triggerMemorial"
        >
          纪念日提醒
        </u-button>
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
import { fetchReminderSettings, saveReminderSettings, triggerDiaryReminder, triggerLedgerReminder, triggerMonthlyReminder, triggerMemorialReminder } from '@/api/reminder'

const submitting = ref(false)
const form = reactive({
  diaryReminderEnabled: false,
  miniProgramReminderEnabled: false,
  officialAccountReminderEnabled: false
})
const triggering = reactive({
  diary: false,
  ledger: false,
  monthly: false,
  memorial: false
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

  const { granted, isReRequest } = await requestMiniProgramSubscription()
  if (granted) {
    form.miniProgramReminderEnabled = true
    return
  }

  // 订阅失败（首次拒绝或重复请求）
  form.miniProgramReminderEnabled = false
  if (isReRequest) {
    uni.showModal({
      title: '已拒绝订阅',
      content: '之前已拒绝过订阅消息，微信不再弹出授权窗口。\n\n请前往：小程序右上角 "···" → 设置 → 订阅消息，重新开启后返回此页保存。',
      confirmText: '知道了',
      showCancel: false
    })
  } else {
    uni.$feedback.info('未授权订阅消息')
  }
}

async function triggerDiary() {
  triggering.diary = true
  try {
    await triggerDiaryReminder()
    uni.$feedback.success('日记提醒已触发，请查看微信服务通知')
  } catch {
    uni.$feedback.error('触发失败，请稍后重试')
  } finally {
    triggering.diary = false
  }
}

async function triggerLedger() {
  triggering.ledger = true
  try {
    await triggerLedgerReminder()
    uni.$feedback.success('记账提醒已触发，请查看微信服务通知')
  } catch {
    uni.$feedback.error('触发失败，请稍后重试')
  } finally {
    triggering.ledger = false
  }
}

async function triggerMonthly() {
  triggering.monthly = true
  try {
    await triggerMonthlyReminder()
    uni.$feedback.success('记账月报已触发，请查看微信服务通知')
  } catch {
    uni.$feedback.error('触发失败，请稍后重试')
  } finally {
    triggering.monthly = false
  }
}

async function triggerMemorial() {
  triggering.memorial = true
  try {
    await triggerMemorialReminder()
    uni.$feedback.success('纪念日提醒已触发，请查看微信服务通知')
  } catch {
    uni.$feedback.error('触发失败，请稍后重试')
  } finally {
    triggering.memorial = false
  }
}

async function submit() {
  submitting.value = true
  try {
    if (form.miniProgramReminderEnabled) {
      const { granted } = await requestMiniProgramSubscription()
      if (!granted) throw new Error('订阅消息未授权，请先开启订阅')
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

async function requestMiniProgramSubscription(): Promise<{ granted: boolean; isReRequest: boolean }> {
  const templateIds = [
    MP_DIARY_TEMPLATE_ID,
    MP_LEDGER_TEMPLATE_ID,
    MP_LEDGER_MONTHLY_TEMPLATE_ID,
    MP_MEMORIAL_TEMPLATE_ID
  ].filter(Boolean)
  if (!templateIds.length) throw new Error('请先配置订阅消息模板 ID')
  return new Promise((resolve, reject) => {
    // #ifdef MP-WEIXIN
    uni.requestSubscribeMessage({
      tmplIds: templateIds,
      success: (result) => {
        const subscribeResult = result as unknown as Record<string, string>
        const accepted = templateIds.some((id) => subscribeResult?.[id] === 'accept')
        const allRejected = templateIds.every((id) => subscribeResult?.[id] === 'reject')
        // 全部 reject → 可能是第一次拒绝或重复请求，都引导用户去微信设置
        resolve({ granted: accepted, isReRequest: allRejected })
      },
      fail: (err) => {
        // 调用失败（如已拒绝过，微信不再弹窗），也引导去设置
        resolve({ granted: false, isReRequest: true })
      }
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
  padding-bottom: var(--bottom-padding);
}

/* ========== 顶栏 ========== */
.reminder-header {
  padding: var(--space-5) var(--space-6) var(--space-3);
}

.reminder-header__title {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
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

.reminder-card__tip {
  margin-top: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-medium);
  background: #FFF8E1;
}

.reminder-card__tip-text {
  color: var(--color-text-muted);
  font-size: 20rpx;
  line-height: var(--leading-relaxed);
}

/* ========== 手动触发按钮网格 ========== */
.reminder-trigger-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-top: var(--space-4);
}

.reminder-trigger-grid .u-button {
  flex: 1;
  min-width: 140rpx;
}

/* ========== 提交 ========== */
.reminder-submit {
  margin: var(--space-6) var(--space-4) 0;
}
</style>
