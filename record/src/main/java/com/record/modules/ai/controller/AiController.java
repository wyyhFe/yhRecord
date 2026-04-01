package com.record.modules.ai.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.ai.model.dto.AiChatRequest;
import com.record.modules.ai.model.dto.AgentChatRequest;
import com.record.modules.ai.model.dto.BillAnalysisRequest;
import com.record.modules.ai.model.dto.CreateConversationRequest;
import com.record.modules.ai.model.dto.KnowledgeBaseChatRequest;
import com.record.modules.ai.model.vo.AiAgentVO;
import com.record.modules.ai.model.vo.AiChatResponse;
import com.record.modules.ai.model.vo.AiConversationMessageVO;
import com.record.modules.ai.model.vo.AiConversationSummaryVO;
import com.record.modules.ai.model.vo.AiKnowledgeBaseVO;
import com.record.modules.ai.model.vo.BillAnalysisHistoryVO;
import com.record.modules.ai.model.vo.BillAnalysisResponse;
import com.record.modules.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "AI")
@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "AI 聊天")
    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ApiResponse.success(aiService.chat(UserContext.getUserId(), request));
    }

    @Operation(summary = "Agent 聊天")
    @PostMapping("/agents/{agentId}/chat")
    public ApiResponse<AiChatResponse> agentChat(@PathVariable String agentId,
                                                 @Valid @RequestBody AgentChatRequest request) {
        return ApiResponse.success(aiService.agentChat(UserContext.getUserId(), agentId, request));
    }

    @Operation(summary = "知识库聊天")
    @PostMapping("/knowledge-base/chat")
    public ApiResponse<AiChatResponse> knowledgeBaseChat(@Valid @RequestBody KnowledgeBaseChatRequest request) {
        return ApiResponse.success(aiService.knowledgeBaseChat(UserContext.getUserId(), request));
    }

    @Operation(summary = "AI 流式聊天")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody AiChatRequest request) {
        return aiService.streamChat(UserContext.getUserId(), request);
    }

    @Operation(summary = "知识库流式聊天")
    @PostMapping(value = "/knowledge-base/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamKnowledgeBaseChat(@Valid @RequestBody KnowledgeBaseChatRequest request) {
        return aiService.streamKnowledgeBaseChat(UserContext.getUserId(), request);
    }

    @Operation(summary = "Agent 列表")
    @GetMapping("/agents")
    public ApiResponse<List<AiAgentVO>> agents() {
        return ApiResponse.success(aiService.listAgents());
    }

    @Operation(summary = "AI 知识库列表")
    @GetMapping("/knowledge-bases")
    public ApiResponse<List<AiKnowledgeBaseVO>> knowledgeBases() {
        return ApiResponse.success(aiService.listKnowledgeBases(UserContext.getUserId()));
    }

    @Operation(summary = "AI 会话列表")
    @GetMapping("/conversations")
    public ApiResponse<List<AiConversationSummaryVO>> conversations() {
        return ApiResponse.success(aiService.listConversations(UserContext.getUserId()));
    }

    @Operation(summary = "创建 AI 会话")
    @PostMapping("/conversations")
    public ApiResponse<AiConversationSummaryVO> createConversation(@RequestBody(required = false) CreateConversationRequest request) {
        return ApiResponse.success(aiService.createConversation(UserContext.getUserId(), request));
    }

    @Operation(summary = "AI 会话消息列表")
    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<List<AiConversationMessageVO>> conversationMessages(@PathVariable String conversationId) {
        return ApiResponse.success(aiService.listConversationMessages(UserContext.getUserId(), conversationId));
    }

    @Operation(summary = "删除 AI 会话")
    @DeleteMapping("/conversations/{conversationId}")
    public ApiResponse<Boolean> deleteConversation(@PathVariable String conversationId) {
        aiService.deleteConversation(UserContext.getUserId(), conversationId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "AI 账单分析")
    @PostMapping("/bill-analysis")
    public ApiResponse<BillAnalysisResponse> analyzeBill(@RequestBody BillAnalysisRequest request) {
        return ApiResponse.success(aiService.analyzeBill(UserContext.getUserId(), request));
    }

    @Operation(summary = "账单分析历史")
    @GetMapping("/bill-analysis/history")
    public ApiResponse<List<BillAnalysisHistoryVO>> billAnalysisHistory(@RequestParam(required = false) Integer limit) {
        return ApiResponse.success(aiService.listBillAnalysisHistory(UserContext.getUserId(), limit));
    }
}
