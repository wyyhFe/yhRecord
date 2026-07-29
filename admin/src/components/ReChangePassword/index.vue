<script setup lang="ts">
import { ref, reactive } from "vue";
import { type FormInstance, type FormRules } from "element-plus";
import { message } from "@/utils/message";
import { changePassword } from "@/api/user";

const props = defineProps<{
  phone: string;
}>();

const emit = defineEmits<{
  success: [];
}>();

const loading = ref(false);
const ruleFormRef = ref<FormInstance>();

const form = reactive({
  oldPassword: props.phone.slice(-6),
  newPassword: "",
  confirmPassword: ""
});

const validateConfirmPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value === "") {
    callback(new Error("请再次输入新密码"));
  } else if (value !== form.newPassword) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const validateNewPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value === "") {
    callback(new Error("请输入新密码"));
  } else if (value.length < 6) {
    callback(new Error("密码长度至少 6 位"));
  } else if (value === form.oldPassword) {
    callback(new Error("新密码不能与初始密码相同"));
  } else {
    callback();
  }
};

const rules = reactive<FormRules>({
  newPassword: [
    { validator: validateNewPassword, trigger: "blur" }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: "blur" }
  ]
});

async function submit() {
  if (!ruleFormRef.value) return;
  await ruleFormRef.value.validate(async valid => {
    if (!valid) return;
    loading.value = true;
    try {
      await changePassword({
        oldPassword: form.oldPassword,
        newPassword: form.newPassword
      });
      message("密码修改成功", { type: "success" });
      emit("success");
    } catch {
      // 错误已在 request 层处理
    } finally {
      loading.value = false;
    }
  });
}
</script>

<template>
  <el-dialog
    :model-value="true"
    title="修改初始密码"
    width="440px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    :destroy-on-close="true"
    center
  >
    <el-alert
      type="warning"
      :closable="false"
      show-icon
      class="mb-4"
    >
      您的账号使用了初始密码，为了账号安全，请修改密码后再使用系统。
    </el-alert>

    <el-form
      ref="ruleFormRef"
      :model="form"
      :rules="rules"
      size="large"
      label-width="100px"
    >
      <el-form-item label="初始密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          show-password
          disabled
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          show-password
          placeholder="请输入新密码"
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          show-password
          placeholder="请再次输入新密码"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button
        type="primary"
        :loading="loading"
        class="w-full"
        size="large"
        @click="submit"
      >
        确认修改
      </el-button>
    </template>
  </el-dialog>
</template>
