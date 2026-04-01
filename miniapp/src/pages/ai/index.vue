<template>
  <view class="page-shell-safe ai-workbench-page">
    <view class="page-head ai-workbench-head">
      <view class="section-head">
        <view class="section-copy">
          <view class="page-head__eyebrow">AI 工作台</view>
          <view class="page-head__title">完整 AI 模块</view>
          <view class="page-head__desc">
            统一管理 Agent、会话、知识库与引用来源。即使后端接口未补齐，前端结构也能先完整跑通。
          </view>
        </view>
        <u-tag :text="moduleModeLabel" type="warning" plain shape="circle" />
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">工作模式</view>
          <view class="section-copy__desc">先选 Agent，再决定是否启用知识库增强。</view>
        </view>
      </view>

      <view class="mode-grid">
        <view class="mode-card" @tap="showAgentPopup = true">
          <view class="mode-card__label">当前 Agent</view>
          <view class="mode-card__value">{{ activeAgent?.name || '未选择' }}</view>
          <view class="mode-card__hint">{{ activeAgent?.description || '点击切换 Agent' }}</view>
        </view>

        <view class="mode-card" @tap="showKnowledgePopup = true">
          <view class="mode-card__label">知识库增强</view>
          <view class="mode-card__value">{{ activeKnowledgeBase?.name || '未启用' }}</view>
          <view class="mode-card__hint">
            {{ activeKnowledgeBase ? `${activeKnowledgeBase.documentCount} 份文档` : '点击选择知识库' }}
          </view>
        </view>

        <view class="mode-card">
          <view class="mode-card__label">会话数量</view>
          <view class="mode-card__value">{{ conversations.length }}</view>
          <view class="mode-card__hint">{{ activeConversation?.updatedAt || '尚未开始会话' }}</view>
        </view>
      </view>

      <view v-if="activeAgent?.capabilities?.length" class="capability-row">
        <view
          v-for="item in activeAgent.capabilities"
          :key="item"
          class="capability-chip"
        >
          {{ item }}
        </view>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">聊天会话</view>
          <view class="section-copy__desc">支持新建、切换和删除当前会话。</view>
        </view>
        <view class="toolbar-actions">
          <u-button size="mini" type="info" plain @tap="startNewConversation">新建</u-button>
          <u-button
            size="mini"
            type="warning"
            plain
            :disabled="!activeConversationId"
            @tap="removeCurrentConversation"
          >
            删除
          </u-button>
        </view>
      </view>

      <scroll-view scroll-x class="session-strip">
        <view class="session-strip__inner">
          <view class="session-card session-card--new" @tap="startNewConversation">
            <view class="session-card__title">+ 新会话</view>
            <view class="session-card__desc">按当前 Agent 与知识库配置创建</view>
          </view>

          <view
            v-for="item in conversations"
            :key="item.id"
            class="session-card"
            :class="{ 'session-card--active': item.id === activeConversationId }"
            @tap="selectConversation(item.id)"
          >
            <view class="session-card__title">{{ item.title }}</view>
            <view class="session-card__desc">{{ item.lastMessagePreview || '还没有消息' }}</view>
            <view class="session-card__meta">{{ item.updatedAt }}</view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">对话窗口</view>
          <view class="section-copy__desc">
            {{ activeConversation ? `当前会话：${activeConversation.title}` : '先新建一个会话，再开始提问。' }}
          </view>
        </view>
        <u-tag :text="sending ? '发送中' : '空闲'" type="info" plain shape="circle" />
      </view>

      <view v-if="booting || loadingMessages" class="note-card">正在加载 AI 工作台内容...</view>

      <view v-else-if="activeMessages.length" class="message-list">
        <view
          v-for="item in activeMessages"
          :key="item.id"
          class="message-card"
          :class="item.role === 'user' ? 'message-card--user' : 'message-card--assistant'"
        >
          <view class="message-card__meta">
            {{ item.role === 'user' ? '我' : item.role === 'assistant' ? 'AI 助手' : '系统' }}
          </view>
          <view class="message-card__content">{{ item.content }}</view>
          <view class="message-card__time">{{ item.createdAt }}</view>
        </view>
      </view>

      <view v-else class="note-card">
        这里会显示完整聊天记录。你可以直接提问，或者先在上面切换 Agent 与知识库。
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">引用来源</view>
          <view class="section-copy__desc">如果后端返回 RAG 检索结果，这里会展示命中文档与片段。</view>
        </view>
      </view>

      <view v-if="activeSources.length" class="source-list">
        <view v-for="item in activeSources" :key="sourceKey(item)" class="source-card">
          <view class="source-card__title">{{ item.title }}</view>
          <view class="source-card__snippet">{{ item.snippet }}</view>
          <view class="source-card__meta">
            {{ item.sourceType || '文档片段' }}{{ item.score ? ` · 匹配度 ${Math.round(item.score * 100)}%` : '' }}
          </view>
        </view>
      </view>

      <view v-else class="note-card">
        当前还没有引用来源。普通大模型回答或后端未返回 citations 时，这里会保持为空。
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">发送消息</view>
          <view class="section-copy__desc">支持普通对话、Agent 对话和知识库增强问答。</view>
        </view>
      </view>

      <view class="prompt-row">
        <view class="prompt-chip" @tap="applyPrompt('帮我总结这个项目当前 AI 模块还缺哪些核心能力。')">
          模块诊断
        </view>
        <view class="prompt-chip" @tap="applyPrompt('请结合当前配置，告诉我应该先做会话层还是知识库层。')">
          方案建议
        </view>
        <view class="prompt-chip" @tap="applyPrompt(defaultKnowledgePrompt)">
          知识库提问
        </view>
      </view>

      <textarea
        v-model="draft"
        class="composer"
        maxlength="1000"
        placeholder="请输入问题，例如：请帮我设计 AI 工作台需要的后端接口。"
      />

      <view class="composer-actions">
        <u-button :disabled="sending" type="info" plain @tap="fillContextPrompt">带上下文提问</u-button>
        <u-button :disabled="sending || !draft.trim()" type="warning" @tap="sendMessage">发送</u-button>
      </view>
    </view>

    <u-popup
      :model-value="showAgentPopup"
      mode="bottom"
      border-radius="36"
      @update:model-value="handleAgentPopupChange"
    >
      <view class="selector-popup">
        <view class="selector-popup__handle" />
        <view class="selector-popup__title">选择 Agent</view>
        <view class="selector-popup__desc">不同 Agent 可以代表不同系统提示词与工具配置。</view>

        <view class="selector-popup__list">
          <view
            v-for="item in agents"
            :key="item.id"
            class="selector-card"
            :class="{ 'selector-card--active': item.id === selectedAgentId }"
            @tap="selectAgent(item.id)"
          >
            <view class="selector-card__title">{{ item.name }}</view>
            <view class="selector-card__desc">{{ item.description }}</view>
          </view>
        </view>
      </view>
    </u-popup>

    <u-popup
      :model-value="showKnowledgePopup"
      mode="bottom"
      border-radius="36"
      @update:model-value="handleKnowledgePopupChange"
    >
      <view class="selector-popup">
        <view class="selector-popup__handle" />
        <view class="selector-popup__title">选择知识库</view>
        <view class="selector-popup__desc">你可以关闭知识库增强，只保留纯大模型对话。</view>

        <view class="selector-popup__list">
          <view
            class="selector-card"
            :class="{ 'selector-card--active': !selectedKnowledgeBaseId }"
            @tap="selectKnowledgeBase('')"
          >
            <view class="selector-card__title">不启用知识库</view>
            <view class="selector-card__desc">只使用 Agent 或通用模型回答。</view>
          </view>

          <view
            v-for="item in knowledgeBases"
            :key="item.id"
            class="selector-card"
            :class="{ 'selector-card--active': item.id === selectedKnowledgeBaseId }"
            @tap="selectKnowledgeBase(item.id)"
          >
            <view class="selector-card__title">{{ item.name }}</view>
            <view class="selector-card__desc">{{ item.description }}</view>
          </view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  createAgentChat,
  createAiChat,
  createAiConversation,
  createKnowledgeBaseChat,
  deleteAiConversation,
  fetchAiAgents,
  fetchAiConversationMessages,
  fetchAiConversations,
  fetchKnowledgeBases,
  type AgentChatResponse,
  type AiAgentSummary,
  type AiChatResponse,
  type AiCitation,
  type AiConversationMessage,
  type AiConversationSummary,
  type KnowledgeBaseChatResponse,
  type KnowledgeBaseSummary
} from '@/api/ai'

