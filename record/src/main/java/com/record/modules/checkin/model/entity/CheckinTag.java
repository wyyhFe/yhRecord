package com.record.modules.checkin.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打卡标签定义。
 */
@Data
@TableName("biz_checkin_tag")
@Schema(description = "打卡标签定义")
public class CheckinTag {

    @TableId
    @Schema(description = "标签 ID", example = "1")
    private Long id;

    @Schema(description = "用户 ID（系统预设为 NULL）", example = "10001")
    private Long userId;

    @Schema(description = "标签名称", example = "运动出汗")
    private String name;

    @Schema(description = "标签图标 Emoji", example = "💪")
    private String icon;

    @Schema(description = "是否系统预设", example = "1")
    private Boolean isSystem;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
