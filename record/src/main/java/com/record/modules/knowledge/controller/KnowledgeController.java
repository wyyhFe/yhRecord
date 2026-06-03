package com.record.modules.knowledge.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.knowledge.model.dto.CreateKnowledgeBaseRequest;
import com.record.modules.knowledge.model.dto.CreateKnowledgeDocumentRequest;
import com.record.modules.knowledge.model.dto.RagQueryRequest;
import com.record.modules.knowledge.model.vo.KnowledgeBaseVO;
import com.record.modules.knowledge.model.vo.KnowledgeChunkTaskVO;
import com.record.modules.knowledge.model.vo.KnowledgeDocumentVO;
import com.record.modules.knowledge.model.vo.RagAnswerVO;
import com.record.modules.knowledge.model.vo.RagSearchResultVO;
import com.record.modules.knowledge.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "知识库管理")
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    // ─── 知识库管理 ─────────────────────────────────────────────

    @Operation(summary = "创建知识库")
    @PostMapping("/bases/create")
    public ApiResponse<KnowledgeBaseVO> createBase(@Valid @RequestBody CreateKnowledgeBaseRequest request) {
        return ApiResponse.success(knowledgeService.createBase(UserContext.getUserId(), request));
    }

    @Operation(summary = "查询知识库列表")
    @GetMapping("/bases/list")
    public ApiResponse<List<KnowledgeBaseVO>> listBases() {
        return ApiResponse.success(knowledgeService.listBases(UserContext.getUserId()));
    }

    // ─── 文档管理 ─────────────────────────────────────────────

    @Operation(summary = "创建知识库文档（元数据）")
    @PostMapping("/documents/create")
    public ApiResponse<KnowledgeDocumentVO> createDocument(@Valid @RequestBody CreateKnowledgeDocumentRequest request) {
        return ApiResponse.success(knowledgeService.createDocument(UserContext.getUserId(), request));
    }

    @Operation(summary = "上传文档文件到知识库（P1: 上传 + 自动解析 + 切片）")
    @PostMapping("/documents/upload")
    public ApiResponse<KnowledgeDocumentVO> uploadDocument(
            @RequestParam Long knowledgeBaseId,
            @RequestParam(required = false) String title,
            @RequestParam MultipartFile file) {
        return ApiResponse.success(
                knowledgeService.uploadDocument(UserContext.getUserId(), knowledgeBaseId, title, file));
    }

    @Operation(summary = "查询知识库文档列表")
    @GetMapping("/documents/list")
    public ApiResponse<List<KnowledgeDocumentVO>> listDocuments(@RequestParam Long knowledgeBaseId) {
        return ApiResponse.success(knowledgeService.listDocuments(UserContext.getUserId(), knowledgeBaseId));
    }

    // ─── 任务管理 ─────────────────────────────────────────────

    @Operation(summary = "查询知识库任务列表")
    @GetMapping("/tasks/list")
    public ApiResponse<List<KnowledgeChunkTaskVO>> listTasks(@RequestParam Long knowledgeBaseId,
                                                             @RequestParam(required = false) Long documentId) {
        return ApiResponse.success(knowledgeService.listTasks(UserContext.getUserId(), knowledgeBaseId, documentId));
    }

    // ─── P1: RAG 检索与问答 ──────────────────────────────────

    @Operation(summary = "检索知识库切片（基于 MySQL LIKE 兜底检索）")
    @GetMapping("/search")
    public ApiResponse<RagSearchResultVO> search(@RequestParam Long knowledgeBaseId,
                                                  @RequestParam String query,
                                                  @RequestParam(defaultValue = "5") int topK) {
        return ApiResponse.success(knowledgeService.searchChunks(
                UserContext.getUserId(), knowledgeBaseId, query, topK));
    }

    @Operation(summary = "RAG 问答（检索 + LLM 生成回答）")
    @PostMapping("/rag/ask")
    public ApiResponse<RagAnswerVO> askRag(@Valid @RequestBody RagQueryRequest request) {
        return ApiResponse.success(knowledgeService.askRag(UserContext.getUserId(), request));
    }
}
