<script setup lang="ts">
interface Comment {
  id: string
  userId: string
  userNickname: string
  userAvatar: string
  parentId: string | null
  content: string
  deviceModel: string
  platform: string
  createdAt: string
  children: Comment[]
}

const props = defineProps<{ comment: Comment }>()
const emit = defineEmits<{ reply: [comment: Comment] }>()

function formatDate(date: string) {
  const d = new Date(date)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} 小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days} 天前`
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}
</script>

<template>
  <div class="flex gap-3">
    <!-- 头像 -->
    <div
      class="flex-shrink-0 w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center text-xs text-gray-500 overflow-hidden"
    >
      <img
        v-if="comment.userAvatar"
        :src="comment.userAvatar"
        :alt="comment.userNickname"
        class="w-full h-full object-cover"
      />
      <span v-else>{{ (comment.userNickname || '?')[0] }}</span>
    </div>
    <div class="flex-1 min-w-0">
      <!-- 头部 -->
      <div class="flex items-center gap-2 mb-1">
        <span class="text-sm font-medium text-gray-900 dark:text-gray-100">{{
          comment.userNickname || '匿名用户'
        }}</span>
        <span class="text-xs text-gray-400">{{
          formatDate(comment.createdAt)
        }}</span>
        <span v-if="comment.deviceModel" class="text-xs text-gray-400">{{
          comment.deviceModel
        }}</span>
      </div>
      <!-- 内容 -->
      <p class="text-sm text-gray-700 dark:text-gray-300 leading-relaxed">
        {{ comment.content }}
      </p>
      <!-- 操作 -->
      <button
        class="text-xs text-gray-400 hover:text-blue-600 transition mt-1"
        @click="emit('reply', comment)"
      >
        回复
      </button>

      <!-- 子评论 -->
      <div
        v-if="comment.children?.length"
        class="mt-3 space-y-3 pl-4 border-l-2 border-gray-100 dark:border-gray-800"
      >
        <CommentItem
          v-for="child in comment.children"
          :key="child.id"
          :comment="child"
          @reply="emit('reply', $event)"
        />
      </div>
    </div>
  </div>
</template>
