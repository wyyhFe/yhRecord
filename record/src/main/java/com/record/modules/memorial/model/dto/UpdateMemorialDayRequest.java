package com.record.modules.memorial.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新纪念日请求体。
 */
@Data
@Schema(description = "更新纪念日请求体")
public class UpdateMemorialDayRequest extends CreateMemorialDayRequest {
}
