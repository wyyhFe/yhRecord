package com.record.modules.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.ai.model.dto.AiChatRequest;
import com.record.modules.ai.model.dto.BillAnalysisRequest;
import com.record.modules.ai.model.dto.CreateConversationRequest;
import com.record.modules.ai.model.vo.AiConversationMessageVO;
import com.record.modules.ai.model.vo.AiConversationSummaryVO;
import com.record.modules.ai.model.vo.BillAnalysisHistoryVO;
import com.record.modules.ai.model.vo.BillAnalysisResponse;
import com.record.modules.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "AI")
@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "流式聊天")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody AiChatRequest request) {
        return aiService.streamChat(UserContext.getUserId(), request);
    }

    @Operation(summary = "会话列表")
    @GetMapping("/conversations")
    public ApiResponse<List<AiConversationSummaryVO>> conversations() {
        return ApiResponse.success(aiService.listConversations(UserContext.getUserId()));
    }

    @Operation(summary = "创建会话")
    @PostMapping("/conversations")
    public ApiResponse<AiConversationSummaryVO> createConversation(@RequestBody(required = false) CreateConversationRequest request) {
        return ApiResponse.success(aiService.createConversation(UserContext.getUserId(), request));
    }

    @Operation(summary = "会话消息")
    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<List<AiConversationMessageVO>> conversationMessages(@PathVariable String conversationId) {
        return ApiResponse.success(aiService.listConversationMessages(UserContext.getUserId(), conversationId));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/conversations/{conversationId}")
    public ApiResponse<Boolean> deleteConversation(@PathVariable String conversationId) {
        aiService.deleteConversation(UserContext.getUserId(), conversationId);
        return ApiResponse.success(Boolean.TRUE);
    }

    /**
     * 账单分析。
     * 同步接口：后端先做聚合再调模型，请求耗时一般在 3-10s，前端需要给 loading 状态。
     */
    @Operation(summary = "账单分析")
    @PostMapping("/bill-analysis")
    public ApiResponse<BillAnalysisResponse> billAnalysis(@Valid @RequestBody BillAnalysisRequest request) {
        return ApiResponse.success(aiService.analyzeBills(UserContext.getUserId(), request));
    }

    /**
     * 账单分析历史列表（分页）。
     * 历史只保存概要，不保存观察/风险/建议三段明细。
     */
    @Operation(summary = "账单分析历史")
    @GetMapping("/bill-analysis/history")
    public ApiResponse<Page<BillAnalysisHistoryVO>> billAnalysisHistory(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.success(aiService.listBillAnalysisHistory(UserContext.getUserId(), current, size));
    }
}
