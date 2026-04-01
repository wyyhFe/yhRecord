<template>
  <view class="knowledge-chat-panel">
    <view class="page-head">
      <view class="section-head">
        <view class="section-copy">
          <view class="page-head__eyebrow">{{ eyebrow }}</view>
          <view class="page-head__title">{{ title }}</view>
          <view class="page-head__desc">{{ description }}</view>
        </view>
        <u-tag :text="streaming ? '回答中' : '就绪'" type="warning" plain shape="circle" />
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">知识库配置</view>
          <view class="section-copy__desc">支持传入知识库 ID；如果没绑定知识库，会明确提示，避免假装可用。</view>
        </view>
        <u-button size="mini" type="primary" plain @tap="resetConversation">新会话</u-button>
      </view>

      <view class="meta-grid">
        <view class="meta-card">
          <view class="meta-card__label">知识库</view>
          <view class="meta-card__value">{{ resolvedKnowledgeBaseName }}</view>
        </view>
        <view class="meta-card">
          <view class="meta-card__label">会话 ID</view>
          <view class="meta-card__value meta-card__value--small">{{ conversationId }}</view>
        </view>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">对话窗口</view>
          <view class="section-copy__desc">{{ chatHint }}</view>
        </view>
      </view>

      <view v-if="messages.length" class="message-list">
        <view
          v-for="item in messages"
          :key="item.id"
          class="message-card"
          :class="item.role === 'user' ? 'message-card--user' : 'message-card--assistant'"
        >
          <view class="message-card__meta">
            {{ item.role === 'user' ? userLabel : assistantLabel }}
          </view>
          <view class="message-card__content">{{ item.content }}</view>
        </view>
      </view>

      <view v-else class="empty-state">
        {{ emptyText }}
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">提问</view>
          <view class="section-copy__desc">{{ composerHint }}</view>
        </view>
      </view>

      <textarea
        v-model="draft"
        class="composer"
        maxlength="800"
        :placeholder="placeholder"
      />

      <view class="prompt-actions">
        <u-button :disabled="streaming" size="mini" type="info" plain @tap="fillTemplatePrompt">
          模板问题
        </u-button>
        <u-button :disabled="streaming || !draft.trim()" type="warning" @tap="sendMessage">
          发送
        </u-button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onUnmounted } from 'vue'
import { createKnowledgeBaseChat, streamKnowledgeBaseChat } from '@/api/ai'

type ChatRole = 'user' | 'assistant'

interface ChatMessage {
  id: string
  role: ChatRole
  content: string
}

const props = withDefaults(defineProps<{
  knowledgeBaseId?: string
  knowledgeBaseName?: string
  eyebrow?: string
  title?: string
  description?: string
  assistantLabel?: string
  userLabel?: string
  emptyText?: string
  placeholder?: string
  initialPrompt?: string
  templatePrompt?: string
}>(), {
  knowledgeBaseId: '',
  knowledgeBaseName: '',
  eyebrow: 'AI 知识库',
  title: '知识库聊天',
  description: '面向独立知识库的问答窗口，适合作为 AI 模块里的单独能力。',
  assistantLabel: '知识库助手',
  userLabel: '我',
  emptyText: '先问一个和知识库内容相关的问题，比如：这个系统的记账规则是什么？',
  placeholder: '请输入你的问题',
  initialPrompt: '',
  templatePrompt: '请基于当前知识库内容，概括核心主题，并告诉我最值得先阅读的部分。'
})

const emit = defineEmits<{
  sent: [message: string]
  reset: []
  error: [message: string]
}>()

const conversationId = ref(`kb-chat-${Date.now()}`)
const draft = ref(props.initialPrompt)
const streaming = ref(false)
const messages = ref<ChatMessage[]>([])

let streamController: { close: () => void } | null = null

const resolvedKnowledgeBaseName = computed(() => props.knowledgeBaseName || props.knowledgeBaseId || '未指定')
const chatHint = computed(() => (
  props.knowledgeBaseId
    ? `当前会将问题附带到知识库 ${resolvedKnowledgeBaseName.value}。`
    : '当前还没有绑定知识库，页面能打开，但不会伪装成可正常问答。'
))
const composerHint = computed(() => (
  props.knowledgeBaseId
    ? '建议提问具体一点，便于知识库召回更稳定。'
    : '请先传入知识库 ID，再进行知识库问答。'
))