const booting = ref(true)
const loadingMessages = ref(false)
const sending = ref(false)
const showAgentPopup = ref(false)
const showKnowledgePopup = ref(false)
const agents = ref<AiAgentSummary[]>([])
const knowledgeBases = ref<KnowledgeBaseSummary[]>([])
const conversations = ref<AiConversationSummary[]>([])
const messageMap = ref<Record<string, AiConversationMessage[]>>({})
const selectedAgentId = ref('')
const selectedKnowledgeBaseId = ref('')
const activeConversationId = ref('')
const draft = ref('')
const scene = ref('')
const fallbackMode = ref(false)

const defaultKnowledgePrompt = '请基于当前知识库，告诉我最值得优先查看的知识点。'

const activeConversation = computed(() => (
  conversations.value.find((item) => item.id === activeConversationId.value)
))
const activeMessages = computed(() => messageMap.value[activeConversationId.value] || [])
const activeAgent = computed(() => (
  agents.value.find((item) => item.id === selectedAgentId.value) || agents.value[0]
))
const activeKnowledgeBase = computed(() => (
  knowledgeBases.value.find((item) => item.id === selectedKnowledgeBaseId.value)
))
const activeSources = computed(() => {
  const items = [...activeMessages.value].reverse()
  const target = items.find((item) => item.role === 'assistant' && item.sources?.length)
  return target?.sources || []
})
const moduleModeLabel = computed(() => (fallbackMode.value ? '演示模式' : '在线模式'))

