package com.record.modules.checkin.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 勋章返回对象。
 */
@Data
@Builder
@Schema(description = "勋章信息")
public class MedalVO {

    @Schema(description = "勋章 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "勋章编码", example = "streak_7")
    private String code;

    @Schema(description = "勋章名称", example = "小坚持")
    private String name;

    @Schema(description = "解锁条件描述", example = "连续打卡 7 天")
    private String description;

    @Schema(description = "图标 Emoji", example = "🔥")
    private String icon;

    @Schema(description = "分类", example = "STREAK")
    private String category;

    @Schema(description = "难度等级", example = "2")
    private Integer difficulty;

    @Schema(description = "是否已解锁")
    private boolean unlocked;

    @Schema(description = "解锁时间（未解锁为 null）")
    private LocalDateTime unlockedAt;
}
