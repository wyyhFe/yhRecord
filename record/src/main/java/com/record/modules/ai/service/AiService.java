package com.record.modules.ai.service;

import com.record.modules.ai.model.dto.AiChatRequest;
import com.record.modules.ai.model.dto.CreateConversationRequest;
import com.record.modules.ai.model.vo.AiConversationMessageVO;
import com.record.modules.ai.model.vo.AiConversationSummaryVO;
import com.record.modules.ai.model.vo.AiFunctionCallResponse;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiService {

    Flux<ServerSentEvent<String>> streamChat(Long userId, AiChatRequest request);

    AiFunctionCallResponse functionCallDemo(Long userId, AiChatRequest request);

    List<AiConversationSummaryVO> listConversations(Long userId);

    AiConversationSummaryVO createConversation(Long userId, CreateConversationRequest request);

    List<AiConversationMessageVO> listConversationMessages(Long userId, String conversationId);

    void deleteConversation(Long userId, String conversationId);
}
