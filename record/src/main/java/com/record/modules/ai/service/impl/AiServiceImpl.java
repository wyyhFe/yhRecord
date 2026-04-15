package com.record.modules.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.config.AiProperties;
import com.record.common.exception.BusinessException;
import com.record.common.exception.ErrorCode;
import com.record.modules.ai.mapper.AiCallLogMapper;
import com.record.modules.ai.model.dto.AiChatRequest;
import com.record.modules.ai.model.dto.CreateConversationRequest;
import com.record.modules.ai.model.entity.AiCallLog;
import com.record.modules.ai.model.vo.AiConversationMessageVO;
import com.record.modules.ai.model.vo.AiConversationSummaryVO;
import com.record.modules.ai.model.vo.AiFunctionCallResponse;
import com.record.modules.ai.prompt.PromptTemplateLoader;
import com.record.modules.ai.service.AiService;
import com.record.modules.diary.model.vo.DiaryVO;
import com.record.modules.diary.service.DiaryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private static final TypeReference<AiConversationSummaryVO> CONVERSATION_TYPE = new TypeReference<>() { };
    private static final TypeReference<AiConversationMessageVO> MESSAGE_TYPE = new TypeReference<>() { };

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final AiProperties aiProperties;
    private final AiCallLogMapper aiCallLogMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final PromptTemplateLoader promptTemplateLoader;
    private final DiaryService diaryService;

    public AiServiceImpl(ObjectProvider<ChatClient> chatClientProvider,
                         AiProperties aiProperties,
                         AiCallLogMapper aiCallLogMapper,
                         StringRedisTemplate stringRedisTemplate,
                         ObjectMapper objectMapper,
                         PromptTemplateLoader promptTemplateLoader,
                         DiaryService diaryService) {
        this.chatClientProvider = chatClientProvider;
        this.aiProperties = aiProperties;
        this.aiCallLogMapper = aiCallLogMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.promptTemplateLoader = promptTemplateLoader;
        this.diaryService = diaryService;
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
    public AiFunctionCallResponse functionCallDemo(Long userId, AiChatRequest request) {
        DiaryListTool tool = new DiaryListTool(userId, diaryService);
        String reply = requireChatClient().prompt()
                .system("""
                        You are a function calling demo for the life-record app.
                        If the user wants to view, search, or list diary records, call the list_diaries tool.
                        Extract a short keyword from the user message when possible. Use current=1 and size=5 by default.
                        Keep the final answer concise and summarize the returned records in Chinese.
                        """)
                .user(request.getMessage().trim())
                .tools(tool)
                .call()
                .content();

        return AiFunctionCallResponse.builder()
                .reply(reply)
                .toolName(tool.called ? "list_diaries" : null)
                .toolResult(tool.lastResult)
                .build();
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
            log.setProvider(aiProperties.getProvider());
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

    public static class DiaryListTool {

        private final Long userId;
        private final DiaryService diaryService;
        private boolean called;
        private Page<DiaryVO> lastResult;

        private DiaryListTool(Long userId, DiaryService diaryService) {
            this.userId = userId;
            this.diaryService = diaryService;
        }

        @Tool(name = "list_diaries", description = "Search the current user's diary list by keyword and return a paged list.")
        public Page<DiaryVO> listDiaries(
                @ToolParam(required = false, description = "Keyword extracted from the user's prompt. Use null if the user asks for recent diaries without a keyword.")
                String keyword,
                @ToolParam(required = false, description = "Page number, starts from 1. Use 1 by default.")
                Integer current,
                @ToolParam(required = false, description = "Page size. Use 5 by default and never exceed 10.")
                Integer size) {
            called = true;
            long safeCurrent = current == null || current < 1 ? 1 : current;
            long safeSize = size == null || size < 1 ? 5 : Math.min(size, 10);
            String safeKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
            lastResult = diaryService.list(userId, safeCurrent, safeSize, null, null, safeKeyword);
            return lastResult;
        }
    }
}