const mockAgents: AiAgentSummary[] = [
  {
    id: 'general',
    name: '通用助手',
    description: '适合开放式问答、方案讨论和需求梳理。',
    capabilities: ['通用问答', '方案整理', '产品建议']
  },
  {
    id: 'life-record',
    name: '生活记录助手',
    description: '更偏向日记、记账、提醒与回忆场景。',
    capabilities: ['账单分析', '记录总结', '生活建议']
  }
]

const mockKnowledgeBases: KnowledgeBaseSummary[] = [
  {
    id: 'ledger-guide',
    name: '记账规则库',
    description: '沉淀账本、分类、统计口径等规则说明。',
    documentCount: 12,
    updatedAt: '今天'
  },
  {
    id: 'product-manual',
    name: '产品说明库',
    description: '覆盖页面设计、模块边界与交互约定。',
    documentCount: 18,
    updatedAt: '今天'
  }
]

function nowText() {
  return new Date().toLocaleString('zh-CN', { hour12: false })
}

function sourceKey(item: AiCitation) {
  return `${item.id || item.title}-${item.score || 0}`
}

function conversationTitle() {
  const base = activeKnowledgeBase.value?.name || activeAgent.value?.name || '新会话'
  return `${base} 对话`
}

function buildGreeting() {
  if (activeKnowledgeBase.value) {
    return `已进入 ${activeKnowledgeBase.value.name} 知识库问答。你可以直接提问，我会优先结合知识库回答。`
  }

  if (activeAgent.value) {
    return `当前使用 ${activeAgent.value.name}。你可以开始提问，我会按当前 Agent 的定位来回答。`
  }

  return '可以开始提问了。'
}

