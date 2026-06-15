package com.record.modules.checkin.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打卡记录与标签关联。
 */
@Data
@TableName("biz_checkin_record_tag")
@Schema(description = "打卡记录与标签关联")
public class CheckinRecordTag {

    @TableId
    @Schema(description = "记录 ID", example = "1")
    private Long id;

    @Schema(description = "打卡记录 ID", example = "101")
    private Long recordId;

    @Schema(description = "标签 ID", example = "5")
    private Long tagId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
