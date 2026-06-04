<script setup lang="ts">
import { ref } from "vue";
import { useUser } from "./utils/hook";

defineOptions({ name: "UserManage" });

const tableRef = ref();
const treeRef = ref();

const {
  form,
  loading,
  columns,
  dataList,
  pagination,
  onSearch,
  resetForm,
  handleRole,
  handleToggleStatus,
  handleSizeChange,
  handleCurrentChange
} = useUser(tableRef, treeRef);
</script>

<template>
  <div class="main">
    <el-form
      ref="formRef"
      :inline="true"
      :model="form"
      class="bg-bg_color px-8 pt-4 pb-4"
    >
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="form.nickname"
          placeholder="请输入昵称"
          clearable
          class="!w-[200px]"
        />
      </el-form-item>
      <el-form-item label="登录方式" prop="loginType">
        <el-select
          v-model="form.loginType"
          placeholder="请选择"
          clearable
          class="!w-[120px]"
        >
          <el-option label="微信" value="WECHAT" />
          <el-option label="GitHub" value="GITHUB" />
          <el-option label="Google" value="GOOGLE" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="form.status"
          placeholder="请选择"
          clearable
          class="!w-[120px]"
        >
          <el-option label="启用" value="ENABLED" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="onSearch">
          搜索
        </el-button>
        <el-button :icon="Refresh" @click="resetForm(formRef?.formEl)">
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <pure-table
      ref="tableRef"
      v-loading="loading"
      :data="dataList"
      :columns="columns"
      :pagination="pagination"
      :header-cell-style="{
        background: 'var(--el-fill-color-light)',
        color: 'var(--el-text-color-primary)'
      }"
      @page-size-change="handleSizeChange"
      @page-current-change="handleCurrentChange"
    >
      <template #operation="{ row }">
        <el-button size="small" type="primary" @click="handleRole(row)">
          分配角色
        </el-button>
        <el-button
          size="small"
          :type="row.status === 'ENABLED' ? 'danger' : 'success'"
          @click="handleToggleStatus(row)"
        >
          {{ row.status === "ENABLED" ? "禁用" : "启用" }}
        </el-button>
      </template>
    </pure-table>
  </div>
</template>

<script lang="ts">
import { Search, Refresh } from "@element-plus/icons-vue";
</script>
