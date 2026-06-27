<script setup lang="ts">
definePageMeta({ layout: 'sidebar' })
useHead({ title: '文章' })

interface Post {
  id: string
  title: string
  slug: string
  summary: string
  category: string
  tags: string[]
  viewCount: number
  commentCount: number
  authorNickname: string
  publishedAt: string
  createdAt: string
}

const posts = ref<Post[]>([])
const loading = ref(true)
const error = ref('')
const currentPage = ref(1)
const total = ref(0)
const pageSize = 10

const selectedCategory = ref('')

const categories = [
  { label: '全部', value: '' },
  { label: '技术', value: 'tech' },
  { label: '生活', value: 'life' },
  { label: '阅读', value: 'reading' },
]

async function fetchPosts() {
  loading.value = true
  error.value = ''
  try {
    const params: Record<string, any> = {
      pageNum: currentPage.value,
      pageSize,
    }
    if (selectedCategory.value) params.category = selectedCategory.value
    const data = await $http.get<{ list: Post[]; total: number }>(
      '/blog/public/posts',
      params,
    )
    posts.value = data.list
    total.value = data.total
  } catch (e: any) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function selectCategory(val: string) {
  selectedCategory.value = val
  currentPage.value = 1
  fetchPosts()
}

function changePage(page: number) {
  currentPage.value = page
  fetchPosts()
}

const totalPages = computed(() => Math.ceil(total.value / pageSize))

onMounted(fetchPosts)

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}
</script>

<template>
  <div class="max-w-3xl mx-auto px-6 py-10">
    <h1 class="text-3xl font-bold mb-8 text-gray-900 dark:text-gray-100">
      文章
    </h1>

    <!-- 分类过滤 -->
    <div class="flex gap-2 mb-8">
      <button
        v-for="cat in categories"
        :key="cat.value"
        class="px-4 py-1.5 rounded-full text-sm transition"
        :class="
          selectedCategory === cat.value
            ? 'bg-blue-600 text-white'
            : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700'
        "
        @click="selectCategory(cat.value)"
      >
        {{ cat.label }}
      </button>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="space-y-6">
      <div v-for="i in 3" :key="i" class="animate-pulse">
        <div class="h-6 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-3"></div>
        <div class="h-4 bg-gray-100 dark:bg-gray-800 rounded w-full mb-2"></div>
        <div class="h-4 bg-gray-100 dark:bg-gray-800 rounded w-1/2"></div>
      </div>
    </div>

    <!-- 错误 -->
    <div v-else-if="error" class="text-center py-20 text-gray-400">
      <p>{{ error }}</p>
    </div>

    <!-- 空 -->
    <div v-else-if="posts.length === 0" class="text-center py-20 text-gray-400">
      <p>暂无文章</p>
    </div>

    <!-- 列表 -->
    <div v-else class="space-y-8">
      <article v-for="post in posts" :key="post.id" class="group">
        <NuxtLink :to="`/posts/${post.slug}`" class="block">
          <div class="flex items-center gap-2 text-xs text-gray-400 mb-2">
            <span
              v-if="post.category"
              class="px-2 py-0.5 bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded"
            >
              {{ post.category }}
            </span>
            <span>{{ formatDate(post.publishedAt || post.createdAt) }}</span>
            <span class="opacity-50">·</span>
            <span>{{ post.viewCount || 0 }} 次阅读</span>
          </div>
          <h2
            class="text-xl font-semibold text-gray-900 dark:text-gray-100 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition mb-2"
          >
            {{ post.title }}
          </h2>
          <p
            v-if="post.summary"
            class="text-gray-500 dark:text-gray-400 text-sm leading-relaxed line-clamp-2"
          >
            {{ post.summary }}
          </p>
          <div v-if="post.tags?.length" class="flex gap-1.5 mt-3">
            <span
              v-for="tag in post.tags"
              :key="tag"
              class="text-xs text-gray-400 dark:text-gray-500"
              >#{{ tag }}</span
            >
          </div>
        </NuxtLink>
      </article>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="flex justify-center gap-2 mt-12">
      <button
        :disabled="currentPage === 1"
        class="px-3 py-1.5 rounded text-sm border border-gray-200 dark:border-gray-700 disabled:opacity-30"
        @click="changePage(currentPage - 1)"
      >
        上一页
      </button>
      <span class="px-3 py-1.5 text-sm text-gray-500"
        >{{ currentPage }} / {{ totalPages }}</span
      >
      <button
        :disabled="currentPage >= totalPages"
        class="px-3 py-1.5 rounded text-sm border border-gray-200 dark:border-gray-700 disabled:opacity-30"
        @click="changePage(currentPage + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>
