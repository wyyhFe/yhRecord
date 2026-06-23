package com.record.modules.memorial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.enums.CommonStatus;
import com.record.common.exception.MemorialException;
import com.record.common.util.AuthUtil;
import com.record.common.util.PageQuery;
import com.record.modules.memorial.mapper.MemorialDayMapper;
import com.record.modules.memorial.model.dto.CreateMemorialDayRequest;
import com.record.modules.memorial.model.dto.UpdateMemorialDayRequest;
import com.record.modules.memorial.model.entity.MemorialDay;
import com.record.modules.memorial.model.vo.MemorialDayVO;
import com.record.modules.memorial.service.MemorialDayService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 纪念日服务实现。
 */
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
    public Page<MemorialDayVO> list(Long userId, PageQuery pageQuery) {
        LambdaQueryWrapper<MemorialDay> wrapper = new LambdaQueryWrapper<MemorialDay>()
                .orderByAsc(MemorialDay::getMemorialDate);
        if (!AuthUtil.isAdmin()) {
            wrapper.eq(MemorialDay::getUserId, userId);
        }
        Page<MemorialDay> page = memorialDayMapper.selectPage(pageQuery.toPage(), wrapper);
        List<MemorialDayVO> vos = page.getRecords().stream().map(this::toVO).toList();
        Page<MemorialDayVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(vos);
        return result;
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
        LambdaQueryWrapper<MemorialDay> wrapper = new LambdaQueryWrapper<>();
        if (!AuthUtil.isAdmin()) {
            wrapper.eq(MemorialDay::getUserId, userId);
        }
        return memorialDayMapper.selectList(wrapper)
                .stream()
                .filter(item -> isSameDay(item, date))
                .map(this::toVO)
                .toList();
    }

    @Override
    public Map<LocalDate, Long> countByDateRange(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<MemorialDay> wrapper = new LambdaQueryWrapper<>();
        if (!AuthUtil.isAdmin()) {
            wrapper.eq(MemorialDay::getUserId, userId);
        }
        List<MemorialDay> all = memorialDayMapper.selectList(wrapper);

        Map<LocalDate, Long> result = new HashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            final LocalDate current = d;
            long cnt = all.stream().filter(md -> isSameDay(md, current)).count();
            if (cnt > 0) {
                result.put(d, cnt);
            }
        }
        return result;
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
        if (memorialDay == null) {
            throw new MemorialException("纪念日不存在或无权访问");
        }
        if (!AuthUtil.isAdmin() && !memorialDay.getUserId().equals(userId)) {
            throw new MemorialException("纪念日不存在或无权访问");
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
