<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";
import { useI18n } from "vue-i18n";
import Motion from "../utils/motion";
import ReQrcode from "@/components/ReQrcode";
import { message } from "@/utils/message";
import { useUserStoreHook } from "@/store/modules/user";
import { getQrToken, getQrStatus } from "@/api/user";
import { setToken } from "@/utils/auth";
import { initRouter, getTopMenu } from "@/router/utils";
import { useRouter } from "vue-router";
import { $t, transformI18n } from "@/plugins/i18n";

const { t } = useI18n();
const router = useRouter();

const qrText = ref("");
const expired = ref(false);
const errorMsg = ref("");
let pollTimer: ReturnType<typeof setInterval> | null = null;

// 扫码登录 URL 前缀，可通过 VITE_QR_LOGIN_BASE_URL 环境变量配置
const QR_LOGIN_URL = (import.meta as any).env?.VITE_QR_LOGIN_BASE_URL
  ? ((import.meta as any).env.VITE_QR_LOGIN_BASE_URL as string).replace(/\/+$/, "") + "/qr-login"
  : "https://recordlife.top/api/qr-login";

async function generateAndPoll() {
  expired.value = false;
  errorMsg.value = "";

  try {
    const res = await getQrToken() as any;
    const qrToken = res?.data?.qrToken;
    if (!qrToken) {
      errorMsg.value = "获取二维码失败";
      return;
    }
    qrText.value = QR_LOGIN_URL + "?token=" + qrToken;
    startPolling(qrToken);
  } catch {
    errorMsg.value = "获取二维码失败";
  }
}

function startPolling(qrToken: string) {
  stopPolling();
  pollTimer = setInterval(async () => {
    try {
      const res = await getQrStatus(qrToken) as any;
      // code === 0 表示已确认，data 里有 token 信息
      if (res?.code === 0 && res?.data) {
        stopPolling();
        setToken({
          ...res.data,
          expires: new Date(Date.now() + ((res.data.expiresIn || 0) * 1000))
        });
        await initRouter();
        router.push(getTopMenu(true).path);
        message(transformI18n($t("login.pureLoginSuccess")), { type: "success" });
      }
      // code 202 = 等待中，继续轮询；其他 code 抛出异常走 catch
    } catch (err: any) {
      const msg = err?.message || "";
      if (msg.includes("过期")) {
        expired.value = true;
        stopPolling();
      }
    }
  }, 2000);
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}

function onRefresh() {
  generateAndPoll();
}

onMounted(() => {
  generateAndPoll();
});

onUnmounted(() => {
  stopPolling();
});
</script>

<template>
  <Motion class="-my-2">
    <ReQrcode
      v-if="qrText"
      :text="qrText"
      :width="200"
      :disabled="expired"
      :disabled-text="t('login.pureTest')"
      @disabled-click="onRefresh"
    />
    <div v-else-if="errorMsg" class="text-center text-gray-400 py-8">
      {{ errorMsg }}
    </div>
  </Motion>
  <Motion :delay="100">
    <div class="text-center text-gray-400 text-xs mt-2">
      {{ expired ? '二维码已过期，点击刷新' : '打开小程序 → 个人中心 → 扫码登录' }}
    </div>
    <el-divider>
      <p class="text-gray-500 text-xs">{{ t("login.pureTip") }}</p>
    </el-divider>
  </Motion>
  <Motion :delay="150">
    <el-button
      class="w-full mt-4!"
      @click="useUserStoreHook().SET_CURRENTPAGE(0)"
    >
      {{ t("login.pureBack") }}
    </el-button>
  </Motion>
</template>
