import { ref, readonly } from "vue";
import MarkdownIt from "markdown-it";
import hljs from "highlight.js";
import type { MarkdownItConfig, UseMarkdownItReturn } from "./types";

/**
 * 默认配置
 */
const defaultConfig: Required<MarkdownItConfig> = {
  html: true,
  linkify: true,
  typographer: true,
  highlightTheme: "github",
};

/**
 * markdown-it 渲染器 composable
 * 封装 markdown-it 的创建、代码高亮、渲染逻辑
 */
export function useMarkdownIt(config: MarkdownItConfig = {}): UseMarkdownItReturn {
  const merged = { ...defaultConfig, ...config };

  const md = ref<MarkdownIt>(
    new MarkdownIt({
      html: merged.html,
      linkify: merged.linkify,
      typographer: merged.typographer,
      highlight(str: string, lang: string) {
        const langLabel = lang ? lang : "";
        const langClass = lang ? ` class="hljs language-${lang}"` : "";
        if (lang && hljs.getLanguage(lang)) {
          try {
            const highlighted = hljs.highlight(str, { language: lang }).value;
            const labelHtml = langLabel
              ? `<span class="code-lang-label">${langLabel}</span>`
              : "";
            return `<pre class="hljs">${labelHtml}<code${langClass}>${highlighted}</code></pre>`;
          } catch {
            // 高亮失败，退回转义
          }
        }
        // 无语言或无法识别，转义 HTML 并返回
        const labelHtml = langLabel
          ? `<span class="code-lang-label">${langLabel}</span>`
          : "";
        return `<pre>${labelHtml}<code${langClass}>${md.value.utils.escapeHtml(str)}</code></pre>`;
      },
    }),
  );

  function render(markdown: string): string {
    return md.value.render(markdown);
  }

  return {
    md: readonly(md),
    render,
  };
}
