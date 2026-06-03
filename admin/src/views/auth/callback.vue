<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { message } from "@/utils/message";
import { setToken } from "@/utils/auth";
import { initRouter, getTopMenu } from "@/router/utils";
import { useUserStoreHook } from "@/store/modules/user";

defineOptions({
  name: "AuthCallback"
});

const router = useRouter();
const route = useRoute();
const loading = ref(true);

onMounted(async () => {
  const { accessToken, refreshToken, userId, provider } = route.query;

  if (!accessToken || !refreshToken) {
    message("授权失败，未获取到登录信息", { type: "error" });
    router.push("/login");
    return;
  }

  try {
    // 计算过期时间（默认 2 小时后）
    const expires = new Date(Date.now() + 2 * 60 * 60 * 1000);

    // 复用现有 token 存储机制
    setToken({
      accessToken: accessToken as string,
      refreshToken: refreshToken as string,
      expires
    } as any);

    // 获取后端路由并跳转首页
    await initRouter();
    message(`通过 ${provider} 登录成功`, { type: "success" });
    router.push(getTopMenu(true).path);
  } catch (err) {
    message("登录处理失败", { type: "error" });
    router.push("/login");
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="w-full h-screen flex flex-col items-center justify-center gap-4">
    <el-icon class="is-loading" :size="32" color="var(--el-color-primary)">
      <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
        <path
          d="M512 64a32 32 0 0 1 32 32v192a32 32 0 0 1-64 0V96a32 32 0 0 1 32-32zm0 640a32 32 0 0 1 32 32v192a32 32 0 0 1-64 0V736a32 32 0 0 1 32-32zm448-192a32 32 0 0 1-32 32H736a32 32 0 0 1 0-64h192a32 32 0 0 1 32 32zM288 512a32 32 0 0 1-32 32H64a32 32 0 0 1 0-64h192a32 32 0 0 1 32 32z"
          fill="currentColor"
        />
      </svg>
    </el-icon>
    <p class="text-gray-500 text-sm">正在处理登录...</p>
  </div>
</template>
