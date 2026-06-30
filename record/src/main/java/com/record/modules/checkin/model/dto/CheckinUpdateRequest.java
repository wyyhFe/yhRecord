package com.record.modules.checkin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 编辑打卡记录请求体。
 */
@Data
@Schema(description = "编辑打卡记录请求体")
public class CheckinUpdateRequest {

    @Schema(description = "备注", example = "今天也完成了")
    private String remark;

    @Schema(description = "心情 Emoji", example = "😊")
    private String mood;

    @Schema(description = "选中的标签 ID 列表")
    private List<Long> tagIds;

    @Schema(description = "附件列表（传空列表表示清空，传 null 表示不修改）")
    private List<CheckinMediaDTO> mediaList;
}
