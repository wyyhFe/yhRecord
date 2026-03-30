package com.record.modules.ai.service;

import com.record.modules.ai.model.dto.AiChatRequest;
import com.record.modules.ai.model.dto.BillAnalysisRequest;
import com.record.modules.ai.model.vo.AiChatResponse;
import com.record.modules.ai.model.vo.BillAnalysisHistoryVO;
import com.record.modules.ai.model.vo.BillAnalysisResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AiService {

    AiChatResponse chat(Long userId, AiChatRequest request);

    SseEmitter streamChat(Long userId, AiChatRequest request);

    BillAnalysisResponse analyzeBill(Long userId, BillAnalysisRequest request);

    List<BillAnalysisHistoryVO> listBillAnalysisHistory(Long userId, Integer limit);
}
