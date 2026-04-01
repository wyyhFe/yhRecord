package com.record.modules.knowledge.service;

import com.record.modules.knowledge.model.dto.CreateKnowledgeBaseRequest;
import com.record.modules.knowledge.model.dto.CreateKnowledgeDocumentRequest;
import com.record.modules.knowledge.model.vo.KnowledgeBaseVO;
import com.record.modules.knowledge.model.vo.KnowledgeChunkTaskVO;
import com.record.modules.knowledge.model.vo.KnowledgeDocumentVO;

import java.util.List;

public interface KnowledgeService {

    KnowledgeBaseVO createBase(Long userId, CreateKnowledgeBaseRequest request);

    List<KnowledgeBaseVO> listBases(Long userId);

    KnowledgeDocumentVO createDocument(Long userId, CreateKnowledgeDocumentRequest request);

    List<KnowledgeDocumentVO> listDocuments(Long userId, Long knowledgeBaseId);

    List<KnowledgeChunkTaskVO> listTasks(Long userId, Long knowledgeBaseId, Long documentId);
}
