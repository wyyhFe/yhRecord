<template>
  <view class="page-shell-safe memorial-page">
    <!-- 顶栏 -->
    <view class="memorial-header">
      <text class="memorial-header__title">纪念日管理</text>
    </view>

    <!-- 列表 -->
    <view v-if="items && items.length" class="memorial-list">
      <view v-for="item in items" :key="item.id" class="memorial-item">
        <view class="memorial-item__header">
          <text class="memorial-item__title">{{ item.title }}</text>
          <view class="memorial-item__badge" :class="item.annualRepeat ? 'memorial-item__badge--repeat' : ''">
            <text class="memorial-item__badge-text">{{ item.annualRepeat ? '每年重复' : '单次' }}</text>
          </view>
        </view>
        <text class="memorial-item__meta">{{ item.type || '纪念日' }} · {{ item.memorialDate }}</text>
        <text v-if="item.remark" class="memorial-item__remark">{{ item.remark }}</text>
        <text class="memorial-item__remind">{{ item.remindAt ? `提醒：${formatReminder(item.remindAt)}` : '未设置提醒' }}</text>
        <view class="memorial-item__actions">
          <u-button shape="circle" plain :hair-line="false" size="small" @click="editItem(item)">编辑</u-button>
          <u-button shape="circle" type="error" plain :hair-line="false" size="small" @click="removeItem(item.id)">删除</u-button>
        </view>
      </view>
    </view>
    <EmptyStateCard v-else title="还没有纪念日" description="新建一条重要日期，提醒和回顾页面会自动使用" mode="history" />

    <!-- 加载更多 -->
    <LoadMore :state="loadingMore ? 'loading' : noMore ? 'noMore' : 'hidden'" />

    <!-- 新建按钮 -->
    <view class="memorial-action">
      <u-button shape="circle" type="primary" color="var(--color-memory-gradient)" @click="openCreatePopup">
        ＋ 新建纪念日
      </u-button>
    </view>

    <!-- 弹窗 -->
    <u-popup v-model="showPopup" mode="bottom" border-radius="28" :safe-area-inset-bottom="true">
      <view class="memorial-popup">
        <scroll-view scroll-y class="memorial-popup__body">
          <view class="memorial-popup__head">
            <text class="memorial-popup__title">{{ editingId ? '编辑纪念日' : '新建纪念日' }}</text>
          </view>

          <!-- 标题 -->
          <view class="memorial-popup__card">
            <text class="memorial-popup__label">标题</text>
            <input v-model="form.title" class="memorial-popup__input" placeholder="例如：第一次旅行、生日" />
          </view>

          <!-- 类型 -->
          <view class="memorial-popup__card">
            <text class="memorial-popup__label">类型</text>
            <input v-model="form.type" class="memorial-popup__input" placeholder="例如：LIFE / LOVE / FAMILY" />
          </view>

          <!-- 日期选择 -->
          <view class="memorial-popup__card">
            <picker mode="date" :value="form.memorialDate" @change="onMemorialDateChange">
              <view class="memorial-popup__row">
                <text class="memorial-popup__row-label">纪念日期</text>
                <text class="memorial-popup__row-value">{{ form.memorialDate }} ›</text>
              </view>
            </picker>
            <picker mode="date" :value="remindDateText" @change="onRemindDateChange">
              <view class="memorial-popup__row">
                <text class="memorial-popup__row-label">提醒日期</text>
                <text class="memorial-popup__row-value">{{ remindDateText }} ›</text>
              </view>
            </picker>
            <picker mode="time" :value="remindTimeText" @change="onRemindTimeChange">
              <view class="memorial-popup__row">
                <text class="memorial-popup__row-label">提醒时间</text>
                <text class="memorial-popup__row-value">{{ remindTimeText }} ›</text>
              </view>
            </picker>
          </view>

          <!-- 每年重复 -->
          <view class="memorial-popup__card">
            <view class="memorial-popup__switch">
              <view>
                <text class="memorial-popup__label">每年重复</text>
                <text class="memorial-popup__hint">生日、纪念日建议打开</text>
              </view>
              <switch :checked="form.annualRepeat" color="var(--color-primary)" @change="onRepeatChange" />
            </view>
          </view>

          <!-- 备注 -->
          <view class="memorial-popup__card">
            <text class="memorial-popup__label">备注</text>
            <textarea v-model="form.remark" class="memorial-popup__textarea" placeholder="补充背景信息" />
          </view>

          <!-- 操作 -->
          <view class="memorial-popup__actions">
            <u-button shape="circle" plain :hair-line="false" @click="closePopup">取消</u-button>
            <u-button shape="circle" type="primary" color="var(--color-memory-gradient)" :loading="submitting" @click="submit">
              {{ submitting ? '保存中...' : '保存' }}
            </u-button>
          </view>
        </scroll-view>
      </view>
    </u-popup>
  </view>
