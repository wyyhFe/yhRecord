package com.record.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一分页查询 DTO。
 * <p>
 * 所有分页接口统一使用此 DTO，避免各模块散落 current/size 参数。
 * three-level default: page默认1, size默认10, maxSize上限100。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery {

    @Min(1)
    private long current = 1;

    @Min(1)
    @Max(100)
    private long size = 10;

    /**
     * 转换为 MyBatis-Plus Page 对象。
     * 带兜底保护：current 至少为 1；size 钳制在 [1, 100]。
     */
    public <T> Page<T> toPage() {
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        return new Page<>(safeCurrent, safeSize);
    }
}
