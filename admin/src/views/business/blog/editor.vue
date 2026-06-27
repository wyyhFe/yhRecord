<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { message } from "@/utils/message";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import {
  getBlogDetail,
  createBlog,
  updateBlog,
  type BlogPostRequest
} from "@/api/blog";
import { uploadFile } from "@/api/upload";
import MarkdownEditor from "@/components/markdown-editor/MarkdownEditor.vue";
import MarkdownPreview from "@/components/markdown-editor/MarkdownPreview.vue";
import { useMarkdownIt } from "@/composables/markdown-editor";

import ArrowLeft from "~icons/ep/arrow-left";
import Setting from "~icons/ep/setting";
import View from "~icons/ep/view";
import EditPen from "~icons/ep/edit-pen";

defineOptions({ name: "BlogEditor" });

const route = useRoute();
const router = useRouter();

const isEdit = ref(false);
const saving = ref(false);
const showMeta = ref(false);
// "edit" | "split" | "preview"
const viewMode = ref<"edit" | "split" | "preview">("split");
const editorRef = ref<InstanceType<typeof MarkdownEditor> | null>(null);

// 自动展开元信息（编辑模式时）
if (route.query.id) {
  showMeta.value = true;
}

const form = ref<BlogPostRequest>({
  title: "",
  markdownContent: "",
  htmlContent: "",
  summary: "",
  category: "",
  tags: [],
  slug: "",
  status: "DRAFT"
});

const tagInput = ref("");

// 字数统计
const wordCount = computed(() => form.value.markdownContent.length);

// markdown-it 用于保存时生成 HTML
const { render } = useMarkdownIt();

// 加载已有文章
async function loadPost() {
  const id = route.query.id as string;
  if (!id) return;
  isEdit.value = true;
  try {
    const res = await getBlogDetail(Number(id));
    if (res.code === 0 && res.data) {
      const d = res.data;
      form.value = {
        title: d.title,
        markdownContent: d.markdownContent || "",
        htmlContent: d.htmlContent || "",
        summary: d.summary || "",
        category: d.category || "",
        tags: d.tags || [],
        slug: d.slug || "",
        status: d.status
      };
      if (d.category || d.tags?.length || d.summary || d.slug) {
        showMeta.value = true;
      }
    }
  } catch {
    message("加载文章失败", { type: "error" });
  }
}

// 图片上传
async function handleUploadImage(file: File): Promise<string> {
  try {
    const res = await uploadFile(file);
    if (res.code === 0 && res.data?.url) {
      return res.data.url;
    }
    throw new Error("上传失败");
  } catch {
    message("图片上传失败", { type: "error" });
    throw new Error("图片上传失败");
  }
}

// 添加标签
function addTag() {
  const name = tagInput.value.trim();
  if (!name) return;
  if (form.value.tags.includes(name)) {
    message("标签已存在", { type: "warning" });
    return;
  }
  form.value.tags.push(name);
  tagInput.value = "";
}

function removeTag(idx: number) {
  form.value.tags.splice(idx, 1);
}

