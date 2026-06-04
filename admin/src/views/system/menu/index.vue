<script setup lang="ts">
import { ref, onMounted } from "vue";
import { message } from "@/utils/message";
import { http } from "@/utils/http";

defineOptions({ name: "MenuManage" });

const loading = ref(false);
const menuList = ref([]);
const dialogVisible = ref(false);
const menuForm = ref<any>({});

async function loadMenus() {
  loading.value = true;
  try {
    const { data } = await http.request<any>("get", "/system/menu/list");
    menuList.value = data || [];
  } finally {
    loading.value = false;
  }
}

function handleAdd(parentId?: number) {
  menuForm.value = { parentId: parentId || null, menuType: "PAGE", showLink: true, keepAlive: false, rank: 0, status: "ENABLED" };
  dialogVisible.value = true;
}

function handleEdit(row: any) {
  menuForm.value = { ...row };
  dialogVisible.value = true;
}

async function handleSave() {
  await http.request("post", "/system/menu/save", { data: menuForm.value });
  message("保存成功", { type: "success" });
  dialogVisible.value = false;
  loadMenus();
}

async function handleDelete(id: number) {
  await http.request("delete", `/system/menu/${id}`);
  message("删除成功", { type: "success" });
  loadMenus();
}

onMounted(loadMenus);
</script>

<template>
  <div class="p-4">
    <div class="flex justify-between mb-4">
      <h2 class="text-lg font-semibold">菜单管理</h2>
      <el-button type="primary" @click="handleAdd()">新增菜单</el-button>
    </div>

    <el-table :data="menuList" v-loading="loading" row-key="id" border default-expand-all>
      <el-table-column prop="title" label="菜单名称" min-width="160" />
      <el-table-column prop="name" label="路由名称" min-width="120" />
      <el-table-column prop="path" label="路由路径" min-width="140" />
      <el-table-column prop="component" label="组件路径" min-width="160" />
      <el-table-column prop="menuType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.menuType === 'DIRECTORY' ? '' : row.menuType === 'PAGE' ? 'success' : 'warning'" size="small">
            {{ row.menuType === 'DIRECTORY' ? '目录' : row.menuType === 'PAGE' ? '页面' : '按钮' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="icon" label="图标" width="100" />
      <el-table-column prop="rank" label="排序" width="80" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleAdd(row.id)">添加子菜单</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="menuForm.id ? '编辑菜单' : '新增菜单'" width="600px">
      <el-form :model="menuForm" label-width="100px">
        <el-form-item label="上级菜单">
          <el-input v-model="menuForm.parentId" placeholder="父菜单 ID（留空为顶级）" />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-select v-model="menuForm.menuType">
            <el-option label="目录" value="DIRECTORY" />
            <el-option label="页面" value="PAGE" />
            <el-option label="按钮" value="BUTTON" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单名称">
          <el-input v-model="menuForm.title" placeholder="如：日记管理" />
        </el-form-item>
        <el-form-item label="路由名称">
          <el-input v-model="menuForm.name" placeholder="如：Diary（唯一标识）" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="menuForm.path" placeholder="如：/business/diary" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="menuForm.menuType !== 'DIRECTORY'">
          <el-input v-model="menuForm.component" placeholder="如：/business/diary/index" />
        </el-form-item>
        <el-form-item label="重定向" v-if="menuForm.menuType === 'DIRECTORY'">
          <el-input v-model="menuForm.redirect" placeholder="如：/business/diary" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="menuForm.icon" placeholder="如：ep/notebook" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="menuForm.rank" :min="0" />
        </el-form-item>
        <el-form-item label="是否显示">
          <el-switch v-model="menuForm.showLink" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
