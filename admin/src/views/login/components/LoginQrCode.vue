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

const QR_LOGIN_PREFIX = "LR_LOGIN:";

async function generateAndPoll() {
  expired.value = false;
  errorMsg.value = "";

  try {
    const { data } = await getQrToken();
    if (!data?.qrToken) {
      errorMsg.value = "获取二维码失败";
      return;
    }
    qrText.value = QR_LOGIN_PREFIX + data.qrToken;
    startPolling(data.qrToken);
  } catch {
    errorMsg.value = "获取二维码失败";
  }
}

function startPolling(qrToken: string) {
  stopPolling();
  pollTimer = setInterval(async () => {
    try {
      const result = await getQrStatus(qrToken);
      // 成功拿到 token（HTTP 200 + data 不为空）
      if (result) {
        stopPolling();
        setToken({
          ...result,
          expires: new Date(Date.now() + ((result as any).expiresIn || 0) * 1000)
        });
        await initRouter();
        router.push(getTopMenu(true).path);
        message(transformI18n($t("login.pureLoginSuccess")), { type: "success" });
      }
      // code 202 = 等待中，继续轮询
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
