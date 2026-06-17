<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getDashboardStats, type DashboardStatsVO } from "@/api/dashboard";

defineOptions({
  name: "Dashboard"
});

const greeting = ref("欢迎使用 Life Record 后台管理系统");
const loading = ref(true);
const stats = ref<DashboardStatsVO>({
  userCount: 0,
  diaryCount: 0,
  checkinCount: 0,
  ledgerCount: 0,
  memorialCount: 0,
  knowledgeCount: 0
});

const metricCards = [
  { key: "userCount", label: "用户总数", icon: "ep:user", color: "#409eff" },
  { key: "diaryCount", label: "日记总数", icon: "ep:notebook", color: "#67c23a" },
  { key: "checkinCount", label: "打卡记录", icon: "ep:checked", color: "#e6a23c" },
  { key: "ledgerCount", label: "记账条目", icon: "ep:wallet", color: "#f56c6c" },
  { key: "memorialCount", label: "纪念日", icon: "ep:calendar", color: "#909399" },
  { key: "knowledgeCount", label: "知识库", icon: "ep:collection", color: "#b37feb" }
];

onMounted(async () => {
  try {
    const { data } = await getDashboardStats();
    if (data.code === 0) {
      stats.value = data.data;
    }
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="dashboard">
    <el-card shadow="hover">
      <div class="dashboard-header">
        <h2 style="margin: 0; font-size: 22px; color: #303133">{{ greeting }}</h2>
      </div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col
        :xs="12"
        :sm="8"
        :md="6"
        :lg="4"
        v-for="item in metricCards"
        :key="item.key"
      >
        <el-card shadow="hover" class="metric-card" v-loading="loading">
          <div class="metric-value" :style="{ color: item.color }">
            {{ stats[item.key] }}
          </div>
          <div class="metric-label">{{ item.label }}</div>
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
  margin-bottom: 16px;
}
.metric-value {
  font-size: 32px;
  font-weight: 700;
}
.metric-label {
  margin-top: 8px;
  font-size: 14px;
  color: #909399;
}
</style>
