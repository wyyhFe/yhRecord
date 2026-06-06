package com.record.modules.system.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台合并用户请求体。
 */
@Data
@Schema(description = "合并用户请求")
public class UserMergeRequest {

    @NotNull
    @Schema(description = "源用户 ID（被合并）", example = "10001")
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
    private Long sourceUserId;

    @NotNull
    @Schema(description = "目标用户 ID（保留）", example = "10002")
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
    private Long targetUserId;
}
