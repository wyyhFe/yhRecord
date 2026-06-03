<script setup lang="ts">
import { ref } from "vue";

defineOptions({
  name: "Dashboard"
});

const greeting = ref("欢迎使用 Life Record 后台管理系统");
</script>

<template>
  <div class="dashboard">
    <el-card shadow="hover">
      <div class="dashboard-header">
        <h2 style="margin: 0; font-size: 22px; color: #303133">{{ greeting }}</h2>
      </div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6" v-for="item in 4" :key="item">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-value">{{ item * 128 }}</div>
          <div class="metric-label">数据指标 {{ item }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard-header {
  padding: 8px 0;
}
.metric-card {
  text-align: center;
}
.metric-value {
  font-size: 32px;
  font-weight: 700;
  color: #409eff;
}
.metric-label {
  margin-top: 8px;
  font-size: 14px;
  color: #909399;
}
</style>