</template>

<script setup lang="ts">
import EmptyStateCard from '@/components/business/empty-state-card'
import LoadMore from '@/components/business/load-more/index.vue'
import { computed, reactive, ref } from 'vue'
import { onReachBottom, onShareAppMessage, onShareTimeline, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import {
  createMemorialDay,
  deleteMemorialDay,
  fetchMemorialDays,
  updateMemorialDay,
  type MemorialDayPayload
} from '@/api/memorial'
import type { Id, MemorialDay } from '@/types/domain'
import { MP_MEMORIAL_TEMPLATE_ID } from '@/config/app'

/** 纪念日订阅续订 */
function renewMemorialSubscription() {
  // #ifdef MP-WEIXIN
  if (!MP_MEMORIAL_TEMPLATE_ID) return
  const today = new Date().toDateString()
  if (uni.getStorageSync('last_memorial_subscribe_date') === today) return

  uni.requestSubscribeMessage({
    tmplIds: [MP_MEMORIAL_TEMPLATE_ID],
    success: () => uni.setStorageSync('last_memorial_subscribe_date', today),
    fail: () => {}
  })
  // #endif
}

onShareAppMessage(() => ({ title: '纪念日' }))
onShareTimeline(() => ({ title: '纪念日' }))

onShow(() => {
  loadMemorialDays(true)
  renewMemorialSubscription()
})
const items = ref<MemorialDay[]>([])
const pageNum = ref(1)
const total = ref(0)
const noMore = ref(false)
const loadingMore = ref(false)
const showPopup = ref(false)
const submitting = ref(false)
const editingId = ref<Id>()
const today = new Date().toISOString().slice(0, 10)

const form = reactive({
  title: '',
  type: '',
  memorialDate: today,
  annualRepeat: true,
  remark: '',
  remindDate: today,
  remindTime: '09:00'
})

const remindDateText = computed(() => form.remindDate || '不设置')
const remindTimeText = computed(() => form.remindTime || '09:00')

function resetForm() {
  editingId.value = undefined
  form.title = ''
  form.type = ''
  form.memorialDate = today
  form.annualRepeat = true
  form.remark = ''
  form.remindDate = today
  form.remindTime = '09:00'
}

function openCreatePopup() { resetForm(); showPopup.value = true }
function closePopup() { showPopup.value = false; resetForm() }

function onMemorialDateChange(event: { detail: { value: string } }) {
  form.memorialDate = event.detail.value
  if (!editingId.value) form.remindDate = event.detail.value
}
function onRemindDateChange(event: { detail: { value: string } }) { form.remindDate = event.detail.value }
function onRemindTimeChange(event: { detail: { value: string } }) { form.remindTime = event.detail.value }
function onRepeatChange(event: Event) {
  const payload = event as Event & { detail?: { value?: boolean } }
  form.annualRepeat = Boolean(payload.detail?.value)
}

function buildPayload(): MemorialDayPayload {
  return {
    title: form.title.trim(),
    type: form.type.trim() || 'MEMORIAL',
    memorialDate: form.memorialDate,
    annualRepeat: form.annualRepeat,
    remark: form.remark.trim() || undefined,
    remindAt: form.remindDate && form.remindTime ? `${form.remindDate} ${form.remindTime}:00` : undefined
  }
}

function editItem(item: MemorialDay) {
  editingId.value = item.id
  form.title = item.title
  form.type = item.type || 'MEMORIAL'
  form.memorialDate = item.memorialDate
  form.annualRepeat = Boolean(item.annualRepeat)
  form.remark = item.remark || ''
  if (item.remindAt) {
    const [datePart, timePart] = item.remindAt.replace('T', ' ').split(' ')
    form.remindDate = datePart || item.memorialDate
    form.remindTime = (timePart || '09:00:00').slice(0, 5)
  } else {
    form.remindDate = item.memorialDate
    form.remindTime = '09:00'
  }
  showPopup.value = true
}

function formatReminder(value: string) { return value.replace('T', ' ').slice(0, 16) }

async function loadMemorialDays(reset = false) {
  if (reset) { pageNum.value = 1; noMore.value = false }
  if (loadingMore.value || noMore.value) return
  loadingMore.value = true
  try {
    const result = await fetchMemorialDays(pageNum.value)
    const list = result.list || []
    if (reset) { items.value = list } else { items.value = [...items.value, ...list] }
    total.value = result.total
    noMore.value = items.value.length >= total.value
    if (!noMore.value) pageNum.value++
  } catch (error) {
    if (reset) items.value = []
    uni.$feedback.error(error, undefined, '加载失败')
  } finally { loadingMore.value = false }
}

async function removeItem(id: Id) {
  const result = await uni.showModal({ title: '确认删除', content: '删除后不再参与提醒和回顾。' })
  if (!result.confirm) return
  try { await deleteMemorialDay(id); uni.$feedback.success('已删除'); await loadMemorialDays(true) }
  catch (error) { uni.$feedback.error(error, undefined, '删除失败') }
}

async function submit() {
  if (!form.title.trim()) { uni.$feedback.error('请先填写标题'); return }
  if (!form.type.trim()) { uni.$feedback.error('请先填写类型'); return }
  submitting.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) { await updateMemorialDay(editingId.value, payload); uni.$feedback.success('已更新') }
    else { await createMemorialDay(payload); uni.$feedback.success('已创建') }
    closePopup()
    await loadMemorialDays(true)
  } catch (error) { uni.$feedback.error(error, undefined, '保存失败') }
  finally { submitting.value = false }
}

