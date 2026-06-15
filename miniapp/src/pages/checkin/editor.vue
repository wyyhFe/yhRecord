<template>
  <view class="page-shell-safe editor-page">
    <!-- Hero -->
    <view class="editor-hero">
      <text class="editor-hero__icon">✓</text>
      <text class="editor-hero__title">创建打卡任务</text>
      <text class="editor-hero__sub">设定一个小目标，每天坚持完成</text>
    </view>

    <!-- 表单卡片 -->
    <view class="editor-card">
      <!-- 任务名称 -->
      <view class="editor-field">
        <view class="editor-field__label">
          <text class="editor-field__icon">✏️</text>
          <text class="editor-field__text">任务名称</text>
          <text class="editor-field__required">*</text>
        </view>
        <view class="editor-field__input-wrap">
          <input
            v-model="form.name"
            class="editor-field__input"
            placeholder="例如：20 个俯卧撑"
            :maxlength="128"
          />
          <text class="editor-field__count">{{ form.name.length }}/128</text>
        </view>
      </view>

      <!-- 任务描述 -->
      <view class="editor-field">
        <view class="editor-field__label">
          <text class="editor-field__icon">📝</text>
          <text class="editor-field__text">任务描述</text>
          <text class="editor-field__optional">选填</text>
        </view>
        <view class="editor-field__input-wrap">
          <textarea
            v-model="form.description"
            class="editor-field__textarea"
            placeholder="描述一下任务规则和目标..."
            :maxlength="255"
            :auto-height="true"
            :max-height="200"
          />
        </view>
      </view>

      <!-- 开始日期 -->
      <view class="editor-field">
        <view class="editor-field__label">
          <text class="editor-field__icon">📅</text>
          <text class="editor-field__text">开始日期</text>
        </view>
        <picker mode="date" :value="form.startDate" @change="onDateChange">
          <view class="editor-field__picker">
            <text class="editor-field__picker-value">{{ form.startDate }}</text>
            <text class="editor-field__picker-arrow">›</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 预览卡片 -->
    <view v-if="form.name.trim()" class="editor-preview">
      <view class="editor-preview__header">
        <text class="editor-preview__label">预览效果</text>
      </view>
      <view class="editor-preview__card">
        <view class="editor-preview__left">
          <view class="editor-preview__dot" />
          <view class="editor-preview__info">
            <text class="editor-preview__name">{{ form.name }}</text>
            <text class="editor-preview__meta">{{ form.description || '暂无描述' }} · {{ form.startDate }}</text>
          </view>
        </view>
        <view class="editor-preview__btn">
          <text class="editor-preview__btn-text">打卡</text>
        </view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="editor-submit">
      <u-button
        shape="circle"
        type="primary"
        color="linear-gradient(135deg, var(--color-checkin) 0%, #B49AD8 100%)"
        :loading="submitting"
        :disabled="!form.name.trim()"
        @click="submit"
      >
        {{ submitting ? '创建中...' : '创建任务' }}
      </u-button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
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
.editor-page {
  padding-bottom: var(--space-10);
}

/* ========== Hero ========== */
.editor-hero {
  background: var(--color-checkin-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-8) var(--space-6) var(--space-7);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  color: #fff;
}

.editor-hero__icon {
  width: 96rpx;
  height: 96rpx;
  line-height: 96rpx;
  text-align: center;
  font-size: 48rpx;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.2);
  margin-bottom: var(--space-4);
}

.editor-hero__title {
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.editor-hero__sub {
  margin-top: var(--space-2);
  font-size: var(--font-meta);
  opacity: 0.85;
}

/* ========== 表单卡片 ========== */
.editor-card {
  margin: var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.editor-field {
  & + & {
    margin-top: var(--space-5);
  }
}

.editor-field__label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.editor-field__icon {
  font-size: 28rpx;
}

.editor-field__text {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.editor-field__required {
  color: var(--color-danger);
  font-size: var(--font-meta);
}

.editor-field__optional {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.editor-field__input-wrap {
  position: relative;
}

.editor-field__input {
  width: 100%;
  height: 88rpx;
  padding: 0 var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-body);
  box-sizing: border-box;
}

.editor-field__textarea {
  width: 100%;
  min-height: 120rpx;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-body);
  box-sizing: border-box;
}

.editor-field__count {
  position: absolute;
  right: var(--space-3);
  bottom: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.editor-field__picker {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
}

.editor-field__picker-value {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-medium);
}

.editor-field__picker-arrow {
  color: var(--color-text-muted);
  font-size: 32rpx;
}

/* ========== 预览卡片 ========== */
.editor-preview {
  margin: var(--space-4) var(--space-4) 0;
}

.editor-preview__header {
  margin-bottom: var(--space-2);
}

.editor-preview__label {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.editor-preview__card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
}

.editor-preview__left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1;
  min-width: 0;
}

.editor-preview__dot {
  width: 48rpx;
  height: 48rpx;
  border-radius: var(--radius-full);
  border: 3rpx solid var(--color-border-strong);
  flex-shrink: 0;
}

.editor-preview__info {
  flex: 1;
  min-width: 0;
}

.editor-preview__name {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.editor-preview__meta {
  display: block;
  margin-top: 4rpx;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.editor-preview__btn {
  padding: var(--space-2) var(--space-5);
  border-radius: var(--radius-full);
  background: var(--color-checkin-gradient);
  flex-shrink: 0;
  margin-left: var(--space-3);
}

.editor-preview__btn-text {
  color: #fff;
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

/* ========== 提交按钮 ========== */
.editor-submit {
  margin: var(--space-6) var(--space-4) 0;
}
</style>
