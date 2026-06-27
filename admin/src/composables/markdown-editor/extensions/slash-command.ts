import { CompletionContext, type Completion, type CompletionResult } from "@codemirror/autocomplete";
import { syntaxTree } from "@codemirror/language";
import type { EditorView } from "@codemirror/view";

/**
 * Slash 命令选项列表
 * 输入 / 时触发自动补全
 */
const slashOptions: Completion[] = [
  { label: "Heading 1", type: "keyword", apply: "# ", detail: "一级标题" },
  { label: "Heading 2", type: "keyword", apply: "## ", detail: "二级标题" },
  { label: "Heading 3", type: "keyword", apply: "### ", detail: "三级标题" },
  { label: "Heading 4", type: "keyword", apply: "#### ", detail: "四级标题" },
  { label: "Code Block", type: "keyword", apply: "```\n$\n```", detail: "代码块" },
  { label: "Quote", type: "keyword", apply: "> ", detail: "引用" },
  { label: "Link", type: "keyword", apply: "[text](url)", detail: "链接" },
  { label: "Image", type: "keyword", apply: "![alt](url)", detail: "图片" },
  { label: "Divider", type: "keyword", apply: "\n---\n", detail: "分割线" },
  { label: "Table", type: "keyword", apply: "\n| 列1 | 列2 | 列3 |\n| --- | --- | --- |\n| 内容 | 内容 | 内容 |\n", detail: "表格" },
  { label: "Task List", type: "keyword", apply: "\n- [ ] 任务\n- [ ] 任务\n", detail: "任务列表" },
  { label: "Bold", type: "keyword", apply: "**bold**", detail: "加粗" },
  { label: "Italic", type: "keyword", apply: "*italic*", detail: "斜体" },
  { label: "Strikethrough", type: "keyword", apply: "~~strikethrough~~", detail: "删除线" },
  { label: "Inline Code", type: "keyword", apply: "`code`", detail: "行内代码" },
];

/**
 * Slash 命令自动补全源
 * 当用户输入 / 时触发（仅在行首或空白后）
 */
export function slashCommandSource(context: CompletionContext): CompletionResult | null {
  const { state, pos } = context;

  // 获取当前行
  const line = state.doc.lineAt(pos);
  const offset = pos - line.from;
  const textBefore = line.text.slice(0, offset);

  // 正则匹配：以 / 结尾，且前面是行首或空格
  const match = /(?:^|\s)\/(\w*)$/.exec(textBefore);

  if (!match) return null;

  // 排除代码块和注释区域
  const tree = syntaxTree(state);
  const node = tree.resolveInner(pos, -1);
  if (node.name.includes("Code") || node.name.includes("Comment")) {
    return null;
  }

  // 获取搜索词（斜线后的部分）
  const query = match[1] ?? "";

  // 手动筛选选项（支持中文 detail 搜索）
  const filteredOptions = slashOptions.filter((option) => {
    const searchStr = query.toLowerCase();
    return (
      option.label.toLowerCase().includes(searchStr) ||
      (option.detail ?? "").toLowerCase().includes(searchStr)
    );
  });

  if (filteredOptions.length === 0) return null;

  return {
    // from 包含斜线，选中后会把 / 替换掉
    from: line.from + match.index + (match[0].startsWith(" ") ? 1 : 0),
    to: pos,
    options: filteredOptions,
    // 关闭 CM 默认过滤（因为 range 包含 /）
    filter: false,
  };
}