function appendMessage(role: ChatRole, content: string) {
  messages.value.push({
    id: `${role}-${Date.now()}-${messages.value.length}`,
    role,
    content
  })
}

function updateLastAssistantMessage(chunk: string) {
  const lastMessage = messages.value.at(-1)
  if (!lastMessage || lastMessage.role !== 'assistant') {
    appendMessage('assistant', chunk)
    return
  }

  lastMessage.content += chunk
}

function closeStream() {
  streamController?.close()
  streamController = null
  streaming.value = false
}

function fillTemplatePrompt() {
  draft.value = props.templatePrompt
}

function resetConversation() {
  closeStream()
  conversationId.value = `kb-chat-${Date.now()}`
  messages.value = []
  draft.value = props.initialPrompt
  emit('reset')
}

async function sendByRequest(message: string) {
  try {
    const result = await createKnowledgeBaseChat({
      knowledgeBaseId: props.knowledgeBaseId || undefined,
      conversationId: conversationId.value,
      message
    })
    conversationId.value = result.conversationId || conversationId.value
    updateLastAssistantMessage(result.reply || '已收到，但后端没有返回内容。')
  } catch (error) {
    const messageText = error instanceof Error ? error.message : '发送失败，请稍后重试'
    const content = `请求失败：${messageText}`
    const lastMessage = messages.value.at(-1)
    if (lastMessage?.role === 'assistant' && !lastMessage.content.trim()) {
      lastMessage.content = content
    } else {
      appendMessage('assistant', content)
    }
    emit('error', messageText)
  } finally {
    closeStream()
  }
}

function sendMessage() {
  const message = draft.value.trim()
  if (!message || streaming.value) {
    return
  }

  if (!props.knowledgeBaseId) {
    const content = '当前还没有绑定知识库，暂时不能发起知识库问答。请先选择或传入知识库 ID。'
    appendMessage('assistant', content)
    emit('error', content)
    return
  }

  appendMessage('user', message)
  appendMessage('assistant', '')
  draft.value = ''
  streaming.value = true
  emit('sent', message)

  streamController = streamKnowledgeBaseChat({
    knowledgeBaseId: props.knowledgeBaseId || undefined,
    conversationId: conversationId.value,
    message,
    onMessage(chunk) {
      updateLastAssistantMessage(chunk)
    },
    onDone() {
      closeStream()
    },
    onError(errorMessage) {
      sendByRequest(message).catch(() => undefined)
    }
  })
}

onUnmounted(() => {
  closeStream()
})
</script>

<style scoped lang="scss">
.knowledge-chat-panel {
  padding-bottom: 40rpx;
}

.meta-grid {
  margin-top: 18rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.meta-card {
  padding: 20rpx 22rpx;
  border-radius: 22rpx;
  background: #fffdf8;
  border: 1rpx solid rgba(196, 124, 82, 0.1);
}

.meta-card__label {
  color: #8a735f;
  font-size: 22rpx;
}

.meta-card__value {
  margin-top: 10rpx;
  color: #2d241c;
  font-size: 28rpx;
  font-weight: 600;
  word-break: break-word;
}

.meta-card__value--small {
  font-size: 24rpx;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.message-card {
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  border: 1rpx solid rgba(196, 124, 82, 0.12);
}

.message-card--user {
  background: linear-gradient(180deg, #fff6e9 0%, #fde7ca 100%);
}

.message-card--assistant {
  background: linear-gradient(180deg, #fffefb 0%, #f7efe5 100%);
}

.message-card__meta {
  color: #9b6b47;
  font-size: 22rpx;
  font-weight: 700;
}

.message-card__content {
  margin-top: 12rpx;
  color: #2d241c;
  font-size: 28rpx;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.empty-state {
  padding: 30rpx 0;
  color: #8a735f;
  font-size: 26rpx;
  line-height: 1.7;
}

.composer {
  width: 100%;
  min-height: 220rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: #fffdf9;
  border: 1rpx solid rgba(196, 124, 82, 0.12);
  box-sizing: border-box;
  color: #2d241c;
  font-size: 28rpx;
  line-height: 1.7;
}

.prompt-actions {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 20rpx;
}
</style>
