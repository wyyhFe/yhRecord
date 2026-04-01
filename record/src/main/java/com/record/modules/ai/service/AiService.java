package com.record.modules.ai.service;

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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AiService {

    AiChatResponse chat(Long userId, AiChatRequest request);

    AiChatResponse agentChat(Long userId, String agentId, AgentChatRequest request);

    AiChatResponse knowledgeBaseChat(Long userId, KnowledgeBaseChatRequest request);

    SseEmitter streamChat(Long userId, AiChatRequest request);

    SseEmitter streamKnowledgeBaseChat(Long userId, KnowledgeBaseChatRequest request);

    List<AiAgentVO> listAgents();

    List<AiKnowledgeBaseVO> listKnowledgeBases(Long userId);

    List<AiConversationSummaryVO> listConversations(Long userId);

    AiConversationSummaryVO createConversation(Long userId, CreateConversationRequest request);

    List<AiConversationMessageVO> listConversationMessages(Long userId, String conversationId);

    void deleteConversation(Long userId, String conversationId);

    BillAnalysisResponse analyzeBill(Long userId, BillAnalysisRequest request);

    List<BillAnalysisHistoryVO> listBillAnalysisHistory(Long userId, Integer limit);
}
