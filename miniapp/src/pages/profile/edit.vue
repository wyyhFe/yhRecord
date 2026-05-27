<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-copy">
        <view class="section-copy__title">编辑个人信息</view>
        <view class="section-copy__desc">
          微信原生支持的昵称和头像在这里直接采集，保存后会同步更新个人中心。
        </view>
      </view>

      <view class="edit-profile-avatar">
        <image
          v-if="avatarPreview"
          :src="avatarPreview"
          mode="aspectFill"
          class="edit-profile-avatar__image"
        />
        <view v-else class="edit-profile-avatar__fallback">{{ avatarText }}</view>
      </view>

      <button class="wechat-avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
        使用微信头像
      </button>

      <view class="block-stack">
        <view class="field-label">昵称</view>
        <input
          v-model="form.nickname"
          class="wechat-input"
          type="nickname"
          placeholder="请输入昵称"
        />
      </view>

      <view class="block-stack">
        <view class="field-label">性别</view>
        <view class="edit-profile-gender">
          <view
            v-for="item in genderOptions"
            :key="item.value"
            class="edit-profile-gender__item"
            :class="{ 'edit-profile-gender__item--active': form.gender === item.value }"
            @tap="form.gender = item.value"
          >
            {{ item.label }}
          </view>
        </view>
      </view>

      <view class="block-stack">
        <view class="field-label">生日</view>
        <picker mode="date" :value="form.birthday || ''" @change="onBirthdayChange">
          <view class="picker-row">
            <text class="picker-row__value">{{ form.birthday || '请选择' }}</text>
            <text class="picker-row__arrow">></text>
          </view>
        </picker>
      </view>

      <view class="block-stack">
        <view class="field-label">个性签名</view>
        <u-textarea
          v-model="form.signature"
          placeholder="写一句让自己喜欢的话"
          :border="true"
          border-color="var(--color-border-strong)"
          :custom-style="textareaStyle"
          height="180"
          :maxlength="80"
        />
      </view>
    </view>

    <u-button
      class="primary-action"
      type="primary"
      shape="circle"
      :hair-line="false"
      color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
      :loading="submitting"
      @click="submit"
    >
      {{ submitting ? '保存中...' : '保存资料' }}
    </u-button>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { updateUserProfile } from '@/api/user'
import { useAppStore } from '@/stores/app'
import type { UserProfileUpdatePayload } from '@/types/domain'
import { uploadImageToOss } from '@/utils/upload'

const appStore = useAppStore()
const submitting = ref(false)

const genderOptions: Array<{ label: string; value: 'UNKNOWN' | 'MALE' | 'FEMALE' }> = [
  { label: '未设置', value: 'UNKNOWN' },
  { label: '男', value: 'MALE' },
  { label: '女', value: 'FEMALE' }
]

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

const textareaStyle = {
  background: 'var(--color-surface-soft)',
  borderRadius: '20rpx',
  padding: '18rpx 22rpx',
  fontSize: '26rpx',
  width: '100%',
  boxSizing: 'border-box' as const
}

function onBirthdayChange(event: { detail: { value: string } }) {
  form.value.birthday = event.detail.value
}

async function onChooseAvatar(event: { detail?: { avatarUrl?: string } }) {
  const avatarUrl = event.detail?.avatarUrl
  if (!avatarUrl) return

  try {
    avatarPreview.value = avatarUrl
    form.value.avatarPath = await uploadImageToOss({
      filePath: avatarUrl,
      dir: 'avatar/'
    })
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
  avatarPreview.value = profile.avatarPath || ''
})
</script>

<style scoped lang="scss">
.edit-profile-avatar {
  width: 148rpx;
  height: 148rpx;
  margin: 24rpx auto 18rpx;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 30rpx rgba(144, 88, 49, 0.18);
}

.edit-profile-avatar__image {
  width: 100%;
  height: 100%;
}

.edit-profile-avatar__fallback {
  color: var(--color-bg);
  font-size: 54rpx;
  font-weight: 700;
}

.wechat-avatar-btn {
  margin-bottom: 24rpx;
  border: none;
  background: transparent;
  color: var(--color-primary);
  font-size: 26rpx;
  line-height: 1.6;
}

.wechat-avatar-btn::after {
  border: none;
}

.wechat-input {
  min-height: 84rpx;
  border-radius: 20rpx;
  background: var(--color-surface-soft);
  padding: 0 22rpx;
  /* 用户输入内容 → 中性色 */
  color: var(--color-text-neutral);
  font-size: 28rpx;
  box-sizing: border-box;
}

.picker-row {
  min-height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  border-radius: 20rpx;
  background: var(--color-surface-soft);
  padding: 0 22rpx;
}

.picker-row__value {
  color: var(--color-text-primary);
  font-size: 28rpx;
}

.picker-row__arrow {
  color: var(--color-text-muted);
  font-size: 26rpx;
}

.edit-profile-gender {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.edit-profile-gender__item {
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  border: 1rpx solid var(--color-border-strong);
  background: var(--color-surface);
  color: var(--color-text-muted);
  font-size: 24rpx;
}

.edit-profile-gender__item--active {
  border-color: var(--color-primary);
  background: var(--color-border-strong);
  color: var(--color-primary-strong);
}
</style>
