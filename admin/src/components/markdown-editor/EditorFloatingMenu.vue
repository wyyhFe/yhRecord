<script setup lang="ts">
import { computed } from "vue";
import { Bold, Italic, Strikethrough, Code } from "@element-plus/icons-vue";

type FormatType = "bold" | "italic" | "code" | "strike";

interface Props {
  visible: boolean;
  pos: { top: number; left: number };
  activeFormats: Set<FormatType>;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  pos: () => ({ top: 0, left: 0 }),
  activeFormats: () => new Set<FormatType>(),
});

const emit = defineEmits<{
  (e: "command", type: FormatType): void;
}>();

const style = computed(() => ({
  top: `${props.pos.top}px`,
  left: `${props.pos.left}px`,
  transform: "translate(-50%, -120%)",
  position: "fixed" as const,
  zIndex: 9999,
}));

const buttons = [
  { type: "bold" as const, icon: Bold, tip: "加粗" },
  { type: "italic" as const, icon: Italic, tip: "斜体" },
  { type: "strike" as const, icon: Strikethrough, tip: "删除线" },
  { type: "code" as const, icon: Code, tip: "行内代码" },
];
</script>

<template>
  <Teleport to="body">
    <Transition name="fmt-fade">
      <div
        v-if="visible"
        class="fm-menu"
        :style="style"
        @mousedown.prevent
      >
        <div class="fm-content">
          <el-button
            v-for="btn in buttons"
            :key="btn.type"
            text
            size="small"
            :class="{ 'is-active': activeFormats.has(btn.type) }"
            @click="emit('command', btn.type)"
          >
            <el-icon :size="16"><component :is="btn.icon" /></el-icon>
          </el-button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fm-menu {
  pointer-events: auto;
}

.fm-content {
  display: flex;
  align-items: center;
  padding: 4px;
  gap: 2px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border: 1px solid #ebeef5;
}

:deep(.el-button) {
  width: 32px;
  height: 32px;
  border-radius: 4px !important;
  color: #606266;
}

:deep(.el-button:hover) {
  background: #ecf5ff;
  color: #409eff;
}

:deep(.el-button.is-active) {
  background: #ecf5ff;
  color: #409eff;
}

.fmt-fade-enter-active,
.fmt-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
  transform-origin: bottom center;
}
.fmt-fade-enter-from,
.fmt-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -100%) scale(0.95);
}
</style>
