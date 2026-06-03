package com.record.modules.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.util.PageQuery;
import com.record.modules.ai.model.dto.AiChatRequest;
import com.record.modules.ai.model.dto.BillAnalysisRequest;
import com.record.modules.ai.model.dto.CreateConversationRequest;
import com.record.modules.ai.model.vo.AiConversationMessageVO;
import com.record.modules.ai.model.vo.AiConversationSummaryVO;
import com.record.modules.ai.model.vo.BillAnalysisHistoryVO;
import com.record.modules.ai.model.vo.BillAnalysisResponse;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiService {

    Flux<ServerSentEvent<String>> streamChat(Long userId, AiChatRequest request);

    List<AiConversationSummaryVO> listConversations(Long userId);

    AiConversationSummaryVO createConversation(Long userId, CreateConversationRequest request);

    List<AiConversationMessageVO> listConversationMessages(Long userId, String conversationId);

    void deleteConversation(Long userId, String conversationId);

    BillAnalysisResponse analyzeBills(Long userId, BillAnalysisRequest request);

    Page<BillAnalysisHistoryVO> listBillAnalysisHistory(Long userId, PageQuery pageQuery);
}
