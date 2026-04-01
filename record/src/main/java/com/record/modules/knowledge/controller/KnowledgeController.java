package com.record.modules.knowledge.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.knowledge.model.dto.CreateKnowledgeBaseRequest;
import com.record.modules.knowledge.model.dto.CreateKnowledgeDocumentRequest;
import com.record.modules.knowledge.model.vo.KnowledgeBaseVO;
import com.record.modules.knowledge.model.vo.KnowledgeChunkTaskVO;
import com.record.modules.knowledge.model.vo.KnowledgeDocumentVO;
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

import java.util.List;

@Tag(name = "知识库管理")
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

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

    @Operation(summary = "创建知识库文档")
    @PostMapping("/documents/create")
    public ApiResponse<KnowledgeDocumentVO> createDocument(@Valid @RequestBody CreateKnowledgeDocumentRequest request) {
        return ApiResponse.success(knowledgeService.createDocument(UserContext.getUserId(), request));
    }

    @Operation(summary = "查询知识库文档列表")
    @GetMapping("/documents/list")
    public ApiResponse<List<KnowledgeDocumentVO>> listDocuments(@RequestParam Long knowledgeBaseId) {
        return ApiResponse.success(knowledgeService.listDocuments(UserContext.getUserId(), knowledgeBaseId));
    }

    @Operation(summary = "查询知识库任务列表")
    @GetMapping("/tasks/list")
    public ApiResponse<List<KnowledgeChunkTaskVO>> listTasks(@RequestParam Long knowledgeBaseId,
                                                             @RequestParam(required = false) Long documentId) {
        return ApiResponse.success(knowledgeService.listTasks(UserContext.getUserId(), knowledgeBaseId, documentId));
    }
}
