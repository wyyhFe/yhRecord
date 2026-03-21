package com.record.common.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用审计基类。
 * 统一维护创建时间、更新时间、创建人和更新人。
 */
@Data
public class BaseEntity {

    @Schema(description = "创建时间", example = "2026-03-21T10:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "最后更新时间", example = "2026-03-21T18:30:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Schema(description = "创建人用户 ID，系统任务写 0", example = "10001")
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @Schema(description = "最后更新人用户 ID，系统任务写 0", example = "10001")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
}
