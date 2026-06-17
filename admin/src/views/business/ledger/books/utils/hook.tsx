import { message } from "@/utils/message";
import { getLedgerBookList, deleteLedgerBook } from "@/api/ledger";
import { reactive, ref, onMounted } from "vue";
import type { PaginationProps } from "@pureadmin/table";
import { createColumns, formatDateTime } from "@/utils/table";

export function useLedgerBook() {
  const form = reactive({
    name: ""
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
        label: "账本名称",
        prop: "name",
        minWidth: 200,
        showOverflowTooltip: true
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
    const { code, data } = await getLedgerBookList({
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
    const { code } = await deleteLedgerBook(row.id);
    if (code === 0) {
      message("删除账本成功", { type: "success" });
      onSearch();
    }
  }

  async function handleBatchDelete() {
    if (!selectedIds.value.length) {
      message("请选择要删除的账本", { type: "warning" });
      return;
    }
    for (const id of selectedIds.value) {
      await deleteLedgerBook(id);
    }
    message(`批量删除${selectedIds.value.length}个账本成功`, {
      type: "success"
    });
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
