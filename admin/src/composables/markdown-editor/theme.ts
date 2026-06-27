import { EditorView } from "@codemirror/view";
import { HighlightStyle, syntaxHighlighting } from "@codemirror/language";
import { tags } from "@lezer/highlight";

/**
 * CM6 浅色主题 —— 适配 Element Plus 默认浅色环境
 */
export const lightTheme = EditorView.theme(
  {
    "&": {
      backgroundColor: "#ffffff",
      color: "#303133",
      height: "100%",
      fontSize: "15px",
      lineHeight: "1.8",
    },
    ".cm-content": {
      fontFamily:
        "'JetBrains Mono', 'Fira Code', 'Cascadia Code', Consolas, 'Microsoft YaHei', sans-serif",
      padding: "16px 20px",
      caretColor: "#409eff",
    },
    ".cm-cursor": {
      borderLeftColor: "#409eff",
    },
    "&.cm-focused .cm-cursor": {
      borderLeftColor: "#409eff",
    },
    "&.cm-focused .cm-selectionBackground, .cm-selectionBackground": {
      backgroundColor: "#b3d8ff",
    },
    ".cm-gutters": {
      backgroundColor: "#fafbfc",
      color: "#909399",
      border: "none",
      borderRight: "1px solid #ebeef5",
    },
    ".cm-activeLineGutter": {
      backgroundColor: "#ecf5ff",
    },
    ".cm-activeLine": {
      backgroundColor: "#f0f7ff33",
    },
    ".cm-matchingBracket": {
      backgroundColor: "#b3d8ff66",
      outline: "1px solid #409eff",
    },
    ".cm-searchMatch": {
      backgroundColor: "#ffeaa7",
      outline: "1px solid #fdcb6e",
    },
    ".cm-foldPlaceholder": {
      backgroundColor: "#f0f2f5",
      border: "1px solid #dcdfe6",
      color: "#909399",
    },
    // Markdown 特定样式
    ".cm-header": {
      color: "#303133",
    },
    ".cm-header-1": {
      fontSize: "1.6em",
      fontWeight: "700",
    },
    ".cm-header-2": {
      fontSize: "1.35em",
      fontWeight: "600",
    },
    ".cm-header-3": {
      fontSize: "1.15em",
      fontWeight: "600",
    },
    ".cm-quote": {
      color: "#606266",
    },
    ".cm-link": {
      color: "#409eff",
      textDecoration: "underline",
    },
    ".cm-url": {
      color: "#909399",
    },
    ".cm-strikethrough": {
      textDecoration: "line-through",
    },
    ".cm-strong": {
      fontWeight: "700",
    },
    ".cm-emphasis": {
      fontStyle: "italic",
    },
    ".cm-code": {
      backgroundColor: "#f0f2f5",
      padding: "2px 6px",
      borderRadius: "4px",
      fontSize: "0.9em",
    },
    ".cm-hr": {
      color: "#dcdfe6",
    },
    ".cm-list": {
      color: "#409eff",
    },
    ".cm-tooltip": {
      border: "1px solid #e4e7ed",
      borderRadius: "6px",
      boxShadow: "0 2px 12px rgba(0,0,0,0.1)",
    },
    ".cm-tooltip-autocomplete": {
      "& .cm-completionIcon": {
        width: "18px",
      },
      "& .cm-completionLabel": {
        fontSize: "14px",
      },
      "& .cm-completionDetail": {
        fontSize: "12px",
        color: "#909399",
      },
    },
    ".cm-completionInfo": {
      border: "1px solid #e4e7ed",
      borderRadius: "6px",
      padding: "8px 12px",
      fontSize: "13px",
    },
  },
  { dark: false },
);

/**
 * CM6 深色主题 —— 适配 Element Plus 深色模式
 */
