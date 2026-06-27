import { message } from "@/utils/message";
import { getBlogList, deleteBlog } from "@/api/blog";
import type { BlogPostVO } from "@/api/blog";
import { reactive, ref, onMounted, h } from "vue";
import type { PaginationProps } from "@pureadmin/table";
import { ElTag, ElButton } from "element-plus";
import dayjs from "dayjs";

const statusMap: Record<string, { text: string; type: "primary" | "success" | "warning" | "info" | "danger" }> = {
  DRAFT: { text: "草稿", type: "info" },
  PUBLISHED: { text: "已发布", type: "success" },
  ARCHIVED: { text: "已归档", type: "warning" }
};

function renderStatus(status: string) {
  const s = statusMap[status] || { text: status, type: "info" as const };
  return h(ElTag, { type: s.type, size: "small" }, () => s.text);
}

function renderTags(tags: string[]) {
  if (!tags || tags.length === 0) return "-";
  return h(
    "div",
    { class: "flex flex-wrap gap-1 justify-center" },
    tags.map(t => h(ElTag, { size: "small", type: "info" }, () => t))
  );
}

export function useBlog() {
  const form = reactive({
    keyword: "",
    category: "",
    status: ""
  });

  const dataList = ref<BlogPostVO[]>([]);
  const loading = ref(true);
  const selectedIds = ref<number[]>([]);

  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });

  const columns: TableColumnList = [
    { type: "selection", width: 50, align: "center" },
    { label: "ID", prop: "id", width: 70 },
    {
      label: "标题",
      prop: "title",
      minWidth: 200,
      showOverflowTooltip: true
    },
    {
      label: "分类",
      prop: "category",
      width: 100,
      formatter: ({ category }) => category || "-"
    },
    {
      label: "标签",
      prop: "tags",
      width: 200,
      cellRenderer: ({ row }) => renderTags(row.tags)
    },
    {
      label: "状态",
      prop: "status",
      width: 100,
      align: "center",
      cellRenderer: ({ row }) => renderStatus(row.status)
    },
    {
      label: "浏览",
      prop: "viewCount",
      width: 80,
      align: "center"
    },
    {
      label: "评论",
      prop: "commentCount",
      width: 80,
      align: "center"
    },
    {
      label: "创建时间",
      prop: "createdAt",
      width: 160,
      formatter: ({ createdAt }) => dayjs(createdAt).format("YYYY-MM-DD HH:mm")
    },
    {
      label: "操作",
      fixed: "right",
      width: 160,
      slot: "operation"
    }
  ];

  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    onSearch();
  }

  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    onSearch();
  }

  function handleSelectionChange(val: BlogPostVO[]) {
    selectedIds.value = val.map(v => v.id);
  }

  async function onSearch() {
    loading.value = true;
    try {
      const params: Record<string, unknown> = {
        pageNum: pagination.currentPage,
        pageSize: pagination.pageSize
      };
      if (form.keyword) params.keyword = form.keyword;
      if (form.category) params.category = form.category;
      if (form.status) params.status = form.status;

      const res = await getBlogList(params);
      if (res.code === 0 && res.data) {
        dataList.value = res.data.list || [];
        pagination.total = res.data.total;
      }
    } catch {
      message("加载文章列表失败", { type: "error" });
    } finally {
      setTimeout(() => { loading.value = false; }, 200);
    }
  }

  function resetForm() {
    form.keyword = "";
    form.category = "";
    form.status = "";
    pagination.currentPage = 1;
    onSearch();
  }

  async function handleDelete(row: BlogPostVO) {
    try {
      const res = await deleteBlog(row.id);
      if (res.code === 0) {
        message("删除成功", { type: "success" });
        onSearch();
      }
    } catch {
      message("删除失败", { type: "error" });
    }
  }

  async function handleBatchDelete() {
    if (selectedIds.value.length === 0) {
      message("请先选择文章", { type: "warning" });
      return;
    }
    try {
      for (const id of selectedIds.value) {
        await deleteBlog(id);
      }
      message(`已删除 ${selectedIds.value.length} 篇文章`, { type: "success" });
      selectedIds.value = [];
      onSearch();
    } catch {
      message("批量删除失败", { type: "error" });
    }
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
