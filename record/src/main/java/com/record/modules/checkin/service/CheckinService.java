package com.record.modules.checkin.service;

import com.record.modules.checkin.model.dto.CheckinRequest;
import com.record.modules.checkin.model.dto.CreateCheckinTaskRequest;
import com.record.modules.checkin.model.vo.CheckinTaskVO;

import java.time.LocalDate;
import java.util.List;

public interface CheckinService {
    CheckinTaskVO createTask(Long userId, CreateCheckinTaskRequest request);
    List<CheckinTaskVO> listTasks(Long userId);
    void checkin(Long userId, Long taskId, CheckinRequest request);
    List<CheckinTaskVO> listByDate(Long userId, LocalDate date);
}

