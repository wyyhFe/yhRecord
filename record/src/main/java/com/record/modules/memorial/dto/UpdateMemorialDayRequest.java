package com.record.modules.memorial.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新纪念日请求体。
 * 当前沿用创建纪念日的字段结构。
 */
@Data
@Schema(description = "更新纪念日请求体")
public class UpdateMemorialDayRequest extends CreateMemorialDayRequest {
}
