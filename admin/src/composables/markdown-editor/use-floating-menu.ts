import { ref, type Ref } from "vue";
import type { UpdateHook } from "../use-codemirror";
import type { EditorView, ViewUpdate } from "@codemirror/view";
import { getActiveStyles, toggleFormat, type FormatType } from "./ast";

/**
 * 浮动菜单上下文
 */
interface EditorContext {
  view: Ref<EditorView | null | undefined>;
  onViewUpdate: (callback: UpdateHook) => void;
}

/**
 * 浮动菜单 composable
 * 选中文本时出现气泡菜单，支持加粗/斜体/删除线/行内代码
 */
export function useFloatingMenu({ view, onViewUpdate }: EditorContext) {
  const isVisible = ref(false);
  const menuPos = ref({ top: 0, left: 0 });
  const activeFormats = ref<Set<FormatType>>(new Set());

  onViewUpdate((_update: ViewUpdate) => {
    const currentView = view.value;
    if (!currentView) return;

    const { selection } = currentView.state;

    // 空选区不显示
    if (selection.main.empty) {
      isVisible.value = false;
      return;
    }

    // 计算坐标
    const start = currentView.coordsAtPos(selection.main.from);
    const end = currentView.coordsAtPos(selection.main.to);

    if (!start || !end) {
      isVisible.value = false;
      return;
    }

    // 更新菜单位置（在选区上方居中）
    menuPos.value = {
      left: (start.left + end.right) / 2,
      top: start.top - 8,
    };

    // 查询当前激活的样式
    activeFormats.value = getActiveStyles(currentView);
    isVisible.value = true;
  });

  // 执行格式命令
  function executeCommand(type: FormatType): void {
    if (view.value) {
      toggleFormat(view.value, type);
      view.value.focus();
    }
  }

  return {
    isVisible,
    menuPos,
    activeFormats,
    executeCommand,
  };
}
