<template>
  <view class="page-shell-safe ai-chat-page">
    <view class="page-head ai-chat-head">
      <view class="page-head__eyebrow">AI 聊天</view>
      <view class="page-head__title">基础会话</view>
      <view class="page-head__desc">这里只保留最基础的流式聊天和会话管理，方便继续学习后端实现。</view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">会话列表</view>
          <view class="section-copy__desc">支持新建、切换和删除当前会话。</view>
        </view>
        <view class="toolbar-actions">
          <u-button size="mini" type="info" plain @click="startNewConversation">新建</u-button>
          <u-button size="mini" type="warning" plain :disabled="!activeConversationId" @click="removeCurrentConversation">
            删除
          </u-button>
        </view>
      </view>

      <scroll-view scroll-x class="session-strip">
        <view class="session-strip__inner">
          <view
            v-for="item in conversations"
            :key="item.id"
            class="session-card"
            :class="{ 'session-card--active': item.id === activeConversationId }"
            @tap="selectConversation(item.id)"
          >
            <view class="session-card__title">{{ item.title }}</view>
            <view class="session-card__desc">{{ item.lastMessagePreview || '暂无消息' }}</view>
            <view class="session-card__meta">{{ formatTime(item.updatedAt) }}</view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">聊天窗口</view>
          <view class="section-copy__desc">
            {{ activeConversation ? `当前会话：${activeConversation.title}` : '正在准备会话...' }}
          </view>
        </view>
      </view>

      <view v-if="booting || loadingMessages" class="note-card">正在加载聊天内容...</view>

      <view v-else-if="activeMessages.length" class="message-list">
        <view
          v-for="item in activeMessages"
          :key="item.id"
          class="message-row"
          :class="item.role === 'user' ? 'message-row--user' : 'message-row--assistant'"
        >
          <view class="message-card" :class="item.role === 'user' ? 'message-card--user' : 'message-card--assistant'">
            <view class="message-card__meta">{{ item.role === 'user' ? '我' : 'AI' }}</view>
            <view class="message-card__content">{{ item.content }}</view>
            <view class="message-card__time">{{ formatTime(item.createdAt) }}</view>
          </view>
        </view>
      </view>

      <view v-else class="note-card">这里会显示完整聊天记录。先输入一个问题开始会话。</view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">发送消息</view>
          <view class="section-copy__desc">当前页面只走一个流式聊天接口。</view>
        </view>
      </view>

      <textarea
        v-model="draft"
        class="composer"
        maxlength="1000"
        confirm-type="send"
        placeholder="请输入问题，例如：请解释一下 Spring AI 的 Flux 流式输出是怎么工作的。"
        @confirm="sendMessage"
      ></textarea>
      <view class="composer-actions">
        <u-button type="info" plain :disabled="sending" @click="fillExamplePrompt">示例问题</u-button>
        <u-button type="warning" :disabled="sendDisabled" @click="sendMessage">发送</u-button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import {
  createAiConversation,
  deleteAiConversation,
  fetchAiConversationMessages,
  fetchAiConversations,
  streamAiChat,
  type AiConversationMessage,
  type AiConversationSummary
} from '@/api/ai'

type StreamController = { close: () => void }

const booting = ref(true)
const loadingMessages = ref(false)
const sending = ref(false)
const conversations = ref<AiConversationSummary[]>([])
const messageMap = ref<Record<string, AiConversationMessage[]>>({})
const activeConversationId = ref('')
const draft = ref('')

let streamController: StreamController | null = null

const activeConversation = computed(() => conversations.value.find((item) => item.id === activeConversationId.value))
const activeMessages = computed(() => messageMap.value[activeConversationId.value] || [])
const sendDisabled = computed(() => sending.value || !draft.value.trim())

function nowText() {
  return new Date().toISOString()
}

