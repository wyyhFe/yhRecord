<script setup lang="ts">
definePageMeta({ layout: 'sidebar' })

const route = useRoute()
const slug = computed(() => route.params.slug as string)

interface Post {
  id: string
  title: string
  slug: string
  htmlContent: string
  markdownContent: string
  summary: string
  category: string
  tags: string[]
  viewCount: number
  commentCount: number
  authorNickname: string
  authorAvatar: string
  publishedAt: string
  createdAt: string
}

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

const post = ref<Post | null>(null)
const comments = ref<Comment[]>([])
const loading = ref(true)
const error = ref('')

const newComment = ref('')
const replyTo = ref<{ id: string; nickname: string } | null>(null)
const submitting = ref(false)

useHead({
  title: computed(() => post.value?.title || '文章详情'),
})

async function fetchPost() {
  loading.value = true
  error.value = ''
  try {
    post.value = await $http.get<Post>(`/blog/public/posts/slug/${slug.value}`)
    // 记录浏览
    $http.post(`/blog/public/posts/${post.value.id}/view`).catch(() => {})
    // 加载评论
    fetchComments()
  } catch (e: any) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  if (!post.value) return
  try {
    comments.value = await $http.get<Comment[]>('/blog/comments', {
      targetType: 'BLOG_POST',
      targetId: post.value.id,
    })
  } catch {
    // 评论加载失败不影响正文展示
  }
}

async function submitComment() {
  if (!newComment.value.trim() || !post.value) return
  submitting.value = true
  try {
    await $http.post('/blog/comments', {
      targetType: 'BLOG_POST',
      targetId: post.value.id,
      parentId: replyTo.value?.id || undefined,
      content: newComment.value.trim(),
      platform: 'WEB',
    })
    newComment.value = ''
    replyTo.value = null
    fetchComments()
  } catch (e: any) {
    alert(e.message || '评论失败，请先登录')
  } finally {
    submitting.value = false
  }
}

function reply(comment: Comment) {
  replyTo.value = { id: comment.id, nickname: comment.userNickname }
  newComment.value = ''
  // 滚动到输入框
  document.getElementById('comment-input')?.focus()
}

function cancelReply() {
  replyTo.value = null
  newComment.value = ''
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(fetchPost)
watch(slug, fetchPost)
</script>

<template>
  <div class="max-w-3xl mx-auto px-6 py-10">
    <!-- 加载 -->
    <div v-if="loading" class="animate-pulse space-y-4">
      <div class="h-8 bg-gray-200 dark:bg-gray-700 rounded w-2/3"></div>
      <div class="h-4 bg-gray-100 dark:bg-gray-800 rounded w-1/3"></div>
      <div class="space-y-3 mt-8">
        <div
          v-for="i in 10"
          :key="i"
          class="h-4 bg-gray-100 dark:bg-gray-800 rounded w-full"
        ></div>
      </div>
    </div>

    <!-- 错误 -->
    <div v-else-if="error" class="text-center py-20">
      <p class="text-gray-400 text-lg">{{ error }}</p>
      <NuxtLink
        to="/posts"
        class="mt-4 inline-block text-blue-600 hover:underline"
        >← 返回文章列表</NuxtLink
      >
    </div>

    <!-- 正文 -->
    <template v-else-if="post">
      <article>
        <!-- 头部 -->
        <header class="mb-8">
          <NuxtLink
            to="/posts"
            class="text-sm text-gray-400 hover:text-blue-600 transition mb-4 inline-block"
          >
            ← 文章列表
          </NuxtLink>
          <h1
            class="text-3xl font-bold text-gray-900 dark:text-gray-100 mt-2 mb-4"
          >
            {{ post.title }}
          </h1>
          <div class="flex items-center gap-3 text-sm text-gray-400">
            <span
              v-if="post.category"
              class="px-2 py-0.5 bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded text-xs"
            >
              {{ post.category }}
            </span>
            <span>{{ post.authorNickname }}</span>
            <span>{{ formatDate(post.publishedAt || post.createdAt) }}</span>
            <span>{{ post.viewCount || 0 }} 阅读</span>
            <span v-if="post.commentCount">{{ post.commentCount }} 评论</span>
          </div>
          <div v-if="post.tags?.length" class="flex gap-2 mt-3">
            <span
              v-for="tag in post.tags"
              :key="tag"
              class="text-xs text-gray-400"
              >#{{ tag }}</span
            >
          </div>
        </header>

        <!-- HTML 正文 -->
        <div
          class="prose prose-gray dark:prose-invert max-w-none"
          v-html="post.htmlContent"
        />
      </article>

      <!-- 评论区 -->
      <div class="mt-16 pt-8 border-t border-gray-200 dark:border-gray-700">
        <h3 class="text-lg font-semibold mb-6 text-gray-900 dark:text-gray-100">
          评论 ({{ comments.length }})
        </h3>

        <!-- 评论输入 -->
        <div class="mb-8">
          <div v-if="replyTo" class="text-sm text-gray-400 mb-2">
            回复 <span class="text-blue-600">@{{ replyTo.nickname }}</span>
            <button
              class="ml-2 text-gray-400 hover:text-gray-600"
              @click="cancelReply"
            >
              取消
            </button>
          </div>
          <textarea
            id="comment-input"
            v-model="newComment"
            rows="3"
            class="w-full px-4 py-3 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100 text-sm resize-none focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
            placeholder="写下你的评论..."
          />
          <div class="flex justify-end mt-2">
            <button
              class="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 transition disabled:opacity-50"
              :disabled="!newComment.trim() || submitting"
              @click="submitComment"
            >
              {{ submitting ? '提交中...' : '发表评论' }}
            </button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div
          v-if="comments.length === 0"
          class="text-center py-10 text-gray-400 text-sm"
        >
          暂无评论，来说两句吧
        </div>
        <div v-else class="space-y-6">
          <CommentItem
            v-for="comment in comments"
            :key="comment.id"
            :comment="comment"
            @reply="reply"
          />
        </div>
      </div>
    </template>
  </div>
</template>
