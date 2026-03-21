package com.record.modules.memorial.service;

import com.record.modules.memorial.model.dto.CreateMemorialDayRequest;
import com.record.modules.memorial.model.dto.UpdateMemorialDayRequest;
import com.record.modules.memorial.model.vo.MemorialDayVO;

import java.time.LocalDate;
import java.util.List;

public interface MemorialDayService {
    MemorialDayVO create(Long userId, CreateMemorialDayRequest request);
    List<MemorialDayVO> list(Long userId);
    MemorialDayVO update(Long userId, Long id, UpdateMemorialDayRequest request);
    void delete(Long userId, Long id);
    List<MemorialDayVO> listByDate(Long userId, LocalDate date);
}

