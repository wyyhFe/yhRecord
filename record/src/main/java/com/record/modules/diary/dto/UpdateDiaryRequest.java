package com.record.modules.diary.dto;

import lombok.Data;

/**
 * 更新日记请求体。
 * 当前与创建日记字段保持一致，因此直接继承创建请求。
 */
@Data
public class UpdateDiaryRequest extends CreateDiaryRequest {
}
