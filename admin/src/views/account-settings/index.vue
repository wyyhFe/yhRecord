<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import {
  getUserProfile,
  updateUserProfile,
  type UserProfileVO
} from "@/api/user";

defineOptions({
  name: "AccountSettings"
});

const loading = ref(true);
const saving = ref(false);
const profile = ref<UserProfileVO>({
  id: 0,
  openid: "",
  nickname: "",
  avatarPath: "",
  gender: "",
  officialAccountOpenid: "",
  birthday: "",
  signature: "",
  diaryCount: 0
});

const form = ref({
  nickname: "",
  gender: "",
  birthday: "",
  signature: ""
});

const genderOptions = [
  { label: "男", value: "MALE" },
  { label: "女", value: "FEMALE" },
  { label: "保密", value: "UNKNOWN" }
];

async function loadProfile() {
  loading.value = true;
  try {
    const { data } = await getUserProfile();
    if (data.code === 0) {
      profile.value = data.data;
      form.value = {
        nickname: data.data.nickname || "",
        gender: data.data.gender || "",
        birthday: data.data.birthday || "",
        signature: data.data.signature || ""
      };
    }
  } finally {
    loading.value = false;
  }
}

async function handleSave() {
  saving.value = true;
  try {
    const { data } = await updateUserProfile(form.value);
    if (data.code === 0) {
      profile.value = data.data;
      ElMessage.success("保存成功");
    }
  } finally {
    saving.value = false;
  }
}

onMounted(() => loadProfile());
</script>

<template>
  <div class="account-settings" v-loading="loading">
    <el-card shadow="hover">
      <template #header>
        <span>基本信息</span>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户 ID">
          {{ profile.id }}
        </el-descriptions-item>
        <el-descriptions-item label="日记数量">
          {{ profile.diaryCount }}
        </el-descriptions-item>
        <el-descriptions-item label="OpenID" :span="2">
          {{ profile.openid || "-" }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>编辑资料</span>
      </template>

      <el-form
        :model="form"
        label-width="80px"
        style="max-width: 600px"
      >
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" placeholder="请选择性别">
            <el-option
              v-for="item in genderOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            placeholder="请选择生日"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input
            v-model="form.signature"
            type="textarea"
            :rows="3"
            placeholder="请输入个性签名"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.account-settings {
  padding: 20px;
}
</style>
