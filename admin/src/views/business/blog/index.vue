<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useBlog } from "./utils/hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";

import Delete from "~icons/ep/delete";
import EditPen from "~icons/ep/edit-pen";
import Search from "~icons/ep/search";
import Refresh from "~icons/ep/refresh";
import Plus from "~icons/ep/plus";

defineOptions({ name: "BlogManage" });

const router = useRouter();
const tableRef = ref();

const {
  form,
  loading,
  columns,
  dataList,
  pagination,
  selectedIds,
  onSearch,
  resetForm,
  handleDelete,
  handleBatchDelete,
  handleSizeChange,
  handleCurrentChange,
  handleSelectionChange
} = useBlog();

function goEditor(id?: number) {
  if (id) {
    router.push({ name: "BlogEditor", query: { id } });
  } else {
    router.push({ name: "BlogEditor" });
  }
}
</script>

<template>
  <div class="main">
    <!-- 搜索栏 -->
    <el-form
      :inline="true"
      :model="form"
      class="search-form bg-bg_color w-full pl-8 pt-3"
    >
      <el-form-item label="关键词：" prop="keyword">
        <el-input
          v-model="form.keyword"
          placeholder="搜索标题/摘要"
          clearable
          class="!w-[200px]"
          @keyup.enter="onSearch"
        />
      </el-form-item>
      <el-form-item label="分类：" prop="category">
        <el-input
          v-model="form.category"
          placeholder="分类名称"
          clearable
          class="!w-[150px]"
        />
      </el-form-item>
      <el-form-item label="状态：" prop="status">
        <el-select
          v-model="form.status"
          placeholder="全部"
          clearable
          class="!w-[120px]"
        >
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="useRenderIcon(Search)" @click="onSearch">
          搜索
        </el-button>
        <el-button :icon="useRenderIcon(Refresh)" @click="resetForm">
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <PureTableBar
      title="博客管理"
      :columns="columns"
      :tableRef="tableRef?.getTableRef()"
      @refresh="onSearch"
    >
      <template #buttons>
        <el-button
          type="primary"
          :icon="useRenderIcon(Plus)"
          @click="goEditor()"
        >
          写文章
        </el-button>
        <el-button
          type="danger"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
      </template>

      <template v-slot="{ size, dynamicColumns }">
        <pure-table
          ref="tableRef"
          adaptive
          :adaptiveConfig="{ offsetBottom: 45 }"
          align-whole="center"
          row-key="id"
          :loading="loading"
          :size="size"
          :data="dataList"
          :columns="dynamicColumns"
          :pagination="pagination"
          @selection-change="handleSelectionChange"
          @page-size-change="handleSizeChange"
          @page-current-change="handleCurrentChange"
        >
          <template #operation="{ row }">
            <el-button
              link
              type="primary"
              :size="size"
              :icon="useRenderIcon(EditPen)"
              @click="goEditor(row.id)"
            >
              编辑
            </el-button>
            <el-popconfirm
              :title="`确定删除「${row.title}」？`"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button
                  link
                  type="danger"
                  :size="size"
                  :icon="useRenderIcon(Delete)"
                >
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </pure-table>
      </template>
    </PureTableBar>
  </div>
</template>

<style scoped>
.main {
  margin: 0;
}
</style>
