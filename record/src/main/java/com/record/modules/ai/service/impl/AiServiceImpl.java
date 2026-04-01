package com.record.modules.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.record.common.config.AiProperties;
import com.record.common.enums.LedgerType;
import com.record.common.exception.BusinessException;
import com.record.common.exception.ErrorCode;
import com.record.modules.ai.mapper.AiBillAnalysisRecordMapper;
import com.record.modules.ai.mapper.AiCallLogMapper;
import com.record.modules.ai.model.dto.AiChatRequest;
import com.record.modules.ai.model.dto.AgentChatRequest;
import com.record.modules.ai.model.dto.BillAnalysisRequest;
import com.record.modules.ai.model.dto.CreateConversationRequest;
import com.record.modules.ai.model.dto.KnowledgeBaseChatRequest;
import com.record.modules.ai.model.entity.AiBillAnalysisRecord;
import com.record.modules.ai.model.entity.AiCallLog;
import com.record.modules.ai.model.vo.AiAgentVO;
import com.record.modules.ai.model.vo.AiChatResponse;
import com.record.modules.ai.model.vo.AiCitationVO;
import com.record.modules.ai.model.vo.AiConversationMessageVO;
import com.record.modules.ai.model.vo.AiConversationSummaryVO;
import com.record.modules.ai.model.vo.AiKnowledgeBaseVO;
import com.record.modules.ai.model.vo.BillAnalysisHistoryVO;
import com.record.modules.ai.model.vo.BillAnalysisResponse;
import com.record.modules.ai.prompt.PromptTemplateLoader;
import com.record.modules.ai.service.AiService;
import com.record.modules.knowledge.mapper.KnowledgeBaseMapper;
import com.record.modules.knowledge.mapper.KnowledgeDocumentMapper;
import com.record.modules.knowledge.model.entity.KnowledgeBase;
import com.record.modules.knowledge.model.entity.KnowledgeDocument;
import com.record.modules.ledger.mapper.LedgerBookMapper;
import com.record.modules.ledger.mapper.LedgerEntryMapper;
import com.record.modules.ledger.mapper.LedgerEntryTagRelMapper;
import com.record.modules.ledger.model.entity.LedgerBook;
import com.record.modules.ledger.model.entity.LedgerEntry;
import com.record.modules.ledger.model.entity.LedgerEntryTagRel;
import com.record.modules.tag.mapper.UserTagMapper;
import com.record.modules.tag.model.entity.UserTag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    private static final String DEFAULT_CONVERSATION_ID = "default";
    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final int CATEGORY_TOP_N = 6;
    private static final int SAMPLE_TOP_N = 12;
    private static final int DEFAULT_HISTORY_LIMIT = 10;
    private static final int MAX_HISTORY_LIMIT = 50;
    private static final int MAX_CONVERSATIONS = 50;
    private static final int MAX_KNOWLEDGE_CITATIONS = 4;
    private static final long CHAT_HISTORY_TTL_DAYS = 7L;
    private static final TypeReference<AiConversationSummaryVO> CONVERSATION_TYPE = new TypeReference<>() { };
    private static final TypeReference<AiConversationMessageVO> MESSAGE_TYPE = new TypeReference<>() { };

    private static final Map<String, AgentDefinition> AGENT_DEFINITIONS = Map.of(
            "general", new AgentDefinition(
                    "general",
                    "通用助手",
                    "适合开放式问答、方案讨论和需求梳理。",
                    List.of("通用问答", "方案整理", "产品建议"),
                    "prompts/ai/agent/general.md"
            ),
            "life-record", new AgentDefinition(
                    "life-record",
                    "生活记录助手",
                    "更偏向日记、记账、提醒与回忆场景。",
                    List.of("账单分析", "记录总结", "生活建议"),
                    "prompts/ai/agent/life-record.md"
            )
    );

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final AiProperties aiProperties;
    private final AiBillAnalysisRecordMapper aiBillAnalysisRecordMapper;
    private final AiCallLogMapper aiCallLogMapper;
    private final LedgerEntryMapper ledgerEntryMapper;
    private final LedgerBookMapper ledgerBookMapper;
    private final LedgerEntryTagRelMapper ledgerEntryTagRelMapper;
    private final UserTagMapper userTagMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final PromptTemplateLoader promptTemplateLoader;

    public AiServiceImpl(ObjectProvider<ChatClient> chatClientProvider,
                         AiProperties aiProperties,
                         AiBillAnalysisRecordMapper aiBillAnalysisRecordMapper,
                         AiCallLogMapper aiCallLogMapper,
                         LedgerEntryMapper ledgerEntryMapper,
                         LedgerBookMapper ledgerBookMapper,
                         LedgerEntryTagRelMapper ledgerEntryTagRelMapper,
                         UserTagMapper userTagMapper,
                         KnowledgeBaseMapper knowledgeBaseMapper,
                         KnowledgeDocumentMapper knowledgeDocumentMapper,
                         StringRedisTemplate stringRedisTemplate,
                         ObjectMapper objectMapper,
                         PromptTemplateLoader promptTemplateLoader) {
        this.chatClientProvider = chatClientProvider;
        this.aiProperties = aiProperties;
        this.aiBillAnalysisRecordMapper = aiBillAnalysisRecordMapper;
        this.aiCallLogMapper = aiCallLogMapper;
        this.ledgerEntryMapper = ledgerEntryMapper;
        this.ledgerBookMapper = ledgerBookMapper;
        this.ledgerEntryTagRelMapper = ledgerEntryTagRelMapper;
        this.userTagMapper = userTagMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.promptTemplateLoader = promptTemplateLoader;
    }

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        ensureAiEnabled();
        long start = System.currentTimeMillis();
        String conversationId = normalizeConversationId(request.getConversationId());
        String historyText = loadConversationHistory(userId, conversationId);

        try {
            String reply = requireChatClient().prompt()
                    .system(resolveSystemPrompt())
                    .user(buildChatPrompt(request.getMessage(), historyText))
                    .call()
                    .content();

            if (!StringUtils.hasText(reply)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 未返回有效内容");
            }

            saveConversationTurn(userId, conversationId, ROLE_USER, request.getMessage(), "general", null, List.of());
            saveConversationTurn(userId, conversationId, ROLE_ASSISTANT, reply, "general", null, List.of());
            saveAiCallLog(userId, "CHAT", conversationId, true, start, null);

            return AiChatResponse.builder()
                    .conversationId(conversationId)
                    .reply(reply)
                    .sources(List.of())
                    .build();
        } catch (RuntimeException ex) {
            saveAiCallLog(userId, "CHAT", conversationId, false, start, ex.getMessage());
            throw ex;
        }
    }

    @Override
    public AiChatResponse agentChat(Long userId, String agentId, AgentChatRequest request) {
        ensureAiEnabled();
        long start = System.currentTimeMillis();
        String conversationId = normalizeConversationId(request.getConversationId());
        AgentDefinition agent = requireAgent(agentId);
        String historyText = loadConversationHistory(userId, conversationId);
        KnowledgeContext knowledgeContext = buildKnowledgeContext(userId, request.getKnowledgeBaseId(), request.getMessage());

        try {
            String reply = requireChatClient().prompt()
                    .system(buildAgentSystemPrompt(agent, knowledgeContext))
                    .user(buildAgentPrompt(request.getMessage(), historyText, knowledgeContext))
                    .call()
                    .content();

            if (!StringUtils.hasText(reply)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Agent 未返回有效内容");
            }

            saveConversationTurn(userId, conversationId, ROLE_USER, request.getMessage(), agent.id(), request.getKnowledgeBaseId(), List.of());
            saveConversationTurn(userId, conversationId, ROLE_ASSISTANT, reply, agent.id(), request.getKnowledgeBaseId(), knowledgeContext.citations());
            saveAiCallLog(userId, "AGENT_CHAT", conversationId, true, start, null);

            return AiChatResponse.builder()
                    .conversationId(conversationId)
                    .reply(reply)
                    .sources(knowledgeContext.citations())
                    .build();
        } catch (RuntimeException ex) {
            saveAiCallLog(userId, "AGENT_CHAT", conversationId, false, start, ex.getMessage());
            throw ex;
        }
    }

    @Override
    public AiChatResponse knowledgeBaseChat(Long userId, KnowledgeBaseChatRequest request) {
        ensureAiEnabled();
        long start = System.currentTimeMillis();
        String conversationId = normalizeConversationId(request.getConversationId());
        String historyText = loadConversationHistory(userId, conversationId);
        KnowledgeContext knowledgeContext = buildKnowledgeContext(userId, request.getKnowledgeBaseId(), request.getMessage());

        try {
            String reply = requireChatClient().prompt()
                    .system(buildKnowledgeSystemPrompt(knowledgeContext))
                    .user(buildKnowledgeUserPrompt(request.getMessage(), historyText, knowledgeContext))
                    .call()
                    .content();

            if (!StringUtils.hasText(reply)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "知识库聊天未返回有效内容");
            }

            saveConversationTurn(userId, conversationId, ROLE_USER, request.getMessage(), "general", request.getKnowledgeBaseId(), List.of());
            saveConversationTurn(userId, conversationId, ROLE_ASSISTANT, reply, "general", request.getKnowledgeBaseId(), knowledgeContext.citations());
            saveAiCallLog(userId, "KNOWLEDGE_CHAT", conversationId, true, start, null);

            return AiChatResponse.builder()
                    .conversationId(conversationId)
                    .reply(reply)
                    .sources(knowledgeContext.citations())
                    .build();
        } catch (RuntimeException ex) {
            saveAiCallLog(userId, "KNOWLEDGE_CHAT", conversationId, false, start, ex.getMessage());
            throw ex;
        }
    }

    @Override
    public SseEmitter streamChat(Long userId, AiChatRequest request) {
        ensureAiEnabled();
        long start = System.currentTimeMillis();
        String conversationId = normalizeConversationId(request.getConversationId());
        String historyText = loadConversationHistory(userId, conversationId);
        SseEmitter emitter = new SseEmitter(0L);
        StringBuilder fullReply = new StringBuilder();

        Flux<String> stream = requireChatClient().prompt()
                .system(resolveSystemPrompt())
                .user(buildChatPrompt(request.getMessage(), historyText))
                .stream()
                .content();

        stream.subscribe(
                chunk -> {
                    if (!StringUtils.hasText(chunk)) {
                        return;
                    }
                    fullReply.append(chunk);
                    try {
                        emitter.send(SseEmitter.event().name("message").data(chunk));
                    } catch (IOException ex) {
                        saveAiCallLog(userId, "CHAT_STREAM", conversationId, false, start, ex.getMessage());
                        emitter.completeWithError(ex);
                    }
                },
                error -> {
                    saveAiCallLog(userId, "CHAT_STREAM", conversationId, false, start,
                            error != null ? error.getMessage() : "stream error");
                    emitter.completeWithError(error);
                },
                () -> {
                    saveConversationTurn(userId, conversationId, ROLE_USER, request.getMessage(), "general", null, List.of());
                    saveConversationTurn(userId, conversationId, ROLE_ASSISTANT, fullReply.toString(), "general", null, List.of());
                    saveAiCallLog(userId, "CHAT_STREAM", conversationId, true, start, null);
                    try {
                        emitter.send(SseEmitter.event().name("done").data("DONE"));
                    } catch (IOException ex) {
                        emitter.completeWithError(ex);
                        return;
                    }
                    emitter.complete();
                }
        );
        return emitter;
    }

    @Override
    public SseEmitter streamKnowledgeBaseChat(Long userId, KnowledgeBaseChatRequest request) {
        ensureAiEnabled();
        long start = System.currentTimeMillis();
        String conversationId = normalizeConversationId(request.getConversationId());
        String historyText = loadConversationHistory(userId, conversationId);
        KnowledgeContext knowledgeContext = buildKnowledgeContext(userId, request.getKnowledgeBaseId(), request.getMessage());
        SseEmitter emitter = new SseEmitter(0L);
        StringBuilder fullReply = new StringBuilder();

        Flux<String> stream = requireChatClient().prompt()
                .system(buildKnowledgeSystemPrompt(knowledgeContext))
                .user(buildKnowledgeUserPrompt(request.getMessage(), historyText, knowledgeContext))
                .stream()
                .content();

        stream.subscribe(
                chunk -> {
                    if (!StringUtils.hasText(chunk)) {
                        return;
                    }
                    fullReply.append(chunk);
                    try {
                        emitter.send(SseEmitter.event().name("message").data(chunk));
                    } catch (IOException ex) {
                        saveAiCallLog(userId, "KNOWLEDGE_CHAT_STREAM", conversationId, false, start, ex.getMessage());
                        emitter.completeWithError(ex);
                    }
                },
                error -> {
                    saveAiCallLog(userId, "KNOWLEDGE_CHAT_STREAM", conversationId, false, start,
                            error != null ? error.getMessage() : "stream error");
                    emitter.completeWithError(error);
                },
                () -> {
                    saveConversationTurn(userId, conversationId, ROLE_USER, request.getMessage(), "general", request.getKnowledgeBaseId(), List.of());
                    saveConversationTurn(userId, conversationId, ROLE_ASSISTANT, fullReply.toString(), "general", request.getKnowledgeBaseId(), knowledgeContext.citations());
                    saveAiCallLog(userId, "KNOWLEDGE_CHAT_STREAM", conversationId, true, start, null);
                    try {
                        emitter.send(SseEmitter.event().name("done").data("DONE"));
                    } catch (IOException ex) {
                        emitter.completeWithError(ex);
                        return;
                    }
                    emitter.complete();
                }
        );
        return emitter;
    }

    @Override
    public List<AiAgentVO> listAgents() {
        return AGENT_DEFINITIONS.values().stream()
                .map(agent -> AiAgentVO.builder()
                        .id(agent.id())
                        .name(agent.name())
                        .description(agent.description())
                        .capabilities(agent.capabilities())
                        .build())
                .toList();
    }

    @Override
    public List<AiKnowledgeBaseVO> listKnowledgeBases(Long userId) {
        List<KnowledgeBase> bases = knowledgeBaseMapper.selectList(new LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getUserId, userId)
                .eq(KnowledgeBase::getStatus, "ENABLED")
                .orderByDesc(KnowledgeBase::getUpdatedAt));
        if (bases.isEmpty()) {
            return List.of();
        }

        List<Long> baseIds = bases.stream().map(KnowledgeBase::getId).toList();
        Map<Long, Integer> documentCountMap = knowledgeDocumentMapper.selectList(new LambdaQueryWrapper<KnowledgeDocument>()
                        .in(KnowledgeDocument::getKnowledgeBaseId, baseIds))
                .stream()
                .collect(Collectors.groupingBy(KnowledgeDocument::getKnowledgeBaseId, Collectors.summingInt(item -> 1)));

        return bases.stream()
                .map(base -> AiKnowledgeBaseVO.builder()
                        .id(base.getId())
                        .name(base.getName())
                        .description(base.getDescription())
                        .documentCount(documentCountMap.getOrDefault(base.getId(), 0))
                        .updatedAt(base.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public List<AiConversationSummaryVO> listConversations(Long userId) {
        Set<String> ids = stringRedisTemplate.opsForZSet().reverseRange(buildConversationIndexKey(userId), 0, MAX_CONVERSATIONS - 1);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<AiConversationSummaryVO> result = new ArrayList<>();
        for (String id : ids) {
            AiConversationSummaryVO summary = readConversationSummary(userId, id);
            if (summary != null) {
                result.add(summary);
            }
        }
        return result;
    }

    @Override
    public AiConversationSummaryVO createConversation(Long userId, CreateConversationRequest request) {
        String conversationId = UUID.randomUUID().toString().replace("-", "");
        String agentId = request != null && StringUtils.hasText(request.getAgentId()) ? request.getAgentId().trim() : "general";
        if (StringUtils.hasText(agentId)) {
            requireAgent(agentId);
        }
        Long knowledgeBaseId = request != null ? request.getKnowledgeBaseId() : null;
        if (knowledgeBaseId != null) {
            requireOwnedKnowledgeBase(userId, knowledgeBaseId);
        }

        String title = request != null && StringUtils.hasText(request.getTitle())
                ? request.getTitle().trim()
                : defaultConversationTitle(agentId, knowledgeBaseId);

        AiConversationSummaryVO summary = AiConversationSummaryVO.builder()
                .id(conversationId)
                .title(title)
                .agentId(agentId)
                .knowledgeBaseId(knowledgeBaseId != null ? String.valueOf(knowledgeBaseId) : null)
                .lastMessagePreview("新会话已创建")
                .updatedAt(LocalDateTime.now())
                .messageCount(0)
                .build();
        writeConversationSummary(userId, summary);
        return summary;
    }

    @Override
    public List<AiConversationMessageVO> listConversationMessages(Long userId, String conversationId) {
        String normalizedConversationId = normalizeConversationId(conversationId);
        List<String> items = stringRedisTemplate.opsForList().range(buildConversationMessagesKey(userId, normalizedConversationId), 0, -1);
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(this::deserializeMessage)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void deleteConversation(Long userId, String conversationId) {
        String normalizedConversationId = normalizeConversationId(conversationId);
        stringRedisTemplate.delete(buildConversationSummaryKey(userId, normalizedConversationId));
        stringRedisTemplate.delete(buildConversationMessagesKey(userId, normalizedConversationId));
        stringRedisTemplate.delete(buildConversationKey(userId, normalizedConversationId));
        stringRedisTemplate.opsForZSet().remove(buildConversationIndexKey(userId), normalizedConversationId);
    }

    @Override
    public BillAnalysisResponse analyzeBill(Long userId, BillAnalysisRequest request) {
        ensureAiEnabled();
        long start = System.currentTimeMillis();
        try {
            LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
            LocalDate startDate = request.getStartDate() != null
                    ? request.getStartDate()
                    : endDate.minusDays(aiProperties.getBillAnalysis().getDefaultDays() - 1L);
            if (startDate.isAfter(endDate)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "开始日期不能晚于结束日期");
            }

            int limit = request.getLimit() != null ? request.getLimit() : aiProperties.getBillAnalysis().getMaxEntries();
            if (limit <= 0 || limit > aiProperties.getBillAnalysis().getMaxEntries()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "分析条数超出允许范围");
            }

            LedgerBook book = null;
            if (request.getBookId() != null) {
                book = ledgerBookMapper.selectById(request.getBookId());
                if (book == null || !Objects.equals(book.getUserId(), userId)) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "账本不存在或无权访问");
                }
            }

            List<LedgerEntry> entries = ledgerEntryMapper.selectList(new LambdaQueryWrapper<LedgerEntry>()
                            .eq(LedgerEntry::getUserId, userId)
                            .eq(request.getBookId() != null, LedgerEntry::getBookId, request.getBookId())
                            .isNull(LedgerEntry::getDeletedAt)
                            .ge(LedgerEntry::getEntryDate, startDate)
                            .le(LedgerEntry::getEntryDate, endDate)
                            .orderByDesc(LedgerEntry::getEntryDate)
                            .orderByDesc(LedgerEntry::getId))
                    .stream()
                    .limit(limit)
                    .toList();

            Map<Long, LedgerBook> bookMap = loadBookMap(entries);
            Map<Long, String> categoryMap = loadEntryCategoryMap(entries);

            BigDecimal totalIncome = sumAmountByType(entries, LedgerType.INCOME);
            BigDecimal totalExpense = sumAmountByType(entries, LedgerType.EXPENSE);
            BigDecimal balance = totalIncome.subtract(totalExpense);

            List<BillAnalysisResponse.CategoryAmountVO> expenseCategories = buildCategoryAmounts(entries, categoryMap, LedgerType.EXPENSE);
            List<BillAnalysisResponse.CategoryAmountVO> incomeCategories = buildCategoryAmounts(entries, categoryMap, LedgerType.INCOME);
            List<BillAnalysisResponse.EntrySampleVO> samples = buildSamples(entries, bookMap, categoryMap);

            AiInsight insight = analyzeWithModel(request, startDate, endDate, book, entries, totalIncome, totalExpense,
                    balance, expenseCategories, incomeCategories, samples);

            BillAnalysisResponse response = BillAnalysisResponse.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .bookId(book != null ? book.getId() : null)
                    .bookName(book != null ? book.getName() : "全部账本")
                    .entryCount(entries.size())
                    .totalIncome(totalIncome)
                    .totalExpense(totalExpense)
                    .balance(balance)
                    .expenseCategories(expenseCategories)
                    .incomeCategories(incomeCategories)
                    .samples(samples)
                    .summary(insight.summary())
                    .observations(insight.observations())
                    .risks(insight.risks())
                    .suggestions(insight.suggestions())
                    .build();

            saveBillAnalysisHistory(userId, request, response);
            saveAiCallLog(userId, "BILL_ANALYSIS", null, true, start, null);
            return response;
        } catch (RuntimeException ex) {
            saveAiCallLog(userId, "BILL_ANALYSIS", null, false, start, ex.getMessage());
            throw ex;
        }
    }

    @Override
    public List<BillAnalysisHistoryVO> listBillAnalysisHistory(Long userId, Integer limit) {
        int size = limit == null ? DEFAULT_HISTORY_LIMIT : Math.min(Math.max(limit, 1), MAX_HISTORY_LIMIT);
        return aiBillAnalysisRecordMapper.selectList(new LambdaQueryWrapper<AiBillAnalysisRecord>()
                        .eq(AiBillAnalysisRecord::getUserId, userId)
                        .orderByDesc(AiBillAnalysisRecord::getCreatedAt)
                        .last("LIMIT " + size))
                .stream()
                .map(record -> BillAnalysisHistoryVO.builder()
                        .id(record.getId() != null ? String.valueOf(record.getId()) : null)
                        .createdAt(record.getCreatedAt())
                        .startDate(record.getStartDate())
                        .endDate(record.getEndDate())
                        .bookId(record.getBookId())
                        .bookName(record.getBookName())
                        .entryCount(record.getEntryCount() != null ? record.getEntryCount() : 0)
                        .summary(record.getSummary())
                        .question(record.getQuestion())
                        .build())
                .toList();
    }

    private ChatClient requireChatClient() {
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 客户端未初始化，请检查 app.ai.enabled 与 spring.ai.openai.api-key 配置");
        }
        return chatClient;
    }

    private void ensureAiEnabled() {
        if (!aiProperties.isEnabled()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "AI 功能未开启");
        }
    }

    private AgentDefinition requireAgent(String agentId) {
        AgentDefinition definition = AGENT_DEFINITIONS.get(agentId);
        if (definition == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Agent 不存在");
        }
        return definition;
    }

    private KnowledgeBase requireOwnedKnowledgeBase(Long userId, Long knowledgeBaseId) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "知识库不存在");
        }
        boolean accessible = Objects.equals(knowledgeBase.getUserId(), userId)
                || ("PUBLIC".equals(knowledgeBase.getVisibility()) && "ENABLED".equals(knowledgeBase.getStatus()));
        if (!accessible) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该知识库");
        }
        if (!"ENABLED".equals(knowledgeBase.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "知识库未启用");
        }
        return knowledgeBase;
    }

    private String normalizeConversationId(String conversationId) {
        return StringUtils.hasText(conversationId) ? conversationId.trim() : DEFAULT_CONVERSATION_ID;
    }

    private String buildChatPrompt(String message, String historyText) {
        if (!StringUtils.hasText(historyText)) {
            return message;
        }
        return """
                以下是当前会话最近的上下文，请参考这些内容回答，但不要原样复述上下文。
                %s

                当前用户消息：
                %s
                """.formatted(historyText, message);
    }

    private String buildAgentSystemPrompt(AgentDefinition agent, KnowledgeContext knowledgeContext) {
        String agentPrompt = promptTemplateLoader.load(agent.promptPath());
        String systemPrompt = resolveSystemPrompt();
        if (!StringUtils.hasText(knowledgeContext.contextText())) {
            return systemPrompt + "\n" + agentPrompt;
        }
        return systemPrompt + "\n" + agentPrompt + "\n"
                + "你还可以结合知识库上下文回答，若引用知识库，请优先依据给定上下文，不要编造不存在的内容。";
    }

    private String resolveSystemPrompt() {
        return promptTemplateLoader.resolve(aiProperties.getSystemPrompt(), "prompts/ai/system.md");
    }

    private String resolveBillAnalysisPrompt() {
        return promptTemplateLoader.resolve(aiProperties.getBillAnalysis().getSystemPrompt(), "prompts/ai/bill-analysis/system.md");
    }

    private String buildAgentPrompt(String message, String historyText, KnowledgeContext knowledgeContext) {
        StringBuilder prompt = new StringBuilder();
        if (StringUtils.hasText(historyText)) {
            prompt.append("会话上下文：\n").append(historyText).append("\n\n");
        }
        if (StringUtils.hasText(knowledgeContext.contextText())) {
            prompt.append("知识库上下文：\n").append(knowledgeContext.contextText()).append("\n\n");
        }
        prompt.append("用户消息：\n").append(message);
        return prompt.toString();
    }

    private String buildKnowledgeSystemPrompt(KnowledgeContext knowledgeContext) {
        String basePrompt = promptTemplateLoader.load("prompts/ai/knowledge/base.md");
        if (!StringUtils.hasText(knowledgeContext.contextText())) {
            return basePrompt + "\n" + promptTemplateLoader.load("prompts/ai/knowledge/empty-context.md");
        }
        return basePrompt;
    }

    private String buildKnowledgeUserPrompt(String message, String historyText, KnowledgeContext knowledgeContext) {
        StringBuilder prompt = new StringBuilder();
        if (StringUtils.hasText(historyText)) {
            prompt.append("会话上下文：\n").append(historyText).append("\n\n");
        }
        prompt.append("知识库上下文：\n")
                .append(StringUtils.hasText(knowledgeContext.contextText()) ? knowledgeContext.contextText() : "暂无可用知识片段")
                .append("\n\n用户问题：\n")
                .append(message);
        return prompt.toString();
    }

    private String loadConversationHistory(Long userId, String conversationId) {
        if (!aiProperties.getChat().isMemoryEnabled()) {
            return "";
        }

        List<AiConversationMessageVO> structuredMessages = listConversationMessages(userId, conversationId);
        if (!structuredMessages.isEmpty()) {
            List<AiConversationMessageVO> recentMessages = structuredMessages.stream()
                    .filter(item -> ROLE_USER.equals(item.getRole()) || ROLE_ASSISTANT.equals(item.getRole()))
                    .toList();
            int from = Math.max(0, recentMessages.size() - aiProperties.getChat().getMaxMessages());
            return recentMessages.subList(from, recentMessages.size()).stream()
                    .map(item -> item.getRole().toUpperCase(Locale.ROOT) + ": " + item.getContent())
                    .collect(Collectors.joining("\n"));
        }

        String key = buildConversationKey(userId, conversationId);
        Long size = stringRedisTemplate.opsForList().size(key);
        if (size == null || size <= 0) {
            return "";
        }
        long start = Math.max(0, size - aiProperties.getChat().getMaxMessages());
        List<String> items = stringRedisTemplate.opsForList().range(key, start, size - 1);
        if (items == null || items.isEmpty()) {
            return "";
        }
        return String.join("\n", items);
    }

    private void saveConversationTurn(Long userId,
                                      String conversationId,
                                      String role,
                                      String content,
                                      String agentId,
                                      Long knowledgeBaseId,
                                      List<AiCitationVO> citations) {
        if (!StringUtils.hasText(content)) {
            return;
        }

        if (aiProperties.getChat().isMemoryEnabled()) {
            String key = buildConversationKey(userId, conversationId);
            stringRedisTemplate.opsForList().rightPush(key, role.toUpperCase(Locale.ROOT) + ": " + content.trim());
            stringRedisTemplate.expire(key, CHAT_HISTORY_TTL_DAYS, TimeUnit.DAYS);
            Long size = stringRedisTemplate.opsForList().size(key);
            int maxMessages = aiProperties.getChat().getMaxMessages();
            if (size != null && size > maxMessages) {
                stringRedisTemplate.opsForList().trim(key, size - maxMessages, -1);
            }
        }

        AiConversationMessageVO message = AiConversationMessageVO.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .conversationId(conversationId)
                .role(role)
                .content(content.trim())
                .createdAt(LocalDateTime.now())
                .sources(citations == null ? List.of() : citations)
                .build();
        appendStructuredMessage(userId, conversationId, message);
        touchConversationSummary(userId, conversationId, role, content.trim(), agentId, knowledgeBaseId);
    }

    private void appendStructuredMessage(Long userId, String conversationId, AiConversationMessageVO message) {
        String key = buildConversationMessagesKey(userId, conversationId);
        stringRedisTemplate.opsForList().rightPush(key, serialize(message));
        stringRedisTemplate.expire(key, CHAT_HISTORY_TTL_DAYS, TimeUnit.DAYS);
        Long size = stringRedisTemplate.opsForList().size(key);
        int maxMessages = Math.max(aiProperties.getChat().getMaxMessages(), 40);
        if (size != null && size > maxMessages) {
            stringRedisTemplate.opsForList().trim(key, size - maxMessages, -1);
        }
    }

    private void touchConversationSummary(Long userId,
                                          String conversationId,
                                          String role,
                                          String content,
                                          String agentId,
                                          Long knowledgeBaseId) {
        AiConversationSummaryVO summary = readConversationSummary(userId, conversationId);
        int messageCount = 1;
        if (summary != null && summary.getMessageCount() != null) {
            messageCount = summary.getMessageCount() + 1;
        }

        String title = summary != null && StringUtils.hasText(summary.getTitle())
                ? summary.getTitle()
                : defaultConversationTitle(agentId, knowledgeBaseId);
        if (ROLE_USER.equals(role) && (summary == null || messageCount <= 2)) {
            title = abbreviate(content, 20);
        }

        AiConversationSummaryVO nextSummary = AiConversationSummaryVO.builder()
                .id(conversationId)
                .title(title)
                .agentId(StringUtils.hasText(agentId) ? agentId : (summary != null ? summary.getAgentId() : "general"))
                .knowledgeBaseId(knowledgeBaseId != null ? String.valueOf(knowledgeBaseId)
                        : (summary != null ? summary.getKnowledgeBaseId() : null))
                .lastMessagePreview(abbreviate(content, 80))
                .updatedAt(LocalDateTime.now())
                .messageCount(messageCount)
                .build();
        writeConversationSummary(userId, nextSummary);
    }

    private void writeConversationSummary(Long userId, AiConversationSummaryVO summary) {
        String summaryKey = buildConversationSummaryKey(userId, summary.getId());
        String indexKey = buildConversationIndexKey(userId);
        stringRedisTemplate.opsForValue().set(summaryKey, serialize(summary), CHAT_HISTORY_TTL_DAYS, TimeUnit.DAYS);
        stringRedisTemplate.opsForZSet().add(indexKey, summary.getId(), toScore(summary.getUpdatedAt()));
        stringRedisTemplate.expire(indexKey, CHAT_HISTORY_TTL_DAYS, TimeUnit.DAYS);
    }

    private AiConversationSummaryVO readConversationSummary(Long userId, String conversationId) {
        String json = stringRedisTemplate.opsForValue().get(buildConversationSummaryKey(userId, conversationId));
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, CONVERSATION_TYPE);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private AiConversationMessageVO deserializeMessage(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, MESSAGE_TYPE);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private String buildConversationKey(Long userId, String conversationId) {
        return "record:ai:chat:" + userId + ":" + conversationId;
    }

    private String buildConversationSummaryKey(Long userId, String conversationId) {
        return "record:ai:conversation:summary:" + userId + ":" + conversationId;
    }

    private String buildConversationMessagesKey(Long userId, String conversationId) {
        return "record:ai:conversation:messages:" + userId + ":" + conversationId;
    }

    private String buildConversationIndexKey(Long userId) {
        return "record:ai:conversation:index:" + userId;
    }

    private double toScore(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private String defaultConversationTitle(String agentId, Long knowledgeBaseId) {
        if (knowledgeBaseId != null) {
            KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
            if (knowledgeBase != null && StringUtils.hasText(knowledgeBase.getName())) {
                return knowledgeBase.getName() + " 对话";
            }
            return "知识库对话";
        }
        AgentDefinition definition = AGENT_DEFINITIONS.get(agentId);
        return definition != null ? definition.name() + " 对话" : "新会话";
    }

    private KnowledgeContext buildKnowledgeContext(Long userId, Long knowledgeBaseId, String message) {
        if (knowledgeBaseId == null) {
            return new KnowledgeContext(null, List.of(), "");
        }

        KnowledgeBase knowledgeBase = requireOwnedKnowledgeBase(userId, knowledgeBaseId);
        List<KnowledgeDocument> documents = knowledgeDocumentMapper.selectList(new LambdaQueryWrapper<KnowledgeDocument>()
                .eq(KnowledgeDocument::getKnowledgeBaseId, knowledgeBaseId)
                .orderByDesc(KnowledgeDocument::getUpdatedAt)
                .orderByDesc(KnowledgeDocument::getId));
        if (documents.isEmpty()) {
            return new KnowledgeContext(knowledgeBase, List.of(), "");
        }

        List<String> keywords = extractKeywords(message);
        List<ScoredDocument> scoredDocuments = documents.stream()
                .map(document -> new ScoredDocument(document, scoreDocument(document, message, keywords)))
                .sorted(Comparator.comparing(ScoredDocument::score).reversed()
                        .thenComparing(item -> item.document().getUpdatedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(MAX_KNOWLEDGE_CITATIONS)
                .toList();

        List<AiCitationVO> citations = scoredDocuments.stream()
                .map(item -> toCitation(item.document(), item.score()))
                .toList();
        String context = scoredDocuments.stream()
                .map(item -> buildDocumentContext(item.document()))
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n\n"));
        return new KnowledgeContext(knowledgeBase, citations, context);
    }

    private List<String> extractKeywords(String message) {
        if (!StringUtils.hasText(message)) {
            return List.of();
        }
        Set<String> keywords = new LinkedHashSet<>();
        String normalized = message.trim().toLowerCase(Locale.ROOT);
        for (String item : normalized.split("[\\s,，。！？;；:/\\\\|]+")) {
            if (item.length() >= 2) {
                keywords.add(item);
            }
        }
        if (keywords.isEmpty()) {
            keywords.add(normalized.length() > 12 ? normalized.substring(0, 12) : normalized);
        }
        return List.copyOf(keywords);
    }

    private double scoreDocument(KnowledgeDocument document, String message, List<String> keywords) {
        String haystack = String.join(" ",
                defaultString(document.getTitle()),
                defaultString(document.getFileName()),
                defaultString(document.getFilePath()),
                defaultString(document.getSourceType()))
                .toLowerCase(Locale.ROOT);
        double score = 0.1D;
        for (String keyword : keywords) {
            if (haystack.contains(keyword)) {
                score += 0.3D;
            }
        }
        if (StringUtils.hasText(message) && haystack.contains(message.trim().toLowerCase(Locale.ROOT))) {
            score += 0.4D;
        }
        if ("VECTORIZED".equals(document.getStatus())) {
            score += 0.2D;
        }
        return score;
    }

    private String buildDocumentContext(KnowledgeDocument document) {
        StringBuilder context = new StringBuilder()
                .append("文档标题：").append(defaultString(document.getTitle())).append("\n")
                .append("来源类型：").append(defaultString(document.getSourceType())).append("\n");
        if (StringUtils.hasText(document.getFileName())) {
            context.append("文件名：").append(document.getFileName()).append("\n");
        }
        if (StringUtils.hasText(document.getFilePath())) {
            context.append("文件路径：").append(document.getFilePath()).append("\n");
        }
        if (StringUtils.hasText(document.getStatus())) {
            context.append("文档状态：").append(document.getStatus()).append("\n");
        }
        String textPreview = loadLocalTextPreview(document);
        if (StringUtils.hasText(textPreview)) {
            context.append("文档片段：").append(textPreview);
        }
        return context.toString().trim();
    }

    private String loadLocalTextPreview(KnowledgeDocument document) {
        if (!StringUtils.hasText(document.getFilePath())) {
            return "";
        }
        try {
            Path path = Paths.get(document.getFilePath());
            if (!Files.exists(path) || Files.isDirectory(path)) {
                return "";
            }
            byte[] bytes = Files.readAllBytes(path);
            String content = new String(bytes, StandardCharsets.UTF_8);
            return abbreviate(content.replaceAll("\\s+", " "), 240);
        } catch (IOException | InvalidPathException ex) {
            return "";
        }
    }

    private AiCitationVO toCitation(KnowledgeDocument document, double score) {
        String snippet = loadLocalTextPreview(document);
        if (!StringUtils.hasText(snippet)) {
            snippet = "标题：" + defaultString(document.getTitle())
                    + "；文件：" + defaultString(document.getFileName())
                    + "；路径：" + defaultString(document.getFilePath());
        }
        return AiCitationVO.builder()
                .id(document.getId() != null ? String.valueOf(document.getId()) : null)
                .title(defaultString(document.getTitle()))
                .snippet(abbreviate(snippet, 240))
                .sourceType(document.getSourceType())
                .sourcePath(document.getFilePath())
                .score(score)
                .build();
    }

    private String abbreviate(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String value = text.trim();
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 数据序列化失败");
        }
    }

    private BigDecimal sumAmountByType(List<LedgerEntry> entries, LedgerType ledgerType) {
        return entries.stream()
                .filter(entry -> entry.getType() == ledgerType)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Long, LedgerBook> loadBookMap(List<LedgerEntry> entries) {
        List<Long> bookIds = entries.stream()
                .map(LedgerEntry::getBookId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (bookIds.isEmpty()) {
            return Map.of();
        }
        return ledgerBookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(LedgerBook::getId, item -> item));
    }

    private Map<Long, String> loadEntryCategoryMap(List<LedgerEntry> entries) {
        List<Long> entryIds = entries.stream().map(LedgerEntry::getId).toList();
        if (entryIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<Long>> entryTagIds = ledgerEntryTagRelMapper.selectList(new LambdaQueryWrapper<LedgerEntryTagRel>()
                        .in(LedgerEntryTagRel::getEntryId, entryIds))
                .stream()
                .collect(Collectors.groupingBy(LedgerEntryTagRel::getEntryId,
                        Collectors.mapping(LedgerEntryTagRel::getTagId, Collectors.toList())));

        List<Long> tagIds = entryTagIds.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        Map<Long, UserTag> tagMap = tagIds.isEmpty()
                ? Map.of()
                : userTagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(UserTag::getId, tag -> tag));

        Map<Long, String> result = new LinkedHashMap<>();
        for (LedgerEntry entry : entries) {
            List<Long> ids = entryTagIds.get(entry.getId());
            if (ids == null || ids.isEmpty()) {
                result.put(entry.getId(), "未分类");
                continue;
            }
            String category = ids.stream()
                    .map(tagMap::get)
                    .filter(Objects::nonNull)
                    .map(UserTag::getName)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.joining(" / "));
            result.put(entry.getId(), StringUtils.hasText(category) ? category : "未分类");
        }
        return result;
    }

    private List<BillAnalysisResponse.CategoryAmountVO> buildCategoryAmounts(List<LedgerEntry> entries,
                                                                             Map<Long, String> categoryMap,
                                                                             LedgerType ledgerType) {
        Map<String, BigDecimal> amountMap = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (LedgerEntry entry : entries) {
            if (entry.getType() != ledgerType) {
                continue;
            }
            String category = categoryMap.getOrDefault(entry.getId(), "未分类");
            amountMap.merge(category, entry.getAmount(), BigDecimal::add);
            total = total.add(entry.getAmount());
        }
        BigDecimal divisor = total.compareTo(BigDecimal.ZERO) > 0 ? total : BigDecimal.ONE;
        return amountMap.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(CATEGORY_TOP_N)
                .map(item -> BillAnalysisResponse.CategoryAmountVO.builder()
                        .name(item.getKey())
                        .amount(item.getValue())
                        .ratio(item.getValue().divide(divisor, 4, RoundingMode.HALF_UP))
                        .build())
                .toList();
    }

    private List<BillAnalysisResponse.EntrySampleVO> buildSamples(List<LedgerEntry> entries,
                                                                  Map<Long, LedgerBook> bookMap,
                                                                  Map<Long, String> categoryMap) {
        return entries.stream()
                .sorted(Comparator.comparing(LedgerEntry::getAmount).reversed())
                .limit(SAMPLE_TOP_N)
                .map(entry -> {
                    LedgerBook book = bookMap.get(entry.getBookId());
                    return BillAnalysisResponse.EntrySampleVO.builder()
                            .entryDate(entry.getEntryDate())
                            .type(entry.getType().name())
                            .amount(entry.getAmount())
                            .bookName(book != null ? book.getName() : null)
                            .category(categoryMap.getOrDefault(entry.getId(), "未分类"))
                            .remark(entry.getRemark())
                            .build();
                })
                .toList();
    }

    private AiInsight analyzeWithModel(BillAnalysisRequest request,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       LedgerBook book,
                                       List<LedgerEntry> entries,
                                       BigDecimal totalIncome,
                                       BigDecimal totalExpense,
                                       BigDecimal balance,
                                       List<BillAnalysisResponse.CategoryAmountVO> expenseCategories,
                                       List<BillAnalysisResponse.CategoryAmountVO> incomeCategories,
                                       List<BillAnalysisResponse.EntrySampleVO> samples) {
        if (entries.isEmpty()) {
            return new AiInsight("分析区间内没有账单数据。", List.of(), List.of(), List.of("先补充账单记录后再发起分析。"));
        }

        String payload;
        try {
            Map<String, Object> promptData = new LinkedHashMap<>();
            promptData.put("startDate", startDate);
            promptData.put("endDate", endDate);
            promptData.put("bookName", book != null ? book.getName() : "全部账本");
            promptData.put("entryCount", entries.size());
            promptData.put("totalIncome", totalIncome);
            promptData.put("totalExpense", totalExpense);
            promptData.put("balance", balance);
            promptData.put("expenseCategories", expenseCategories);
            promptData.put("incomeCategories", incomeCategories);
            promptData.put("samples", samples);
            promptData.put("question", request.getQuestion());
            payload = objectMapper.writeValueAsString(promptData);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "账单分析上下文构建失败");
        }

        String content = requireChatClient().prompt()
                .system(resolveBillAnalysisPrompt())
                .user("""
                        请基于下面的账单统计数据输出 JSON，字段必须严格为：
                        summary: string
                        observations: string[]
                        risks: string[]
                        suggestions: string[]

                        不要输出 markdown，不要补充额外字段，不要编造不存在的数据。
                        账单数据：
                        %s
                        """.formatted(payload))
                .call()
                .content();

        if (!StringUtils.hasText(content)) {
            return fallbackInsight(totalIncome, totalExpense, balance, expenseCategories);
        }

        try {
            AiInsightPayload aiPayload = objectMapper.readValue(extractJson(content), AiInsightPayload.class);
            return new AiInsight(
                    defaultText(aiPayload.getSummary(), "已完成账单分析。"),
                    defaultList(aiPayload.getObservations()),
                    defaultList(aiPayload.getRisks()),
                    defaultList(aiPayload.getSuggestions())
            );
        } catch (Exception ex) {
            return fallbackInsight(totalIncome, totalExpense, balance, expenseCategories);
        }
    }

    private AiInsight fallbackInsight(BigDecimal totalIncome,
                                      BigDecimal totalExpense,
                                      BigDecimal balance,
                                      List<BillAnalysisResponse.CategoryAmountVO> expenseCategories) {
        List<String> observations = new ArrayList<>();
        if (!expenseCategories.isEmpty()) {
            BillAnalysisResponse.CategoryAmountVO topCategory = expenseCategories.get(0);
            observations.add("支出最高的分类是 %s，占总支出的 %s。".formatted(
                    topCategory.getName(),
                    topCategory.getRatio().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) + "%"
            ));
        }

        List<String> risks = new ArrayList<>();
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            risks.add("当前分析周期支出高于收入，存在透支风险。");
        }

        List<String> suggestions = new ArrayList<>();
        suggestions.add("优先复核高频大额支出，并为主要支出分类设置预算。");

        return new AiInsight(
                "本次账单分析已完成。总收入 %s，总支出 %s，结余 %s。".formatted(totalIncome, totalExpense, balance),
                observations,
                risks,
                suggestions
        );
    }

    private void saveBillAnalysisHistory(Long userId, BillAnalysisRequest request, BillAnalysisResponse response) {
        AiBillAnalysisRecord record = new AiBillAnalysisRecord();
        record.setUserId(userId);
        record.setBookId(response.getBookId());
        record.setBookName(response.getBookName());
        record.setStartDate(response.getStartDate());
        record.setEndDate(response.getEndDate());
        record.setEntryCount(response.getEntryCount());
        record.setQuestion(request.getQuestion());
        record.setSummary(response.getSummary());
        aiBillAnalysisRecordMapper.insert(record);
    }

    private void saveAiCallLog(Long userId,
                               String scene,
                               String conversationId,
                               boolean success,
                               long startTimeMillis,
                               String errorMessage) {
        try {
            AiCallLog log = new AiCallLog();
            log.setUserId(userId);
            log.setScene(scene);
            log.setProvider(aiProperties.getProvider());
            log.setModel(null);
            log.setConversationId(conversationId);
            log.setSuccessFlag(success ? 1 : 0);
            log.setDurationMs(System.currentTimeMillis() - startTimeMillis);
            log.setErrorMessage(shortErrorMessage(errorMessage));
            log.setCalledAt(LocalDateTime.now());
            aiCallLogMapper.insert(log);
        } catch (Exception ignored) {
        }
    }

    private String shortErrorMessage(String errorMessage) {
        if (!StringUtils.hasText(errorMessage)) {
            return null;
        }
        String message = errorMessage.trim();
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }

    private String defaultText(String text, String fallback) {
        return StringUtils.hasText(text) ? text : fallback;
    }

    private List<String> defaultList(List<String> items) {
        return items == null ? List.of() : items.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
    }

    private record AiInsight(String summary, List<String> observations, List<String> risks, List<String> suggestions) {
    }

    private record AgentDefinition(String id,
                                   String name,
                                   String description,
                                   List<String> capabilities,
                                   String promptPath) {
    }

    private record KnowledgeContext(KnowledgeBase knowledgeBase, List<AiCitationVO> citations, String contextText) {
    }

    private record ScoredDocument(KnowledgeDocument document, double score) {
    }

    private static class AiInsightPayload {
        private String summary;
        private List<String> observations;
        private List<String> risks;
        private List<String> suggestions;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<String> getObservations() {
            return observations;
        }

        public void setObservations(List<String> observations) {
            this.observations = observations;
        }

        public List<String> getRisks() {
            return risks;
        }

        public void setRisks(List<String> risks) {
            this.risks = risks;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions;
        }
    }
}