// 保存
async function handleSave(status: string) {
  saving.value = true;
  try {
    form.value.status = status;
    // markdownContent 通过 v-model 已同步
    form.value.htmlContent = render(form.value.markdownContent);

    if (!form.value.title.trim()) {
      message("请输入标题", { type: "warning" });
      saving.value = false;
      return;
    }

    let res;
    if (isEdit.value) {
      const id = Number(route.query.id);
      res = await updateBlog(id, form.value);
    } else {
      res = await createBlog(form.value);
    }

    if (res.code === 0) {
      message(status === "PUBLISHED" ? "发布成功" : "保存成功", { type: "success" });
      router.push({ name: "BlogManage" });
    }
  } catch {
    message("保存失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

function goBack() {
  router.push({ name: "BlogManage" });
}

function toggleMeta() {
  showMeta.value = !showMeta.value;
}

function toggleViewMode() {
  const modes: Array<"edit" | "split" | "preview"> = ["edit", "split", "preview"];
  const idx = modes.indexOf(viewMode.value);
  viewMode.value = modes[(idx + 1) % modes.length];
}

// 快捷键 Ctrl+S 保存草稿
function handleKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === "s") {
    e.preventDefault();
    handleSave("DRAFT");
  }
}

onMounted(async () => {
  await loadPost();
  document.addEventListener("keydown", handleKeydown);
});

onBeforeUnmount(() => {
  document.removeEventListener("keydown", handleKeydown);
});

// 监听 markdownContent 变化（用于字数统计和自动保存标记等）
watch(
  () => form.value.markdownContent,
  () => {
    // 可以在这里做自动保存草稿等
  }
);
</script>

<template>
  <div class="blog-editor">
    <!-- 顶部工具栏 -->
    <header class="editor-header">
      <div class="flex items-center gap-3">
        <el-button
          text
          :icon="useRenderIcon(ArrowLeft)"
          class="!text-gray-500 hover:!text-gray-800"
          @click="goBack"
        >
          返回
        </el-button>
        <span class="text-xs text-gray-400">
          {{ isEdit ? "编辑文章" : "新建文章" }}
        </span>
      </div>
      <div class="flex items-center gap-2">
        <!-- 视图模式切换 -->
        <el-button
          text
          :icon="useRenderIcon(
            viewMode === 'edit' ? View :
            viewMode === 'split' ? Setting : EditPen
          )"
          class="!text-gray-400 hover:!text-gray-600"
          @click="toggleViewMode"
          :title="viewMode === 'edit' ? '切换为分屏' : viewMode === 'split' ? '切换为预览' : '切换为编辑'"
        >
          {{ viewMode === 'edit' ? '分屏' : viewMode === 'split' ? '预览' : '编辑' }}
        </el-button>
        <el-divider direction="vertical" />
        <el-button
          text
          :icon="useRenderIcon(Setting)"
          class="!text-gray-400 hover:!text-gray-600"
          :class="{ '!text-blue-500': showMeta }"
          @click="toggleMeta"
        >
          文章设置
        </el-button>
        <el-divider direction="vertical" />
        <el-button
          :loading="saving"
          @click="handleSave('DRAFT')"
        >
          存草稿
        </el-button>
        <el-button
          type="primary"
          :loading="saving"
          @click="handleSave('PUBLISHED')"
        >
          发布
        </el-button>
      </div>
    </header>

    <!-- 标题 -->
    <div class="title-area">
      <textarea
        v-model="form.title"
        class="title-input"
        placeholder="输入文章标题..."
        rows="1"
        @input="(e) => {
          const el = e.target as HTMLTextAreaElement;
          el.style.height = 'auto';
          el.style.height = el.scrollHeight + 'px';
        }"
      />
    </div>

    <!-- 元信息面板（可折叠） -->
    <transition name="meta-slide">
      <div v-if="showMeta" class="meta-panel">
        <div class="meta-grid">
          <div class="meta-item">
            <label class="meta-label">分类</label>
            <el-input
              v-model="form.category"
              placeholder="技术、生活..."
              size="small"
            />
          </div>
          <div class="meta-item">
            <label class="meta-label">Slug</label>
            <el-input
              v-model="form.slug"
              placeholder="留空自动生成"
              size="small"
            />
          </div>
          <div class="meta-item">
            <label class="meta-label">摘要</label>
            <el-input
              v-model="form.summary"
              placeholder="简要描述文章内容"
              size="small"
            />
          </div>
        </div>
        <div class="meta-item">
          <label class="meta-label">标签</label>
          <div class="tags-area">
            <el-tag
              v-for="(tag, idx) in form.tags"
              :key="idx"
              closable
              size="small"
              effect="plain"
              @close="removeTag(idx)"
            >
              {{ tag }}
            </el-tag>
            <el-input
              v-model="tagInput"
              placeholder="输入标签，回车添加"
              size="small"
              class="tag-input-inline"
              @keyup.enter="addTag"
            />
          </div>
        </div>
      </div>
    </transition>

    <!-- 编辑器 / 预览区 -->
    <div class="editor-wrapper" :class="{ 'is-split': viewMode === 'split' }">
      <!-- 编辑模式：只显示编辑器 -->
      <MarkdownEditor
        v-if="viewMode === 'edit'"
        ref="editorRef"
        v-model="form.markdownContent"
        placeholder="开始写作..."
        :read-only="false"
        :on-upload-image="handleUploadImage"
      />
      <!-- 分屏模式：左编辑 + 右预览 -->
      <template v-else-if="viewMode === 'split'">
        <div class="split-container">
          <div class="split-editor">
            <MarkdownEditor
              ref="editorRef"
              v-model="form.markdownContent"
              placeholder="开始写作..."
              :read-only="false"
              :on-upload-image="handleUploadImage"
            />
          </div>
          <div class="split-divider"></div>
          <div class="split-preview">
            <MarkdownPreview :content="form.markdownContent" />
          </div>
        </div>
      </template>
      <!-- 预览模式：只显示预览 -->
      <MarkdownPreview
        v-else
        :content="form.markdownContent"
      />
    </div>

    <!-- 底部状态栏 -->
    <footer class="editor-footer">
      <span>字数 {{ wordCount }}</span>
      <span class="text-gray-300">|</span>
      <span>Markdown</span>
      <span class="text-gray-300">|</span>
      <span>{{ form.status === "PUBLISHED" ? "已发布" : "草稿" }}</span>
    </footer>
  </div>
