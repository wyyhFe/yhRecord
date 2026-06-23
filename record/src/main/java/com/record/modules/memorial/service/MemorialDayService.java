package com.record.modules.memorial.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.util.PageQuery;
import com.record.modules.memorial.model.dto.CreateMemorialDayRequest;
import com.record.modules.memorial.model.dto.UpdateMemorialDayRequest;
import com.record.modules.memorial.model.vo.MemorialDayVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MemorialDayService {
    MemorialDayVO create(Long userId, CreateMemorialDayRequest request);

    /**
     * 分页查询纪念日列表（管理员可查全量）。
     */
    Page<MemorialDayVO> list(Long userId, PageQuery pageQuery);

    MemorialDayVO update(Long userId, Long id, UpdateMemorialDayRequest request);

    void delete(Long userId, Long id);

    List<MemorialDayVO> listByDate(Long userId, LocalDate date);

    Map<LocalDate, Long> countByDateRange(Long userId, LocalDate start, LocalDate end);
}