function buildFallbackReply(message: string) {
  if (activeKnowledgeBase.value) {
    return `知识库问答前端已经接通，但后端暂未返回正式结果。当前问题是：${message}。等后端补齐检索增强接口后，这里会展示真正答案和引用来源。`
  }

  if (activeAgent.value?.id && activeAgent.value.id !== 'general') {
    return `Agent 工作台前端已经就绪，但后端暂未返回 ${activeAgent.value.name} 的执行结果。当前问题已记录：${message}。`
  }

  return `普通对话前端已经就绪，但后端暂未返回正式结果。当前问题已记录：${message}。`
}

function buildFallbackSources(message: string): AiCitation[] {
  if (!activeKnowledgeBase.value) {
    return []
  }

  return [
    {
      id: `${activeKnowledgeBase.value.id}-demo`,
      title: activeKnowledgeBase.value.name,
      snippet: `当前问题：${message}。这里预留给后端返回的知识片段与引用信息。`,
      sourceType: '演示引用',
      score: 0.72
    }
  ]
}

function createLocalConversationSummary(): AiConversationSummary {
  return {
    id: `local-${Date.now()}`,
    title: conversationTitle(),
    agentId: selectedAgentId.value || null,
    knowledgeBaseId: selectedKnowledgeBaseId.value || null,
    lastMessagePreview: '新会话已创建',
    updatedAt: nowText(),
    messageCount: 1
  }
}

function createGreetingMessage(conversationId: string): AiConversationMessage {
  return {
    id: `${conversationId}-greeting`,
    conversationId,
    role: 'assistant',
    content: buildGreeting(),
    createdAt: nowText()
  }
}

function setConversationMessages(conversationId: string, items: AiConversationMessage[]) {
  messageMap.value = {
    ...messageMap.value,
    [conversationId]: items
  }
}

function upsertConversation(summary: AiConversationSummary) {
  const exists = conversations.value.some((item) => item.id === summary.id)
  const nextList = exists
    ? conversations.value.map((item) => (item.id === summary.id ? summary : item))
    : [summary, ...conversations.value]

  conversations.value = [
    nextList.find((item) => item.id === summary.id) as AiConversationSummary,
    ...nextList.filter((item) => item.id !== summary.id)
  ]
}

function replaceConversationId(oldId: string, nextId: string) {
  if (!oldId || oldId === nextId) {
    return
  }

  const target = conversations.value.find((item) => item.id === oldId)
  if (!target) {
    activeConversationId.value = nextId
    return
  }

  const nextSummary: AiConversationSummary = {
    ...target,
    id: nextId
  }

  const cachedMessages = messageMap.value[oldId] || []
  const nextMap = { ...messageMap.value }
  delete nextMap[oldId]
  nextMap[nextId] = cachedMessages.map((item) => ({
    ...item,
    conversationId: nextId
  }))

  messageMap.value = nextMap
  conversations.value = conversations.value.map((item) => (
    item.id === oldId ? nextSummary : item
  ))
  activeConversationId.value = nextId
}

function appendMessage(conversationId: string, message: AiConversationMessage) {
  const items = messageMap.value[conversationId] || []
  setConversationMessages(conversationId, [...items, message])
}

function updateLastAssistant(conversationId: string, content: string, sources: AiCitation[] = []) {
  const items = [...(messageMap.value[conversationId] || [])]
  for (let index = items.length - 1; index >= 0; index -= 1) {
    if (items[index].role === 'assistant') {
      items[index] = {
        ...items[index],
        content,
        sources
      }
      setConversationMessages(conversationId, items)
      return
    }
  }
}

