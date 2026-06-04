package com.record.modules.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_knowledge_document")
public class KnowledgeDocument extends BaseEntity {

    @TableId
    private Long id;

    private Long knowledgeBaseId;

    private Long userId;

    private String title;

    private String sourceType;

    private String fileName;

    private String filePath;

    private String mimeType;

    private String contentHash;

    private String status;

    private Integer chunkCount;

    private String lastError;
}
