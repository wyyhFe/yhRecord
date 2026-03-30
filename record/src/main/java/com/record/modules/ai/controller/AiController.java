package com.record.modules.ai.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.ai.model.dto.AiChatRequest;
import com.record.modules.ai.model.dto.BillAnalysisRequest;
import com.record.modules.ai.model.vo.AiChatResponse;
import com.record.modules.ai.model.vo.BillAnalysisHistoryVO;
import com.record.modules.ai.model.vo.BillAnalysisResponse;
import com.record.modules.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(summary = "AI 流式聊天")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody AiChatRequest request) {
        return aiService.streamChat(UserContext.getUserId(), request);
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
