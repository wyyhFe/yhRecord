package com.record.modules.memorial.service;

import com.record.modules.memorial.model.dto.CreateMemorialDayRequest;
import com.record.modules.memorial.model.dto.UpdateMemorialDayRequest;
import com.record.modules.memorial.model.vo.MemorialDayVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MemorialDayService {
    MemorialDayVO create(Long userId, CreateMemorialDayRequest request);
    List<MemorialDayVO> list(Long userId);
    MemorialDayVO update(Long userId, Long id, UpdateMemorialDayRequest request);
    void delete(Long userId, Long id);
    List<MemorialDayVO> listByDate(Long userId, LocalDate date);

    /**
     * 按日期范围批量统计每天的纪念日数量。
     * 纪念日数据量小，一次查询后在 Java 中过滤。
     *
     * @return Map<日期, 数量>
     */
    Map<LocalDate, Long> countByDateRange(Long userId, LocalDate start, LocalDate end);
}

