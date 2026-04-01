import { API_BASE_URL } from '@/config/app'
import { request } from '@/utils/request'
import { tokenStorage } from '@/utils/storage'

export interface AiChatRequest {
  conversationId?: string
  message: string
}

export interface AiChatResponse {
  conversationId: string
  reply: string
  sources?: AiCitation[]
}

export interface KnowledgeBaseChatRequest {
  knowledgeBaseId?: string
  conversationId?: string
  message: string
}

export interface KnowledgeBaseChatResponse {
  conversationId: string
  reply: string
  sources?: AiCitation[]
}

export interface AgentChatRequest {
  conversationId?: string
  message: string
  knowledgeBaseId?: string
}

export interface AgentChatResponse {
  conversationId: string
  reply: string
  sources?: AiCitation[]
}

export interface AiCitation {
  id?: string
  title: string
  snippet: string
  sourceType?: string
  sourcePath?: string
  score?: number
}

export interface AiAgentSummary {
  id: string
  name: string
  description: string
  capabilities?: string[]
}

export interface KnowledgeBaseSummary {
  id: string
  name: string
  description: string
  documentCount: number
  updatedAt?: string
}

export interface AiConversationSummary {
  id: string
  title: string
  agentId?: string | null
  knowledgeBaseId?: string | null
  lastMessagePreview?: string | null
  updatedAt: string
  messageCount: number
}

export interface AiConversationMessage {
  id: string
  conversationId: string
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt: string
  sources?: AiCitation[]
}

export interface BillAnalysisHistory {
  id: string
  createdAt: string
  startDate: string
  endDate: string
  bookId?: string | number | null
  bookName?: string | null
  entryCount: number
  summary?: string | null
  question?: string | null
}

export interface BillAnalysisCategoryItem {
  name: string
  amount: number
  ratio: number
}

export interface BillAnalysisSampleItem {
  entryDate: string
  type: 'INCOME' | 'EXPENSE'
  amount: number
  bookName?: string | null
  category?: string | null
  remark?: string | null
}

export interface BillAnalysisResponse {
  startDate: string
  endDate: string
  bookId?: string | number | null
  bookName?: string | null
  entryCount: number
  totalIncome: number
  totalExpense: number
  balance: number
  expenseCategories: BillAnalysisCategoryItem[]
  incomeCategories: BillAnalysisCategoryItem[]
  samples: BillAnalysisSampleItem[]
  summary: string
  observations: string[]
  risks: string[]
  suggestions: string[]
}

interface StreamAiChatOptions extends AiChatRequest {
  onMessage: (chunk: string) => void
  onDone?: () => void
  onError?: (message: string) => void
}

interface StreamKnowledgeBaseChatOptions extends KnowledgeBaseChatRequest {
  onMessage: (chunk: string) => void
  onDone?: () => void
  onError?: (message: string) => void
}

interface StreamController {
  close: () => void
}

function decodeChunk(chunk: ArrayBuffer) {
  if (typeof TextDecoder !== 'undefined') {
    return new TextDecoder('utf-8').decode(chunk)
  }

  const bytes = new Uint8Array(chunk)
  let result = ''
  for (let index = 0; index < bytes.length; index += 1) {
    result += String.fromCharCode(bytes[index])
  }
  try {
    return decodeURIComponent(escape(result))
  } catch {
    return result
  }
}

function parseSseBuffer(buffer: string, onEvent: (event: string, data: string) => void) {
  let remain = buffer
  let separatorIndex = remain.indexOf('\n\n')

  while (separatorIndex !== -1) {
    const block = remain.slice(0, separatorIndex).trim()
    remain = remain.slice(separatorIndex + 2)

    if (block) {
      let eventName = 'message'
      const dataLines: string[] = []

      block.split('\n').forEach((line) => {
        if (line.startsWith('event:')) {
          eventName = line.slice(6).trim()
          return
        }

        if (line.startsWith('data:')) {
          dataLines.push(line.slice(5).trimStart())
        }
      })

      onEvent(eventName, dataLines.join('\n'))
    }

    separatorIndex = remain.indexOf('\n\n')
  }

  return remain
}

