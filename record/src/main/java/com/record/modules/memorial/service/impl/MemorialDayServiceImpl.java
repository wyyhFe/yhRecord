package com.record.modules.memorial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.CommonStatus;
import com.record.common.exception.MemorialException;
import com.record.modules.memorial.dto.CreateMemorialDayRequest;
import com.record.modules.memorial.dto.UpdateMemorialDayRequest;
import com.record.modules.memorial.entity.MemorialDay;
import com.record.modules.memorial.mapper.MemorialDayMapper;
import com.record.modules.memorial.service.MemorialDayService;
import com.record.modules.memorial.vo.MemorialDayVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MemorialDayServiceImpl implements MemorialDayService {

    private final MemorialDayMapper memorialDayMapper;

    public MemorialDayServiceImpl(MemorialDayMapper memorialDayMapper) {
        this.memorialDayMapper = memorialDayMapper;
    }

    @Override
    public MemorialDayVO create(Long userId, CreateMemorialDayRequest request) {
        MemorialDay memorialDay = new MemorialDay();
        fill(memorialDay, userId, request);
        memorialDay.setStatus(CommonStatus.ENABLED);
        memorialDayMapper.insert(memorialDay);
        return toVO(memorialDay);
    }

    @Override
    public List<MemorialDayVO> list(Long userId) {
        return memorialDayMapper.selectList(new LambdaQueryWrapper<MemorialDay>()
                        .eq(MemorialDay::getUserId, userId)
                        .orderByAsc(MemorialDay::getMemorialDate))
                .stream().map(this::toVO).toList();
    }

    @Override
    public MemorialDayVO update(Long userId, Long id, UpdateMemorialDayRequest request) {
        MemorialDay memorialDay = requireOwned(userId, id);
        fill(memorialDay, userId, request);
        memorialDayMapper.updateById(memorialDay);
        return toVO(memorialDay);
    }

    @Override
    public void delete(Long userId, Long id) {
        requireOwned(userId, id);
        memorialDayMapper.deleteById(id);
    }

    @Override
    public List<MemorialDayVO> listByDate(Long userId, LocalDate date) {
        return memorialDayMapper.selectList(new LambdaQueryWrapper<MemorialDay>().eq(MemorialDay::getUserId, userId))
                .stream().filter(item -> isSameDay(item, date)).map(this::toVO).toList();
    }

    private void fill(MemorialDay memorialDay, Long userId, CreateMemorialDayRequest request) {
        memorialDay.setUserId(userId);
        memorialDay.setTitle(request.getTitle());
        memorialDay.setType(request.getType());
        memorialDay.setMemorialDate(request.getMemorialDate());
        memorialDay.setAnnualRepeat(Boolean.TRUE.equals(request.getAnnualRepeat()));
        memorialDay.setRemark(request.getRemark());
        memorialDay.setRemindAt(request.getRemindAt());
    }

    private MemorialDay requireOwned(Long userId, Long id) {
        MemorialDay memorialDay = memorialDayMapper.selectById(id);
        if (memorialDay == null || !memorialDay.getUserId().equals(userId)) {
            throw new MemorialException("纪念日不存在");
        }
        return memorialDay;
    }

    private boolean isSameDay(MemorialDay memorialDay, LocalDate date) {
        if (Boolean.TRUE.equals(memorialDay.getAnnualRepeat())) {
            return memorialDay.getMemorialDate().getMonthValue() == date.getMonthValue()
                    && memorialDay.getMemorialDate().getDayOfMonth() == date.getDayOfMonth();
        }
        return memorialDay.getMemorialDate().equals(date);
    }

    private MemorialDayVO toVO(MemorialDay memorialDay) {
        return MemorialDayVO.builder()
                .id(memorialDay.getId())
                .title(memorialDay.getTitle())
                .type(memorialDay.getType())
                .memorialDate(memorialDay.getMemorialDate())
                .annualRepeat(memorialDay.getAnnualRepeat())
                .remark(memorialDay.getRemark())
                .remindAt(memorialDay.getRemindAt())
                .build();
    }
}
