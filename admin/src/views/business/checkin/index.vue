<script setup lang="ts">
import { ref } from "vue";
import { useCheckin } from "./utils/hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";

import Delete from "~icons/ep/delete";
import Refresh from "~icons/ep/refresh";
import Search from "~icons/ep/search";

defineOptions({ name: "CheckinManage" });

const taskFormRef = ref();
const recordFormRef = ref();
const taskTableRef = ref();
const recordTableRef = ref();

const {
  activeTab,
  // 任务
  taskForm,
  taskList,
  taskLoading,
  taskColumns,
  taskPagination,
  selectedTaskIds,
  onSearchTasks,
  resetTaskForm,
  handleDeleteTask,
  handleBatchDeleteTasks,
  handleTaskSizeChange,
  handleTaskCurrentChange,
  handleTaskSelectionChange,
  // 记录
  recordForm,
  recordList,
  recordLoading,
  recordColumns,
  recordPagination,
  selectedRecordIds,
  onSearchRecords,
  resetRecordForm,
  handleDeleteRecord,
  handleBatchDeleteRecords,
  handleRecordSizeChange,
  handleRecordCurrentChange,
  handleRecordSelectionChange
} = useCheckin();

function onTaskFullscreen() {
  taskTableRef.value.setAdaptive();
}

function onRecordFullscreen() {
  recordTableRef.value.setAdaptive();
}
</script>

<template>
  <div class="main">
    <el-tabs v-model="activeTab" type="border-card" class="demo-tabs">
      <!-- 打卡任务管理 -->
      <el-tab-pane label="打卡任务" name="task">
        <el-form
          ref="taskFormRef"
          :inline="true"
          :model="taskForm"
          class="search-form bg-bg_color w-full pl-8 pt-3 overflow-auto"
        >
          <el-form-item label="任务名称：" prop="name">
            <el-input
              v-model="taskForm.name"
              placeholder="搜索任务名称"
              clearable
              class="w-45!"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :icon="useRenderIcon(Search)"
              :loading="taskLoading"
              @click="onSearchTasks"
            >
              搜索
            </el-button>
            <el-button
              :icon="useRenderIcon(Refresh)"
              @click="resetTaskForm(taskFormRef)"
            >
              重置
            </el-button>
          </el-form-item>
        </el-form>

        <PureTableBar
          title="打卡任务列表"
          :columns="taskColumns"
          :tableRef="taskTableRef?.getTableRef()"
          @refresh="onSearchTasks"
          @fullscreen="onTaskFullscreen"
        >
          <template #buttons>
            <el-button
              type="danger"
              :icon="useRenderIcon(Delete)"
              :disabled="selectedTaskIds.length === 0"
              @click="handleBatchDeleteTasks"
            >
              批量删除
            </el-button>
          </template>
          <template v-slot="{ size, dynamicColumns }">
            <pure-table
              ref="taskTableRef"
              adaptive
              :adaptiveConfig="{ offsetBottom: 45 }"
              align-whole="center"
              row-key="id"
              showOverflowTooltip
              table-layout="auto"
              :loading="taskLoading"
              :size="size"
              :data="taskList"
              :columns="dynamicColumns"
              :pagination="taskPagination"
              :header-cell-style="{
                background: 'var(--el-fill-color-light)',
                color: 'var(--el-text-color-primary)'
              }"
              @selection-change="handleTaskSelectionChange"
              @page-size-change="handleTaskSizeChange"
              @page-current-change="handleTaskCurrentChange"
            >
              <template #operation="{ row }">
                <el-popconfirm
                  :title="`是否确认删除任务「${row.name}」？`"
                  @confirm="handleDeleteTask(row)"
                >
                  <template #reference>
                    <el-button
                      class="reset-margin"
                      link
                      type="primary"
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
      </el-tab-pane>

      <!-- 打卡记录管理 -->
      <el-tab-pane label="打卡记录" name="record">
        <el-form
          ref="recordFormRef"
          :inline="true"
          :model="recordForm"
          class="search-form bg-bg_color w-full pl-8 pt-3 overflow-auto"
        >
          <el-form-item label="任务名称：" prop="taskName">
            <el-input
              v-model="recordForm.taskName"
              placeholder="搜索任务名称"
              clearable
              class="w-45!"
            />
          </el-form-item>
          <el-form-item label="打卡日期：" prop="checkinDate">
            <el-date-picker
              v-model="recordForm.checkinDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="w-45!"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :icon="useRenderIcon(Search)"
              :loading="recordLoading"
              @click="onSearchRecords"
            >
              搜索
            </el-button>
            <el-button
              :icon="useRenderIcon(Refresh)"
              @click="resetRecordForm(recordFormRef)"
            >
              重置
            </el-button>
          </el-form-item>
        </el-form>

        <PureTableBar
          title="打卡记录列表"
          :columns="recordColumns"
          :tableRef="recordTableRef?.getTableRef()"
          @refresh="onSearchRecords"
          @fullscreen="onRecordFullscreen"
        >
          <template #buttons>
            <el-button
              type="danger"
              :icon="useRenderIcon(Delete)"
              :disabled="selectedRecordIds.length === 0"
              @click="handleBatchDeleteRecords"
            >
              批量删除
            </el-button>
          </template>
          <template v-slot="{ size, dynamicColumns }">
            <pure-table
              ref="recordTableRef"
              adaptive
              :adaptiveConfig="{ offsetBottom: 45 }"
              align-whole="center"
              row-key="id"
              showOverflowTooltip
              table-layout="auto"
              :loading="recordLoading"
              :size="size"
              :data="recordList"
              :columns="dynamicColumns"
              :pagination="recordPagination"
              :header-cell-style="{
                background: 'var(--el-fill-color-light)',
                color: 'var(--el-text-color-primary)'
              }"
              @selection-change="handleRecordSelectionChange"
              @page-size-change="handleRecordSizeChange"
              @page-current-change="handleRecordCurrentChange"
            >
              <template #operation="{ row }">
                <el-popconfirm
                  :title="`是否确认删除这条打卡记录？`"
                  @confirm="handleDeleteRecord(row)"
                >
                  <template #reference>
                    <el-button
                      class="reset-margin"
                      link
                      type="primary"
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
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style lang="scss" scoped>
:deep(.el-table__inner-wrapper::before) {
  height: 0;
}

.main-content {
  margin: 24px 24px 0 !important;
}

.search-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}

.demo-tabs {
  :deep(.el-tabs__content) {
    padding: 0;
  }
}
</style>
