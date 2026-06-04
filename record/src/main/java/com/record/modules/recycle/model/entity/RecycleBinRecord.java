package com.record.modules.recycle.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.ResourceType;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 回收站记录实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_recycle_bin")
@Schema(description = "回收站记录实体")
public class RecycleBinRecord extends BaseEntity {

    @TableId
    @Schema(description = "回收站记录 ID", example = "1")
    private Long id;

    @Schema(description = "所属用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "资源类型", example = "DIARY")
    private ResourceType resourceType;

    @Schema(description = "资源 ID", example = "99")
    private Long resourceId;

    @Schema(description = "删除时间", example = "2026-03-21 10:00:00")
    private LocalDateTime deletedAt;

    @Schema(description = "过期时间", example = "2026-04-05 10:00:00")
    private LocalDateTime expireAt;
}
