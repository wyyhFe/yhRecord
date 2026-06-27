import { RangeSet } from "@codemirror/state";
import { Decoration, type DecorationSet, EditorView, ViewPlugin, ViewUpdate } from "@codemirror/view";

/**
 * 空行 Slash 提示装饰
 * 当光标在空行时，在该行显示一个灰色的 "/" 提示
 */
const lineHint = Decoration.line({ class: "cm-slash-hint-line" });

function buildDecorations(view: EditorView): DecorationSet {
  const { state } = view;
  if (!view.hasFocus) {
    return RangeSet.empty;
  }

  const cursor = state.selection.main.head;
  const line = state.doc.lineAt(cursor);

  // 只在完全空的行显示提示
  if (line.text.trim() !== "") {
    return RangeSet.empty;
  }

  const decorations: { from: number; to: number; value: Decoration }[] = [];

  decorations.push({
    from: line.from,
    to: line.from,
    value: lineHint,
  });

  return RangeSet.of(decorations, true);
}

const slashHintPlugin = ViewPlugin.fromClass(
  class {
    decorations: DecorationSet;
    constructor(view: EditorView) {
      this.decorations = buildDecorations(view);
    }
    update(update: ViewUpdate) {
      if (update.docChanged || update.selectionSet || update.viewportChanged) {
        this.decorations = buildDecorations(update.view);
      }
    }
  },
  {
    decorations: (v) => v.decorations,
  },
);

export const slashHintExtension = [slashHintPlugin];
