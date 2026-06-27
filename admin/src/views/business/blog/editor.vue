<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { message } from "@/utils/message";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import {
  getBlogDetail,
  createBlog,
  updateBlog,
  type BlogPostRequest
} from "@/api/blog";
import Vditor from "vditor";
import "vditor/dist/index.css";

import ArrowLeft from "~icons/ep/arrow-left";
import Setting from "~icons/ep/setting";

defineOptions({ name: "BlogEditor" });

const route = useRoute();
const router = useRouter();

const isEdit = ref(false);
const saving = ref(false);
const vditor = ref<Vditor | null>(null);
const showMeta = ref(false);
const wordCount = ref(0);
let resizeObserver: ResizeObserver | null = null;

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

// 自动展开元信息（编辑模式或有内容时）
if (route.query.id) {
  showMeta.value = true;
}

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

// 初始化 Vditor
async function initVditor() {
  await nextTick();

  const editorEl = document.getElementById("vditor-container");
  if (!editorEl) return;

  const wrapper = editorEl.parentElement;
  if (!wrapper) return;

  // 用 ResizeObserver 等待容器获得真实高度后再初始化
  resizeObserver = new ResizeObserver((entries) => {
    const h = entries[0]?.contentRect?.height || 0;
    if (h <= 0) return;

    // 拿到高度后立即初始化，只执行一次
    resizeObserver?.disconnect();
    resizeObserver = null;

    createVditor(editorEl, h);
  });

  resizeObserver.observe(wrapper);

  // 兜底：500ms 后如果还没初始化，用 fallback 高度
  setTimeout(() => {
    if (!vditor.value && resizeObserver) {
      resizeObserver.disconnect();
      resizeObserver = null;
      const h = wrapper.clientHeight || 600;
      createVditor(editorEl, h);
    }
  }, 500);
}

function createVditor(editorEl: HTMLElement, h: number) {
  if (vditor.value) return; // 防止重复初始化

  try {
    const inst = new Vditor(editorEl.id, {
      height: h,
      mode: "ir",
      placeholder: "开始写作...",
      cdn: "",
      cache: { enable: false },
      counter: {
        enable: true,
        after(length: number) {
          wordCount.value = length;
        }
      },
      preview: {
        markdown: { autoSpace: true },
        hljs: { style: "github" }
      },
      toolbar: [
        "headings",
        "bold",
        "italic",
        "strike",
        "|",
        "line",
        "quote",
        "list",
        "ordered-list",
        "check",
        "code",
        "inline-code",
        "|",
        "upload",
        "link",
        "table",
        "|",
        "undo",
        "redo",
        "|",
        "fullscreen",
        "outline"
      ],
      after: () => {
        if (isEdit.value && form.value.markdownContent) {
          inst.setValue(form.value.markdownContent);
        }
      }
    });
    vditor.value = inst;
  } catch (e) {
    console.error("Vditor 初始化失败:", e);
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
    form.value.markdownContent = vditor.value?.getValue() || "";
    form.value.htmlContent = vditor.value?.getHTML() || "";

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

onMounted(async () => {
  await loadPost();
  await initVditor();
});

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect();
    resizeObserver = null;
  }
  try {
    if (vditor.value && (vditor.value as any).vditor) {
      vditor.value.destroy();
    }
  } catch {
    // Vditor 内部状态已清理，忽略
  }
});
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

    <!-- 编辑器 —— 占据所有剩余空间 -->
    <div class="editor-wrapper">
      <div id="vditor-container" class="vditor-root" />
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

/* ===== Editor —— 核心写作区 ===== */
.editor-wrapper {
  flex: 1;
  min-height: 0;
  position: relative;
  margin: 12px 32px 0;
  max-width: 960px;
  width: calc(100% - 64px);
  align-self: center;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* Vditor 容器绝对定位，撑满 editor-wrapper */
.vditor-root {
  position: absolute;
  inset: 0;
  border-radius: 8px;
  overflow: hidden;
}

/* Vditor 顶层不做任何高度覆盖，让 Vditor 原生管理 */
:deep(.vditor) {
  border: none !important;
  border-radius: 8px;
}

:deep(.vditor-toolbar) {
  background: #fafbfc !important;
  border-bottom: 1px solid #f0f0f0 !important;
  border-radius: 8px 8px 0 0;
  padding: 0 8px;
}

:deep(.vditor-toolbar__item) {
  margin: 0 2px;
}

:deep(.vditor-toolbar__item svg) {
  fill: #606266;
}

:deep(.vditor-toolbar__item:hover svg) {
  fill: #409eff;
}

/* 写作区内容内边距 — 类似 grtblog 的 padding-bottom: 50vh 思路 */
:deep(.vditor-ir) {
  padding: 20px 28px !important;
}

:deep(.vditor-content) {
  padding: 20px 28px;
}

:deep(.vditor-ir pre.vditor-reset) {
  font-family: "JetBrains Mono", "Fira Code", "Cascadia Code", Consolas, monospace;
  font-size: 14px;
  line-height: 1.6;
  background: #f6f8fa;
  border-radius: 6px;
  padding: 16px 20px;
}

:deep(.vditor-reset) {
  font-size: 15px;
  line-height: 1.8;
  color: #2c3e50;
}

:deep(.vditor-reset h1) {
  font-size: 24px;
  font-weight: 700;
  margin: 24px 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

:deep(.vditor-reset h2) {
  font-size: 20px;
  font-weight: 600;
  margin: 20px 0 10px;
}

:deep(.vditor-reset h3) {
  font-size: 17px;
  font-weight: 600;
  margin: 16px 0 8px;
}

:deep(.vditor-reset p) {
  margin: 8px 0;
}

:deep(.vditor-reset blockquote) {
  border-left: 3px solid #409eff;
  padding: 8px 16px;
  margin: 12px 0;
  background: #f0f7ff;
  border-radius: 0 6px 6px 0;
  color: #606266;
}

:deep(.vditor-reset ul) {
  padding-left: 24px;
}

:deep(.vditor-reset a) {
  color: #409eff;
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
</style>
