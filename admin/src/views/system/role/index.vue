<script setup lang="ts">
import { ref, onMounted } from "vue";
import { message } from "@/utils/message";
import { http } from "@/utils/http";

defineOptions({ name: "RoleManage" });

const loading = ref(false);
const roleList = ref([]);
const dialogVisible = ref(false);
const roleForm = ref<any>({});
const menuList = ref([]);
const assignDialogVisible = ref(false);
const assignForm = ref<any>({});
const checkedMenus = ref<number[]>([]);

async function loadRoles() {
  loading.value = true;
  try {
    const { data } = await http.request<any>("get", "/system/role/list");
    roleList.value = data || [];
  } finally {
    loading.value = false;
  }
}

function handleAdd() {
  roleForm.value = { status: "ENABLED" };
  dialogVisible.value = true;
}

function handleEdit(row: any) {
  roleForm.value = { ...row };
  dialogVisible.value = true;
}

async function handleSave() {
  await http.request("post", "/system/role/save", { data: roleForm.value });
  message("保存成功", { type: "success" });
  dialogVisible.value = false;
  loadRoles();
}

async function handleAssign(row: any) {
  assignForm.value = { ...row };
  // 加载菜单列表
  const { data: menus } = await http.request<any>("get", "/system/menu/list");
  menuList.value = menus || [];
  assignDialogVisible.value = true;
}

async function handleAssignSave() {
  await http.request("post", `/system/role/assign-menus?roleId=${assignForm.value.id}`, {
    data: checkedMenus.value
  });
  message("权限分配成功", { type: "success" });
  assignDialogVisible.value = false;
}

onMounted(loadRoles);
</script>

<template>
  <div class="p-4">
    <div class="flex justify-between mb-4">
      <h2 class="text-lg font-semibold">角色管理</h2>
      <el-button type="primary" @click="handleAdd()">新增角色</el-button>
    </div>

    <el-table :data="roleList" v-loading="loading" border>
      <el-table-column prop="name" label="角色标识" min-width="120" />
      <el-table-column prop="label" label="角色名称" min-width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'" size="small">
            {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="200" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="handleAssign(row)">分配权限</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="roleForm.id ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="roleForm" label-width="100px">
        <el-form-item label="角色标识">
          <el-input v-model="roleForm.name" placeholder="如：admin" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.label" placeholder="如：管理员" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="roleForm.status">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="roleForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" title="分配菜单权限" width="500px">
      <el-tree
        :data="menuList"
        show-checkbox
        node-key="id"
        :props="{ label: 'title', children: 'children' }"
        v-model:checked-keys="checkedMenus"
        default-expand-all
      />
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
