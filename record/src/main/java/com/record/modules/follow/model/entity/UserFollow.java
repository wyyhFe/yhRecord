package com.record.modules.follow.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_user_follow")
@Schema(description = "用户关注关系")
public class UserFollow {

    @TableId
    @Schema(description = "主键 ID", example = "1")
    private Long id;

    @Schema(description = "关注者用户 ID", example = "10001")
    private Long followerId;

    @Schema(description = "被关注用户 ID", example = "10002")
    private Long followingId;

    @Schema(description = "关注时间")
    private LocalDateTime createdAt;
}
