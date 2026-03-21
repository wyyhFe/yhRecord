package com.record.modules.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.CommonStatus;
import com.record.common.exception.CheckinException;
import com.record.modules.checkin.mapper.CheckinRecordMapper;
import com.record.modules.checkin.mapper.CheckinTaskMapper;
import com.record.modules.checkin.model.dto.CheckinRequest;
import com.record.modules.checkin.model.dto.CreateCheckinTaskRequest;
import com.record.modules.checkin.model.entity.CheckinRecord;
import com.record.modules.checkin.model.entity.CheckinTask;
import com.record.modules.checkin.model.vo.CheckinTaskVO;
import com.record.modules.checkin.service.CheckinService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 打卡服务实现。
 */
@Service
public class CheckinServiceImpl implements CheckinService {

    private final CheckinTaskMapper checkinTaskMapper;
    private final CheckinRecordMapper checkinRecordMapper;

    public CheckinServiceImpl(CheckinTaskMapper checkinTaskMapper, CheckinRecordMapper checkinRecordMapper) {
        this.checkinTaskMapper = checkinTaskMapper;
        this.checkinRecordMapper = checkinRecordMapper;
    }

    @Override
    public CheckinTaskVO createTask(Long userId, CreateCheckinTaskRequest request) {
        CheckinTask task = new CheckinTask();
        task.setUserId(userId);
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setStartDate(request.getStartDate());
        task.setStatus(CommonStatus.ENABLED);
        checkinTaskMapper.insert(task);
        return toVO(task);
    }

    @Override
    public List<CheckinTaskVO> listTasks(Long userId) {
        return checkinTaskMapper.selectList(new LambdaQueryWrapper<CheckinTask>()
                        .eq(CheckinTask::getUserId, userId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public void checkin(Long userId, Long taskId, CheckinRequest request) {
        CheckinTask task = checkinTaskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new CheckinException("打卡任务不存在");
        }

        LocalDate checkinDate = request.getCheckinDate() == null ? LocalDate.now() : request.getCheckinDate();
        Long count = checkinRecordMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getTaskId, taskId)
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinDate, checkinDate));
        if (count > 0) {
            throw new CheckinException("同一天不能重复打卡");
        }

        CheckinRecord record = new CheckinRecord();
        record.setTaskId(taskId);
        record.setUserId(userId);
        record.setCheckinDate(checkinDate);
        record.setCheckedAt(LocalDateTime.now());
        record.setRemark(request.getRemark());
        checkinRecordMapper.insert(record);
    }

    @Override
    public List<CheckinTaskVO> listByDate(Long userId, LocalDate date) {
        List<Long> taskIds = checkinRecordMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                        .eq(CheckinRecord::getUserId, userId)
                        .eq(CheckinRecord::getCheckinDate, date))
                .stream()
                .map(CheckinRecord::getTaskId)
                .distinct()
                .toList();
        if (taskIds.isEmpty()) {
            return List.of();
        }
        return checkinTaskMapper.selectBatchIds(taskIds).stream().map(this::toVO).toList();
    }

    private CheckinTaskVO toVO(CheckinTask task) {
        List<CheckinRecord> records = checkinRecordMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getTaskId, task.getId())
                .orderByDesc(CheckinRecord::getCheckedAt));
        return CheckinTaskVO.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .startDate(task.getStartDate())
                .totalCount(records.size())
                .latestCheckedAt(records.stream().map(CheckinRecord::getCheckedAt).max(Comparator.naturalOrder()).orElse(null))
                .build();
    }
}
