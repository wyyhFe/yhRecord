package com.record.modules.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 账单分析。
     * 后端先聚合统计（金额、分类占比、样本），再让模型生成总结/观察/风险/建议。
     * 模型只输出 JSON，由服务端解析；解析失败时只回填 summary，其余三段为空数组。
     */
    BillAnalysisResponse analyzeBills(Long userId, BillAnalysisRequest request);

    /**
     * 查询当前用户的账单分析历史，倒序分页。
     * 列表只展示概要，详情内容（observations/risks/suggestions 等）当前不持久化。
     */
    Page<BillAnalysisHistoryVO> listBillAnalysisHistory(Long userId, long current, long size);
}
