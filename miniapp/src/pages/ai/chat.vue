<template>
  <view class="page-shell-safe ai-chat-page">
    <view class="page-head">
      <view class="section-head">
        <view class="section-copy">
          <view class="page-head__eyebrow">AI Assistant</view>
          <view class="page-head__title">AI Chat</view>
          <view class="page-head__desc">
            Streamed replies are enabled. Ask a question directly or request a recent spending summary.
          </view>
        </view>
        <u-tag :text="streaming ? 'Streaming' : 'Ready'" type="warning" plain shape="circle" />
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">Conversation</view>
          <view class="section-copy__desc">{{ conversationId }}</view>
        </view>
        <u-button size="mini" type="primary" plain @tap="resetConversation">New Chat</u-button>
      </view>

      <view v-if="messages.length" class="message-list">
        <view
          v-for="item in messages"
          :key="item.id"
          class="message-card"
          :class="item.role === 'user' ? 'message-card--user' : 'message-card--assistant'"
        >
          <view class="message-card__meta">
            {{ item.role === 'user' ? 'You' : 'AI' }}
          </view>
          <view class="message-card__content">{{ item.content }}</view>
        </view>
      </view>

      <view v-else class="empty-state">
        Start with something like: summarize my recent spending habits.
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">Message</view>
          <view class="section-copy__desc">Short questions usually stream more smoothly.</view>
        </view>
      </view>

      <textarea
        v-model="draft"
        class="composer"
        maxlength="500"
        placeholder="Type your message"
      />

      <view class="composer-actions">
        <u-button :disabled="streaming" type="info" plain @tap="fillLedgerPrompt">Ledger Prompt</u-button>
        <u-button :disabled="streaming || !draft.trim()" type="warning" @tap="sendMessage">Send</u-button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { streamAiChat } from '@/api/ai'

type ChatRole = 'user' | 'assistant'

interface ChatMessage {
  id: string
  role: ChatRole
  content: string
}

const conversationId = ref(`chat-${Date.now()}`)
const draft = ref('')
const streaming = ref(false)
const messages = ref<ChatMessage[]>([])
const bookId = ref('')
const bookName = ref('')

let streamController: { close: () => void } | null = null

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

function sendMessage() {
  const message = draft.value.trim()
  if (!message || streaming.value) {
    return
  }

  appendMessage('user', message)
  appendMessage('assistant', '')
  draft.value = ''
  streaming.value = true

  streamController = streamAiChat({
    conversationId: conversationId.value,
    message,
    onMessage(chunk) {
      updateLastAssistantMessage(chunk)
    },
    onDone() {
      closeStream()
    },
    onError(errorMessage) {
      const lastMessage = messages.value.at(-1)
      const text = `Request failed: ${errorMessage}`
      if (lastMessage?.role === 'assistant' && !lastMessage.content.trim()) {
        lastMessage.content = text
      } else {
        appendMessage('assistant', text)
      }
      closeStream()
    }
  })
}

function resetConversation() {
  closeStream()
  conversationId.value = `chat-${Date.now()}`
  messages.value = []
  draft.value = buildInitialPrompt()
}

function fillLedgerPrompt() {
  draft.value = buildInitialPrompt()
}

function buildInitialPrompt() {
  if (bookId.value) {
    const name = bookName.value || 'this ledger'
    return `Analyze recent records in ${name}, summarize spending habits, and point out the category that needs the most attention.`
  }
  return 'Summarize my recent spending habits and point out the category that needs the most attention.'
}

onLoad((query) => {
  if (query?.bookId) {
    bookId.value = String(query.bookId)
  }
  if (query?.bookName) {
    bookName.value = decodeURIComponent(String(query.bookName))
  }
  draft.value = buildInitialPrompt()
})

onUnload(() => {
  closeStream()
})
</script>

<style scoped lang="scss">
.ai-chat-page {
  padding-bottom: 40rpx;
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

.composer-actions {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 20rpx;
}
</style>
