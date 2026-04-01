package com.record.modules.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.exception.KnowledgeException;
import com.record.modules.knowledge.mapper.KnowledgeBaseMapper;
import com.record.modules.knowledge.mapper.KnowledgeChunkTaskMapper;
import com.record.modules.knowledge.mapper.KnowledgeDocumentMapper;
import com.record.modules.knowledge.model.dto.CreateKnowledgeBaseRequest;
import com.record.modules.knowledge.model.dto.CreateKnowledgeDocumentRequest;
import com.record.modules.knowledge.model.entity.KnowledgeBase;
import com.record.modules.knowledge.model.entity.KnowledgeChunkTask;
import com.record.modules.knowledge.model.entity.KnowledgeDocument;
import com.record.modules.knowledge.model.vo.KnowledgeBaseVO;
import com.record.modules.knowledge.model.vo.KnowledgeChunkTaskVO;
import com.record.modules.knowledge.model.vo.KnowledgeDocumentVO;
import com.record.modules.knowledge.service.KnowledgeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final List<String> SUPPORTED_VISIBILITY = List.of("PRIVATE", "PUBLIC");
    private static final List<String> SUPPORTED_SOURCE_TYPES = List.of("UPLOAD", "URL", "MANUAL");

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeChunkTaskMapper knowledgeChunkTaskMapper;

    public KnowledgeServiceImpl(KnowledgeBaseMapper knowledgeBaseMapper,
                                KnowledgeDocumentMapper knowledgeDocumentMapper,
                                KnowledgeChunkTaskMapper knowledgeChunkTaskMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
        this.knowledgeChunkTaskMapper = knowledgeChunkTaskMapper;
    }

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

        KnowledgeDocument document = new KnowledgeDocument();
        document.setKnowledgeBaseId(knowledgeBase.getId());
        document.setUserId(userId);
        document.setTitle(request.getTitle().trim());
        document.setSourceType(sourceType);
        document.setFileName(request.getFileName());
        document.setFilePath(request.getFilePath());
        document.setMimeType(request.getMimeType());
        document.setContentHash(request.getContentHash());
        document.setStatus("PENDING");
        document.setChunkCount(0);
        knowledgeDocumentMapper.insert(document);

        KnowledgeChunkTask task = new KnowledgeChunkTask();
        task.setKnowledgeBaseId(knowledgeBase.getId());
        task.setDocumentId(document.getId());
        task.setTaskType("PARSE");
        task.setStatus("PENDING");
        task.setRetryCount(0);
        knowledgeChunkTaskMapper.insert(task);
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
        if (!StringUtils.hasText(namePart)) {
            namePart = "kb";
        }
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
