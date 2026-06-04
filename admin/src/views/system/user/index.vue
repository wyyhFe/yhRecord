<script setup lang="ts">
import { ref, onMounted } from "vue";
import { message } from "@/utils/message";
import { http } from "@/utils/http";

defineOptions({ name: "UserManage" });

const loading = ref(false);
const userList = ref([]);

async function loadUsers() {
  loading.value = true;
  try {
    // TODO: 后续添加用户列表接口
    userList.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(loadUsers);
</script>

<template>
  <div class="p-4">
    <div class="flex justify-between mb-4">
      <h2 class="text-lg font-semibold">用户管理</h2>
    </div>

    <el-table :data="userList" v-loading="loading" border>
      <el-table-column prop="id" label="用户 ID" width="180" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="loginType" label="登录方式" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createdAt" label="注册时间" min-width="180" />
    </el-table>

    <el-empty v-if="!loading && userList.length === 0" description="暂无用户数据" />
  </div>
</template>