onPullDownRefresh(() => { loadMemorialDays(true).finally(() => uni.stopPullDownRefresh()) })
onReachBottom(() => { loadMemorialDays() })
</script>

<style scoped lang="scss">
.memorial-page {
  padding-bottom: var(--bottom-padding);
}

/* ========== 顶栏 ========== */
.memorial-header {
  padding: var(--space-5) var(--space-6) var(--space-3);
}

.memorial-header__title {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

/* ========== 列表 ========== */
.memorial-list {
  margin: var(--space-4) var(--space-4) 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.memorial-item {
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-4) var(--space-5);
}

.memorial-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.memorial-item__title {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-bold);
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.memorial-item__badge {
  padding: 4rpx 14rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  flex-shrink: 0;
}

.memorial-item__badge--repeat {
  background: var(--color-memory-soft);
}

.memorial-item__badge-text {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.memorial-item__badge--repeat .memorial-item__badge-text {
  color: var(--color-memory);
}

.memorial-item__meta {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.memorial-item__remark {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  line-height: var(--leading-relaxed);
}

.memorial-item__remind {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.memorial-item__actions {
  margin-top: var(--space-3);
  display: flex;
  gap: var(--space-3);
}

/* ========== 操作 ========== */
.memorial-action {
  margin: var(--space-6) var(--space-4) 0;
}

/* ========== 弹窗 ========== */
.memorial-popup {
  background: var(--color-bg);
  max-height: 80vh;
  padding: var(--space-5) var(--space-4) calc(var(--space-5) + env(safe-area-inset-bottom));
}

.memorial-popup__body {
  max-height: calc(80vh - 40rpx);
}

.memorial-popup__head {
  text-align: center;
  margin-bottom: var(--space-4);
}

.memorial-popup__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.memorial-popup__card {
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-4);
  margin-bottom: var(--space-3);
}

.memorial-popup__label {
  display: block;
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
  margin-bottom: var(--space-2);
}

.memorial-popup__hint {
  display: block;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  margin-top: 4rpx;
}

.memorial-popup__input {
  width: 100%;
  height: 80rpx;
  padding: 0 var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-body);
  box-sizing: border-box;
}

.memorial-popup__textarea {
  width: 100%;
  min-height: 100rpx;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-body);
  box-sizing: border-box;
}

.memorial-popup__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) 0;
  border-bottom: 1rpx solid var(--color-divider);

  &:last-child {
    border-bottom: none;
  }
}

.memorial-popup__row-label {
  color: var(--color-text-secondary);
  font-size: var(--font-body);
}

.memorial-popup__row-value {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-medium);
}

.memorial-popup__switch {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.memorial-popup__actions {
  margin-top: var(--space-2);
  display: flex;
  gap: var(--space-3);
}

.memorial-popup__actions .u-button {
  flex: 1;
}
</style>
