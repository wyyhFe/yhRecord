package com.record.modules.ai.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ai_bill_analysis_record")
public class AiBillAnalysisRecord extends BaseEntity {

    @TableId
    private Long id;

    private Long userId;

    private Long bookId;

    private String bookName;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer entryCount;

    private String question;

    private String summary;
}
