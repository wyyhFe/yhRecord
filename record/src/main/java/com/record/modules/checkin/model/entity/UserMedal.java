package com.record.modules.checkin.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户勋章解锁记录。
 */
@Data
@TableName("biz_user_medal")
@Schema(description = "用户勋章解锁记录")
public class UserMedal {

    @TableId
    @Schema(description = "记录 ID", example = "1")
    private Long id;

    @Schema(description = "用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "勋章 ID", example = "3")
    private Long medalId;

    @Schema(description = "解锁时间")
    private LocalDateTime unlockedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
