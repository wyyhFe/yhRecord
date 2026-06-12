<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { message } from "@/utils/message";
import {
  getKnowledgeDocumentList,
  uploadKnowledgeDocument,
  getKnowledgeTaskList,
  type KnowledgeDocumentVO,
  type KnowledgeChunkTaskVO
} from "@/api/knowledge";

defineOptions({ name: "KnowledgeDocument" });

const route = useRoute();
const router = useRouter();
const baseId = Number(route.query.id);
const baseName = String(route.query.name || "");

const loading = ref(false);
const documentList = ref<KnowledgeDocumentVO[]>([]);
const uploadDialogVisible = ref(false);
const uploadTitle = ref("");
const uploadFile = ref<File | null>(null);
const uploading = ref(false);

const taskDialogVisible = ref(false);
const taskList = ref<KnowledgeChunkTaskVO[]>([]);
const taskLoading = ref(false);

async function loadDocuments() {
  loading.value = true;
  try {
    const { data } = await getKnowledgeDocumentList(baseId);
    documentList.value = data || [];
  } finally {
    loading.value = false;
  }
}

function handleFileChange(file: File | undefined) {
  if (file) uploadFile.value = file;
}

async function handleUpload() {
  if (!uploadFile.value) {
    message("请选择文件", { type: "warning" });
    return;
  }
  uploading.value = true;
  try {
    await uploadKnowledgeDocument(
      baseId,
      uploadTitle.value || undefined,
      uploadFile.value
    );
    message("上传成功，正在解析...", { type: "success" });
    uploadDialogVisible.value = false;
    uploadTitle.value = "";
    uploadFile.value = null;
    loadDocuments();
  } finally {
    uploading.value = false;
  }

}

function viewTasks(docId: number) {
  taskLoading.value = true;
  taskDialogVisible.value = true;
  getKnowledgeTaskList(baseId, docId).then(({ data }) => {
    taskList.value = data || [];
  }).finally(() => {
    taskLoading.value = false;
  });
}

function goBack() {
  router.push("/business/knowledge");
}

onMounted(() => {
  loadDocuments();
});
</script>

<template>
  <div class="main">
    <div class="mb-4 flex items-center gap-2">
      <el-button text @click="goBack">← 返回</el-button>
      <span class="text-lg font-medium">{{ baseName }} — 文档管理</span>
    </div>

    <div class="mb-4">
      <el-button type="primary" @click="uploadDialogVisible = true">上传文档</el-button>
    </div>

    <el-card shadow="hover">
      <el-table :data="documentList" v-loading="loading" stripe>
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="标题" prop="title" min-width="160" />
        <el-table-column label="文件名" prop="fileName" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" prop="sourceType" width="100" />
        <el-table-column label="状态" prop="status" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'VECTORIZED' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="切片数" prop="chunkCount" width="80" />
        <el-table-column label="上传时间" prop="createdAt" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewTasks(row.id)">任务</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 上传文档对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文档" width="500px">
      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="uploadTitle" placeholder="可选" />
        </el-form-item>
        <el-form-item label="文件" required>
          <el-upload
            :auto-upload="false"
            :show-file-list="true"
            :on-change="(e) => handleFileChange(e.raw)"
            :limit="1"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>

    <!-- 任务对话框 -->
    <el-dialog v-model="taskDialogVisible" title="解析任务" width="600px">
      <el-table :data="taskList" v-loading="taskLoading" stripe>
        <el-table-column label="任务类型" prop="taskType" width="120" />
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="重试" prop="retryCount" width="60" />
        <el-table-column label="错误信息" prop="lastError" min-width="200" show-overflow-tooltip />
        <el-table-column label="开始时间" prop="startedAt" width="170" />
        <el-table-column label="结束时间" prop="finishedAt" width="170" />
      </el-table>
    </el-dialog>
  </div>
</template>
