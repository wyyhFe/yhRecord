<script setup lang="ts">
import { ref, onMounted } from "vue";
import { message } from "@/utils/message";
import { http } from "@/utils/http";
import { handleTree } from "@/utils/tree";

defineOptions({ name: "MenuManage" });

const loading = ref(false);
const menuList = ref([]);
const menuTree = ref([]);
const dialogVisible = ref(false);
const menuForm = ref<any>({});
const parentOptions = ref([]);

async function loadMenus() {
  loading.value = true;
  try {
    const { data } = await http.request<any>("get", "/system/menu/list");
    menuList.value = data || [];
    menuTree.value = handleTree(data || []);
    // 构建上级菜单选项（仅目录类型可选作父级）
    parentOptions.value = handleTree(data || []);
  } finally {
    loading.value = false;
  }
}

function handleAdd(parentId?: number) {
  menuForm.value = {
    parentId: parentId || null,
    menuType: "PAGE",
    showLink: true,
    keepAlive: false,
    rank: 0,
    status: "ENABLED"
  };
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

    <el-table :data="menuTree" v-loading="loading" row-key="id" border default-expand-all>
      <el-table-column prop="title" label="菜单名称" min-width="160" />
      <el-table-column prop="name" label="路由名称" min-width="120" />
      <el-table-column prop="path" label="路由路径" min-width="140" />
      <el-table-column prop="component" label="组件路径" min-width="160" />
      <el-table-column prop="menuType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag
            :type="row.menuType === 'DIRECTORY' ? '' : row.menuType === 'PAGE' ? 'success' : 'warning'"
            size="small"
          >
            {{ row.menuType === 'DIRECTORY' ? '目录' : row.menuType === 'PAGE' ? '页面' : row.menuType === 'BUTTON' ? '按钮' : '外链' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="icon" label="图标" width="100" />
      <el-table-column prop="rank" label="排序" width="80" />
      <el-table-column prop="showLink" label="显示" width="80">
        <template #default="{ row }">
          <el-tag :type="row.showLink ? 'success' : 'info'" size="small">
            {{ row.showLink ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
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
          <el-cascader
            v-model="menuForm.parentId"
            :options="parentOptions"
            :props="{
              value: 'id',
              label: 'title',
              children: 'children',
              emitPath: false,
              checkStrictly: true
            }"
            clearable
            filterable
            placeholder="留空为顶级菜单"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-select v-model="menuForm.menuType">
            <el-option label="目录" value="DIRECTORY" />
            <el-option label="页面" value="PAGE" />
            <el-option label="外链" value="LINK" />
            <el-option label="按钮" value="BUTTON" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单名称">
          <el-input v-model="menuForm.title" placeholder="如：日记管理" />
        </el-form-item>
        <el-form-item label="路由名称">
          <el-input v-model="menuForm.name" placeholder="如：Diary（唯一标识）" />
        </el-form-item>
        <el-form-item label="路由路径" v-if="menuForm.menuType !== 'BUTTON'">
          <el-input v-model="menuForm.path" placeholder="如：/business/diary" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="menuForm.menuType === 'PAGE'">
          <el-input v-model="menuForm.component" placeholder="如：/business/diary/index" />
        </el-form-item>
        <el-form-item label="重定向" v-if="menuForm.menuType === 'DIRECTORY'">
          <el-input v-model="menuForm.redirect" placeholder="如：/business/diary" />
        </el-form-item>
        <el-form-item label="外链地址" v-if="menuForm.menuType === 'LINK'">
          <el-input v-model="menuForm.frameSrc" placeholder="如：https://example.com" />
        </el-form-item>
        <el-form-item label="权限标识" v-if="menuForm.menuType === 'BUTTON'">
          <el-input v-model="menuForm.auths" placeholder="如：btn:add,btn:edit" />
        </el-form-item>
        <el-form-item label="图标" v-if="menuForm.menuType !== 'BUTTON'">
          <el-input v-model="menuForm.icon" placeholder="如：ep/notebook" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="menuForm.rank" :min="0" />
        </el-form-item>
        <el-form-item label="是否显示" v-if="menuForm.menuType !== 'BUTTON'">
          <el-switch v-model="menuForm.showLink" />
        </el-form-item>
        <el-form-item label="是否缓存" v-if="menuForm.menuType === 'PAGE'">
          <el-switch v-model="menuForm.keepAlive" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
