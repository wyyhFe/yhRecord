import dayjs from "dayjs";
import roleForm from "../form/role.vue";
import editForm from "../form/index.vue";
import { handleTree } from "@/utils/tree";
import { message } from "@/utils/message";
import { usePublicHooks } from "../../hooks";
import { addDialog } from "@/components/ReDialog";
import type { FormItemProps, RoleFormItemProps } from "../utils/types";
import type { PaginationProps } from "@pureadmin/table";
import { deviceDetection } from "@pureadmin/utils";
import {
  getUserList,
  getAllRoleList,
  getRoleIds,
  assignUserRoles,
  updateUserStatus
} from "@/api/system";
import {
  type Ref,
  h,
  ref,
  toRaw,
  reactive,
  onMounted
} from "vue";

export function useUser(tableRef: Ref, treeRef: Ref) {
  const form = reactive({
    nickname: "",
    loginType: "",
    status: ""
  });
  const formRef = ref();
  const ruleFormRef = ref();
  const dataList = ref([]);
  const loading = ref(true);
  const switchLoadMap = ref({});
  const { switchStyle } = usePublicHooks();
  const treeData = ref([]);
  const treeLoading = ref(false);
  const selectedNum = ref(0);
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });
  const roleOptions = ref([]);

  const columns: TableColumnList = [
    {
      label: "用户 ID",
      prop: "id",
      width: 120
    },
    {
      label: "昵称",
      prop: "nickname",
      minWidth: 120
    },
    {
      label: "头像",
      prop: "avatarPath",
      width: 80,
      cellRenderer: ({ row }) => (
        <el-image
          fit="cover"
          preview-teleported={true}
          src={row.avatarPath || ""}
          class="size-6 rounded-full align-middle"
        >
          {{ error: () => <el-icon size={24}><i class="ep:avatar" /></el-icon> }}
        </el-image>
      )
    },
    {
      label: "登录方式",
      prop: "loginType",
      width: 100,
      formatter: ({ loginType }) => {
        const map = { WECHAT: "微信", GITHUB: "GitHub", GOOGLE: "Google" };
        return map[loginType] || loginType;
      }
    },
    {
      label: "性别",
      prop: "gender",
      width: 80,
      formatter: ({ gender }) => {
        const map = { MALE: "男", FEMALE: "女", UNKNOWN: "未知" };
        return map[gender] || "未知";
      }
    },
    {
      label: "状态",
      prop: "status",
      width: 100,
      cellRenderer: scope => (
        <el-tag
          size={scope.props.size}
          type={scope.row.status === "ENABLED" ? "success" : "danger"}
          effect="plain"
        >
          {scope.row.status === "ENABLED" ? "启用" : "禁用"}
        </el-tag>
      )
    },
    {
      label: "注册时间",
      prop: "createdAt",
      minWidth: 160,
      formatter: ({ createdAt }) =>
        createdAt ? dayjs(createdAt).format("YYYY-MM-DD HH:mm:ss") : ""
    },
    {
      label: "操作",
      fixed: "right",
      width: 180,
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
    selectedNum.value = val.length;
  }

  async function onSearch() {
    loading.value = true;
    const { code, data } = await getUserList({
      currentPage: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...toRaw(form)
    });
    if (code === 0) {
      dataList.value = data.records || [];
      pagination.total = data.total;
      pagination.pageSize = data.size;
      pagination.currentPage = data.current;
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

  /** 分配角色 */
  async function handleRole(row) {
    const ids = (await getRoleIds({ userId: row.id })).data ?? [];
    addDialog({
      title: `分配 ${row.nickname} 用户的角色`,
      props: {
        formInline: {
          username: row?.nickname ?? "",
          nickname: row?.nickname ?? "",
          roleOptions: roleOptions.value ?? [],
          ids
        }
      },
      width: "400px",
      draggable: true,
      fullscreen: deviceDetection(),
      fullscreenIcon: true,
      closeOnClickModal: false,
      contentRenderer: () => h(roleForm),
      beforeSure: (done, { options }) => {
        const curData = options.props.formInline as RoleFormItemProps;
        assignUserRoles(row.id, curData.ids).then(() => {
          message(`角色分配成功`, { type: "success" });
          done();
        });
      }
    });
  }

  /** 启用/禁用用户 */
  function handleToggleStatus(row) {
    const newStatus = row.status === "ENABLED" ? "DISABLED" : "ENABLED";
    const tip = newStatus === "DISABLED" ? "禁用" : "启用";
    updateUserStatus(row.id, newStatus).then(() => {
      row.status = newStatus;
      message(`已${tip}用户 ${row.nickname}`, { type: "success" });
    });
  }

  onMounted(async () => {
    onSearch();
    // 角色列表（分配角色用）
    const { data } = await getAllRoleList();
    roleOptions.value = data ?? [];
  });

  return {
    form,
    loading,
    columns,
    dataList,
    treeData,
    treeLoading,
    selectedNum,
    pagination,
    onSearch,
    resetForm,
    handleRole,
    handleToggleStatus,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange
  };
}
