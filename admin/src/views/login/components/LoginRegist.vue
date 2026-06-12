<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { ref, reactive } from "vue";
import Motion from "../utils/motion";
import { message } from "@/utils/message";
import { registerRules } from "../utils/rule";
import type { FormInstance } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import { $t, transformI18n } from "@/plugins/i18n";
import { registerAccount } from "@/api/user";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import Lock from "~icons/ri/lock-fill";
import User from "~icons/ri/user-3-fill";
import Keyhole from "~icons/ri/shield-keyhole-line";

const { t } = useI18n();
const checked = ref(false);
const loading = ref(false);
const ruleForm = reactive({
  username: "",
  password: "",
  repeatPassword: ""
});
const ruleFormRef = ref<FormInstance>();
const repeatPasswordRule = [
  {
    validator: (rule, value, callback) => {
      if (value === "") {
        callback(new Error(transformI18n($t("login.purePassWordSureReg"))));
      } else if (ruleForm.password !== value) {
        callback(
          new Error(transformI18n($t("login.purePassWordDifferentReg")))
        );
      } else {
        callback();
      }
    },
    trigger: "blur"
  }
];

const onRegister = async (formEl: FormInstance | undefined) => {
  loading.value = true;
  if (!formEl) return;
  await formEl.validate(async valid => {
    if (valid) {
      if (checked.value) {
        try {
          const res = await registerAccount({
            username: ruleForm.username,
            password: ruleForm.password
          });
          if (res.code === 0) {
            message(transformI18n($t("login.pureRegisterSuccess")), {
              type: "success"
            });
            // 注册成功，切回登录页
            useUserStoreHook().SET_CURRENTPAGE(0);
          } else {
            message(res.message, { type: "error" });
          }
        } catch (err: any) {
          message(err?.message || "注册失败", { type: "error" });
        }
      } else {
        message(transformI18n($t("login.pureTickPrivacy")), {
          type: "warning"
        });
      }
    }
    loading.value = false;
  });
};

function onBack() {
  useUserStoreHook().SET_CURRENTPAGE(0);
}
</script>

<template>
  <el-form
    ref="ruleFormRef"
    :model="ruleForm"
    :rules="registerRules"
    size="large"
  >
    <Motion>
      <el-form-item
        :rules="[
          {
            required: true,
            message: transformI18n($t('login.pureUsernameReg')),
            trigger: 'blur'
          }
        ]"
        prop="username"
      >
        <el-input
          v-model="ruleForm.username"
          clearable
          :placeholder="t('login.pureUsername')"
          :prefix-icon="useRenderIcon(User)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="100">
      <el-form-item prop="password">
        <el-input
          v-model="ruleForm.password"
          clearable
          type="password"
          show-password
          :placeholder="t('login.purePassword')"
          :prefix-icon="useRenderIcon(Lock)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="150">
      <el-form-item prop="repeatPassword">
        <el-input
          v-model="ruleForm.repeatPassword"
          clearable
          type="password"
          show-password
          :placeholder="t('login.pureSure')"
          :prefix-icon="useRenderIcon(Keyhole)"
          :rules="repeatPasswordRule"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="200">
      <el-form-item>
        <div class="w-full flex justify-between">
          <div class="flex">
            <el-checkbox v-model="checked" />
            <span
              class="cursor-pointer select-none"
              style="
                color: var(--el-color-primary);
                font-size: var(--el-font-size-base);
                margin-left: 5px;
              "
            >
              {{ t("login.pureReadAccept") }}
            </span>
            <span
              class="cursor-pointer select-none"
              style="
                color: var(--el-color-primary);
                font-size: var(--el-font-size-base);
                margin-left: 5px;
              "
            >
              {{ t("login.purePrivacyPolicy") }}
            </span>
          </div>
        </div>
      </el-form-item>
    </Motion>

    <Motion :delay="250">
      <el-form-item>
        <el-button
          class="w-full"
          size="large"
          color="var(--el-color-primary)"
          :loading="loading"
          @click="onRegister(ruleFormRef)"
        >
          {{ t("login.pureRegister") }}
        </el-button>
      </el-form-item>
    </Motion>

    <Motion :delay="300">
      <el-form-item>
        <el-button class="w-full" size="large" @click="onBack">
          {{ t("login.pureBack") }}
        </el-button>
      </el-form-item>
    </Motion>
  </el-form>
</template>
