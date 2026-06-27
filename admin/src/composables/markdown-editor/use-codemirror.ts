import { ref, shallowRef } from "vue";
import { EditorView, keymap, lineNumbers, drawSelection, highlightActiveLine, highlightActiveLineGutter } from "@codemirror/view";
import { EditorState, Compartment } from "@codemirror/state";
import { markdown, markdownLanguage } from "@codemirror/lang-markdown";
import { languages } from "@codemirror/language-data";
import { GFM, Subscript, Superscript } from "@lezer/markdown";
import { defaultKeymap, history, historyKeymap, indentWithTab } from "@codemirror/commands";
import { autocompletion, completionKeymap, closeBrackets, closeBracketsKeymap } from "@codemirror/autocomplete";
import { searchKeymap, highlightSelectionMatches } from "@codemirror/search";
import { lintKeymap } from "@codemirror/lint";
import { syntaxHighlighting, defaultHighlightStyle, bracketMatching, foldGutter, indentOnInput } from "@codemirror/language";
import { oneDark } from "@codemirror/theme-one-dark";
import { getThemeExtensions } from "./theme";
import { slashCommandSource } from "./extensions/slash-command";
import { slashHintExtension } from "./extensions/slash-hint";
import type { MarkdownEditorConfig, UseCodeMirrorReturn } from "./types";
import type { ViewUpdate } from "@codemirror/view";

/**
 * 核心 CodeMirror 6 composable
 * 封装 EditorView 的创建、生命周期管理、内容读写
 *
 * 设计原则：
 * 1. 与 Vue 框架解耦 —— 核心逻辑只用纯 JS
 * 2. 通过 ref 暴露状态给 Vue
 * 3. onUploadImage 回调支持外部注入上传逻辑
 * 4. onViewUpdate 钩子支持外部注册更新回调（参考 grtblog 架构）
 */
export function useCodeMirror(): UseCodeMirrorReturn {
  const view = shallowRef<EditorView | null>(null);

  /** 注册在 EditorView 更新时的回调集合 */
  const updateCallbacks = new Set<(update: ViewUpdate) => void>();

  /**
   * 外部注册 onViewUpdate 钩子
   * 返回取消注册的函数
   */
  function onViewUpdate(callback: (update: ViewUpdate) => void): () => void {
    updateCallbacks.add(callback);
    return () => updateCallbacks.delete(callback);
  }

  /**
   * 构建 CM6 扩展数组
   */
  function buildExtensions(config: MarkdownEditorConfig) {
    const extensions = [
      // 基础功能
      lineNumbers(),
      drawSelection(),
      highlightActiveLine(),
      highlightActiveLineGutter(),
      highlightSelectionMatches(),
      bracketMatching(),
      closeBrackets(),
      indentOnInput(),
      foldGutter(),

      // Markdown 语言支持（GFM + 代码块语法高亮 + 脚注/删除线）
      markdown({
        base: markdownLanguage,
        codeLanguages: languages,
        extensions: [GFM, Subscript, Superscript],
      }),

      // 历史记录
      history(),

      // 自动补全（接入 Slash 命令）
      autocompletion({
        override: [slashCommandSource],
        icons: false,
        defaultKeymap: true,
      }),

      // Slash 空行提示
      slashHintExtension,

      // 编辑器内容变更监听 + 钩子分发
      EditorView.updateListener.of((update) => {
        // 分发到所有注册的钩子
        updateCallbacks.forEach((cb) => cb(update));

        // 原有的 onChange 回调
        if (update.docChanged) {
          config.onChange?.(update.state.doc.toString());
        }
      }),

      // 按键绑定
      keymap.of([
        ...defaultKeymap,
        ...historyKeymap,
        ...completionKeymap,
        ...closeBracketsKeymap,
        ...searchKeymap,
        ...lintKeymap,
        indentWithTab,
      ]),
    ];

    // 只读模式（使用 Compartment 支持动态切换）
    if (config.readOnly) {
      const readOnlyCompartment = new Compartment();
      extensions.push(readOnlyCompartment.of(EditorState.readOnly.of(true)));
      (config as Record<string, unknown>).__readOnlyCompartment = readOnlyCompartment;
    }

    // 主题
    if (config.dark) {
      extensions.push(oneDark);
    } else {
      extensions.push(...getThemeExtensions(false));
    }

    // 图片粘贴/拖拽上传
    if (config.onUploadImage) {
      extensions.push(buildImageUploadExtension(config.onUploadImage));
    }

    return extensions;
  }

  /**
   * 初始化编辑器
   */
  function init(container: HTMLElement, config: MarkdownEditorConfig = {}): EditorView {
    // 如果已有实例，先销毁
    destroy();

    const state = EditorState.create({
      doc: config.initialValue ?? "",
      extensions: buildExtensions(config),
    });

    const editorView = new EditorView({
      state,
      parent: container,
    });

    view.value = editorView;
    config.onReady?.(editorView);
    return editorView;
  }

  /**
   * 销毁编辑器
   */
  function destroy() {
    if (view.value) {
      view.value.destroy();
      view.value = null;
    }
  }

  /**
   * 获取编辑器当前 Markdown 内容
   */
  function getValue(): string {
    return view.value?.state.doc.toString() ?? "";
  }

  /**
   * 设置编辑器内容（替换全部）
   */
  function setValue(content: string) {
    if (!view.value) return;
    view.value.dispatch({
      changes: {
        from: 0,
        to: view.value.state.doc.length,
        insert: content,
      },
    });
  }

  /**
   * 聚焦编辑器
   */
  function focus() {
    view.value?.focus();
  }

  return {
    view,
    init,
    destroy,
    getValue,
    setValue,
    focus,
    onViewUpdate,
  };
}

