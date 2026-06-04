package com.record.modules.ai.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ai_call_log")
public class AiCallLog extends BaseEntity {

    @TableId
    private Long id;

    private Long userId;

    private String scene;

    private String provider;

    private String model;

    private String conversationId;

    private Integer successFlag;

    private Long durationMs;

    private String errorMessage;

    private LocalDateTime calledAt;
}
