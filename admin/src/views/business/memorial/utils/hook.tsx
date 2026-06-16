import { message } from "@/utils/message";
import { getMemorialList, deleteMemorial } from "@/api/memorial";
import { reactive, ref, onMounted } from "vue";
import type { PaginationProps } from "@pureadmin/table";
import {
  createColumns,
  formatDateTime,
  formatDate
} from "@/utils/table";
import { ElTag } from "element-plus";

export function useMemorial() {
  const form = reactive({
    title: "",
    type: ""
  });

  const dataList = ref([]);
  const loading = ref(true);
  const selectedIds = ref<number[]>([]);

  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });

  const typeMap = {
    BIRTHDAY: { text: "生日", type: "primary" as const },
    ANNIVERSARY: { text: "纪念日", type: "success" as const },
    HOLIDAY: { text: "节日", type: "warning" as const },
    OTHER: { text: "其他", type: "info" as const }
  };

  const columns: TableColumnList = createColumns({
    selectable: true,
    showId: true,
    extra: [
      { label: "标题", prop: "title", minWidth: 150 },
      {
        label: "类型",
        prop: "type",
        width: 100,
        cellRenderer: ({ row }) => {
          const t = typeMap[row.type] || { text: row.type || "-", type: "info" as const };
          return <el-tag type={t.type}>{t.text}</el-tag>;
        }
      },
      {
        label: "纪念日期",
        prop: "memorialDate",
        width: 120,
        formatter: ({ memorialDate }) => formatDate(memorialDate)
      },
      {
        label: "每年重复",
        prop: "annualRepeat",
        width: 100,
        cellRenderer: ({ row }) => (
          <el-tag type={row.annualRepeat ? "success" : "info"}>
            {row.annualRepeat ? "是" : "否"}
          </el-tag>
        )
      },
      { label: "备注", prop: "remark", minWidth: 150, showOverflowTooltip: true },
      {
        label: "创建时间",
        prop: "createdAt",
        width: 160,
        formatter: ({ createdAt }) => formatDateTime(createdAt)
      }
    ],
    operation: true
  });

  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    onSearch();
  }

  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    onSearch();
  }

  function handleSelectionChange(val) {
    selectedIds.value = val.map(item => item.id);
  }

  async function onSearch() {
    loading.value = true;
    const { code, data } = await getMemorialList({
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...form
    });
    if (code === 0) {
      dataList.value = data.list || [];
      pagination.total = data.total;
    }
    setTimeout(() => {
      loading.value = false;
    }, 300);
  }

  const resetForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  };

  async function handleDelete(row) {
    const { code } = await deleteMemorial(row.id);
    if (code === 0) {
      message("删除纪念日成功", { type: "success" });
      onSearch();
    }
  }

  async function handleBatchDelete() {
    if (!selectedIds.value.length) {
      message("请选择要删除的纪念日", { type: "warning" });
      return;
    }
    for (const id of selectedIds.value) {
      await deleteMemorial(id);
    }
    message(`批量删除${selectedIds.value.length}条纪念日成功`, { type: "success" });
    selectedIds.value = [];
    onSearch();
  }

  onMounted(() => {
    onSearch();
  });

  return {
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
  };
}