function refreshConversationSnapshot(conversationId: string, preview: string) {
  const target = conversations.value.find((item) => item.id === conversationId)
  if (!target) {
    return
  }

  const messages = messageMap.value[conversationId] || []
  upsertConversation({
    ...target,
    lastMessagePreview: preview,
    updatedAt: nowText(),
    messageCount: messages.length,
    title: target.messageCount <= 1 ? `${preview.slice(0, 12) || conversationTitle()}${preview.length > 12 ? '...' : ''}` : target.title
  })
}

function fallbackToLocalData() {
  fallbackMode.value = true

  if (!agents.value.length) {
    agents.value = mockAgents
  }

  if (!knowledgeBases.value.length) {
    knowledgeBases.value = mockKnowledgeBases
  }

  if (!selectedAgentId.value) {
    selectedAgentId.value = agents.value[0]?.id || ''
  }
}

async function loadAgents() {
  try {
    const result = await fetchAiAgents()
    agents.value = result.length ? result : mockAgents
  } catch {
    agents.value = mockAgents
    fallbackMode.value = true
  }

  if (!selectedAgentId.value) {
    selectedAgentId.value = agents.value[0]?.id || ''
  }
}

async function loadKnowledgeBases() {
  try {
    const result = await fetchKnowledgeBases()
    knowledgeBases.value = result
  } catch {
    knowledgeBases.value = mockKnowledgeBases
    fallbackMode.value = true
  }
}

async function loadConversations() {
  try {
    conversations.value = await fetchAiConversations()
  } catch {
    conversations.value = []
    fallbackMode.value = true
  }
}

async function ensureConversation() {
  if (activeConversationId.value) {
    return activeConversationId.value
  }

  try {
    const result = await createAiConversation({
      title: conversationTitle(),
      agentId: selectedAgentId.value || undefined,
      knowledgeBaseId: selectedKnowledgeBaseId.value || undefined
    })
    upsertConversation(result)
    activeConversationId.value = result.id
    setConversationMessages(result.id, [createGreetingMessage(result.id)])
    return result.id
  } catch {
    fallbackToLocalData()
    const localConversation = createLocalConversationSummary()
    upsertConversation(localConversation)
    activeConversationId.value = localConversation.id
    setConversationMessages(localConversation.id, [createGreetingMessage(localConversation.id)])
    return localConversation.id
  }
}

async function selectConversation(conversationId: string) {
  activeConversationId.value = conversationId
  const target = conversations.value.find((item) => item.id === conversationId)
  if (target) {
    selectedAgentId.value = target.agentId || selectedAgentId.value
    selectedKnowledgeBaseId.value = target.knowledgeBaseId || ''
  }

  if (messageMap.value[conversationId]?.length) {
    return
  }

  loadingMessages.value = true
  try {
    const result = await fetchAiConversationMessages(conversationId)
    setConversationMessages(
      conversationId,
      result.length ? result : [createGreetingMessage(conversationId)]
    )
  } catch {
    fallbackMode.value = true
    setConversationMessages(conversationId, [createGreetingMessage(conversationId)])
  } finally {
    loadingMessages.value = false
  }
}

async function startNewConversation() {
  activeConversationId.value = ''
  await ensureConversation()
}

function selectAgent(agentId: string) {
  selectedAgentId.value = agentId
  showAgentPopup.value = false
}

function handleAgentPopupChange(value: boolean) {
  showAgentPopup.value = value
}

function selectKnowledgeBase(knowledgeBaseId: string) {
  selectedKnowledgeBaseId.value = knowledgeBaseId
  showKnowledgePopup.value = false
}

function handleKnowledgePopupChange(value: boolean) {
  showKnowledgePopup.value = value
}

function applyPrompt(value: string) {
  draft.value = value
}

function fillContextPrompt() {
  const agentName = activeAgent.value?.name || '当前 Agent'
  const knowledgeName = activeKnowledgeBase.value?.name || '未启用知识库'
  draft.value = `请基于 Agent「${agentName}」和知识库「${knowledgeName}」的当前配置，给我一个下一步实施建议。`
}

