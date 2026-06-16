import dayjs from "dayjs";
import { message } from "@/utils/message";
import {
  getCheckinTaskList,
  getCheckinRecordList,
  deleteCheckinTask,
  deleteCheckinRecord
} from "@/api/checkin";
import { reactive, ref, onMounted } from "vue";
import type { PaginationProps } from "@pureadmin/table";

export function useCheckin() {
  // 任务表单
  const taskForm = reactive({
    name: ""
  });

  // 记录表单
  const recordForm = reactive({
    taskName: "",
    checkinDate: ""
  });

  const activeTab = ref("task");
  const taskList = ref([]);
  const recordList = ref([]);
  const taskLoading = ref(true);
  const recordLoading = ref(true);
  const selectedTaskIds = ref<number[]>([]);
  const selectedRecordIds = ref<number[]>([]);

  const taskPagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });

  const recordPagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });

  const taskColumns: TableColumnList = [
    {
      type: "selection",
      width: 55,
      align: "center"
    },
    {
      label: "ID",
      prop: "id",
      width: 80
    },
    {
      label: "任务名称",
      prop: "name",
      minWidth: 150
    },
    {
      label: "描述",
      prop: "description",
      minWidth: 200,
      showOverflowTooltip: true
    },
    {
      label: "开始日期",
      prop: "startDate",
      width: 120,
      formatter: ({ startDate }) =>
        startDate ? dayjs(startDate).format("YYYY-MM-DD") : "-"
    },
    {
      label: "打卡次数",
      prop: "totalCount",
      width: 100,
      cellRenderer: ({ row }) => (
        <el-tag type="success">{row.totalCount}次</el-tag>
      )
    },
    {
      label: "最近打卡",
      prop: "latestCheckedAt",
      width: 160,
      formatter: ({ latestCheckedAt }) =>
        latestCheckedAt
          ? dayjs(latestCheckedAt).format("YYYY-MM-DD HH:mm:ss")
          : "-"
    },
    {
      label: "创建时间",
      prop: "createdAt",
      width: 160,
      formatter: ({ createdAt }) =>
        createdAt ? dayjs(createdAt).format("YYYY-MM-DD HH:mm:ss") : ""
    },
    {
      label: "操作",
      fixed: "right",
      width: 120,
      slot: "operation"
    }
  ];

  const recordColumns: TableColumnList = [
    {
      type: "selection",
      width: 55,
      align: "center"
    },
    {
      label: "ID",
      prop: "id",
      width: 80
    },
    {
      label: "任务名称",
      prop: "taskName",
      minWidth: 150
    },
    {
      label: "打卡日期",
      prop: "checkinDate",
      width: 120,
      formatter: ({ checkinDate }) =>
        checkinDate ? dayjs(checkinDate).format("YYYY-MM-DD") : ""
    },
    {
      label: "备注",
      prop: "remark",
      minWidth: 150,
      showOverflowTooltip: true
    },
    {
      label: "心情",
      prop: "mood",
      width: 100,
      cellRenderer: ({ row }) => {
        const moodMap = {
          HAPPY: { text: "开心", type: "success" },
          SAD: { text: "难过", type: "info" },
          ANGRY: { text: "生气", type: "danger" },
          CALM: { text: "平静", type: "" },
          EXCITED: { text: "兴奋", type: "warning" }
        };
        const mood = moodMap[row.mood] || { text: row.mood || "-", type: "" };
        return <el-tag type={mood.type as any}>{mood.text}</el-tag>;
      }
    },
    {
      label: "标签",
      prop: "tagNames",
      minWidth: 150,
      cellRenderer: ({ row }) => {
        if (!row.tagNames || row.tagNames.length === 0) return "-";
        return (
          <div class="flex flex-wrap gap-1">
            {row.tagNames.map(tag => (
              <el-tag size="small" key={tag}>
                {tag}
              </el-tag>
            ))}
          </div>
        );
      }
    },
    {
      label: "图片",
      prop: "mediaPaths",
      width: 100,
      cellRenderer: ({ row }) => {
        if (!row.mediaPaths || row.mediaPaths.length === 0) return "-";
        return (
          <el-image
            src={row.mediaPaths[0]}
            preview-teleported={true}
            preview-src-list={row.mediaPaths}
            fit="cover"
            class="w-10 h-10 rounded"
          />
        );
      }
    },
    {
      label: "创建时间",
      prop: "createdAt",
      width: 160,
      formatter: ({ createdAt }) =>
        createdAt ? dayjs(createdAt).format("YYYY-MM-DD HH:mm:ss") : ""
    },
    {
      label: "操作",
      fixed: "right",
      width: 120,
      slot: "operation"
    }
  ];

  // 任务相关方法
  function handleTaskSizeChange(val: number) {
    taskPagination.pageSize = val;
    onSearchTasks();
  }

  function handleTaskCurrentChange(val: number) {
    taskPagination.currentPage = val;
    onSearchTasks();
  }

  function handleTaskSelectionChange(val) {
    selectedTaskIds.value = val.map(item => item.id);
  }

  async function onSearchTasks() {
    taskLoading.value = true;
    const { code, data } = await getCheckinTaskList({
      pageNum: taskPagination.currentPage,
      pageSize: taskPagination.pageSize,
      ...taskForm
    });
    if (code === 0) {
      taskList.value = data.list || [];
      taskPagination.total = data.total;
    }
    setTimeout(() => {
      taskLoading.value = false;
    }, 300);
  }

  const resetTaskForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    onSearchTasks();
  };

  async function handleDeleteTask(row) {
    const { code } = await deleteCheckinTask(row.id);
    if (code === 0) {
      message(`删除打卡任务成功`, { type: "success" });
      onSearchTasks();
    }
  }

  async function handleBatchDeleteTasks() {
    if (selectedTaskIds.value.length === 0) {
      message("请选择要删除的任务", { type: "warning" });
      return;
    }
    for (const id of selectedTaskIds.value) {
      await deleteCheckinTask(id);
    }
    message(`批量删除${selectedTaskIds.value.length}个任务成功`, {
      type: "success"
    });
    selectedTaskIds.value = [];
    onSearchTasks();
  }

  // 记录相关方法
  function handleRecordSizeChange(val: number) {
    recordPagination.pageSize = val;
    onSearchRecords();
  }

  function handleRecordCurrentChange(val: number) {
    recordPagination.currentPage = val;
    onSearchRecords();
  }

  function handleRecordSelectionChange(val) {
    selectedRecordIds.value = val.map(item => item.id);
  }

  async function onSearchRecords() {
    recordLoading.value = true;
    const { code, data } = await getCheckinRecordList({
      pageNum: recordPagination.currentPage,
      pageSize: recordPagination.pageSize,
      ...recordForm
    });
    if (code === 0) {
      recordList.value = data.list || [];
      recordPagination.total = data.total;
    }
    setTimeout(() => {
      recordLoading.value = false;
    }, 300);
  }

  const resetRecordForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    onSearchRecords();
  };

  async function handleDeleteRecord(row) {
    const { code } = await deleteCheckinRecord(row.id);
    if (code === 0) {
      message(`删除打卡记录成功`, { type: "success" });
      onSearchRecords();
    }
  }

  async function handleBatchDeleteRecords() {
    if (selectedRecordIds.value.length === 0) {
      message("请选择要删除的记录", { type: "warning" });
      return;
    }
    for (const id of selectedRecordIds.value) {
      await deleteCheckinRecord(id);
    }
    message(`批量删除${selectedRecordIds.value.length}条记录成功`, {
      type: "success"
    });
    selectedRecordIds.value = [];
    onSearchRecords();
  }

  onMounted(() => {
    onSearchTasks();
    onSearchRecords();
  });

  return {
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
  };
}
