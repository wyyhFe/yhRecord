import { message } from "@/utils/message";
import {
  getLedgerEntryList,
  deleteLedgerEntry
} from "@/api/ledger";
import { reactive, ref, onMounted } from "vue";
import type { PaginationProps } from "@pureadmin/table";
import {
  createColumns,
  formatDateTime,
  formatDate
} from "@/utils/table";
import { ElTag } from "element-plus";

export function useLedger() {
  const form = reactive({
    type: "",
    entryDate: "",
    remark: ""
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

  const columns: TableColumnList = createColumns({
    selectable: true,
    showId: true,
    extra: [
      {
        label: "类型",
        prop: "type",
        width: 100,
        cellRenderer: ({ row }) => (
          <el-tag type={row.type === "INCOME" ? "success" : "danger"}>
            {row.type === "INCOME" ? "收入" : "支出"}
          </el-tag>
        )
      },
      {
        label: "金额",
        prop: "amount",
        width: 120,
        cellRenderer: ({ row }) => (
          <span style={{ color: row.type === "INCOME" ? "#67c23a" : "#f56c6c", fontWeight: "bold" }}>
            {row.type === "INCOME" ? "+" : "-"}{row.amount.toFixed(2)}
          </span>
        )
      },
      {
        label: "记账日期",
        prop: "entryDate",
        width: 120,
        formatter: ({ entryDate }) => formatDate(entryDate)
      },
      { label: "备注", prop: "remark", minWidth: 150, showOverflowTooltip: true },
      {
        label: "标签",
        prop: "tags",
        minWidth: 150,
        cellRenderer: ({ row }) => {
          if (!row.tags?.length) return "-";
          return (
            <div class="flex flex-wrap gap-1">
              {row.tags.map(tag => (
                <el-tag size="small" key={tag.id} style={{ borderColor: tag.color, color: tag.color }}>
                  {tag.name}
                </el-tag>
              ))}
            </div>
          );
        }
      },
      {
        label: "图片",
        prop: "imagePath",
        width: 80,
        cellRenderer: ({ row }) => {
          if (!row.imagePath) return "-";
          return (
            <el-image
              src={row.imagePath}
              preview-teleported
              preview-src-list={[row.imagePath]}
              fit="cover"
              style={{ width: "40px", height: "40px", borderRadius: "4px" }}
            />
          );
        }
      },
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
    const { code, data } = await getLedgerEntryList({
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
    const { code } = await deleteLedgerEntry(row.id);
    if (code === 0) {
      message("删除记账流水成功", { type: "success" });
      onSearch();
    }
  }

  async function handleBatchDelete() {
    if (!selectedIds.value.length) {
      message("请选择要删除的记录", { type: "warning" });
      return;
    }
    for (const id of selectedIds.value) {
      await deleteLedgerEntry(id);
    }
    message(`批量删除${selectedIds.value.length}条记录成功`, { type: "success" });
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
