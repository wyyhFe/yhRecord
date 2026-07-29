<!-- TODO: 个人主体小程序暂不可用 getPhoneNumber，手机号登录代码保留待主体升级后启用 -->
<script setup lang="ts">
import { ref, reactive } from "vue";
import Motion from "../utils/motion";
import { message } from "@/utils/message";
import { phoneLoginRules } from "../utils/rule";
import type { FormInstance } from "element-plus";
import { $t, transformI18n } from "@/plugins/i18n";
import { useUserStoreHook } from "@/store/modules/user";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { initRouter, getTopMenu } from "@/router/utils";
import { useRouter } from "vue-router";
import Iphone from "~icons/ep/iphone";
import Lock from "~icons/ri/lock-fill";
import ReChangePassword from "@/components/ReChangePassword/index.vue";

const router = useRouter();
const loading = ref(false);
const showChangePassword = ref(false);
const phoneValue = ref("");

const ruleForm = reactive({
  phone: "",
  password: ""
});
const ruleFormRef = ref<FormInstance>();

const onLogin = async (formEl: FormInstance | undefined) => {
  loading.value = true;
  if (!formEl) return;
  await formEl.validate(async valid => {
    if (valid) {
      try {
        const result = await useUserStoreHook().loginByPhone({
          phone: ruleForm.phone,
          password: ruleForm.password
        });
        if (result.data?.requirePasswordChange) {
          // 初始密码，需强制修改
          phoneValue.value = ruleForm.phone;
          showChangePassword.value = true;
        } else {
          await initRouter();
          router.push(getTopMenu(true).path);
          message(transformI18n($t("login.pureLoginSuccess")), {
            type: "success"
          });
        }
      } catch {
        // 错误消息已在 store action 中处理
      }
    }
    loading.value = false;
  });
};

async function onChangePasswordSuccess() {
  showChangePassword.value = false;
  await initRouter();
  router.push(getTopMenu(true).path);
  message(transformI18n($t("login.pureLoginSuccess")), {
    type: "success"
  });
}

function onBack() {
  useUserStoreHook().SET_CURRENTPAGE(0);
}
</script>

<template>
  <el-form ref="ruleFormRef" :model="ruleForm" :rules="phoneLoginRules" size="large">
    <Motion>
      <el-form-item prop="phone">
        <el-input
          v-model="ruleForm.phone"
          clearable
          :placeholder="$t('login.purePhone')"
          :prefix-icon="useRenderIcon(Iphone)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="100">
      <el-form-item prop="password">
        <el-input
          v-model="ruleForm.password"
          type="password"
          show-password
          clearable
          :placeholder="$t('login.purePassWord')"
          :prefix-icon="useRenderIcon(Lock)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="150">
      <el-form-item>
        <el-button
          class="w-full"
          size="default"
          type="primary"
          :loading="loading"
          @click="onLogin(ruleFormRef)"
        >
          {{ $t("login.pureLogin") }}
        </el-button>
      </el-form-item>
    </Motion>

    <Motion :delay="200">
      <el-form-item>
        <el-button class="w-full" size="default" @click="onBack">
          {{ $t("login.pureBack") }}
        </el-button>
      </el-form-item>
    </Motion>
  </el-form>

  <!-- 强制修改初始密码弹窗 -->
  <ReChangePassword
    v-if="showChangePassword"
    :phone="phoneValue"
    @success="onChangePasswordSuccess"
  />
</template>