async function sendByMode(message: string, conversationId: string) {
  if (selectedKnowledgeBaseId.value) {
    return createKnowledgeBaseChat({
      knowledgeBaseId: selectedKnowledgeBaseId.value,
      conversationId,
      message
    })
  }

  if (selectedAgentId.value && selectedAgentId.value !== 'general') {
    return createAgentChat(selectedAgentId.value, {
      conversationId,
      message,
      knowledgeBaseId: selectedKnowledgeBaseId.value || undefined
    })
  }

  return createAiChat({
    conversationId,
    message
  })
}

async function sendMessage() {
  const message = draft.value.trim()
  if (!message || sending.value) {
    return
  }

  const conversationId = await ensureConversation()
  const userMessage: AiConversationMessage = {
    id: `${conversationId}-user-${Date.now()}`,
    conversationId,
    role: 'user',
    content: message,
    createdAt: nowText()
  }
  const assistantPlaceholder: AiConversationMessage = {
    id: `${conversationId}-assistant-${Date.now()}`,
    conversationId,
    role: 'assistant',
    content: '正在生成回答...',
    createdAt: nowText()
  }

  appendMessage(conversationId, userMessage)
  appendMessage(conversationId, assistantPlaceholder)
  refreshConversationSnapshot(conversationId, message)
  draft.value = ''
  sending.value = true

  try {
    const result = await sendByMode(message, conversationId) as
      | AiChatResponse
      | AgentChatResponse
      | KnowledgeBaseChatResponse
    const nextConversationId = result.conversationId || conversationId
    replaceConversationId(conversationId, nextConversationId)
    updateLastAssistant(nextConversationId, result.reply || '已收到，但后端没有返回内容。', result.sources || [])
    refreshConversationSnapshot(nextConversationId, result.reply || message)
  } catch (error) {
    fallbackMode.value = true
    const fallbackReply = buildFallbackReply(message)
    const fallbackSources = buildFallbackSources(message)
    updateLastAssistant(conversationId, fallbackReply, fallbackSources)
    refreshConversationSnapshot(conversationId, fallbackReply)
    uni.$feedback.info(error, 1800)
  } finally {
    sending.value = false
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

      try {
        await deleteAiConversation(conversationId)
      } catch {
        fallbackMode.value = true
      }

      conversations.value = conversations.value.filter((item) => item.id !== conversationId)
      const nextMap = { ...messageMap.value }
      delete nextMap[conversationId]
      messageMap.value = nextMap
      activeConversationId.value = ''

      if (conversations.value.length) {
        selectConversation(conversations.value[0].id).catch(() => undefined)
        return
      }

      startNewConversation().catch(() => undefined)
    }
  })
}

async function bootstrap(mode?: string) {
  booting.value = true
  await Promise.all([loadAgents(), loadKnowledgeBases(), loadConversations()])

  fallbackToLocalData()

  if (mode === 'knowledge' && !selectedKnowledgeBaseId.value) {
    selectedKnowledgeBaseId.value = knowledgeBases.value[0]?.id || ''
  }

  if (scene.value) {
    draft.value = `请结合“${scene.value}”这个场景，告诉我应该如何设计完整的 AI 模块。`
  }

  if (conversations.value.length) {
    await selectConversation(conversations.value[0].id)
  } else {
    await startNewConversation()
  }

  booting.value = false
}

onLoad((query) => {
  if (query?.scene) {
    scene.value = decodeURIComponent(String(query.scene))
  }

  bootstrap(query?.mode ? String(query.mode) : undefined).catch(() => {
    fallbackToLocalData()
    startNewConversation().catch(() => undefined)
    booting.value = false
  })
})
</script>

<style scoped lang="scss">
.ai-workbench-page {
  padding-bottom: 40rpx;
}

.ai-workbench-head {
  background:
    radial-gradient(circle at top right, rgba(215, 166, 72, 0.16), transparent 34%),
    linear-gradient(135deg, rgba(255, 250, 244, 0.96) 0%, rgba(252, 244, 234, 0.98) 100%);
}

