<script setup lang="ts">
const emit = defineEmits<{ close: [] }>()

const dialogEl = ref<HTMLElement | null>(null)
const dragging = ref(false)
const offsetX = ref(0)
const offsetY = ref(0)
let startX = 0
let startY = 0

function onPointerDown(e: PointerEvent) {
  dragging.value = true
  startX = e.clientX - offsetX.value
  startY = e.clientY - offsetY.value
  // 防止拖拽时选中文字
  ;(e.target as HTMLElement).setPointerCapture(e.pointerId)
}

function onPointerMove(e: PointerEvent) {
  if (!dragging.value) return
  offsetX.value = e.clientX - startX
  offsetY.value = e.clientY - startY
}

function onPointerUp() {
  dragging.value = false
}

function onBackdropClick(e: MouseEvent) {
  if (e.target === e.currentTarget) emit('close')
}

const providers = [
  {
    id: 'github',
    label: 'GitHub',
    icon: 'i-ri-github-fill',
    color: 'hover:text-gray-900 dark:hover:text-white',
  },
  {
    id: 'google',
    label: 'Google',
    icon: 'i-ri-google-fill',
    color: 'hover:text-blue-500',
  },
]

function handleOAuth(provider: string) {
  window.location.href = `/api/auth/${provider}/authorize`
}
</script>

<template>
  <!-- 全屏遮罩 -->
  <div
    class="fixed inset-0 z-[100] bg-black/30 backdrop-blur-sm flex items-center justify-center"
    @click="onBackdropClick"
  >
    <!-- 可拖动弹窗 -->
    <div
      ref="dialogEl"
      class="w-[360px] rounded-2xl bg-white dark:bg-gray-900 shadow-2xl shadow-black/20 overflow-hidden"
      :class="dragging ? 'cursor-grabbing select-none' : ''"
      :style="{ transform: `translate(${offsetX}px, ${offsetY}px)` }"
    >
      <!-- 标题栏（拖拽手柄） -->
      <div
        class="flex items-center justify-between px-5 py-3.5 border-b border-gray-100 dark:border-gray-800 cursor-grab active:cursor-grabbing select-none"
        @pointerdown="onPointerDown"
        @pointermove="onPointerMove"
        @pointerup="onPointerUp"
        @pointerleave="onPointerUp"
      >
        <span class="text-sm font-medium text-gray-700 dark:text-gray-300"
          >登录生活记录</span
        >
        <button
          class="w-7 h-7 flex items-center justify-center rounded-lg text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          @click="emit('close')"
        >
          <svg
            width="14"
            height="14"
            viewBox="0 0 14 14"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M1 1l12 12M13 1L1 13" />
          </svg>
        </button>
      </div>

      <!-- 登录选项 -->
      <div class="p-5 space-y-3">
        <!-- 微信登录 -->
        <button
          class="w-full flex items-center gap-3 py-3 px-4 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 hover:bg-green-50 dark:hover:bg-green-900/20 hover:border-green-300 transition-colors text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-green-600"
          @click="handleOAuth('wechat')"
        >
          <span class="text-xl">💬</span>
          微信登录
        </button>

        <!-- GitHub / Google -->
        <button
          v-for="p in providers"
          :key="p.id"
          class="w-full flex items-center gap-3 py-3 px-4 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 hover:bg-gray-100 dark:hover:bg-gray-700/80 transition-colors text-sm font-medium text-gray-700 dark:text-gray-300"
          :class="p.color"
          @click="handleOAuth(p.id)"
        >
          <Icon :name="p.icon" class="w-5 h-5" />
          {{ p.label }} 登录
        </button>
      </div>
    </div>
  </div>
</template>
