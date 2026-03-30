package com.record.modules.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.record.common.config.AiProperties;
import com.record.common.enums.LedgerType;
import com.record.common.exception.BusinessException;
import com.record.common.exception.ErrorCode;
import com.record.modules.ai.mapper.AiBillAnalysisRecordMapper;
import com.record.modules.ai.mapper.AiCallLogMapper;
import com.record.modules.ai.model.dto.AiChatRequest;
import com.record.modules.ai.model.dto.BillAnalysisRequest;
import com.record.modules.ai.model.entity.AiBillAnalysisRecord;
import com.record.modules.ai.model.entity.AiCallLog;
import com.record.modules.ai.model.vo.AiChatResponse;
import com.record.modules.ai.model.vo.BillAnalysisHistoryVO;
import com.record.modules.ai.model.vo.BillAnalysisResponse;
import com.record.modules.ai.service.AiService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    private static final String DEFAULT_CONVERSATION_ID = "default";
    private static final String ROLE_USER = "USER";
    private static final String ROLE_ASSISTANT = "ASSISTANT";
    private static final int CATEGORY_TOP_N = 6;
    private static final int SAMPLE_TOP_N = 12;
    private static final int DEFAULT_HISTORY_LIMIT = 10;
    private static final int MAX_HISTORY_LIMIT = 50;
    private static final long CHAT_HISTORY_TTL_DAYS = 7L;

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final AiProperties aiProperties;
    private final AiBillAnalysisRecordMapper aiBillAnalysisRecordMapper;
    private final AiCallLogMapper aiCallLogMapper;
    private final LedgerEntryMapper ledgerEntryMapper;
    private final LedgerBookMapper ledgerBookMapper;
    private final LedgerEntryTagRelMapper ledgerEntryTagRelMapper;
    private final UserTagMapper userTagMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public AiServiceImpl(ObjectProvider<ChatClient> chatClientProvider,
                         AiProperties aiProperties,
                         AiBillAnalysisRecordMapper aiBillAnalysisRecordMapper,
                         AiCallLogMapper aiCallLogMapper,
                         LedgerEntryMapper ledgerEntryMapper,
                         LedgerBookMapper ledgerBookMapper,
                         LedgerEntryTagRelMapper ledgerEntryTagRelMapper,
                         UserTagMapper userTagMapper,
                         StringRedisTemplate stringRedisTemplate,
                         ObjectMapper objectMapper) {
        this.chatClientProvider = chatClientProvider;
        this.aiProperties = aiProperties;
        this.aiBillAnalysisRecordMapper = aiBillAnalysisRecordMapper;
        this.aiCallLogMapper = aiCallLogMapper;
        this.ledgerEntryMapper = ledgerEntryMapper;
        this.ledgerBookMapper = ledgerBookMapper;
        this.ledgerEntryTagRelMapper = ledgerEntryTagRelMapper;
        this.userTagMapper = userTagMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        ensureAiEnabled();
        long start = System.currentTimeMillis();
        String conversationId = normalizeConversationId(request.getConversationId());
        String historyText = loadConversationHistory(userId, conversationId);
        try {
            String reply = requireChatClient().prompt()
                    .system(aiProperties.getSystemPrompt())
                    .user(buildChatPrompt(request.getMessage(), historyText))
                    .call()
                    .content();

            if (!StringUtils.hasText(reply)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 未返回有效内容");
            }

            saveConversationTurn(userId, conversationId, ROLE_USER, request.getMessage());
            saveConversationTurn(userId, conversationId, ROLE_ASSISTANT, reply);
            saveAiCallLog(userId, "CHAT", conversationId, true, start, null);

            return AiChatResponse.builder()
                    .conversationId(conversationId)
                    .reply(reply)
                    .build();
        } catch (RuntimeException ex) {
            saveAiCallLog(userId, "CHAT", conversationId, false, start, ex.getMessage());
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
                .system(aiProperties.getSystemPrompt())
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
                    saveConversationTurn(userId, conversationId, ROLE_USER, request.getMessage());
                    saveConversationTurn(userId, conversationId, ROLE_ASSISTANT, fullReply.toString());
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
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 客户端未初始化，请检查 app.ai.enabled 和模型配置");
        }
        return chatClient;
    }

    private void ensureAiEnabled() {
        if (!aiProperties.isEnabled()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "AI 功能未开启");
        }
    }

    private String normalizeConversationId(String conversationId) {
        return StringUtils.hasText(conversationId) ? conversationId.trim() : DEFAULT_CONVERSATION_ID;
    }

    private String buildChatPrompt(String message, String historyText) {
        if (!StringUtils.hasText(historyText)) {
            return message;
        }
        return """
                以下是当前会话的最近上下文，请参考这些内容回答，但不要重复输出上下文原文。

                %s

                当前用户消息：
                %s
                """.formatted(historyText, message);
    }

    private String loadConversationHistory(Long userId, String conversationId) {
        if (!aiProperties.getChat().isMemoryEnabled()) {
            return "";
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

    private void saveConversationTurn(Long userId, String conversationId, String role, String content) {
        if (!aiProperties.getChat().isMemoryEnabled() || !StringUtils.hasText(content)) {
            return;
        }
        String key = buildConversationKey(userId, conversationId);
        stringRedisTemplate.opsForList().rightPush(key, role + ": " + content.trim());
        stringRedisTemplate.expire(key, CHAT_HISTORY_TTL_DAYS, TimeUnit.DAYS);
        Long size = stringRedisTemplate.opsForList().size(key);
        int maxMessages = aiProperties.getChat().getMaxMessages();
        if (size != null && size > maxMessages) {
            stringRedisTemplate.opsForList().trim(key, size - maxMessages, -1);
        }
    }

    private String buildConversationKey(Long userId, String conversationId) {
        return "record:ai:chat:" + userId + ":" + conversationId;
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
                .system(aiProperties.getBillAnalysis().getSystemPrompt())
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
            observations.add("支出最高的分类是%s，占总支出的%s。".formatted(
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
                "本次账单分析已完成。总收入%s，总支出%s，结余%s。".formatted(totalIncome, totalExpense, balance),
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