.mode-grid {
  margin-top: 18rpx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.mode-card {
  padding: 20rpx 22rpx;
  border-radius: 22rpx;
  background: #fffdf8;
  border: 1rpx solid rgba(196, 124, 82, 0.1);
}

.mode-card__label {
  color: #8a735f;
  font-size: 22rpx;
}

.mode-card__value {
  margin-top: 10rpx;
  color: #2d241c;
  font-size: 28rpx;
  font-weight: 700;
}

.mode-card__hint {
  margin-top: 10rpx;
  color: #7a6a5c;
  font-size: 22rpx;
  line-height: 1.6;
}

.capability-row {
  margin-top: 18rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.capability-chip {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(196, 124, 82, 0.12);
  color: #9b6b47;
  font-size: 22rpx;
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
  background: #fffdf8;
  border: 1rpx solid rgba(196, 124, 82, 0.1);
  box-sizing: border-box;
}

.session-card--new {
  background:
    radial-gradient(circle at top right, rgba(215, 166, 72, 0.12), transparent 44%),
    linear-gradient(180deg, #fff9f1 0%, #fdf1e3 100%);
}

.session-card--active {
  border-color: rgba(196, 124, 82, 0.36);
  box-shadow: 0 16rpx 32rpx rgba(67, 41, 26, 0.08);
}

.session-card__title {
  color: #2b2118;
  font-size: 28rpx;
  font-weight: 700;
  white-space: normal;
}

.session-card__desc {
  margin-top: 12rpx;
  color: #7a6a5c;
  font-size: 22rpx;
  line-height: 1.6;
  white-space: normal;
}

.session-card__meta {
  margin-top: 14rpx;
  color: #a56d4b;
  font-size: 20rpx;
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
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-card__time {
  margin-top: 12rpx;
  color: #8a735f;
  font-size: 20rpx;
}

.source-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.source-card {
  padding: 20rpx 22rpx;
  border-radius: 22rpx;
  background: #fffdf8;
  border: 1rpx solid rgba(196, 124, 82, 0.1);
}

.source-card__title {
  color: #2b2118;
  font-size: 26rpx;
  font-weight: 700;
}

.source-card__snippet {
  margin-top: 10rpx;
  color: #4f3f31;
  font-size: 24rpx;
  line-height: 1.7;
}

.source-card__meta {
  margin-top: 10rpx;
  color: #8a735f;
  font-size: 22rpx;
}

.prompt-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.prompt-chip {
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(196, 124, 82, 0.1);
  color: #9b6b47;
  font-size: 22rpx;
}

.composer {
  width: 100%;
  min-height: 220rpx;
  margin-top: 18rpx;
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

.selector-popup {
  padding: 30rpx 28rpx 48rpx;
}

.selector-popup__handle {
  width: 88rpx;
  height: 8rpx;
  margin: 0 auto;
  border-radius: 999rpx;
  background: #e7d7c7;
}

.selector-popup__title {
  margin-top: 24rpx;
  color: #2b2118;
  font-size: 36rpx;
  font-weight: 700;
}

.selector-popup__desc {
  margin-top: 14rpx;
  color: #6b5b4e;
  font-size: 24rpx;
  line-height: 1.7;
}

.selector-popup__list {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.selector-card {
  padding: 24rpx 22rpx;
  border-radius: 24rpx;
  background: #fffdf8;
  border: 1rpx solid rgba(196, 124, 82, 0.1);
}

.selector-card--active {
  border-color: rgba(196, 124, 82, 0.36);
  background: linear-gradient(180deg, #fff8ef 0%, #fdf2e5 100%);
}

.selector-card__title {
  color: #2b2118;
  font-size: 28rpx;
  font-weight: 700;
}

.selector-card__desc {
  margin-top: 10rpx;
  color: #7a6a5c;
  font-size: 23rpx;
  line-height: 1.6;
}
</style>
