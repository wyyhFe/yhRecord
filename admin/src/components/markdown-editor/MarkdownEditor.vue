<template>
  <div class="markdown-editor">
    <!-- 工具栏 -->
    <div class="editor-toolbar" v-if="!readOnly">
      <button
        v-for="item in toolbarItems"
        :key="item.label"
        :title="item.tooltip"
        :class="{ divider: item.divider }"
        @click="item.action"
      >
        <span v-if="item.html" v-html="item.html"></span>
        <span v-else>{{ item.label }}</span>
      </button>
    </div>
    <!-- 编辑器容器 -->
    <div class="cm-editor-wrapper" ref="containerRef"></div>
  </div>
</template>

<script setup lang="ts">
import {
  ref,
  onMounted,
  onBeforeUnmount,
  watch,
  nextTick,
} from "vue";
import { useCodeMirror } from "@/composables/markdown-editor";
import type { MarkdownEditorConfig } from "@/composables/markdown-editor";

// ========== Props ==========
const props = withDefaults(
  defineProps<{
    modelValue?: string;
    placeholder?: string;
    readOnly?: boolean;
    dark?: boolean;
    onUploadImage?: (file: File) => Promise<string>;
  }>(),
  {
    modelValue: "",
    placeholder: "开始写作...",
    readOnly: false,
    dark: false,
  },
);

// ========== Emits ==========
const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
  (e: "ready", view: import("@codemirror/view").EditorView): void;
}>();

// ========== CM6 实例 ==========
const containerRef = ref<HTMLDivElement | null>(null);
const { view, init, destroy, getValue, setValue, focus } = useCodeMirror();

// ========== 工具栏配置 ==========
interface ToolbarItem {
  label: string;
  tooltip: string;
  html?: string;
  divider?: boolean;
  action: () => void;
}

function insertAround(before: string, after: string = before) {
  if (!view.value) return;
  const { from, to } = view.value.state.selection.main;
  const selected = view.value.state.sliceDoc(from, to);
  const insertText = selected || "text";
  const start = from;
  const end = from + before.length + insertText.length + after.length;
  view.value.dispatch({
    changes: { from, to, insert: before + insertText + after },
    selection: { anchor: start + before.length, head: start + before.length + insertText.length },
  });
  focus();
}

function insertAtLineStart(prefix: string) {
  if (!view.value) return;
  const { from } = view.value.state.selection.main;
  const line = view.value.state.doc.lineAt(from);
  view.value.dispatch({ changes: { from: line.from, insert: prefix } });
  focus();
}

function insertSnippet(snippet: string, cursorOffset?: number) {
  if (!view.value) return;
  const { from } = view.value.state.selection.main;
  view.value.dispatch({ changes: { from, insert: snippet } });
  if (cursorOffset) {
    const pos = from + cursorOffset;
    view.value.dispatch({ selection: { anchor: pos, head: pos } });
  }
  focus();
}

const toolbarItems = ref<ToolbarItem[]>([
  { label: "B", tooltip: "加粗 (Ctrl+B)", action: () => insertAround("**", "**") },
  { label: "I", tooltip: "斜体 (Ctrl+I)", action: () => insertAround("*", "*") },
  { label: "S", tooltip: "删除线", action: () => insertAround("~~", "~~") },
  { label: "", tooltip: "", divider: true, action: () => {} },
  { label: "H1", tooltip: "一级标题", action: () => insertAtLineStart("# ") },
  { label: "H2", tooltip: "二级标题", action: () => insertAtLineStart("## ") },
  { label: "H3", tooltip: "三级标题", action: () => insertAtLineStart("### ") },
  { label: "", tooltip: "", divider: true, action: () => {} },
  { label: "🔗", tooltip: "链接", action: () => {
    if (!view.value) return;
    const { from, to } = view.value.state.selection.main;
    const selected = view.value.state.sliceDoc(from, to);
    const text = selected || "链接文本";
    const snippet = `[${text}](url)`;
    view.value.dispatch({ changes: { from, to, insert: snippet } });
    const urlStart = from + text.length + 3;
    view.value.dispatch({ selection: { anchor: urlStart, head: urlStart + 3 } });
    focus();
  }},
  { label: "🖼", tooltip: "图片", action: () => insertSnippet("![alt](url)", 2) },
  { label: "", tooltip: "", divider: true, action: () => {} },
  { label: "`", tooltip: "行内代码", action: () => insertAround("`", "`") },
  { label: "```", tooltip: "代码块", action: () => {
    if (!view.value) return;
    const { from, to } = view.value.state.selection.main;
    const selected = view.value.state.sliceDoc(from, to);
    const snippet = "\n```\n" + (selected || "") + "\n```\n";
    view.value.dispatch({ changes: { from, to, insert: snippet } });
    focus();
  }},
  { label: "", tooltip: "", divider: true, action: () => {} },
  { label: "❝", tooltip: "引用", action: () => insertAtLineStart("> ") },
  { label: "•", tooltip: "无序列表", action: () => insertAtLineStart("- ") },
  { label: "1.", tooltip: "有序列表", action: () => insertAtLineStart("1. ") },
  { label: "", tooltip: "", divider: true, action: () => {} },
  { label: "—", tooltip: "分割线", action: () => insertSnippet("\n---\n") },
]);

