package com.record.modules.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建账本请求体。
 */
@Data
public class CreateBookRequest {
    /** 账本名称。 */
    @NotBlank
    private String name;
    /** 账本描述。 */
    private String description;
}
