<template>
  <view class="page-shell-safe edit-page">
    <!-- Hero -->
    <view class="edit-hero">
      <view class="edit-hero__avatar" @tap="onChooseAvatar">
        <image v-if="avatarPreview" :src="avatarPreview" mode="aspectFill" class="edit-hero__avatar-img" />
        <text v-else class="edit-hero__avatar-text">{{ avatarText }}</text>
        <view class="edit-hero__avatar-badge">
          <text class="edit-hero__avatar-badge-text">📷</text>
        </view>
      </view>
      <button class="edit-hero__btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">更换头像</button>
    </view>

    <!-- 表单 -->
    <view class="edit-card">
      <view class="edit-field">
        <text class="edit-field__label">👤 昵称</text>
        <input v-model="form.nickname" class="edit-field__input" type="nickname" placeholder="请输入昵称" />
      </view>

      <view class="edit-field">
        <text class="edit-field__label">⚧ 性别</text>
        <view class="edit-field__picker" @tap="genderPickerVisible = true">
          <text
            class="edit-field__picker-value"
            :class="{ 'edit-field__picker-placeholder': !form.gender || form.gender === 'UNKNOWN' }"
          >
            {{ genderLabel || '请选择' }}
          </text>
          <text class="edit-field__picker-arrow">›</text>
        </view>
      </view>

      <view class="edit-field">
        <text class="edit-field__label">📅 生日</text>
        <picker mode="date" :value="form.birthday || ''" @change="onBirthdayChange">
          <view class="edit-field__picker">
            <text class="edit-field__picker-value">{{ form.birthday || '请选择' }}</text>
            <text class="edit-field__picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="edit-field">
        <text class="edit-field__label">✍️ 个性签名</text>
        <textarea
          v-model="form.signature"
          class="edit-field__textarea"
          placeholder="写一句让自己喜欢的话"
          :maxlength="80"
          :auto-height="true"
        />
      </view>
    </view>

    <!-- 性别选择弹窗 -->
    <u-popup :model-value="genderPickerVisible" mode="bottom" border-radius="28" @update:model-value="genderPickerVisible = $event">
      <view class="edit-gender-popup">
        <view class="edit-gender-popup__header">
          <text class="edit-gender-popup__title">选择性别</text>
          <text class="edit-gender-popup__close" @tap="genderPickerVisible = false">✕</text>
        </view>
        <view class="edit-gender-popup__tags">
          <view
            v-for="item in genderOptions"
            :key="item.value"
            class="edit-gender-popup__tag"
            :class="{ 'edit-gender-popup__tag--active': form.gender === item.value }"
            hover-class="edit-gender-popup__tag--pressed"
            :hover-stay-time="80"
            @tap="onGenderSelect(item.value)"
          >
            <text
              class="edit-gender-popup__tag-icon"
              :class="`edit-gender-popup__tag-icon--${item.value.toLowerCase()}`"
            >{{ genderIcon(item.value) }}</text>
            <text class="edit-gender-popup__tag-text">{{ item.label }}</text>
          </view>
        </view>
      </view>
    </u-popup>

    <!-- 保存 -->
    <view class="edit-submit">
      <u-button
        shape="circle"
        type="primary"
        color="var(--color-primary-gradient)"
        :loading="submitting"
        @click="submit"
      >
        {{ submitting ? '保存中...' : '保存资料' }}
      </u-button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { updateUserProfile } from '@/api/user'
import { useAppStore } from '@/stores/app'
import type { UserProfileUpdatePayload } from '@/types/domain'
import { uploadImageToOss } from '@/utils/upload'
import { resolveImage } from '@/utils/image'

const appStore = useAppStore()
const submitting = ref(false)

const genderPickerVisible = ref(false)

const genderOptions: Array<{ label: string; value: 'UNKNOWN' | 'MALE' | 'FEMALE' }> = [
  { label: '未设置', value: 'UNKNOWN' },
  { label: '男', value: 'MALE' },
  { label: '女', value: 'FEMALE' }
]

const genderLabel = computed(() => genderOptions.find((g) => g.value === form.value.gender)?.label)

function genderIcon(value: string) {
  return value === 'MALE' ? '♂' : value === 'FEMALE' ? '♀' : '—'
}

function onGenderSelect(value: 'UNKNOWN' | 'MALE' | 'FEMALE') {
  form.value.gender = value
  genderPickerVisible.value = false
}

const form = ref<UserProfileUpdatePayload>({
  nickname: '',
  avatarPath: '',
  gender: 'UNKNOWN',
  birthday: '',
  signature: ''
})

const avatarPreview = ref('')

const avatarText = computed(() => {
  const name = form.value.nickname?.trim()
  return name ? name.slice(0, 1) : '我'
})

