package com.record.modules.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.config.AiProperties;
import com.record.common.enums.LedgerType;
import com.record.common.exception.BusinessException;
import com.record.common.exception.ErrorCode;
import com.record.modules.ai.mapper.AiBillAnalysisRecordMapper;
import com.record.modules.ai.mapper.AiCallLogMapper;
import com.record.modules.ai.model.dto.AiChatRequest;
import com.record.modules.ai.model.dto.BillAnalysisRequest;
import com.record.modules.ai.model.dto.CreateConversationRequest;
import com.record.modules.ai.model.entity.AiBillAnalysisRecord;
import com.record.modules.ai.model.entity.AiCallLog;
import com.record.modules.ai.model.vo.AiConversationMessageVO;
import com.record.modules.ai.model.vo.AiConversationSummaryVO;
import com.record.modules.ai.model.vo.BillAnalysisHistoryVO;
import com.record.modules.ai.model.vo.BillAnalysisResponse;
import com.record.modules.ai.prompt.PromptTemplateLoader;
import com.record.modules.ai.service.AiService;
import com.record.modules.ledger.model.vo.LedgerBookVO;
import com.record.modules.ledger.model.vo.LedgerEntryVO;
import com.record.modules.ledger.service.LedgerService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AiServiceImpl implements AiService {

    private static final String DEFAULT_CONVERSATION_ID = "default";
    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final long CHAT_HISTORY_TTL_DAYS = 7L;
    private static final int MAX_CONVERSATIONS = 50;
    private static final int BILL_ANALYSIS_SAMPLE_LIMIT = 12;
    private static final int BILL_ANALYSIS_SUMMARY_MAX_LENGTH = 1000;
    private static final long BILL_ANALYSIS_HISTORY_MAX_SIZE = 50L;
    private static final String CATEGORY_UNCATEGORIZED = "未分类";
    private static final TypeReference<AiConversationSummaryVO> CONVERSATION_TYPE = new TypeReference<>() { };
    private static final TypeReference<AiConversationMessageVO> MESSAGE_TYPE = new TypeReference<>() { };

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final AiProperties aiProperties;
    private final AiCallLogMapper aiCallLogMapper;
    private final AiBillAnalysisRecordMapper aiBillAnalysisRecordMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final PromptTemplateLoader promptTemplateLoader;
    private final LedgerService ledgerService;

    public AiServiceImpl(ObjectProvider<ChatClient> chatClientProvider,
                         AiProperties aiProperties,
                         AiCallLogMapper aiCallLogMapper,
                         AiBillAnalysisRecordMapper aiBillAnalysisRecordMapper,
                         StringRedisTemplate stringRedisTemplate,
                         ObjectMapper objectMapper,
                         PromptTemplateLoader promptTemplateLoader,
                         LedgerService ledgerService) {
        this.chatClientProvider = chatClientProvider;
        this.aiProperties = aiProperties;
        this.aiCallLogMapper = aiCallLogMapper;
        this.aiBillAnalysisRecordMapper = aiBillAnalysisRecordMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.promptTemplateLoader = promptTemplateLoader;
        this.ledgerService = ledgerService;
    }

    @Override
    public Flux<ServerSentEvent<String>> streamChat(Long userId, AiChatRequest request) {
        long startTime = System.currentTimeMillis();
        String conversationId = normalizeConversationId(request.getConversationId());
        String message = request.getMessage().trim();
        String historyText = loadConversationHistory(userId, conversationId);
        StringBuilder replyBuilder = new StringBuilder();

        saveConversationTurn(userId, conversationId, ROLE_USER, message);

        return requireChatClient().prompt()
                .system(resolveSystemPrompt())
                .user(buildChatPrompt(message, historyText))
                .stream()
                .content()
                .filter(StringUtils::hasText)
                .map(chunk -> {
                    replyBuilder.append(chunk);
                    return sseEvent("message", chunk);
                })
                .concatWith(Flux.defer(() -> {
                    String reply = replyBuilder.toString().trim();
                    if (StringUtils.hasText(reply)) {
                        saveConversationTurn(userId, conversationId, ROLE_ASSISTANT, reply);
                    }
                    saveAiCallLog(userId, conversationId, true, startTime, null);
                    return Flux.just(sseEvent("done", "DONE"));
                }))
                .doOnError(error -> saveAiCallLog(
                        userId,
                        conversationId,
                        false,
                        startTime,
                        error != null ? error.getMessage() : "stream error"
                ));
    }

    @Override
    public List<AiConversationSummaryVO> listConversations(Long userId) {
        Set<String> ids = stringRedisTemplate.opsForZSet()
                .reverseRange(buildConversationIndexKey(userId), 0, MAX_CONVERSATIONS - 1);
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
        String title = request != null && StringUtils.hasText(request.getTitle())
                ? request.getTitle().trim()
                : "新会话";

        AiConversationSummaryVO summary = AiConversationSummaryVO.builder()
                .id(conversationId)
                .title(title)
                .lastMessagePreview("会话已创建")
                .updatedAt(LocalDateTime.now())
                .messageCount(0)
                .build();
        writeConversationSummary(userId, summary);
        return summary;
    }

    @Override
    public List<AiConversationMessageVO> listConversationMessages(Long userId, String conversationId) {
        String normalizedConversationId = normalizeConversationId(conversationId);
        List<String> items = stringRedisTemplate.opsForList()
                .range(buildConversationMessagesKey(userId, normalizedConversationId), 0, -1);
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
    public BillAnalysisResponse analyzeBills(Long userId, BillAnalysisRequest request) {
        long startTime = System.currentTimeMillis();
        AiProperties.BillAnalysis cfg = aiProperties.getBillAnalysis();

        // 1. 规整入参：日期范围、账本、单次最大账单条数
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        // 不传 startDate 时按配置的 default-days 往前推
        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : endDate.minusDays(Math.max(cfg.getDefaultDays(), 1));
        // limit 上限永远不能超过 cfg.getMaxEntries()，避免 prompt 过大
        int safeLimit = resolveLimit(request.getLimit(), cfg.getMaxEntries());
        Long bookId = request.getBookId();
        String question = StringUtils.hasText(request.getQuestion()) ? request.getQuestion().trim() : null;

        // 2. 取账本名（仅用于回显），bookId 为空表示分析所有账本
        LedgerBookVO book = bookId != null ? ledgerService.findBook(userId, bookId) : null;
        String bookName = book != null ? book.getName() : null;

        // 3. 拉账单数据并做聚合统计
        List<LedgerEntryVO> entries = ledgerService.rangeEntries(userId, bookId, startDate, endDate, safeLimit);
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        // 按"分类（标签）名 + 收支方向"聚合，使用 LinkedHashMap 保留首次出现顺序，便于稳定输出
        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();
        Map<String, BigDecimal> incomeByCategory = new LinkedHashMap<>();
        for (LedgerEntryVO entry : entries) {
            BigDecimal amount = entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
            // 多标签时按每个标签都计入一次，无标签则归到"未分类"
            List<String> categories = resolveCategories(entry);
            if (entry.getType() == LedgerType.INCOME) {
                totalIncome = totalIncome.add(amount);
                for (String category : categories) {
                    incomeByCategory.merge(category, amount, BigDecimal::add);
                }
            } else if (entry.getType() == LedgerType.EXPENSE) {
                totalExpense = totalExpense.add(amount);
                for (String category : categories) {
                    expenseByCategory.merge(category, amount, BigDecimal::add);
                }
            }
        }
        BigDecimal balance = totalIncome.subtract(totalExpense);

        // 4. 构造分类占比 VO 与 Top 样本
        List<BillAnalysisResponse.CategoryAmountVO> expenseCategories = buildCategoryList(expenseByCategory, totalExpense);
        List<BillAnalysisResponse.CategoryAmountVO> incomeCategories = buildCategoryList(incomeByCategory, totalIncome);
        List<BillAnalysisResponse.EntrySampleVO> samples = buildSamples(entries, bookName);

        // 5. 调用 LLM 生成结构化分析
        AiAnalysisOutput analysis;
        String errorMessage = null;
        try {
            analysis = invokeBillAnalysisModel(startDate, endDate, bookName, entries.size(),
                    totalIncome, totalExpense, balance,
                    expenseCategories, incomeCategories, samples, question);
        } catch (Exception ex) {
            // 模型调用失败时不抛错，前端仍能拿到聚合结果，summary 写一段降级文案
            errorMessage = ex.getMessage();
            analysis = AiAnalysisOutput.fallback("AI 分析失败：" + safeText(errorMessage));
        }

        // 6. 落库一条历史记录（仅 summary，明细字段不持久化）
        AiBillAnalysisRecord record = new AiBillAnalysisRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBookName(bookName);
        record.setStartDate(startDate);
        record.setEndDate(endDate);
        record.setEntryCount(entries.size());
        record.setQuestion(question);
        record.setSummary(truncate(analysis.summary(), BILL_ANALYSIS_SUMMARY_MAX_LENGTH));
        try {
            aiBillAnalysisRecordMapper.insert(record);
        } catch (Exception ignored) {
            // 历史落库失败不影响主响应，避免阻断用户体验
        }

        // 7. 写一条 AI 调用日志（成功 / 失败都写）
        saveBillAnalysisCallLog(userId, errorMessage == null, startTime, errorMessage);

        return BillAnalysisResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .bookId(bookId)
                .bookName(bookName)
                .entryCount(entries.size())
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .expenseCategories(expenseCategories)
                .incomeCategories(incomeCategories)
                .samples(samples)
                .summary(analysis.summary())
                .observations(analysis.observations())
                .risks(analysis.risks())
                .suggestions(analysis.suggestions())
                .build();
    }

    @Override
    public Page<BillAnalysisHistoryVO> listBillAnalysisHistory(Long userId, long current, long size) {
        // 兜底：current 至少为 1；size 钳制在 [1, BILL_ANALYSIS_HISTORY_MAX_SIZE]，避免一次拉太多
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), BILL_ANALYSIS_HISTORY_MAX_SIZE);

        Page<AiBillAnalysisRecord> page = aiBillAnalysisRecordMapper.selectPage(
                new Page<>(safeCurrent, safeSize),
                new LambdaQueryWrapper<AiBillAnalysisRecord>()
                        .eq(AiBillAnalysisRecord::getUserId, userId)
                        .orderByDesc(AiBillAnalysisRecord::getCreatedAt)
                        .orderByDesc(AiBillAnalysisRecord::getId)
        );

        Page<BillAnalysisHistoryVO> result = new Page<>(safeCurrent, safeSize, page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::toBillAnalysisHistoryVO)
                .toList());
        return result;
    }

    /**
     * 把 limit 钳制到 [1, maxEntries]，传 null 时按 maxEntries 兜底。
     * 这里很重要，因为 limit 决定了 prompt 大小，过大会浪费 token、还可能触发模型上下文上限。
     */
    private int resolveLimit(Integer requestedLimit, int maxEntries) {
        int max = Math.max(maxEntries, 1);
        if (requestedLimit == null || requestedLimit <= 0) {
            return max;
        }
        return Math.min(requestedLimit, max);
    }

    /**
     * 从一条账单的标签里取分类名。
     * 没有标签时返回单元素列表 ["未分类"]，保证每条账单至少计入一次分类。
     */
    private List<String> resolveCategories(LedgerEntryVO entry) {
        List<LedgerEntryVO.TagItemVO> tags = entry.getTags();
        if (tags == null || tags.isEmpty()) {
            return List.of(CATEGORY_UNCATEGORIZED);
        }
        List<String> names = tags.stream()
                .map(LedgerEntryVO.TagItemVO::getName)
                .filter(StringUtils::hasText)
                .toList();
        return names.isEmpty() ? List.of(CATEGORY_UNCATEGORIZED) : names;
    }

    /**
     * 把 "分类 -> 金额" 转成响应里的占比列表，并按金额从大到小排。
     * total 为 0 时占比固定 0，避免除零。
     */
    private List<BillAnalysisResponse.CategoryAmountVO> buildCategoryList(Map<String, BigDecimal> map, BigDecimal total) {
        if (map.isEmpty()) {
            return List.of();
        }
        BigDecimal safeTotal = total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : total;
        return map.entrySet().stream()
                .map(item -> BillAnalysisResponse.CategoryAmountVO.builder()
                        .name(item.getKey())
                        .amount(item.getValue())
                        .ratio(total.compareTo(BigDecimal.ZERO) == 0
                                ? BigDecimal.ZERO
                                : item.getValue().divide(safeTotal, 4, RoundingMode.HALF_UP))
                        .build())
                .sorted(Comparator.comparing(
                        BillAnalysisResponse.CategoryAmountVO::getAmount,
                        Comparator.reverseOrder()))
                .toList();
    }

    /**
     * 取金额 Top N 的账单作为样本喂给模型。
     * 这里没用全部账单，主要是控制 prompt 大小；同时高金额样本对 AI 判断"风险"也更有信息量。
     */
    private List<BillAnalysisResponse.EntrySampleVO> buildSamples(List<LedgerEntryVO> entries, String defaultBookName) {
        return entries.stream()
                .sorted(Comparator.comparing(
                        (LedgerEntryVO entry) -> entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO,
                        Comparator.reverseOrder()))
                .limit(BILL_ANALYSIS_SAMPLE_LIMIT)
                .map(entry -> BillAnalysisResponse.EntrySampleVO.builder()
                        .entryDate(entry.getEntryDate())
                        .type(entry.getType() != null ? entry.getType().name() : null)
                        .amount(entry.getAmount())
                        .bookName(defaultBookName)
                        .category(joinCategoryNames(entry))
                        .remark(entry.getRemark())
                        .build())
                .toList();
    }

    /**
     * 把一条账单的所有标签名拼成 "饮食/通勤" 形式，专门给样本展示用。
     */
    private String joinCategoryNames(LedgerEntryVO entry) {
        List<LedgerEntryVO.TagItemVO> tags = entry.getTags();
        if (tags == null || tags.isEmpty()) {
            return CATEGORY_UNCATEGORIZED;
        }
        String joined = tags.stream()
                .map(LedgerEntryVO.TagItemVO::getName)
                .filter(StringUtils::hasText)
                .reduce((left, right) -> left + "/" + right)
                .orElse(CATEGORY_UNCATEGORIZED);
        return StringUtils.hasText(joined) ? joined : CATEGORY_UNCATEGORIZED;
    }

    /**
     * 真正调模型：拼 system + user prompt，要求模型返回严格 JSON，再解析。
     * 解析失败时降级为 fallback：把原始文本塞 summary，其余三段为空。
     */
    private AiAnalysisOutput invokeBillAnalysisModel(LocalDate startDate, LocalDate endDate, String bookName,
                                                    int entryCount, BigDecimal totalIncome, BigDecimal totalExpense,
                                                    BigDecimal balance,
                                                    List<BillAnalysisResponse.CategoryAmountVO> expenseCategories,
                                                    List<BillAnalysisResponse.CategoryAmountVO> incomeCategories,
                                                    List<BillAnalysisResponse.EntrySampleVO> samples,
                                                    String question) {
        // 系统提示词优先用配置覆盖，没配则读 prompts/ai/bill-analysis/system.md
        String systemPrompt = promptTemplateLoader.resolve(
                aiProperties.getBillAnalysis().getSystemPrompt(),
                "prompts/ai/bill-analysis/system.md");
        String userPrompt = buildBillAnalysisPrompt(startDate, endDate, bookName, entryCount,
                totalIncome, totalExpense, balance,
                expenseCategories, incomeCategories, samples, question);

        // 同步调用：账单分析数据量可控，没必要走流式
        String reply = requireChatClient().prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        return parseAnalysisJson(reply);
    }

    /**
     * 把统计结果拼成给模型看的纯文本。
     * 故意不用 JSON 包装，避免模型把输入 JSON 当成输出格式照抄。
     */
    private String buildBillAnalysisPrompt(LocalDate startDate, LocalDate endDate, String bookName,
                                           int entryCount, BigDecimal totalIncome, BigDecimal totalExpense,
                                           BigDecimal balance,
                                           List<BillAnalysisResponse.CategoryAmountVO> expenseCategories,
                                           List<BillAnalysisResponse.CategoryAmountVO> incomeCategories,
                                           List<BillAnalysisResponse.EntrySampleVO> samples,
                                           String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("以下是用户账单的结构化概览，请基于这些数据生成 JSON 格式的分析。\n\n");
        sb.append("【时间范围】").append(startDate).append(" 至 ").append(endDate).append('\n');
        sb.append("【账本】").append(StringUtils.hasText(bookName) ? bookName : "全部账本").append('\n');
        sb.append("【账单条数】").append(entryCount).append('\n');
        sb.append("【总收入】").append(formatAmount(totalIncome)).append('\n');
        sb.append("【总支出】").append(formatAmount(totalExpense)).append('\n');
        sb.append("【结余】").append(formatAmount(balance)).append("\n\n");

        sb.append("【支出分类占比】\n");
        appendCategoryLines(sb, expenseCategories);
        sb.append('\n');
        sb.append("【收入分类占比】\n");
        appendCategoryLines(sb, incomeCategories);
        sb.append('\n');

        sb.append("【高金额样本】\n");
        if (samples.isEmpty()) {
            sb.append("- 无\n");
        } else {
            for (BillAnalysisResponse.EntrySampleVO sample : samples) {
                sb.append("- ").append(sample.getEntryDate())
                        .append(" ").append(sample.getType())
                        .append(" ").append(formatAmount(sample.getAmount()))
                        .append(" 分类=").append(safeText(sample.getCategory()))
                        .append(" 备注=").append(safeText(sample.getRemark()))
                        .append('\n');
            }
        }

        if (StringUtils.hasText(question)) {
            sb.append('\n').append("【用户补充问题】").append(question).append('\n');
        }
        sb.append('\n').append("请严格按 system 中描述的 JSON 结构输出，不要输出多余文字。");
        return sb.toString();
    }

    private void appendCategoryLines(StringBuilder sb, List<BillAnalysisResponse.CategoryAmountVO> list) {
        if (list.isEmpty()) {
            sb.append("- 无\n");
            return;
        }
        for (BillAnalysisResponse.CategoryAmountVO item : list) {
            // 占比按百分比展示，便于模型直接复述
            BigDecimal ratioPercent = item.getRatio() == null
                    ? BigDecimal.ZERO
                    : item.getRatio().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            sb.append("- ").append(item.getName())
                    .append(" 金额=").append(formatAmount(item.getAmount()))
                    .append(" 占比=").append(ratioPercent).append("%\n");
        }
    }

    /**
     * 解析模型返回的 JSON。
     * 容错点：模型有时会包 ```json``` 代码块，或者首尾混入解释文字，所以先抠出第一个 { 到最后一个 } 之间的内容再解析。
     */
    private AiAnalysisOutput parseAnalysisJson(String reply) {
        if (!StringUtils.hasText(reply)) {
            return AiAnalysisOutput.fallback("AI 没有返回内容");
        }
        String trimmed = reply.trim();
        int begin = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (begin < 0 || end <= begin) {
            // 完全不像 JSON 时，把原文塞进 summary 兜底
            return AiAnalysisOutput.fallback(trimmed);
        }
        String json = trimmed.substring(begin, end + 1);
        try {
            JsonNode node = objectMapper.readTree(json);
            String summary = textOrEmpty(node.get("summary"));
            List<String> observations = readStringArray(node.get("observations"));
            List<String> risks = readStringArray(node.get("risks"));
            List<String> suggestions = readStringArray(node.get("suggestions"));
            // summary 为空就用原文兜底，避免历史记录全是空字符串
            if (!StringUtils.hasText(summary)) {
                summary = trimmed;
            }
            return new AiAnalysisOutput(summary, observations, risks, suggestions);
        } catch (JsonProcessingException ex) {
            // 解析失败：保留原文作为 summary，方便排查 prompt 问题
            return AiAnalysisOutput.fallback(trimmed);
        }
    }

    private List<String> readStringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        node.forEach(item -> {
            String value = item.isTextual() ? item.asText() : item.toString();
            if (StringUtils.hasText(value)) {
                result.add(value.trim());
            }
        });
        return result;
    }

    private String textOrEmpty(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        return node.isTextual() ? node.asText() : node.toString();
    }

    private BillAnalysisHistoryVO toBillAnalysisHistoryVO(AiBillAnalysisRecord record) {
        return BillAnalysisHistoryVO.builder()
                // 雪花 ID 给前端的字符串形式，避免 JS Number 精度丢失
                .id(record.getId() != null ? record.getId().toString() : null)
                .createdAt(record.getCreatedAt())
                .startDate(record.getStartDate())
                .endDate(record.getEndDate())
                .bookId(record.getBookId())
                .bookName(record.getBookName())
                .entryCount(record.getEntryCount() != null ? record.getEntryCount() : 0)
                .summary(record.getSummary())
                .question(record.getQuestion())
                .build();
    }

    /**
     * 写账单分析的调用日志。
     * 复用与流式聊天相同的 AiCallLog，scene 用 BILL_ANALYSIS 区分。
     */
    private void saveBillAnalysisCallLog(Long userId, boolean success, long startTimeMillis, String errorMessage) {
        try {
            AiCallLog log = new AiCallLog();
            log.setUserId(userId);
            log.setScene("BILL_ANALYSIS");
            // 用激活的供应商枚举名打日志，便于在 ai_call_log 表里按供应商筛
            log.setProvider(aiProperties.getActive().name().toLowerCase());
            log.setSuccessFlag(success ? 1 : 0);
            log.setDurationMs(System.currentTimeMillis() - startTimeMillis);
            log.setErrorMessage(shortErrorMessage(errorMessage));
            log.setCalledAt(LocalDateTime.now());
            aiCallLogMapper.insert(log);
        } catch (Exception ignored) {
            // 日志写不进去也不能阻塞主流程
        }
    }

    private String formatAmount(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String safeText(String value) {
        return StringUtils.hasText(value) ? value : "无";
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    /**
     * 模型解析后的中间结构，仅在本类内部传递。
     * 用 record 而不是 VO，是因为这层数据不对外暴露，没必要再起一个 VO 类。
     */
    private record AiAnalysisOutput(String summary, List<String> observations, List<String> risks, List<String> suggestions) {
        static AiAnalysisOutput fallback(String summary) {
            return new AiAnalysisOutput(summary, List.of(), List.of(), List.of());
        }
    }

    private ChatClient requireChatClient() {
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 客户端未初始化，请检查 AI 模型配置");
        }
        return chatClient;
    }

    private String resolveSystemPrompt() {
        return promptTemplateLoader.resolve(aiProperties.getSystemPrompt(), "prompts/ai/system.md");
    }

    private String normalizeConversationId(String conversationId) {
        return StringUtils.hasText(conversationId) ? conversationId.trim() : DEFAULT_CONVERSATION_ID;
    }

    private String buildChatPrompt(String message, String historyText) {
        if (!StringUtils.hasText(historyText)) {
            return message;
        }
        return """
                以下是最近的会话上下文，请参考这些内容继续回答。
                %s

                当前用户消息：
                %s
                """.formatted(historyText, message);
    }

    private String loadConversationHistory(Long userId, String conversationId) {
        if (!aiProperties.getChat().isMemoryEnabled()) {
            return "";
        }

        List<AiConversationMessageVO> messages = listConversationMessages(userId, conversationId).stream()
                .filter(item -> ROLE_USER.equals(item.getRole()) || ROLE_ASSISTANT.equals(item.getRole()))
                .toList();
        if (messages.isEmpty()) {
            return "";
        }

        int from = Math.max(0, messages.size() - aiProperties.getChat().getMaxMessages());
        return messages.subList(from, messages.size()).stream()
                .map(item -> item.getRole().toUpperCase(Locale.ROOT) + ": " + item.getContent())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private void saveConversationTurn(Long userId, String conversationId, String role, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }

        String normalizedContent = content.trim();
        if (aiProperties.getChat().isMemoryEnabled()) {
            String key = buildConversationKey(userId, conversationId);
            stringRedisTemplate.opsForList()
                    .rightPush(key, role.toUpperCase(Locale.ROOT) + ": " + normalizedContent);
            stringRedisTemplate.expire(key, CHAT_HISTORY_TTL_DAYS, TimeUnit.DAYS);
            trimConversationMemory(key, aiProperties.getChat().getMaxMessages());
        }

        AiConversationMessageVO message = AiConversationMessageVO.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .conversationId(conversationId)
                .role(role)
                .content(normalizedContent)
                .createdAt(LocalDateTime.now())
                .sources(List.of())
                .build();
        appendStructuredMessage(userId, conversationId, message);
        touchConversationSummary(userId, conversationId, role, normalizedContent);
    }

    private void trimConversationMemory(String key, int maxMessages) {
        Long size = stringRedisTemplate.opsForList().size(key);
        if (size != null && size > maxMessages) {
            stringRedisTemplate.opsForList().trim(key, size - maxMessages, -1);
        }
    }

    private void appendStructuredMessage(Long userId, String conversationId, AiConversationMessageVO message) {
        String key = buildConversationMessagesKey(userId, conversationId);
        stringRedisTemplate.opsForList().rightPush(key, serialize(message));
        stringRedisTemplate.expire(key, CHAT_HISTORY_TTL_DAYS, TimeUnit.DAYS);
        trimConversationMemory(key, Math.max(aiProperties.getChat().getMaxMessages(), 40));
    }

    private void touchConversationSummary(Long userId, String conversationId, String role, String content) {
        AiConversationSummaryVO summary = readConversationSummary(userId, conversationId);
        int messageCount = summary != null && summary.getMessageCount() != null
                ? summary.getMessageCount() + 1
                : 1;

        String title = summary != null && StringUtils.hasText(summary.getTitle())
                ? summary.getTitle()
                : "新会话";
        if (ROLE_USER.equals(role) && (summary == null || messageCount <= 2)) {
            title = abbreviate(content, 20);
        }

        AiConversationSummaryVO nextSummary = AiConversationSummaryVO.builder()
                .id(conversationId)
                .title(title)
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

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 会话序列化失败");
        }
    }

    private String abbreviate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return defaultConversationTitle();
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength) + "...";
    }

    private String defaultConversationTitle() {
        return "新会话";
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

    private ServerSentEvent<String> sseEvent(String event, String data) {
        return ServerSentEvent.<String>builder()
                .event(event)
                .data(data)
                .build();
    }

    private void saveAiCallLog(Long userId,
                               String conversationId,
                               boolean success,
                               long startTimeMillis,
                               String errorMessage) {
        try {
            AiCallLog log = new AiCallLog();
            log.setUserId(userId);
            log.setScene("CHAT_STREAM");
            // 用激活的供应商枚举名打日志，便于在 ai_call_log 表里按供应商筛
            log.setProvider(aiProperties.getActive().name().toLowerCase());
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

}
