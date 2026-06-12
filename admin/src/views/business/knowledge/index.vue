<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message } from "@/utils/message";
import {
  getKnowledgeBaseList,
  createKnowledgeBase,
  type KnowledgeBaseVO
} from "@/api/knowledge";

defineOptions({ name: "KnowledgeBase" });

const router = useRouter();
const loading = ref(false);
const baseList = ref<KnowledgeBaseVO[]>([]);
const dialogVisible = ref(false);
const formData = reactive({
  name: "",
  code: "",
  description: "",
  visibility: "PRIVATE"
});
const searchName = ref("");
const isEdit = ref(false);
const editingId = ref<number | null>(null);

async function loadList() {
  loading.value = true;
  try {
    const { data } = await getKnowledgeBaseList();
    baseList.value = data || [];
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  isEdit.value = false;
  editingId.value = null;
  formData.name = "";
  formData.code = "";
  formData.description = "";
  formData.visibility = "PRIVATE";
  dialogVisible.value = true;
}

function openEdit(row: KnowledgeBaseVO) {
  isEdit.value = true;
  editingId.value = row.id;
  formData.name = row.name;
  formData.code = row.code;
  formData.description = row.description || "";
  formData.visibility = row.visibility || "PRIVATE";
  dialogVisible.value = true;
}

async function handleSave() {
  await createKnowledgeBase({
    name: formData.name,
    code: formData.code || undefined,
    description: formData.description || undefined,
    visibility: formData.visibility
  });
  message("创建成功", { type: "success" });
  dialogVisible.value = false;
  loadList();
}

function viewDocuments(row: KnowledgeBaseVO) {
  router.push({ path: "/business/knowledge/detail", query: { id: row.id, name: row.name } });
}

const filteredList = ref<KnowledgeBaseVO[]>([]);

onMounted(() => {
  loadList();
});
</script>

<template>
  <div class="main">
    <el-form :inline="true" class="bg-bg_color px-8 pt-4 pb-4">
      <el-form-item label="知识库名称">
        <el-input v-model="searchName" placeholder="搜索名称" clearable class="!w-[200px]" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadList">搜索</el-button>
        <el-button type="success" @click="openCreate">新建知识库</el-button>
      </el-form-item>
    </el-form>

    <el-card shadow="hover" class="mx-4">
      <el-table :data="baseList.filter(b => !searchName || b.name.includes(searchName))" v-loading="loading" stripe>
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="名称" prop="name" min-width="160" />
        <el-table-column label="编码" prop="code" width="140" />
        <el-table-column label="描述" prop="description" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '启用' : row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可见性" prop="visibility" width="100">
          <template #default="{ row }">
            <el-tag :type="row.visibility === 'PUBLIC' ? 'warning' : 'info'">
              {{ row.visibility === 'PUBLIC' ? '公开' : '私有' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" prop="createdAt" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDocuments(row as unknown as KnowledgeBaseVO)">文档管理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑知识库对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑知识库' : '新建知识库'" width="500px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="formData.name" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="formData.code" placeholder="可选，唯一标识" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-model="formData.visibility">
            <el-radio value="PRIVATE">私有</el-radio>
            <el-radio value="PUBLIC">公开</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
