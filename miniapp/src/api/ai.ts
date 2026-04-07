import { API_BASE_URL } from '@/config/app'
import { request } from '@/utils/request'
import { tokenStorage } from '@/utils/storage'

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