// ========== 初始化 ==========
let isInternalUpdate = false;

onMounted(async () => {
  await nextTick();
  if (!containerRef.value) return;

  const config: MarkdownEditorConfig = {
    initialValue: props.modelValue,
    placeholder: props.placeholder,
    readOnly: props.readOnly,
    dark: props.dark,
    onChange: (content) => {
      isInternalUpdate = true;
      emit("update:modelValue", content);
      nextTick(() => (isInternalUpdate = false));
    },
    onReady: (editorView) => {
      emit("ready", editorView);
    },
    onUploadImage: props.onUploadImage,
  };

  init(containerRef.value, config);
});

// ========== 清理 ==========
onBeforeUnmount(() => {
  destroy();
});

// ========== 外部内容同步 ==========
watch(
  () => props.modelValue,
  (newVal) => {
    if (isInternalUpdate) return;
    if (!view.value) return;
    if (newVal !== view.value.state.doc.toString()) {
      setValue(newVal ?? "");
    }
  },
);

// ========== 深色模式切换 ==========
watch(
  () => props.dark,
  () => {
    if (!view.value) return;
    const content = getValue();
    destroy();
    nextTick(() => {
      if (!containerRef.value) return;
      init(containerRef.value, {
        initialValue: content,
        placeholder: props.placeholder,
        readOnly: props.readOnly,
        dark: props.dark,
        onChange: (c) => emit("update:modelValue", c),
        onUploadImage: props.onUploadImage,
      });
    });
  },
);

// ========== 暴露给父组件 ==========
defineExpose({
  getView: () => view.value,
  getValue,
  setValue,
  focus,
  getSelection: () => {
    if (!view.value) return "";
    const { from, to } = view.value.state.selection.main;
    return view.value.state.sliceDoc(from, to);
  },
});
</script>

<style scoped>
.markdown-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 2px;
  padding: 6px 8px;
  background: #fafbfc;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.editor-toolbar button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 30px;
  height: 30px;
  padding: 0 6px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  transition: all 0.15s;
  font-family: inherit;
}

.editor-toolbar button:hover {
  background: #ecf5ff;
  color: #409eff;
}

.editor-toolbar button.divider {
  width: 1px;
  min-width: 1px;
  height: 18px;
  padding: 0;
  background: #dcdfe6;
  cursor: default;
  margin: 0 4px;
  pointer-events: none;
}

.cm-editor-wrapper {
  flex: 1;
  min-height: 0;
}

.cm-editor-wrapper :deep(.cm-editor) {
  height: 100%;
}

.cm-editor-wrapper :deep(.cm-scroller) {
  overflow: auto;
}

/* 滚动条 */
.cm-editor-wrapper :deep(.cm-scroller::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

.cm-editor-wrapper :deep(.cm-scroller::-webkit-scrollbar-thumb) {
  background: #c0c4cc;
  border-radius: 3px;
}

.cm-editor-wrapper :deep(.cm-scroller::-webkit-scrollbar-thumb:hover) {
  background: #909399;
}
</style>