</template>

<style scoped>
.blog-editor {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
  background: #f8f9fa;
  overflow: hidden;
}

/* ===== Header ===== */
.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  min-height: 48px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

/* ===== Title ===== */
.title-area {
  flex-shrink: 0;
  padding: 20px 32px 0;
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
}

.title-input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.4;
  color: #1a1a1a;
  resize: none;
  overflow: hidden;
  background: transparent;
  padding: 0;
  font-family: inherit;
}

.title-input::placeholder {
  color: #c0c4cc;
  font-weight: 400;
}

/* ===== Meta Panel ===== */
.meta-panel {
  flex-shrink: 0;
  background: #fff;
  border-radius: 8px;
  padding: 14px 20px 10px;
  margin: 12px 32px 0;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  max-width: 960px;
  width: calc(100% - 64px);
  align-self: center;
}

.meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1.5fr;
  gap: 16px;
  margin-bottom: 10px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.tags-area {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  min-height: 28px;
}

.tag-input-inline {
  width: 160px;
}

/* Meta slide transition */
.meta-slide-enter-active,
.meta-slide-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}

.meta-slide-enter-from,
.meta-slide-leave-to {
  opacity: 0;
  max-height: 0;
  margin-top: 0;
  margin-bottom: 0;
  padding-top: 0;
  padding-bottom: 0;
  border-width: 0;
}

.meta-slide-enter-to,
.meta-slide-leave-from {
  opacity: 1;
  max-height: 180px;
}

/* ===== Editor Wrapper ===== */
.editor-wrapper {
  flex: 1;
  min-height: 0;
  margin: 12px 32px 0;
  max-width: 960px;
  width: calc(100% - 64px);
  align-self: center;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

/* ===== Footer ===== */
.editor-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 32px;
  min-height: 32px;
  font-size: 12px;
  color: #909399;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}

/* ===== 分屏模式 ===== */
.is-split {
  max-width: 100%;
  width: calc(100% - 64px);
}

.split-container {
  display: flex;
  height: 100%;
  width: 100%;
}

.split-editor {
  flex: 1;
  min-width: 0;
  border-right: 1px solid #ebeef5;
}

.split-editor :deep(.markdown-editor) {
  border: none;
  border-radius: 0;
}

.split-editor :deep(.cm-editor-wrapper) {
  height: 100%;
}

.split-divider {
  width: 4px;
  background: #f0f2f5;
  cursor: col-resize;
  flex-shrink: 0;
  transition: background 0.15s;
}

.split-divider:hover {
  background: #409eff;
}

.split-preview {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
}
</style>
