import type { Ref } from "vue";
import type { EditorView } from "@codemirror/view";
import type { ViewUpdate } from "@codemirror/view";
import type MarkdownIt from "markdown-it";

/**
 * CodeMirror 编辑器配置
 */
export interface MarkdownEditorConfig {
  /** 初始内容 */
  initialValue?: string;
  /** 占位符文本 */
  placeholder?: string;
  /** 是否只读 */
  readOnly?: boolean;
  /** 是否为深色模式 */
  dark?: boolean;
  /** 内容变化回调 */
  onChange?: (content: string) => void;
  /** 编辑器就绪回调 */
  onReady?: (view: EditorView) => void;
  /** 图片上传回调，返回图片 URL */
  onUploadImage?: (file: File) => Promise<string>;
}

/**
 * use-codemirror composable 返回值
 */
export interface UseCodeMirrorReturn {
  /** EditorView 实例引用 */
  view: Readonly<Ref<EditorView | null>>;
  /** 初始化编辑器 */
  init: (container: HTMLElement, config?: MarkdownEditorConfig) => EditorView;
  /** 销毁编辑器 */
  destroy: () => void;
  /** 获取当前内容 */
  getValue: () => string;
  /** 设置内容 */
  setValue: (content: string) => void;
  /** 聚焦 */
  focus: () => void;
  /** 注册视图更新钩子，返回取消注册函数 */
  onViewUpdate: (callback: (update: ViewUpdate) => void) => () => void;
}

/**
 * markdown-it 渲染配置
 */
export interface MarkdownItConfig {
  /** 是否允许 HTML 标签 */
  html?: boolean;
  /** 是否自动识别链接 */
  linkify?: boolean;
  /** 是否智能排版（弯引号等） */
  typographer?: boolean;
  /** 代码高亮主题 */
  highlightTheme?: "github" | "github-dark" | "monokai" | "one-dark";
}

/**
 * use-markdown-it composable 返回值
 */
export interface UseMarkdownItReturn {
  /** markdown-it 实例 */
  md: Readonly<Ref<MarkdownIt>>;
  /** 渲染 Markdown 为 HTML */
  render: (markdown: string) => string;
}
