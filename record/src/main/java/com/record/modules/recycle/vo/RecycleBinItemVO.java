package com.record.modules.recycle.vo;

import com.record.common.enums.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回收站条目返回对象。
 */
@Data
@Builder
@Schema(description = "回收站条目返回对象")
public class RecycleBinItemVO {
    @Schema(description = "回收站记录 ID", example = "1")
    private Long id;

    @Schema(description = "资源类型", example = "DIARY")
    private ResourceType resourceType;

    @Schema(description = "原始资源 ID", example = "99")
    private Long resourceId;

    @Schema(description = "删除时间", example = "2026-03-21 10:00:00")
    private LocalDateTime deletedAt;

    @Schema(description = "过期清理时间", example = "2026-04-05 10:00:00")
    private LocalDateTime expireAt;
}
