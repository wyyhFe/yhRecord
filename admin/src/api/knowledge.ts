import { http } from "@/utils/http";

// ==================== 知识库管理 ====================

export type KnowledgeBaseVO = {
  id: number;
  name: string;
  code: string;
  description: string;
  status: string;
  visibility: string;
  createdAt: string;
};

export type KnowledgeDocumentVO = {
  id: number;
  knowledgeBaseId: number;
  title: string;
  sourceType: string;
  fileName: string;
  filePath: string;
  mimeType: string;
  contentHash: string;
  status: string;
  chunkCount: number;
  lastError: string;
  createdAt: string;
};

export type KnowledgeChunkTaskVO = {
  id: number;
  knowledgeBaseId: number;
  documentId: number;
  taskType: string;
  status: string;
  retryCount: number;
  lastError: string;
  startedAt: string;
  finishedAt: string;
  createdAt: string;
};

/** 获取知识库列表 */
export const getKnowledgeBaseList = () => {
  return http.request<any>("get", "/knowledge/bases/list");
};

/** 创建知识库 */
export const createKnowledgeBase = (data?: object) => {
  return http.request<any>("post", "/knowledge/bases/create", { data });
};

/** 获取知识库文档列表 */
export const getKnowledgeDocumentList = (knowledgeBaseId: number) => {
  return http.request<any>("get", "/knowledge/documents/list", {
    params: { knowledgeBaseId }
  });
};

/** 上传文档到知识库 */
export const uploadKnowledgeDocument = (
  knowledgeBaseId: number,
  title: string | undefined,
  file: File
) => {
  const formData = new FormData();
  formData.append("knowledgeBaseId", String(knowledgeBaseId));
  if (title) formData.append("title", title);
  formData.append("file", file);
  return http.request<any>("post", "/knowledge/documents/upload", {
    data: formData,
    headers: { "Content-Type": "multipart/form-data" }
  });
};

/** 获取任务列表 */
export const getKnowledgeTaskList = (
  knowledgeBaseId: number,
  documentId?: number
) => {
  return http.request<any>("get", "/knowledge/tasks/list", {
    params: { knowledgeBaseId, documentId }
  });
};

// ==================== RAG 检索与问答 ====================

export type ChunkHitVO = {
  chunkId: number;
  documentId: number;
  documentTitle: string;
  chunkIndex: number;
  content: string;
  relevanceScore: number;
};

export type RagSearchResultVO = {
  hits: ChunkHitVO[];
  totalHits: number;
};

export type RagAnswerVO = {
  answer: string;
  sources: ChunkHitVO[];
};

/** 检索知识库切片 */
export const searchKnowledgeChunks = (
  knowledgeBaseId: number,
  query: string,
  topK?: number
) => {
  return http.request<any>("get", "/knowledge/search", {
    params: { knowledgeBaseId, query, topK: topK ?? 5 }
  });
};

/** RAG 问答 */
export const askRag = (data: { knowledgeBaseId: number; query: string; topK?: number }) => {
  return http.request<any>("post", "/knowledge/rag/ask", { data });
};
