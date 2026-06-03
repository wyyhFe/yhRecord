package com.record.modules.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.config.AiProperties;
import com.record.common.exception.KnowledgeException;
import com.record.modules.knowledge.mapper.KnowledgeBaseMapper;
import com.record.modules.knowledge.mapper.KnowledgeChunkMapper;
import com.record.modules.knowledge.mapper.KnowledgeChunkTaskMapper;
import com.record.modules.knowledge.mapper.KnowledgeDocumentMapper;
import com.record.modules.knowledge.model.dto.CreateKnowledgeBaseRequest;
import com.record.modules.knowledge.model.dto.CreateKnowledgeDocumentRequest;
import com.record.modules.knowledge.model.dto.RagQueryRequest;
import com.record.modules.knowledge.model.entity.KnowledgeBase;
import com.record.modules.knowledge.model.entity.KnowledgeChunk;
import com.record.modules.knowledge.model.entity.KnowledgeChunkTask;
import com.record.modules.knowledge.model.entity.KnowledgeDocument;
import com.record.modules.knowledge.model.vo.KnowledgeBaseVO;
import com.record.modules.knowledge.model.vo.KnowledgeChunkTaskVO;
import com.record.modules.knowledge.model.vo.KnowledgeDocumentVO;
import com.record.modules.knowledge.model.vo.RagAnswerVO;
import com.record.modules.knowledge.model.vo.RagSearchResultVO;
import com.record.modules.knowledge.service.KnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeServiceImpl.class);

    private static final List<String> SUPPORTED_VISIBILITY = List.of("PRIVATE", "PUBLIC");
    private static final List<String> SUPPORTED_SOURCE_TYPES = List.of("UPLOAD", "URL", "MANUAL");

    // metadata 键名常量
    private static final String META_KNOWLEDGE_BASE_ID = "knowledgeBaseId";
    private static final String META_DOCUMENT_ID = "documentId";
    private static final String META_CHUNK_INDEX = "chunkIndex";
    private static final String META_CHUNK_ID = "chunkId";
    private static final String META_USER_ID = "userId";

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeChunkTaskMapper knowledgeChunkTaskMapper;
    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final AiProperties aiProperties;

    public KnowledgeServiceImpl(KnowledgeBaseMapper knowledgeBaseMapper,
                                KnowledgeDocumentMapper knowledgeDocumentMapper,
                                KnowledgeChunkTaskMapper knowledgeChunkTaskMapper,
                                KnowledgeChunkMapper knowledgeChunkMapper,
                                ObjectProvider<ChatClient> chatClientProvider,
                                ObjectProvider<VectorStore> vectorStoreProvider,
                                ObjectProvider<EmbeddingModel> embeddingModelProvider,
                                AiProperties aiProperties) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
        this.knowledgeChunkTaskMapper = knowledgeChunkTaskMapper;
        this.knowledgeChunkMapper = knowledgeChunkMapper;
        this.chatClientProvider = chatClientProvider;
        this.vectorStoreProvider = vectorStoreProvider;
        this.embeddingModelProvider = embeddingModelProvider;
        this.aiProperties = aiProperties;
    }

    // ═══════════════════════════════════════════════════════════════
    // 已有接口
    // ═══════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public KnowledgeBaseVO createBase(Long userId, CreateKnowledgeBaseRequest request) {
        String code = normalizeCode(request.getCode(), request.getName());
        ensureCodeUnique(code);

        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setUserId(userId);
        knowledgeBase.setName(request.getName().trim());
        knowledgeBase.setCode(code);
        knowledgeBase.setDescription(request.getDescription());
        knowledgeBase.setStatus("ENABLED");
        knowledgeBase.setVisibility(normalizeVisibility(request.getVisibility()));
        knowledgeBaseMapper.insert(knowledgeBase);
        return toBaseVO(knowledgeBase);
    }

    @Override
    public List<KnowledgeBaseVO> listBases(Long userId) {
        return knowledgeBaseMapper.selectList(new LambdaQueryWrapper<KnowledgeBase>()
                        .eq(KnowledgeBase::getUserId, userId)
                        .orderByDesc(KnowledgeBase::getId))
                .stream()
                .map(this::toBaseVO)
                .toList();
    }

    @Override
    @Transactional
    public KnowledgeDocumentVO createDocument(Long userId, CreateKnowledgeDocumentRequest request) {
        KnowledgeBase knowledgeBase = requireOwnedBase(userId, request.getKnowledgeBaseId());
        String sourceType = normalizeSourceType(request.getSourceType());
        if (!StringUtils.hasText(request.getFilePath()) && !"MANUAL".equals(sourceType)) {
            throw new KnowledgeException("文档路径不能为空");
        }

        KnowledgeDocument document = buildDocument(request, knowledgeBase.getId(), userId, sourceType);
        knowledgeDocumentMapper.insert(document);

        createParseTask(knowledgeBase.getId(), document.getId());
        return toDocumentVO(document);
    }

    @Override
    public List<KnowledgeDocumentVO> listDocuments(Long userId, Long knowledgeBaseId) {
        requireOwnedBase(userId, knowledgeBaseId);
        return knowledgeDocumentMapper.selectList(new LambdaQueryWrapper<KnowledgeDocument>()
                        .eq(KnowledgeDocument::getKnowledgeBaseId, knowledgeBaseId)
                        .orderByDesc(KnowledgeDocument::getId))
                .stream()
                .map(this::toDocumentVO)
                .toList();
    }

    @Override
    public List<KnowledgeChunkTaskVO> listTasks(Long userId, Long knowledgeBaseId, Long documentId) {
        requireOwnedBase(userId, knowledgeBaseId);
        return knowledgeChunkTaskMapper.selectList(new LambdaQueryWrapper<KnowledgeChunkTask>()
                        .eq(KnowledgeChunkTask::getKnowledgeBaseId, knowledgeBaseId)
                        .eq(documentId != null, KnowledgeChunkTask::getDocumentId, documentId)
                        .orderByDesc(KnowledgeChunkTask::getId))
                .stream()
                .map(this::toTaskVO)
                .toList();
    }

    // ═══════════════════════════════════════════════════════════════
    // 文档上传 + 向量化（同步，直接写 Milvus）
    // ═══════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public KnowledgeDocumentVO uploadDocument(Long userId, Long knowledgeBaseId, String title, MultipartFile file) {
        requireOwnedBase(userId, knowledgeBaseId);

        // 1. 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String mimeType = file.getContentType();
        if (!isSupportedFile(originalFilename, mimeType)) {
            throw new KnowledgeException("不支持的文件类型，仅支持 PDF / DOCX / TXT / MD");
        }

        // 2. 读取文件内容
        String content;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            content = reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new KnowledgeException("文件读取失败: " + e.getMessage());
        }

        // 3. 创建文档元数据
        String actualTitle = StringUtils.hasText(title) ? title.trim() : originalFilename;
        KnowledgeDocument document = new KnowledgeDocument();
        document.setKnowledgeBaseId(knowledgeBaseId);
        document.setUserId(userId);
        document.setTitle(actualTitle);
        document.setSourceType("UPLOAD");
        document.setFileName(originalFilename);
        document.setMimeType(mimeType);
        document.setStatus("PENDING");
        document.setChunkCount(0);
        knowledgeDocumentMapper.insert(document);

        // 4. 创建 PARSE 任务
        KnowledgeChunkTask task = new KnowledgeChunkTask();
        task.setKnowledgeBaseId(knowledgeBaseId);
        task.setDocumentId(document.getId());
        task.setTaskType("PARSE");
        task.setStatus("RUNNING");
        task.setRetryCount(0);
        task.setStartedAt(LocalDateTime.now());
        knowledgeChunkTaskMapper.insert(task);

        try {
            AiProperties.Rag ragConfig = aiProperties.getRag();

            // 5. 文本切片
            List<String> chunks = splitText(content, ragConfig.getChunkSize(), ragConfig.getChunkOverlap());
            if (chunks.isEmpty()) {
                throw new KnowledgeException("文档解析后无有效内容");
            }

            // 6. 写入 MySQL + 构建 Milvus Documents
            List<Document> vectorDocs = new ArrayList<>();
            int chunkCount = 0;
            Map<Long, String> chunkIdToVectorId = new HashMap<>();

            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);
                if (!StringUtils.hasText(chunkText)) {
                    continue;
                }

                // 先写入 MySQL 获取 chunkId
                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setDocumentId(document.getId());
                chunk.setKnowledgeBaseId(knowledgeBaseId);
                chunk.setChunkIndex(i);
                chunk.setContent(chunkText);
                chunk.setContentLength(chunkText.length());
                chunk.setStatus("PENDING");
                knowledgeChunkMapper.insert(chunk);

                // 构建 Milvus Document（自动调用 EmbeddingModel 生成向量）
                String chunkIdStr = chunk.getId().toString();
                Map<String, Object> metadata = new HashMap<>();
                metadata.put(META_KNOWLEDGE_BASE_ID, knowledgeBaseId);
                metadata.put(META_DOCUMENT_ID, document.getId());
                metadata.put(META_CHUNK_INDEX, i);
                metadata.put(META_CHUNK_ID, chunkIdStr);
                metadata.put(META_USER_ID, userId);

                Document vectorDoc = new Document(chunkIdStr, chunkText, metadata);
                vectorDocs.add(vectorDoc);
                chunkIdToVectorId.put(chunk.getId(), chunkIdStr);
                chunkCount++;
            }

            // 7. 批量写入 Milvus（VectorStore.add 会调用 EmbeddingModel 生成向量，再写 Milvus）
            if (!vectorDocs.isEmpty()) {
                requireVectorStore().add(vectorDocs);
            }

            // 8. 回写 vectorId 并更新状态
            for (Map.Entry<Long, String> entry : chunkIdToVectorId.entrySet()) {
                KnowledgeChunk update = new KnowledgeChunk();
                update.setId(entry.getKey());
                update.setVectorId(entry.getValue());
                update.setStatus("VECTORIZED");
                knowledgeChunkMapper.updateById(update);
            }

            // 9. 更新文档状态
            document.setStatus("VECTORIZED");
            document.setChunkCount(chunkCount);
            knowledgeDocumentMapper.updateById(document);

            // 10. 更新任务状态
            task.setStatus("SUCCESS");
            task.setFinishedAt(LocalDateTime.now());
            knowledgeChunkTaskMapper.updateById(task);

            log.info("[knowledge] document vectorized: id={}, title={}, chunks={}",
                    document.getId(), actualTitle, chunkCount);

        } catch (Exception e) {
            log.error("[knowledge] document upload+vectorize failed, documentId={}", document.getId(), e);
            document.setStatus("FAILED");
            document.setLastError(e.getMessage());
            knowledgeDocumentMapper.updateById(document);

            task.setStatus("FAILED");
            task.setLastError(e.getMessage());
            task.setFinishedAt(LocalDateTime.now());
            knowledgeChunkTaskMapper.updateById(task);

            throw new KnowledgeException("文档处理失败: " + e.getMessage());
        }

        return toDocumentVO(document);
    }

    // ═══════════════════════════════════════════════════════════════
    // Milvus 向量检索
    // ═══════════════════════════════════════════════════════════════

    @Override
    public RagSearchResultVO searchChunks(Long userId, Long knowledgeBaseId, String query, int topK) {
        requireOwnedBase(userId, knowledgeBaseId);

        if (!StringUtils.hasText(query)) {
            return RagSearchResultVO.builder().hits(List.of()).totalHits(0).build();
        }

        AiProperties.Rag ragConfig = aiProperties.getRag();
        int safeTopK = Math.min(Math.max(topK, 1), 20);

        // 在 Milvus 中做相似度检索，按 knowledgeBaseId + userId 过滤
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(safeTopK)
                .similarityThreshold(ragConfig.getSimilarityThreshold())
                .filterExpression(META_KNOWLEDGE_BASE_ID + " == " + knowledgeBaseId
                        + " && " + META_USER_ID + " == " + userId)
                .build();

        List<Document> results = requireVectorStore().similaritySearch(searchRequest);

        // 根据召回结果回查 MySQL 拿 chunk 正文和文档信息
        List<RagSearchResultVO.ChunkHitVO> hits = new ArrayList<>();
        for (Document doc : results) {
            Map<String, Object> meta = doc.getMetadata();
            Object chunkIdObj = meta.get(META_CHUNK_ID);
            if (chunkIdObj == null) continue;

            Long chunkId;
            try {
                chunkId = Long.valueOf(chunkIdObj.toString());
            } catch (NumberFormatException e) {
                continue;
            }

            KnowledgeChunk chunk = knowledgeChunkMapper.selectById(chunkId);
            if (chunk == null) continue;

            KnowledgeDocument knowledgeDoc = knowledgeDocumentMapper.selectById(chunk.getDocumentId());

            hits.add(RagSearchResultVO.ChunkHitVO.builder()
                    .chunkId(chunk.getId())
                    .documentId(chunk.getDocumentId())
                    .documentTitle(knowledgeDoc != null ? knowledgeDoc.getTitle() : "未知文档")
                    .chunkIndex(chunk.getChunkIndex())
                    .content(chunk.getContent())
                    .relevanceScore(doc.getScore())
                    .build());
        }

        return RagSearchResultVO.builder()
                .hits(hits)
                .totalHits(hits.size())
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // RAG 问答：向量检索 + LLM 生成
    // ═══════════════════════════════════════════════════════════════

    @Override
    public RagAnswerVO askRag(Long userId, RagQueryRequest request) {
        requireOwnedBase(userId, request.getKnowledgeBaseId());

        int topK = request.getTopK() != null
                ? Math.min(Math.max(request.getTopK(), 1), 20)
                : aiProperties.getRag().getTopK();

        // 1. Milvus 向量检索
        RagSearchResultVO searchResult = searchChunks(
                userId, request.getKnowledgeBaseId(), request.getQuestion(), topK);

        if (searchResult.getHits().isEmpty()) {
            return RagAnswerVO.builder()
                    .answer("知识库中未找到与问题相关的文档内容。")
                    .references(List.of())
                    .referenceCount(0)
                    .build();
        }

        // 2. 拼检索上下文
        String context = searchResult.getHits().stream()
                .map(hit -> "【文档：" + hit.getDocumentTitle() + "】\n" + hit.getContent())
                .collect(Collectors.joining("\n\n---\n\n"));

        // 3. 调 LLM 生成回答
        String systemPrompt = "你是 lifeRecord 的知识库助手。请基于以下提供的文档上下文回答用户问题。" +
                "如果上下文信息不足以回答，请坦诚告知，不要编造。" +
                "回答应简洁、准确，必要时引用文档标题。";

        String userPrompt = "以下是知识库中的相关文档内容：\n\n" + context
                + "\n\n用户问题：" + request.getQuestion()
                + "\n\n请基于以上上下文回答问题。" +
                "\n如果引用到了某篇文档的内容，请在回答末尾标注引用来源 [文档：xxx]。";

        String answer;
        try {
            answer = requireChatClient().prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[knowledge] RAG LLM call failed", e);
            answer = "AI 回答生成失败：" + e.getMessage();
        }

        if (!StringUtils.hasText(answer)) {
            answer = "AI 未能生成回答，请稍后重试。";
        }

        return RagAnswerVO.builder()
                .answer(answer)
                .references(searchResult.getHits())
                .referenceCount(searchResult.getHits().size())
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // 内部工具方法
    // ═══════════════════════════════════════════════════════════════

    /**
     * 文本切片算法。
     * 按目标字符数切割，相邻切片间保留 overlap 字符的重叠，避免语义断裂。
     * 尽量在句子边界断句（换行符 > 句号 > 问号/感叹号 > 逗号/分号）。
     */
    static List<String> splitText(String text, int size, int overlap) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        if (size <= 0) size = 600;
        if (overlap < 0 || overlap >= size) {
            overlap = Math.min(overlap, size / 2);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        int textLen = text.length();

        while (start < textLen) {
            int end = Math.min(start + size, textLen);
            if (end < textLen) {
                int breakAt = findSentenceBoundary(text, end, Math.max(start, end - 50));
                if (breakAt > start) {
                    end = breakAt;
                }
            }
            chunks.add(text.substring(start, end).trim());
            start = end - overlap;
            if (start >= textLen) break;
        }
        return chunks;
    }

    private static int findSentenceBoundary(String text, int position, int searchFrom) {
        for (int i = position - 1; i >= searchFrom; i--) {
            char c = text.charAt(i);
            if (c == '\n' || c == '\r') return i + 1;
        }
        for (int i = position - 1; i >= searchFrom; i--) {
            char c = text.charAt(i);
            if (c == '。' || c == '.' || c == '！' || c == '？' || c == '!' || c == '?') return i + 1;
        }
        for (int i = position - 1; i >= searchFrom; i--) {
            char c = text.charAt(i);
            if (c == '，' || c == '；' || c == ',' || c == ';') return i + 1;
        }
        return position;
    }

    private boolean isSupportedFile(String filename, String contentType) {
        if (filename != null) {
            String lower = filename.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".pdf")) return true;
            if (lower.endsWith(".docx")) return true;
            if (lower.endsWith(".txt")) return true;
            if (lower.endsWith(".md")) return true;
        }
        return false;
    }

    private KnowledgeDocument buildDocument(CreateKnowledgeDocumentRequest request, Long baseId,
                                            Long userId, String sourceType) {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setKnowledgeBaseId(baseId);
        document.setUserId(userId);
        document.setTitle(request.getTitle().trim());
        document.setSourceType(sourceType);
        document.setFileName(request.getFileName());
        document.setFilePath(request.getFilePath());
        document.setMimeType(request.getMimeType());
        document.setContentHash(request.getContentHash());
        document.setStatus("PENDING");
        document.setChunkCount(0);
        return document;
    }

    private void createParseTask(Long baseId, Long documentId) {
        KnowledgeChunkTask task = new KnowledgeChunkTask();
        task.setKnowledgeBaseId(baseId);
        task.setDocumentId(documentId);
        task.setTaskType("PARSE");
        task.setStatus("PENDING");
        task.setRetryCount(0);
        knowledgeChunkTaskMapper.insert(task);
    }

    private KnowledgeBase requireOwnedBase(Long userId, Long knowledgeBaseId) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new KnowledgeException("知识库不存在或无权限访问");
        }
        if (!"ENABLED".equals(knowledgeBase.getStatus())) {
            throw new KnowledgeException("知识库已被停用");
        }
        return knowledgeBase;
    }

    private void ensureCodeUnique(String code) {
        Long count = knowledgeBaseMapper.selectCount(new LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getCode, code));
        if (count != null && count > 0) {
            throw new KnowledgeException("知识库编码已存在");
        }
    }

    private String normalizeCode(String rawCode, String name) {
        if (StringUtils.hasText(rawCode)) {
            return rawCode.trim().toLowerCase(Locale.ROOT);
        }
        String namePart = StringUtils.hasText(name)
                ? name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                : "kb";
        namePart = namePart.replaceAll("^-+|-+$", "");
        if (!StringUtils.hasText(namePart)) namePart = "kb";
        return namePart + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String normalizeVisibility(String visibility) {
        String value = StringUtils.hasText(visibility) ? visibility.trim().toUpperCase(Locale.ROOT) : "PRIVATE";
        if (!SUPPORTED_VISIBILITY.contains(value)) {
            throw new KnowledgeException("visibility 仅支持 PRIVATE 或 PUBLIC");
        }
        return value;
    }

    private String normalizeSourceType(String sourceType) {
        String value = StringUtils.hasText(sourceType) ? sourceType.trim().toUpperCase(Locale.ROOT) : null;
        if (!StringUtils.hasText(value) || !SUPPORTED_SOURCE_TYPES.contains(value)) {
            throw new KnowledgeException("sourceType 仅支持 UPLOAD、URL、MANUAL");
        }
        return value;
    }

    private ChatClient requireChatClient() {
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            throw new KnowledgeException("AI 客户端未初始化，请检查 AI 模型配置");
        }
        return chatClient;
    }

    private VectorStore requireVectorStore() {
        VectorStore vs = vectorStoreProvider.getIfAvailable();
        if (vs == null) {
            throw new KnowledgeException("Milvus 向量库未初始化，请检查 Milvus 配置和连接");
        }
        return vs;
    }

    // ─── VO 转换 ────────────────────────────────────────────────

    private KnowledgeBaseVO toBaseVO(KnowledgeBase knowledgeBase) {
        return KnowledgeBaseVO.builder()
                .id(knowledgeBase.getId())
                .name(knowledgeBase.getName())
                .code(knowledgeBase.getCode())
                .description(knowledgeBase.getDescription())
                .status(knowledgeBase.getStatus())
                .visibility(knowledgeBase.getVisibility())
                .createdAt(knowledgeBase.getCreatedAt())
                .build();
    }

    private KnowledgeDocumentVO toDocumentVO(KnowledgeDocument document) {
        return KnowledgeDocumentVO.builder()
                .id(document.getId())
                .knowledgeBaseId(document.getKnowledgeBaseId())
                .title(document.getTitle())
                .sourceType(document.getSourceType())
                .fileName(document.getFileName())
                .filePath(document.getFilePath())
                .mimeType(document.getMimeType())
                .contentHash(document.getContentHash())
                .status(document.getStatus())
                .chunkCount(document.getChunkCount())
                .lastError(document.getLastError())
                .createdAt(document.getCreatedAt())
                .build();
    }

    private KnowledgeChunkTaskVO toTaskVO(KnowledgeChunkTask task) {
        return KnowledgeChunkTaskVO.builder()
                .id(task.getId())
                .knowledgeBaseId(task.getKnowledgeBaseId())
                .documentId(task.getDocumentId())
                .taskType(task.getTaskType())
                .status(task.getStatus())
                .retryCount(task.getRetryCount())
                .lastError(task.getLastError())
                .startedAt(task.getStartedAt())
                .finishedAt(task.getFinishedAt())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
