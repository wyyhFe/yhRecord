<template>
  <view class="page-shell-safe ai-knowledge-page">
    <knowledge-chat-panel
      :knowledge-base-id="knowledgeBaseId"
      :knowledge-base-name="knowledgeBaseName"
      :title="pageTitle"
      :description="pageDescription"
      :initial-prompt="initialPrompt"
      @error="handleError"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import KnowledgeChatPanel from '@/components/business/knowledge-chat-panel'

const knowledgeBaseId = ref('')
const knowledgeBaseName = ref('')
const scene = ref('')

const pageTitle = computed(() => knowledgeBaseName.value || '知识库聊天')
const pageDescription = computed(() => (
  knowledgeBaseId.value
    ? `当前已接入知识库 ${knowledgeBaseName.value || knowledgeBaseId.value}，可以直接进行问答。`
    : '这是独立的 AI 知识库模块页面。现在页面可以正常打开，但如果没绑定知识库，不会真的发起知识库问答。'
))
const initialPrompt = computed(() => (
  scene.value
    ? `请根据 ${scene.value} 这个场景，结合知识库给我一个准确的答案。`
    : ''
))

function handleError(errorMessage: string) {
  uni.$feedback.error(errorMessage, undefined, '知识库聊天失败')
}

onLoad((query) => {
  if (query?.knowledgeBaseId) {
    knowledgeBaseId.value = String(query.knowledgeBaseId)
  }
  if (query?.knowledgeBaseName) {
    knowledgeBaseName.value = decodeURIComponent(String(query.knowledgeBaseName))
  }
  if (query?.scene) {
    scene.value = decodeURIComponent(String(query.scene))
  }
})
</script>

<style scoped lang="scss">
.ai-knowledge-page {
  padding-bottom: 40rpx;
}
</style>
