import { syntaxTree } from "@codemirror/language";
import { EditorView } from "@codemirror/view";
import type { ChangeSpec, EditorState } from "@codemirror/state";

/**
 * 支持的格式类型
 */
export type FormatType = "bold" | "italic" | "code" | "strike";

interface StyleConfig {
  mark: string;
  node: string;
}

const STYLES: Record<FormatType, StyleConfig> = {
  bold: { mark: "**", node: "StrongEmphasis" },
  italic: { mark: "*", node: "Emphasis" },
  code: { mark: "`", node: "InlineCode" },
  strike: { mark: "~~", node: "Strikethrough" },
};

// 需要避让的节点（黑名单）
const IGNORED_NODES = [
  "FencedCode",
  "Blockquote",
  "HorizontalRule",
  "URL",
  "Image",
];

/**
 * 获取当前选区激活的样式集合
 * 通过解析语法树，检测光标位置是否被某种样式节点包裹
 */
export function getActiveStyles(view: EditorView): Set<FormatType> {
  const { state } = view;
  const { from, to } = state.selection.main;
  const active = new Set<FormatType>();

  // 如果选区非空，探测中间；如果为空，探测光标左侧
  const pos = from === to ? from : from + 1;

  // resolve(pos, -1) 表示向左偏向查找
  let node = syntaxTree(state).resolve(pos, -1);

  // 向上冒泡查找父节点
  while (node.parent) {
    for (const [key, config] of Object.entries(STYLES)) {
      if (node.name === config.node) {
        active.add(key as FormatType);
      }
    }
    node = node.parent;
  }
  return active;
}

/**
 * 切换样式（加粗/斜体/代码/删除线）
 */
export function toggleFormat(view: EditorView, type: FormatType): void {
  const active = getActiveStyles(view);
  const { mark, node } = STYLES[type];

  if (active.has(type)) {
    unwrapStyle(view, mark, node);
  } else {
    wrapStyleSafe(view, mark);
  }
}

/**
 * 拆包逻辑：移除样式
 */
function unwrapStyle(view: EditorView, mark: string, nodeName: string): void {
  const { state, dispatch } = view;
  const changes: ChangeSpec[] = [];
  const { from, to } = state.selection.main;

  syntaxTree(state).iterate({
    from,
    to,
    enter: (node) => {
      if (node.name === nodeName) {
        const text = state.sliceDoc(node.from, node.to);
        // 确保确实是用这个标记包裹的
        if (text.startsWith(mark) && text.endsWith(mark)) {
          changes.push(
            { from: node.from, to: node.from + mark.length, insert: "" },
            { from: node.to - mark.length, to: node.to, insert: "" },
          );
        }
      }
    },
  });

  if (changes.length) dispatch({ changes, userEvent: "format.unwrap" });
}

/**
 * 智能包裹逻辑：避让代码块和特殊节点
 */
function wrapStyleSafe(view: EditorView, mark: string): void {
  const { state, dispatch } = view;
  const changes: ChangeSpec[] = [];
  const range = state.selection.main;

  if (range.empty) return; // 空选区不处理

  let lastPos = range.from;

  // 遍历 AST
  syntaxTree(state).iterate({
    from: range.from,
    to: range.to,
    enter: (node) => {
      if (IGNORED_NODES.some((ignored) => node.name.includes(ignored))) {
        // 处理黑名单节点之前的纯文本
        if (lastPos < node.from) {
          changes.push(...createWrapChanges(state, lastPos, node.from, mark));
        }
        // 跳过黑名单节点
        lastPos = node.to;
        return false; // 不再进入子节点
      }
    },
  });

  // 处理剩下的部分
  if (lastPos < range.to) {
    changes.push(...createWrapChanges(state, lastPos, range.to, mark));
  }

  if (changes.length) dispatch({ changes, userEvent: "format.wrap" });
}

/**
 * 生成包裹变更（剔除首尾空格）
 */
function createWrapChanges(
  state: EditorState,
  from: number,
  to: number,
  mark: string,
): ChangeSpec[] {
  const text = state.sliceDoc(from, to);

  // 边界检查：如果全是空白字符，直接忽略
  if (!text.trim()) {
    return [];
  }

  // 计算前导空格长度
  const leadingSpaceLen = text.search(/\S|$/);
  // 计算尾部空格长度
  const trailingSpaceLen = text.length - text.trimEnd().length;

  // 计算实际插入点的绝对坐标
  const insertFrom = from + leadingSpaceLen;
  const insertTo = to - trailingSpaceLen;

  return [
    { from: insertFrom, insert: mark },
    { from: insertTo, insert: mark },
  ];
}
