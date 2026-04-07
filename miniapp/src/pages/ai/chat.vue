<template>
  <view class="page-shell-safe ai-chat-page">
    <view class="page-head">
      <view class="section-head">
        <view class="section-copy">
          <view class="page-head__eyebrow">AI 助手</view>
          <view class="page-head__title">智能对话</view>
          <view class="page-head__desc">当前页面仅保留流式聊天接口。</view>
        </view>
        <u-tag :text="streaming ? '回答中' : '就绪'" type="warning" plain shape="circle" />
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">当前会话</view>
          <view class="section-copy__desc">{{ conversationId }}</view>
        </view>
        <u-button size="mini" type="primary" plain @tap="resetConversation">新对话</u-button>
      </view>

      <view v-if="messages.length" class="message-list">
        <view
          v-for="item in messages"
          :key="item.id"
          class="message-card"
          :class="item.role === 'user' ? 'message-card--user' : 'message-card--assistant'"
        >
          <view class="message-card__meta">{{ item.role === 'user' ? '我' : 'AI 助手' }}</view>
          <view class="message-card__content">{{ item.content }}</view>
        </view>
      </view>

      <view v-else class="empty-state">先发一句话试试，比如：帮我总结最近的消费习惯。</view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">输入问题</view>
          <view class="section-copy__desc">问题尽量具体一点，回答通常会更稳定。</view>
        </view>
      </view>

      <textarea
        v-model="draft"
        class="composer"
        maxlength="500"
        confirm-type="send"
        placeholder="请输入你的问题"
        @confirm="sendMessage"
      />

      <view class="composer-actions">
        <u-button :disabled="streaming" type="info" plain @tap="fillLedgerPrompt">账单提问模板</u-button>
        <u-button :disabled="streaming || !draft.trim()" type="warning" @tap="sendMessage">发送</u-button>
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

function fillLedgerPrompt() {
  draft.value = buildInitialPrompt()
}

function buildInitialPrompt() {
  if (bookId.value) {
    const name = bookName.value || '这个账本'
    return `请结合 ${name} 最近的记录，帮我总结消费习惯，并指出最需要关注的支出类别。`
  }
  return '请帮我总结最近的消费习惯，并指出最需要关注的支出类别。'
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
      const content = errorMessage || '流式请求失败，请检查后端流式接口。'
      const lastMessage = messages.value.at(-1)
      if (lastMessage?.role === 'assistant' && !lastMessage.content.trim()) {
        lastMessage.content = content
      } else {
        appendMessage('assistant', content)
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
