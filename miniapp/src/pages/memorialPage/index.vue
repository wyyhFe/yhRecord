<template>
  <view class="page-shell-safe memorial-page">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">纪念日管理</view>
          <view class="section-copy__desc">把重要日期留下来，提醒、首页和去年今日都会复用这里的数据。</view>
        </view>
        <u-tag text="纪念日" type="warning" plain shape="circle" />
      </view>

      <view class="action-grid-2">
        <u-button
          type="primary"
          shape="circle"
          color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
          @click="openCreatePopup"
        >
          新建纪念日
        </u-button>
        <u-button shape="circle" plain @click="loadMemorialDays">刷新列表</u-button>
      </view>
    </view>

    <view class="page-section">
      <view v-if="items.length" class="list-stack">
        <view v-for="item in items" :key="item.id" class="list-card">
          <view class="list-card__head">
            <view>
              <view class="list-card__title">{{ item.title }}</view>
              <view class="list-card__meta">{{ item.type || '纪念日' }} | {{ item.memorialDate }}</view>
            </view>
            <view class="memorial-repeat-tag">{{ item.annualRepeat ? '每年重复' : '单次' }}</view>
          </view>

          <view v-if="item.remark" class="list-card__meta memorial-remark">{{ item.remark }}</view>
          <view class="list-card__meta">
            {{ item.remindAt ? `提醒时间 ${formatReminder(item.remindAt)}` : '未设置提醒时间' }}
          </view>

          <view class="action-grid-2">
            <u-button shape="circle" plain :hair-line="false" @click="editItem(item)">编辑</u-button>
            <u-button type="error" shape="circle" plain @click="removeItem(item.id)">删除</u-button>
          </view>
        </view>
      </view>
      <EmptyStateCard
        v-else
        title="还没有纪念日"
        description="先新建一条重要日期，后续提醒和回顾页面都会自动使用。"
      />
    </view>

    <u-popup v-model="showPopup" mode="bottom" border-radius="28" :safe-area-inset-bottom="true">
      <view class="memorial-popup">
        <scroll-view scroll-y class="memorial-popup__body">
          <view class="memorial-popup__head">
            <view class="memorial-popup__title">{{ editingId ? '编辑纪念日' : '新建纪念日' }}</view>
            <view class="memorial-popup__subtitle">先把必要信息填完整，提醒能力会直接复用这些字段。</view>
          </view>

          <view class="block-stack">
            <view class="field-label">标题</view>
            <u-input
              v-model="form.title"
              placeholder="例如：第一次旅行、生日、领证纪念日"
              :border="true"
              border-color="var(--color-border-strong)"
              :custom-style="fieldStyle"
            />
          </view>

          <view class="block-stack">
            <view class="field-label">类型</view>
            <u-input
              v-model="form.type"
              placeholder="例如：LIFE / LOVE / FAMILY"
              :border="true"
              border-color="var(--color-border-strong)"
              :custom-style="fieldStyle"
            />
          </view>

          <view class="block-stack">
            <view class="picker-card-list">
              <picker mode="date" :value="form.memorialDate" @change="onMemorialDateChange">
                <view class="picker-card-row">
                  <view class="picker-card-row__label">纪念日期</view>
                  <view class="picker-card-row__value">{{ form.memorialDate }}</view>
                </view>
              </picker>
              <picker mode="date" :value="remindDateText" @change="onRemindDateChange">
                <view class="picker-card-row">
                  <view class="picker-card-row__label">提醒日期</view>
                  <view class="picker-card-row__value">{{ remindDateText }}</view>
                </view>
              </picker>
              <picker mode="time" :value="remindTimeText" @change="onRemindTimeChange">
                <view class="picker-card-row">
                  <view class="picker-card-row__label">提醒时间</view>
                  <view class="picker-card-row__value">{{ remindTimeText }}</view>
                </view>
              </picker>
            </view>
          </view>

          <view class="memorial-switch-row">
            <view>
              <view class="field-label">每年重复</view>
              <view class="list-card__meta">生日、纪念日这类固定日期建议打开。</view>
            </view>
            <switch :checked="form.annualRepeat" color="var(--color-primary)" @change="onRepeatChange" />
          </view>

          <view class="block-stack">
            <view class="field-label">备注</view>
            <u-textarea
              v-model="form.remark"
              placeholder="补充一点背景信息，后面回顾时更容易看懂。"
              :border="true"
              border-color="var(--color-border-strong)"
              :custom-style="textareaStyle"
              height="180"
            />
          </view>

          <view class="action-grid-2 memorial-popup__actions">
            <u-button shape="circle" plain :hair-line="false" @click="closePopup">取消</u-button>
            <u-button
              type="primary"
              shape="circle"
              color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
              :loading="submitting"
              @click="submit"
            >
              {{ submitting ? '保存中...' : '保存' }}
            </u-button>
          </view>
        </scroll-view>
      </view>
    </u-popup>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import {
  createMemorialDay,
  deleteMemorialDay,
  fetchMemorialDays,
  updateMemorialDay,
  type MemorialDayPayload
} from '@/api/memorial'
import type { Id, MemorialDay } from '@/types/domain'

