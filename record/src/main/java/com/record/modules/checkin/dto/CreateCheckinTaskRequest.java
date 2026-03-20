package com.record.modules.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建打卡任务请求体。
 */
@Data
public class CreateCheckinTaskRequest {
    /** 任务名称。 */
    @NotBlank
    private String name;
    /** 任务描述。 */
    private String description;
    /** 任务开始日期。 */
    @NotNull
    private LocalDate startDate;
}
