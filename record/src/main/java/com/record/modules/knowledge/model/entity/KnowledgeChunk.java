package com.record.modules.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库文档切片。
 * <p>
 * 文档经过解析后，按固定长度切片（500-800 字符，overlap 80-120），
 * 每片写入一条记录。Milvus 存向量索引，MySQL 存切片正文用于回查。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_chunk")
public class KnowledgeChunk extends BaseEntity {

    @TableId
    private Long id;

    /** 所属文档 ID */
    private Long documentId;

    /** 所属知识库 ID */
    private Long knowledgeBaseId;

    /** 切片序号，从 0 开始 */
    private Integer chunkIndex;

    /** 切片文本内容 */
    private String content;

    /** 字符数，用于统计和分析 */
    private Integer contentLength;

    /** Milvus 中对应向量的主键 ID（回写），用于后续删除/重建 */
    private String vectorId;

    /** 状态：PENDING / VECTORIZED / FAILED */
    private String status;

    /** 向量化失败时的错误信息 */
    private String lastError;
}