export const darkTheme = EditorView.theme(
  {
    "&": {
      backgroundColor: "#1d1e1f",
      color: "#cfd3dc",
      height: "100%",
      fontSize: "15px",
      lineHeight: "1.8",
    },
    ".cm-content": {
      fontFamily:
        "'JetBrains Mono', 'Fira Code', 'Cascadia Code', Consolas, 'Microsoft YaHei', sans-serif",
      padding: "16px 20px",
      caretColor: "#409eff",
    },
    ".cm-cursor": {
      borderLeftColor: "#409eff",
    },
    "&.cm-focused .cm-cursor": {
      borderLeftColor: "#409eff",
    },
    "&.cm-focused .cm-selectionBackground, .cm-selectionBackground": {
      backgroundColor: "#2a4b7c",
    },
    ".cm-gutters": {
      backgroundColor: "#262727",
      color: "#6e737d",
      border: "none",
      borderRight: "1px solid #3a3b3c",
    },
    ".cm-activeLineGutter": {
      backgroundColor: "#2a2b2d",
    },
    ".cm-activeLine": {
      backgroundColor: "#2a2b2d",
    },
    ".cm-matchingBracket": {
      backgroundColor: "#2a4b7c66",
      outline: "1px solid #409eff",
    },
    ".cm-searchMatch": {
      backgroundColor: "#5a4a00",
      outline: "1px solid #8a7a00",
    },
    ".cm-foldPlaceholder": {
      backgroundColor: "#2a2b2d",
      border: "1px solid #3a3b3c",
      color: "#6e737d",
    },
    // Markdown 特定样式
    ".cm-header": {
      color: "#cfd3dc",
    },
    ".cm-header-1": {
      fontSize: "1.6em",
      fontWeight: "700",
    },
    ".cm-header-2": {
      fontSize: "1.35em",
      fontWeight: "600",
    },
    ".cm-header-3": {
      fontSize: "1.15em",
      fontWeight: "600",
    },
    ".cm-quote": {
      color: "#909399",
    },
    ".cm-link": {
      color: "#409eff",
      textDecoration: "underline",
    },
    ".cm-url": {
      color: "#6e737d",
    },
    ".cm-strikethrough": {
      textDecoration: "line-through",
    },
    ".cm-strong": {
      fontWeight: "700",
    },
    ".cm-emphasis": {
      fontStyle: "italic",
    },
    ".cm-code": {
      backgroundColor: "#2a2b2d",
      padding: "2px 6px",
      borderRadius: "4px",
      fontSize: "0.9em",
    },
    ".cm-hr": {
      color: "#3a3b3c",
    },
    ".cm-list": {
      color: "#409eff",
    },
    ".cm-tooltip": {
      border: "1px solid #3a3b3c",
      borderRadius: "6px",
      backgroundColor: "#262727",
      boxShadow: "0 2px 12px rgba(0,0,0,0.3)",
    },
    ".cm-tooltip-autocomplete": {
      "& .cm-completionIcon": {
        width: "18px",
      },
      "& .cm-completionLabel": {
        fontSize: "14px",
        color: "#cfd3dc",
      },
      "& .cm-completionDetail": {
        fontSize: "12px",
        color: "#6e737d",
      },
    },
    ".cm-completionInfo": {
      border: "1px solid #3a3b3c",
      borderRadius: "6px",
      padding: "8px 12px",
      fontSize: "13px",
      backgroundColor: "#1d1e1f",
      color: "#cfd3dc",
    },
  },
  { dark: true },
);

/**
 * 浅色语法高亮
 */
export const lightHighlightStyle = HighlightStyle.define([
  { tag: tags.heading1, fontSize: "1.6em", fontWeight: "700" },
  { tag: tags.heading2, fontSize: "1.35em", fontWeight: "600" },
  { tag: tags.heading3, fontSize: "1.15em", fontWeight: "600" },
  { tag: tags.heading, color: "#303133" },
  { tag: tags.strong, fontWeight: "700" },
  { tag: tags.emphasis, fontStyle: "italic" },
  { tag: tags.strikethrough, textDecoration: "line-through" },
  { tag: tags.link, color: "#409eff", textDecoration: "underline" },
  { tag: tags.url, color: "#909399" },
  { tag: tags.monospace, backgroundColor: "#f0f2f5", borderRadius: "4px", padding: "2px 4px", fontFamily: "'JetBrains Mono', monospace" },
  { tag: tags.quote, color: "#606266" },
  { tag: tags.list, color: "#409eff" },
  { tag: tags.keyword, color: "#a626a4" },
  { tag: tags.atom, color: "#986801" },
  { tag: tags.bool, color: "#a626a4" },
  { tag: tags.number, color: "#986801" },
  { tag: tags.string, color: "#50a14f" },
  { tag: tags.comment, color: "#a0a1a7", fontStyle: "italic" },
  { tag: tags.meta, color: "#e45649" },
  { tag: tags.tagName, color: "#e45649" },
  { tag: tags.attributeName, color: "#986801" },
  { tag: tags.attributeValue, color: "#50a14f" },
  { tag: tags.processingInstruction, color: "#0184bc" },
]);

/**
 * 深色语法高亮
 */
export const darkHighlightStyle = HighlightStyle.define([
  { tag: tags.heading1, fontSize: "1.6em", fontWeight: "700" },
  { tag: tags.heading2, fontSize: "1.35em", fontWeight: "600" },
  { tag: tags.heading3, fontSize: "1.15em", fontWeight: "600" },
  { tag: tags.heading, color: "#cfd3dc" },
  { tag: tags.strong, fontWeight: "700" },
  { tag: tags.emphasis, fontStyle: "italic" },
  { tag: tags.strikethrough, textDecoration: "line-through" },
  { tag: tags.link, color: "#58a6ff", textDecoration: "underline" },
  { tag: tags.url, color: "#8b949e" },
  { tag: tags.monospace, backgroundColor: "#2a2b2d", borderRadius: "4px", padding: "2px 4px", fontFamily: "'JetBrains Mono', monospace" },
  { tag: tags.quote, color: "#909399" },
  { tag: tags.list, color: "#58a6ff" },
  { tag: tags.keyword, color: "#c678dd" },
  { tag: tags.atom, color: "#d19a66" },
  { tag: tags.bool, color: "#c678dd" },
  { tag: tags.number, color: "#d19a66" },
  { tag: tags.string, color: "#98c379" },
  { tag: tags.comment, color: "#5c6370", fontStyle: "italic" },
  { tag: tags.meta, color: "#e06c75" },
  { tag: tags.tagName, color: "#e06c75" },
  { tag: tags.attributeName, color: "#d19a66" },
  { tag: tags.attributeValue, color: "#98c379" },
  { tag: tags.processingInstruction, color: "#56b6c2" },
]);

/**
 * 获取主题扩展（按 dark 参数选择）
 */
export function getThemeExtensions(dark: boolean) {
  return [
    dark ? darkTheme : lightTheme,
    syntaxHighlighting(dark ? darkHighlightStyle : lightHighlightStyle),
  ];
}
