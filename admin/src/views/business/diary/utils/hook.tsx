import dayjs from "dayjs";
import { message } from "@/utils/message";
import { getDiaryList, deleteDiary } from "@/api/diary";
import { reactive, ref, onMounted } from "vue";
import type { PaginationProps } from "@pureadmin/table";

export function useDiary() {
  const form = reactive({
    recordDate: "",
    mood: "",
    content: ""
  });

  const formRef = ref();
  const dataList = ref([]);
  const loading = ref(true);
  const selectedIds = ref<number[]>([]);

  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });

  const columns: TableColumnList = [
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
      label: "记录日期",
      prop: "recordDate",
      width: 120,
      formatter: ({ recordDate }) =>
        recordDate ? dayjs(recordDate).format("YYYY-MM-DD") : ""
    },
    {
      label: "内容",
      prop: "content",
      minWidth: 200,
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
      prop: "tags",
      minWidth: 150,
      cellRenderer: ({ row }) => {
        if (!row.tags || row.tags.length === 0) return "-";
        return (
          <div class="flex flex-wrap gap-1">
            {row.tags.map(tag => (
              <el-tag size="small" key={tag.id || tag}>
                {tag.name || tag}
              </el-tag>
            ))}
          </div>
        );
      }
    },
    {
      label: "图片",
      prop: "imageUrls",
      width: 100,
      cellRenderer: ({ row }) => {
        if (!row.imageUrls || row.imageUrls.length === 0) return "-";
        return (
          <el-image
            src={row.imageUrls[0]}
            preview-teleported={true}
            preview-src-list={row.imageUrls}
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
    const { code, data } = await getDiaryList({
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
    const { code } = await deleteDiary(row.id);
    if (code === 0) {
      message(`删除日记成功`, { type: "success" });
      onSearch();
    }
  }

  async function handleBatchDelete() {
    if (selectedIds.value.length === 0) {
      message("请选择要删除的日记", { type: "warning" });
      return;
    }
    // 逐个删除
    for (const id of selectedIds.value) {
      await deleteDiary(id);
    }
    message(`批量删除${selectedIds.value.length}条日记成功`, { type: "success" });
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
