package com.record.modules.checkin.service;

import com.record.modules.checkin.model.dto.CheckinRequest;
import com.record.modules.checkin.model.dto.CreateCheckinTagRequest;
import com.record.modules.checkin.model.dto.CreateCheckinTaskRequest;
import com.record.modules.checkin.model.dto.MendCheckinRequest;
import com.record.modules.checkin.model.entity.CheckinTag;
import com.record.modules.checkin.model.vo.CheckinTaskVO;
import com.record.modules.checkin.model.vo.HeatmapVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CheckinService {

    CheckinTaskVO createTask(Long userId, CreateCheckinTaskRequest request);

    List<CheckinTaskVO> listTasks(Long userId);

    void deleteTask(Long userId, Long taskId);

    void checkin(Long userId, Long taskId, CheckinRequest request);

    List<CheckinTaskVO> listByDate(Long userId, LocalDate date);

    /**
     * 按日期范围批量统计每天的打卡次数。
     * 一次查询完成，比逐天查询高效 N 倍。
     *
     * @return Map<日期, 次数>
     */
    Map<LocalDate, Long> countByDateRange(Long userId, LocalDate start, LocalDate end);

    /**
     * 获取热力图数据（按月统计 + 连续天数）。
     */
    HeatmapVO getHeatmap(Long userId, int year, int month);

    /**
     * 获取标签列表（系统预设 + 用户自定义）。
     */
    List<CheckinTag> listTags(Long userId);

    /**
     * 创建用户自定义标签。
     */
    CheckinTag createTag(Long userId, CreateCheckinTagRequest request);

    /**
     * 补卡。
     */
    void mendCheckin(Long userId, MendCheckinRequest request);

    /**
     * 查询当月剩余补卡次数。
     */
    long getMonthlyMendRemaining(Long userId);
}
