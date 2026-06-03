package com.record.modules.knowledge.service;

import com.record.modules.knowledge.model.dto.CreateKnowledgeBaseRequest;
import com.record.modules.knowledge.model.dto.CreateKnowledgeDocumentRequest;
import com.record.modules.knowledge.model.dto.RagQueryRequest;
import com.record.modules.knowledge.model.vo.KnowledgeBaseVO;
import com.record.modules.knowledge.model.vo.KnowledgeChunkTaskVO;
import com.record.modules.knowledge.model.vo.KnowledgeDocumentVO;
import com.record.modules.knowledge.model.vo.RagAnswerVO;
import com.record.modules.knowledge.model.vo.RagSearchResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeService {

    // ─── 知识库管理（已有） ─────────────────────────────────────

    KnowledgeBaseVO createBase(Long userId, CreateKnowledgeBaseRequest request);

    List<KnowledgeBaseVO> listBases(Long userId);

    KnowledgeDocumentVO createDocument(Long userId, CreateKnowledgeDocumentRequest request);

    List<KnowledgeDocumentVO> listDocuments(Long userId, Long knowledgeBaseId);

    List<KnowledgeChunkTaskVO> listTasks(Long userId, Long knowledgeBaseId, Long documentId);

    // ─── P1: 文档上传 ──────────────────────────────────────────

    /**
     * 上传文档文件到 OSS，创建文档元数据 + PARSE 任务。
     *
     * @param userId          用户 ID
     * @param knowledgeBaseId 知识库 ID
     * @param title           文档标题（可选，默认取文件名）
     * @param file            上传的文件（PDF / DOCX / TXT / MD）
     * @return 文档 VO（包含 status、chunkCount 等）
     */
    KnowledgeDocumentVO uploadDocument(Long userId, Long knowledgeBaseId, String title, MultipartFile file);

    // ─── P1: 切片检索（MySQL 全文检索兜底） ────────────────────

    /**
     * 基于 MySQL 全文检索 + LIKE 兜底的切片检索。
     * 在 Milvus 上线前作为轻量替代方案。
     *
     * @param userId          用户 ID
     * @param knowledgeBaseId 知识库 ID
     * @param query           检索关键词
     * @param topK            返回最多条数
     * @return 命中切片列表
     */
    RagSearchResultVO searchChunks(Long userId, Long knowledgeBaseId, String query, int topK);

    // ─── P1: RAG 问答 ─────────────────────────────────────────

    /**
     * RAG 问答接口：检索相关切片 → 拼入 prompt → 调 LLM 生成回答。
     *
     * @param userId  用户 ID
     * @param request 问答请求（含知识库 ID、问题、topK）
     * @return AI 回答 + 参考切片
     */
    RagAnswerVO askRag(Long userId, RagQueryRequest request);
}
