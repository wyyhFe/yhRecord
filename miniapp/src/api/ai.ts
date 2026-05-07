import { API_BASE_URL } from '@/config/app'
import { request } from '@/utils/request'
import { tokenStorage } from '@/utils/storage'
import type { Pagination } from '@/types/api'
import type { Id } from '@/types/domain'

export interface AiChatRequest {
  conversationId?: string
  message: string
}

export interface AiConversationSummary {
  id: string
  title: string
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
}

/**
 * 账单分析请求体。
 * 所有字段都允许缺省：startDate 缺省时按后端默认窗口推算，bookId 缺省时分析全部账本。
 */
export interface BillAnalysisPayload {
  startDate?: string
  endDate?: string
  bookId?: Id
  /** 单次分析的最大账单条数，后端会再钳到上限。 */
  limit?: number
  /** 用户的补充问题，可选。 */
  question?: string
}

/** 账单分类金额（按标签聚合）。 */
export interface BillAnalysisCategory {
  name: string
  amount: number | string
  /** 占比，0-1 之间的小数。 */
  ratio: number | string
}

/** 高金额样本，给前端展示用。 */
export interface BillAnalysisSample {
  entryDate: string
  type: 'INCOME' | 'EXPENSE'
  amount: number | string
  bookName?: string | null
  category?: string | null
  remark?: string | null
}

/**
 * 账单分析响应。
 * 注意：金额类字段后端使用 BigDecimal 序列化，可能是数字也可能是字符串，前端统一按字符串处理更安全。
 */
export interface BillAnalysisResult {
  startDate: string
  endDate: string
  bookId?: Id | null
  bookName?: string | null
  entryCount: number
  totalIncome: number | string
  totalExpense: number | string
  balance: number | string
  expenseCategories: BillAnalysisCategory[]
  incomeCategories: BillAnalysisCategory[]
  samples: BillAnalysisSample[]
  /** AI 生成的总结，AI 解析失败时也会有兜底文案。 */
  summary?: string | null
  observations?: string[]
  risks?: string[]
  suggestions?: string[]
}

/** 账单分析历史项，列表只展示概要。 */
export interface BillAnalysisHistoryItem {
  /** 雪花 ID 字符串形式，避免 JS 数字精度丢失。 */
  id: string
  createdAt: string
  startDate: string
  endDate: string
  bookId?: Id | null
  bookName?: string | null
  entryCount: number
  summary?: string | null
  question?: string | null
}

interface StreamAiChatOptions extends AiChatRequest {
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

function createStreamRequest(url: string, payload: Record<string, unknown>, options: StreamAiChatOptions): StreamController {
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
      options.onError?.(error.errMsg || '流式请求失败')
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

        if (event === 'error') {
          options.onError?.(dataText || '聊天请求失败')
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

export function fetchAiConversations() {
  return request<AiConversationSummary[]>({
    url: '/ai/conversations',
    method: 'GET'
  })
}

export function createAiConversation(data?: { title?: string }) {
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

/**
 * 发起一次账单分析。
 * 同步接口，后端先聚合再调用模型，整体耗时一般 3-10s，调用方需要给 loading 态。
 */
export function requestBillAnalysis(payload: BillAnalysisPayload) {
  return request<BillAnalysisResult>({
    url: '/ai/bill-analysis',
    method: 'POST',
    data: payload
  })
}

/**
 * 拉取账单分析历史，倒序分页。
 * size 上限由后端控制（当前 50）。
 */
export function fetchBillAnalysisHistory(params?: { current?: number; size?: number }) {
  const current = params?.current ?? 1
  const size = params?.size ?? 10
  // 小程序运行环境没有 URLSearchParams，手动拼接查询串。
  const query = `current=${encodeURIComponent(current)}&size=${encodeURIComponent(size)}`
  return request<Pagination<BillAnalysisHistoryItem>>({
    url: `/ai/bill-analysis/history?${query}`,
    method: 'GET'
  })
}
