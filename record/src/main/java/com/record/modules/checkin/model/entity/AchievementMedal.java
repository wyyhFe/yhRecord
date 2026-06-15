package com.record.modules.checkin.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 勋章定义。
 */
@Data
@TableName("biz_achievement_medal")
@Schema(description = "勋章定义")
public class AchievementMedal {

    @TableId
    @Schema(description = "勋章 ID", example = "1")
    private Long id;

    @Schema(description = "勋章唯一编码", example = "streak_7")
    private String code;

    @Schema(description = "勋章名称", example = "小坚持")
    private String name;

    @Schema(description = "解锁条件描述", example = "连续打卡 7 天")
    private String description;

    @Schema(description = "勋章图标 Emoji", example = "🔥")
    private String icon;

    @Schema(description = "分类", example = "STREAK")
    private String category;

    @Schema(description = "难度等级", example = "2")
    private Integer difficulty;

    @Schema(description = "解锁阈值", example = "7")
    private Integer conditionValue;

    @Schema(description = "排序值", example = "2")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