/**
 * 构建图片粘贴/拖拽上传扩展
 * 监听 paste 和 drop 事件，提取图片文件调用上传回调
 *
 * 参考 grtblog 的实现思路：
 * - paste 事件：检测 ClipboardEvent 中的 image 文件
 * - drop 事件：检测 DragEvent 中的 image 文件
 * - 上传过程中显示 loading widget placeholder
 */
function buildImageUploadExtension(onUploadImage: (file: File) => Promise<string>) {
  return EditorView.domEventHandlers({
    paste(event, view) {
      const items = event.clipboardData?.items;
      if (!items) return;

      for (let i = 0; i < items.length; i++) {
        const item = items[i];
        if (item.type.startsWith("image/")) {
          event.preventDefault();
          const file = item.getAsFile();
          if (file) {
            handleImageUpload(view, file, onUploadImage);
          }
          break;
        }
      }
    },

    drop(event, view) {
      const files = event.dataTransfer?.files;
      if (!files) return;

      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (file.type.startsWith("image/")) {
          event.preventDefault();
          handleImageUpload(view, file, onUploadImage);
          break;
        }
      }
    },
  });
}

/**
 * 处理单张图片上传
 * 在光标位置插入上传中的占位符，完成后替换为 Markdown 图片语法
 */
async function handleImageUpload(
  view: EditorView,
  file: File,
  uploadFn: (file: File) => Promise<string>,
) {
  const placeholder = `![上传中... ${file.name}]()`;

  // 在光标位置插入占位符
  const pos = view.state.selection.main.head;
  const from = pos;
  const to = pos + placeholder.length;

  view.dispatch({
    changes: { from: pos, insert: placeholder },
  });

  try {
    const url = await uploadFn(file);
    // 替换占位符为实际图片
    view.dispatch({
      changes: { from, to, insert: `![${file.name}](${url})` },
    });
  } catch (err) {
    console.error("图片上传失败:", err);
    // 移除占位符
    view.dispatch({
      changes: { from, to, insert: "" },
    });
  }
}
