package com.record.modules.diary.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新日记请求体。
 * 当前字段与创建日记保持一致，因此直接继承创建请求。
 */
@Data
@Schema(description = "更新日记请求体")
public class UpdateDiaryRequest extends CreateDiaryRequest {
}