const items = ref<MemorialDay[]>([])
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

const fieldStyle = {
  background: 'var(--color-surface-soft)',
  borderRadius: 'var(--radius-medium)',
  padding: '0 var(--space-5)',
  fontSize: 'var(--font-body)',
  minHeight: '84rpx'
}

const textareaStyle = {
  background: 'var(--color-surface-soft)',
  borderRadius: 'var(--radius-medium)',
  padding: 'var(--space-4) var(--space-5)',
  fontSize: 'var(--font-caption)',
  width: '100%',
  boxSizing: 'border-box' as const
}

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

function openCreatePopup() {
  resetForm()
  showPopup.value = true
}

function closePopup() {
  showPopup.value = false
  resetForm()
}

function onMemorialDateChange(event: { detail: { value: string } }) {
  form.memorialDate = event.detail.value
  if (!editingId.value) {
    form.remindDate = event.detail.value
  }
}

function onRemindDateChange(event: { detail: { value: string } }) {
  form.remindDate = event.detail.value
}

function onRemindTimeChange(event: { detail: { value: string } }) {
  form.remindTime = event.detail.value
}

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

function formatReminder(value: string) {
  return value.replace('T', ' ').slice(0, 16)
}

async function loadMemorialDays() {
  try {
    items.value = await fetchMemorialDays()
  } catch (error) {
    items.value = []
    uni.$feedback.error(error, undefined, '加载纪念日失败')
  }
}

async function removeItem(id: Id) {
  const result = await uni.showModal({
    title: '确认删除',
    content: '删除后这条纪念日将不再参与提醒和回顾展示。'
  })
  if (!result.confirm) return

  try {
    await deleteMemorialDay(id)
    uni.$feedback.success('已删除')
    await loadMemorialDays()
  } catch (error) {
    uni.$feedback.error(error, undefined, '删除失败')
  }
}

async function submit() {
  if (!form.title.trim()) {
    uni.$feedback.error('请先填写标题')
    return
  }

  if (!form.type.trim()) {
    uni.$feedback.error('请先填写类型')
    return
  }

  submitting.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await updateMemorialDay(editingId.value, payload)
      uni.$feedback.success('纪念日已更新')
    } else {
      await createMemorialDay(payload)
      uni.$feedback.success('纪念日已创建')
    }
    closePopup()
    await loadMemorialDays()
  } catch (error) {
    uni.$feedback.error(error, undefined, editingId.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

onShow(() => {
  loadMemorialDays()
})
</script>

<style scoped lang="scss">
.memorial-page {
  padding-bottom: var(--space-5);
}

.memorial-repeat-tag {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
  font-size: var(--font-tiny);
}

.memorial-remark {
  margin-top: var(--space-3);
}

.memorial-popup {
  max-height: 74vh;
  padding: var(--space-6) var(--space-5) calc(var(--space-6) + env(safe-area-inset-bottom));
  background: var(--color-bg);
}

.memorial-popup__body {
  max-height: calc(74vh - 48rpx);
}

.memorial-popup__head {
  text-align: center;
}

.memorial-popup__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.memorial-popup__subtitle {
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.memorial-switch-row {
  margin-top: var(--space-4);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-5);
}

.memorial-popup__actions {
  margin-top: var(--space-6);
}

.picker-card-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.picker-card-row {
  min-height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: 0 var(--space-5);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
}

.picker-card-row__label {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
}

.picker-card-row__value {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}
</style>
