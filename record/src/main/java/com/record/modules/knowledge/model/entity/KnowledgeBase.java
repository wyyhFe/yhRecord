package com.record.modules.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_knowledge_base")
public class KnowledgeBase extends BaseEntity {

    @TableId
    private Long id;

    private Long userId;

    private String name;

    private String code;

    private String description;

    private String status;

    private String visibility;
}