function formatTime(value?: string) {
  if (!value) {
    return ''
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('zh-CN', { hour12: false })
}

function closeStream() {
  streamController?.close()
  streamController = null
  sending.value = false
}

function createMessage(conversationId: string, role: AiConversationMessage['role'], content: string): AiConversationMessage {
  return {
    id: `${conversationId}-${role}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    conversationId,
    role,
    content,
    createdAt: nowText()
  }
}

function setConversationMessages(conversationId: string, items: AiConversationMessage[]) {
  messageMap.value = {
    ...messageMap.value,
    [conversationId]: items
  }
}

function appendMessage(conversationId: string, message: AiConversationMessage) {
  setConversationMessages(conversationId, [...(messageMap.value[conversationId] || []), message])
}

function appendPendingTurn(conversationId: string, message: string) {
  appendMessage(conversationId, createMessage(conversationId, 'user', message))
  appendMessage(conversationId, createMessage(conversationId, 'assistant', ''))
  updateConversationPreview(conversationId, message)
}

function appendAssistantChunk(conversationId: string, chunk: string) {
  const items = [...(messageMap.value[conversationId] || [])]
  for (let index = items.length - 1; index >= 0; index -= 1) {
    if (items[index].role === 'assistant') {
      items[index] = {
        ...items[index],
        content: `${items[index].content || ''}${chunk}`
      }
      setConversationMessages(conversationId, items)
      return
    }
  }
}

function updateLastAssistant(conversationId: string, content: string) {
  const items = [...(messageMap.value[conversationId] || [])]
  for (let index = items.length - 1; index >= 0; index -= 1) {
    if (items[index].role === 'assistant') {
      items[index] = {
        ...items[index],
        content,
        createdAt: nowText()
      }
      setConversationMessages(conversationId, items)
      return
    }
  }
}

function getLastAssistantContent(conversationId: string) {
  const items = messageMap.value[conversationId] || []
  for (let index = items.length - 1; index >= 0; index -= 1) {
    if (items[index].role === 'assistant') {
      return items[index].content
    }
  }
  return ''
}

function updateConversationPreview(conversationId: string, preview: string) {
  conversations.value = conversations.value.map((item) =>
    item.id === conversationId
      ? {
          ...item,
          lastMessagePreview: preview,
          updatedAt: nowText(),
          messageCount: (messageMap.value[conversationId] || []).length
        }
      : item
  )
}

async function loadConversations(preferredConversationId?: string) {
  conversations.value = await fetchAiConversations()

  if (!conversations.value.length) {
    const conversation = await createAiConversation({ title: '新会话' })
    conversations.value = [conversation]
  }

  const targetId = preferredConversationId || activeConversationId.value || conversations.value[0]?.id
  if (targetId) {
    await selectConversation(targetId)
  }
}

async function ensureConversation() {
  if (activeConversationId.value) {
    return activeConversationId.value
  }

  const conversation = await createAiConversation({ title: '新会话' })
  conversations.value = [conversation, ...conversations.value]
  activeConversationId.value = conversation.id
  setConversationMessages(conversation.id, [])
  return conversation.id
}

async function selectConversation(conversationId: string) {
  if (!conversationId) {
    return
  }

  if (sending.value) {
    closeStream()
  }

  activeConversationId.value = conversationId
  if (messageMap.value[conversationId]?.length) {
    return
  }

  loadingMessages.value = true
  try {
    const result = await fetchAiConversationMessages(conversationId)
    setConversationMessages(conversationId, result)
  } finally {
    loadingMessages.value = false
  }
}

async function startNewConversation() {
  closeStream()
  const conversation = await createAiConversation({ title: '新会话' })
  conversations.value = [conversation, ...conversations.value]
  activeConversationId.value = conversation.id
  setConversationMessages(conversation.id, [])
}

function fillExamplePrompt() {
  draft.value = '请用通俗的话解释一下 Spring AI 里 Flux 流式输出和普通请求的区别。'
}

function startStream(conversationId: string, message: string) {
  sending.value = true
  streamController = streamAiChat({
    conversationId,
    message,
    onMessage(chunk) {
      appendAssistantChunk(conversationId, chunk)
    },
    async onDone() {
      const reply = getLastAssistantContent(conversationId) || '本次没有返回内容'
      updateConversationPreview(conversationId, reply)
      closeStream()
      await loadConversations(conversationId)
    },
    onError(errorMessage) {
      updateLastAssistant(conversationId, errorMessage || '发送失败，请稍后重试')
      updateConversationPreview(conversationId, errorMessage || message)
      closeStream()
    }
  })
}

async function sendMessage() {
  const message = draft.value.trim()
  if (!message || sending.value) {
    return
  }

  try {
    const conversationId = await ensureConversation()
    appendPendingTurn(conversationId, message)
    draft.value = ''
    startStream(conversationId, message)
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '发送失败', icon: 'none' })
  }
}

function removeCurrentConversation() {
  if (!activeConversationId.value) {
    return
  }

  const conversationId = activeConversationId.value
  uni.showModal({
    title: '删除会话',
    content: '确认删除当前会话吗？',
    success: async (result) => {
      if (!result.confirm) {
        return
      }

      closeStream()
      await deleteAiConversation(conversationId)
      conversations.value = conversations.value.filter((item) => item.id !== conversationId)
      const nextMap = { ...messageMap.value }
      delete nextMap[conversationId]
      messageMap.value = nextMap
      activeConversationId.value = ''

      if (conversations.value.length) {
        await selectConversation(conversations.value[0].id)
        return
      }

      await startNewConversation()
    }
  })
}

async function bootstrap(scene?: string) {
  booting.value = true
  try {
    if (scene) {
      draft.value = decodeURIComponent(scene)
    }
    await loadConversations()
  } finally {
    booting.value = false
  }
}

onLoad((query) => {
  bootstrap(query?.scene ? String(query.scene) : '').catch((error) => {
    booting.value = false
    uni.showToast({
      title: error?.message || 'AI 页面加载失败',
      icon: 'none'
    })
  })
})

onUnload(() => {
  closeStream()
})
</script>

<style scoped lang="scss">
.ai-chat-page {
  padding-bottom: 40rpx;
}

.ai-chat-head {
  background:
    radial-gradient(circle at top right, var(--color-primary-soft), transparent 34%),
    linear-gradient(135deg, var(--color-surface) 0%, var(--color-surface) 100%);
}

.page-head__eyebrow {
  color: var(--color-primary-strong);
  font-size: 24rpx;
  font-weight: 700;
}

.page-head__title {
  margin-top: 12rpx;
  color: var(--color-text-primary);
  font-size: 44rpx;
  font-weight: 700;
}

.page-head__desc {
  margin-top: 14rpx;
  color: var(--color-text-secondary);
  font-size: 24rpx;
  line-height: 1.7;
}

.toolbar-actions {
  display: flex;
  gap: 12rpx;
}

.session-strip {
  margin-top: 18rpx;
  white-space: nowrap;
}

.session-strip__inner {
  display: inline-flex;
  gap: 16rpx;
  padding-bottom: 4rpx;
}

.session-card {
  width: 280rpx;
  min-height: 172rpx;
  padding: 22rpx;
  border-radius: 24rpx;
  background: var(--color-surface);
  border: 1rpx solid var(--color-border);
  box-sizing: border-box;
}

.session-card--active {
  border-color: rgba(196, 124, 82, 0.36);
  box-shadow: 0 16rpx 32rpx var(--shadow-card);
}

.session-card__title {
  color: var(--color-text-primary);
  font-size: 28rpx;
  font-weight: 700;
  white-space: normal;
}

.session-card__desc {
  margin-top: 12rpx;
  color: var(--color-text-secondary);
  font-size: 22rpx;
  line-height: 1.6;
  white-space: normal;
}

.session-card__meta {
  margin-top: 14rpx;
  color: var(--color-primary-strong);
  font-size: 20rpx;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.message-row {
  display: flex;
}

.message-row--assistant {
  justify-content: flex-start;
}

.message-row--user {
  justify-content: flex-end;
}

.message-card {
  display: inline-block;
  width: fit-content;
  max-width: 72%;
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  border: 1rpx solid var(--color-border-strong);
  box-sizing: border-box;
}

.message-card--user {
  background: linear-gradient(180deg, var(--color-surface-soft) 0%, var(--color-primary-soft) 100%);
}

.message-card--assistant {
  background: linear-gradient(180deg, var(--color-surface) 0%, var(--color-surface-soft) 100%);
}

.message-card__meta {
  color: var(--color-primary-strong);
  font-size: 22rpx;
  font-weight: 700;
}

.message-card__content {
  margin-top: 12rpx;
  color: var(--color-text-primary);
  font-size: 28rpx;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-card__time {
  margin-top: 12rpx;
  color: var(--color-text-muted);
  font-size: 20rpx;
}

.composer {
  width: 100%;
  min-height: 220rpx;
  margin-top: 18rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: var(--color-surface);
  border: 1rpx solid var(--color-border-strong);
  box-sizing: border-box;
  /* 用户输入内容 → 中性色，不带主题色调 */
  color: var(--color-text-neutral);
  font-size: 28rpx;
  line-height: 1.7;
}

.composer-actions {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 20rpx;
}
</style>
