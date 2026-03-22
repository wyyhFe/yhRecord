<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-copy">
        <view class="section-copy__title">编辑个人信息</view>
        <view class="section-copy__desc">这里修改昵称、性别、生日和个性签名，保存后会同步更新个人中心。</view>
      </view>

      <view class="block-stack">
        <view class="field-label">昵称</view>
        <u-input
          v-model="form.nickname"
          placeholder="请输入昵称"
          :border="true"
          border-color="#eadfd0"
          :custom-style="fieldStyle"
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
        <u-cell-group :border="false">
          <picker mode="date" :value="form.birthday || ''" @change="onBirthdayChange">
            <u-cell-item title="生日" :value="form.birthday || '请选择'" :border-bottom="false" />
          </picker>
        </u-cell-group>
      </view>

      <view class="block-stack">
        <view class="field-label">个性签名</view>
        <u-textarea
          v-model="form.signature"
          placeholder="写一句让自己喜欢的话"
          :border="true"
          border-color="#eadfd0"
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
      color="linear-gradient(135deg, #c47c52 0%, #d7a648 100%)"
      :loading="submitting"
      @click="submit"
    >
      {{ submitting ? '保存中...' : '保存资料' }}
    </u-button>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { updateUserProfile } from '@/api/user'
import { useAppStore } from '@/stores/app'
import type { UserProfileUpdatePayload } from '@/types/domain'

const appStore = useAppStore()
const submitting = ref(false)

const genderOptions: Array<{ label: string; value: 'UNKNOWN' | 'MALE' | 'FEMALE' }> = [
  { label: '未设置', value: 'UNKNOWN' },
  { label: '男', value: 'MALE' },
  { label: '女', value: 'FEMALE' }
]

const form = ref<UserProfileUpdatePayload>({
  nickname: '',
  gender: 'UNKNOWN',
  birthday: '',
  signature: ''
})

const fieldStyle = {
  background: '#fcf5ec',
  borderRadius: '20rpx',
  padding: '0 22rpx',
  fontSize: '28rpx',
  minHeight: '84rpx'
}

const textareaStyle = {
  background: '#fcf5ec',
  borderRadius: '20rpx',
  padding: '18rpx 22rpx',
  fontSize: '26rpx',
  width: '100%',
  boxSizing: 'border-box' as const
}

function onBirthdayChange(event: { detail: { value: string } }) {
  form.value.birthday = event.detail.value
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
  form.value.gender = profile.gender || 'UNKNOWN'
  form.value.birthday = profile.birthday || ''
  form.value.signature = profile.signature || ''
})
</script>

<style scoped lang="scss">
.edit-profile-gender {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.edit-profile-gender__item {
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  border: 1rpx solid #ead9c7;
  background: #fff8ef;
  color: #8a735f;
  font-size: 24rpx;
}

.edit-profile-gender__item--active {
  border-color: #c47c52;
  background: rgba(196, 124, 82, 0.12);
  color: #a15f3d;
}
</style>
