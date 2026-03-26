package com.record.modules.recycle.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "资源类型", example = "DIARY")
    private ResourceType resourceType;

    @Schema(description = "资源 ID", example = "99")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long resourceId;

    @Schema(description = "删除时间", example = "2026-03-21 10:00:00")
    private LocalDateTime deletedAt;

    @Schema(description = "过期时间", example = "2026-04-05 10:00:00")
    private LocalDateTime expireAt;

    @Schema(description = "展示标题", example = "支出 · ¥32.50")
    private String title;

    @Schema(description = "展示副标题", example = "2026-03-26 · 生活账本 · 午饭")
    private String subtitle;
}
