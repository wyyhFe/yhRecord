package com.record.common.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 统一分页响应格式。
 * <p>将 MyBatis-Plus Page 的 records/current/size 转成
 * 管理后台前端期望的 list/pageNum/pageSize。</p>
 */
@Data
@Schema(description = "分页响应")
public class PageResult<T> {

    @Schema(description = "当前页数据列表")
    private List<T> list;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码")
    private long pageNum;

    @Schema(description = "每页大小")
    private long pageSize;

    /**
     * 从 MyBatis-Plus Page 创建统一分页结果。
     */
    public static <T> PageResult<T> from(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setList(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        return result;
    }

    public static <T> PageResult<T> empty() {
        PageResult<T> result = new PageResult<>();
        result.setList(List.of());
        result.setTotal(0);
        result.setPageNum(1);
        result.setPageSize(10);
        return result;
    }
}