function onBirthdayChange(event: { detail: { value: string } }) {
  form.value.birthday = event.detail.value
}

async function onChooseAvatar(event: { detail?: { avatarUrl?: string } }) {
  const avatarUrl = event.detail?.avatarUrl
  if (!avatarUrl) return
  try {
    avatarPreview.value = avatarUrl
    form.value.avatarPath = await uploadImageToOss({ filePath: avatarUrl, dir: 'avatar/' })
    uni.$feedback.success('头像已更新')
  } catch (error) {
    uni.$feedback.error(error, undefined, '头像上传失败')
  }
}

async function submit() {
  if (!form.value.nickname?.trim()) {
    uni.$feedback.error('请先填写昵称')
    return
  }
  submitting.value = true
  try {
    await updateUserProfile({
      nickname: form.value.nickname.trim(),
      avatarPath: form.value.avatarPath || undefined,
      gender: form.value.gender,
      birthday: form.value.birthday || undefined,
      signature: form.value.signature?.trim() || undefined
    })
    await appStore.loadProfile()
    uni.$feedback.success('个人信息已更新')
    setTimeout(() => uni.navigateBack({ delta: 1 }), 500)
  } catch (error) {
    uni.$feedback.error(error, undefined, '保存失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  const profile = appStore.profile
  if (!profile) return
  form.value.nickname = profile.nickname || ''
  form.value.avatarPath = profile.avatarPath || ''
  form.value.gender = profile.gender || 'UNKNOWN'
  form.value.birthday = profile.birthday || ''
  form.value.signature = profile.signature || ''
  avatarPreview.value = resolveImage(profile.avatarPath)
})
</script>

<style scoped lang="scss">
.edit-page {
  padding-bottom: var(--bottom-padding);
}

/* ========== Hero ========== */
.edit-hero {
  background: var(--color-primary-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-8) var(--space-6) var(--space-6);
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #fff;
}

.edit-hero__avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  margin-bottom: var(--space-3);
}

.edit-hero__avatar-img {
  width: 100%;
  height: 100%;
}

.edit-hero__avatar-text {
  font-size: 64rpx;
  font-weight: var(--weight-bold);
}

.edit-hero__avatar-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 48rpx;
  height: 48rpx;
  border-radius: var(--radius-full);
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-hero__avatar-badge-text {
  font-size: 24rpx;
}

.edit-hero__btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
  font-size: var(--font-meta);
  padding: var(--space-2) var(--space-5);
  border-radius: var(--radius-full);
  line-height: 1.6;
}

.edit-hero__btn::after {
  border: none;
}

/* ========== 表单卡片 ========== */
.edit-card {
  margin: var(--space-4) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.edit-field {
  & + & {
    margin-top: var(--space-5);
  }
}

.edit-field__label {
  display: block;
  margin-bottom: var(--space-3);
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.edit-field__input {
  width: 100%;
  height: 88rpx;
  padding: 0 var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-body);
  box-sizing: border-box;
}

.edit-field__textarea {
  width: 100%;
  min-height: 120rpx;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-body);
  box-sizing: border-box;
}

.edit-field__picker {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
}

.edit-field__picker-value {
  color: var(--color-text-primary);
  font-size: var(--font-body);
}

.edit-field__picker-arrow {
  color: var(--color-text-muted);
  font-size: 32rpx;
}

/* 性别下拉框 */
.edit-field__picker-placeholder {
  color: var(--color-text-muted) !important;
}

/* ========== 性别选择弹窗 ========== */
.edit-gender-popup {
  padding: var(--space-5) var(--space-6) var(--space-10);
}

.edit-gender-popup__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.edit-gender-popup__title {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.edit-gender-popup__close {
  width: 52rpx;
  height: 52rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  font-size: var(--font-caption);
  line-height: 1;
}

.edit-gender-popup__tags {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.edit-gender-popup__tag {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-radius: var(--radius-large);
  background: var(--color-surface-soft);
  transition: all var(--motion-fast) var(--ease-standard);
}

.edit-gender-popup__tag--pressed {
  transform: scale(0.97);
  opacity: 0.8;
}

.edit-gender-popup__tag--active {
  background: var(--color-primary-soft);
  border: 2rpx solid var(--color-primary);
}

.edit-gender-popup__tag-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  line-height: 1;
  background: var(--color-surface);
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.edit-gender-popup__tag--active .edit-gender-popup__tag-icon {
  background: var(--color-primary);
  color: #fff;
}

.edit-gender-popup__tag-text {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-medium);
}

.edit-gender-popup__tag--active .edit-gender-popup__tag-text {
  color: var(--color-primary);
  font-weight: var(--weight-semibold);
}

/* ========== 提交 ========== */
.edit-submit {
  margin: var(--space-6) var(--space-4) 0;
}
</style>
