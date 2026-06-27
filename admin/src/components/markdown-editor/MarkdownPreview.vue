<template>
  <div
    class="markdown-preview"
    :class="{ 'markdown-preview--dark': dark }"
    v-html="renderedHtml"
  />
</template>

<script setup lang="ts">
/**
 * MarkdownPreview.vue — markdown-it 渲染预览组件
 *
 * 职责：
 * 1. 将 Markdown 文本渲染为 HTML（v-html）
 * 2. 代码高亮（highlight.js）
 * 3. 浅色/深色模式自适应
 *
 * 给 Web 端的建议：
 * Web 端可以用同样的 useMarkdownIt composable，或者用 Nuxt Content 的渲染
 */
import { computed } from "vue";
import { useMarkdownIt } from "@/composables/markdown-editor";

const props = withDefaults(
  defineProps<{
    /** Markdown 源码 */
    content: string;
    /** 是否深色模式 */
    dark?: boolean;
  }>(),
  {
    content: "",
    dark: false,
  },
);

const { render } = useMarkdownIt();

const renderedHtml = computed(() => render(props.content));
</script>

<style scoped>
.markdown-preview {
  padding: 24px 32px;
  font-size: 15px;
  line-height: 1.85;
  color: #2c3e50;
  word-break: break-word;
}

/* ===== 标题 ===== */
.markdown-preview :deep(h1) {
  font-size: 1.75em;
  font-weight: 700;
  margin: 28px 0 14px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.markdown-preview :deep(h2) {
  font-size: 1.45em;
  font-weight: 600;
  margin: 24px 0 12px;
}

.markdown-preview :deep(h3) {
  font-size: 1.2em;
  font-weight: 600;
  margin: 20px 0 10px;
}

.markdown-preview :deep(h4) {
  font-size: 1.05em;
  font-weight: 600;
  margin: 16px 0 8px;
}

/* ===== 段落 ===== */
.markdown-preview :deep(p) {
  margin: 10px 0;
}

/* ===== 链接 ===== */
.markdown-preview :deep(a) {
  color: #409eff;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s;
}

.markdown-preview :deep(a:hover) {
  border-bottom-color: #409eff;
}

/* ===== 图片 ===== */
.markdown-preview :deep(img) {
  max-width: 100%;
  border-radius: 6px;
  margin: 12px 0;
}

/* ===== 引用 ===== */
.markdown-preview :deep(blockquote) {
  margin: 14px 0;
  padding: 10px 18px;
  border-left: 3px solid #409eff;
  background: #f0f7ff;
  border-radius: 0 6px 6px 0;
  color: #606266;
}

/* ===== 列表 ===== */
.markdown-preview :deep(ul),
.markdown-preview :deep(ol) {
  padding-left: 24px;
  margin: 8px 0;
}

.markdown-preview :deep(li) {
  margin: 4px 0;
}

/* ===== 分割线 ===== */
.markdown-preview :deep(hr) {
  border: none;
  border-top: 1px solid #ebeef5;
  margin: 24px 0;
}

/* ===== 表格 ===== */
.markdown-preview :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 14px 0;
  font-size: 14px;
}

.markdown-preview :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
  padding: 10px 14px;
  border: 1px solid #ebeef5;
  text-align: left;
}

.markdown-preview :deep(td) {
  padding: 8px 14px;
  border: 1px solid #ebeef5;
}

.markdown-preview :deep(tr:nth-child(even) td) {
  background: #fafbfc;
}

/* ===== 行内代码 ===== */
.markdown-preview :deep(code:not(pre code)) {
  background: #f0f2f5;
  padding: 2px 7px;
  border-radius: 4px;
  font-size: 0.9em;
  font-family: "JetBrains Mono", "Fira Code", Consolas, monospace;
  color: #e45649;
}

/* ===== 代码块语言标签（右上角） ===== */
.markdown-preview :deep(.code-lang-label) {
  position: absolute;
  top: 6px;
  right: 12px;
  font-size: 11px;
  font-family: "JetBrains Mono", "Fira Code", Consolas, monospace;
  color: #909399;
  background: rgba(255, 255, 255, 0.75);
  padding: 2px 8px;
  border-radius: 4px;
  line-height: 1.4;
  pointer-events: none;
  user-select: none;
}

.markdown-preview :deep(pre) {
  position: relative;
  margin: 14px 0;
  border-radius: 8px;
  overflow-x: auto;
}

.markdown-preview--dark :deep(.code-lang-label) {
  color: #8b949e;
  background: rgba(30, 30, 30, 0.75);
}

/* ===== 代码块 ===== */
.markdown-preview :deep(pre code) {
  display: block;
  padding: 16px 20px;
  font-family: "JetBrains Mono", "Fira Code", Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  background: #f6f8fa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

/* ===== 任务列表 ===== */
.markdown-preview :deep(input[type="checkbox"]) {
  margin-right: 6px;
  accent-color: #409eff;
}

/* ===== 深色模式 ===== */
.markdown-preview--dark {
  color: #cfd3dc;
}

.markdown-preview--dark :deep(h1) {
  border-bottom-color: #3a3b3c;
}

.markdown-preview--dark :deep(blockquote) {
  background: #2a2b2d;
  color: #909399;
}

.markdown-preview--dark :deep(th) {
  background: #2a2b2d;
  border-color: #3a3b3c;
}

.markdown-preview--dark :deep(td) {
  border-color: #3a3b3c;
}

.markdown-preview--dark :deep(tr:nth-child(even) td) {
  background: #262727;
}

.markdown-preview--dark :deep(code:not(pre code)) {
  background: #2a2b2d;
  color: #da6e75;
}

.markdown-preview--dark :deep(pre code) {
  background: #1d1e1f;
  border-color: #3a3b3c;
  color: #cfd3dc;
}

.markdown-preview--dark :deep(a) {
  color: #58a6ff;
}

.markdown-preview--dark :deep(a:hover) {
  border-bottom-color: #58a6ff;
}
</style>
