package com.record.modules.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_chunk_task")
public class KnowledgeChunkTask extends BaseEntity {

    @TableId
    private Long id;

    private Long knowledgeBaseId;

    private Long documentId;

    private String taskType;

    private String status;

    private Integer retryCount;

    private String lastError;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;
}