function createStreamRequest(
  url: string,
  payload: Record<string, unknown>,
  options: {
    onMessage: (chunk: string) => void
    onDone?: () => void
    onError?: (message: string) => void
  }
): StreamController {
  const token = tokenStorage.getAccessToken()
  let closed = false
  let sseBuffer = ''

  const requestTask = uni.request({
    url,
    method: 'POST',
    data: payload,
    header: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    enableChunked: true as never,
    responseType: 'arraybuffer' as never,
    fail: (error) => {
      if (closed) {
        return
      }
      options.onError?.(error.errMsg || 'Stream request failed')
    }
  })

  const task = requestTask as UniApp.RequestTask & {
    onChunkReceived?: (callback: (result: { data: ArrayBuffer }) => void) => void
    abort: () => void
  }

  if (typeof task.onChunkReceived === 'function') {
    task.onChunkReceived(({ data }) => {
      if (closed) {
        return
      }

      sseBuffer += decodeChunk(data).replace(/\r\n/g, '\n')
      sseBuffer = parseSseBuffer(sseBuffer, (event, dataText) => {
        if (event === 'done') {
          options.onDone?.()
          return
        }

        if (dataText) {
          options.onMessage(dataText)
        }
      })
    })
  }

  return {
    close() {
      closed = true
      task.abort()
    }
  }
}

export function streamAiChat(options: StreamAiChatOptions): StreamController {
  return createStreamRequest(
    `${API_BASE_URL}/ai/chat/stream`,
    {
      conversationId: options.conversationId,
      message: options.message
    },
    options
  )
}

export function createAiChat(payload: AiChatRequest) {
  return request<AiChatResponse>({
    url: '/ai/chat',
    method: 'POST',
    data: payload
  })
}

export function streamKnowledgeBaseChat(options: StreamKnowledgeBaseChatOptions): StreamController {
  return createStreamRequest(
    `${API_BASE_URL}/ai/knowledge-base/chat/stream`,
    {
      knowledgeBaseId: options.knowledgeBaseId,
      conversationId: options.conversationId,
      message: options.message
    },
    options
  )
}

export function createKnowledgeBaseChat(payload: KnowledgeBaseChatRequest) {
  return request<KnowledgeBaseChatResponse>({
    url: '/ai/knowledge-base/chat',
    method: 'POST',
    data: payload
  })
}

export function createAgentChat(agentId: string, payload: AgentChatRequest) {
  return request<AgentChatResponse>({
    url: `/ai/agents/${agentId}/chat`,
    method: 'POST',
    data: payload
  })
}

export function fetchAiAgents() {
  return request<AiAgentSummary[]>({
    url: '/ai/agents',
    method: 'GET'
  })
}

export function fetchKnowledgeBases() {
  return request<KnowledgeBaseSummary[]>({
    url: '/ai/knowledge-bases',
    method: 'GET'
  })
}

export function fetchAiConversations() {
  return request<AiConversationSummary[]>({
    url: '/ai/conversations',
    method: 'GET'
  })
}

export function createAiConversation(data?: {
  title?: string
  agentId?: string
  knowledgeBaseId?: string
}) {
  return request<AiConversationSummary>({
    url: '/ai/conversations',
    method: 'POST',
    data
  })
}

export function fetchAiConversationMessages(conversationId: string) {
  return request<AiConversationMessage[]>({
    url: `/ai/conversations/${conversationId}/messages`,
    method: 'GET'
  })
}

export function deleteAiConversation(conversationId: string) {
  return request<boolean>({
    url: `/ai/conversations/${conversationId}`,
    method: 'DELETE'
  })
}

export function fetchBillAnalysisHistory(limit = 10) {
  return request<BillAnalysisHistory[]>({
    url: `/ai/bill-analysis/history?limit=${limit}`,
    method: 'GET'
  })
}

export function analyzeBill(data: {
  bookId?: string
  startDate?: string
  endDate?: string
  question?: string
}) {
  return request<BillAnalysisResponse>({
    url: '/ai/bill-analysis',
    method: 'POST',
    data
  })
}
