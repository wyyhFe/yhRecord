package com.record.modules.checkin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.util.PageQuery;
import com.record.modules.checkin.model.dto.CheckinRequest;
import com.record.modules.checkin.model.dto.CreateCheckinTagRequest;
import com.record.modules.checkin.model.dto.CreateCheckinTaskRequest;
import com.record.modules.checkin.model.dto.MendCheckinRequest;
import com.record.modules.checkin.model.entity.CheckinTag;
import com.record.modules.checkin.model.vo.CheckinRecordVO;
import com.record.modules.checkin.model.vo.CheckinTaskVO;
import com.record.modules.checkin.model.vo.HeatmapVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CheckinService {

    CheckinTaskVO createTask(Long userId, CreateCheckinTaskRequest request);

    /**
     * 分页查询打卡任务。
     * <p>管理员（ROLE_admin）可查看所有用户的任务，普通用户只查看自己的。</p>
     */
    Page<CheckinTaskVO> listTasks(Long userId, PageQuery pageQuery, String name);

    /**
     * 删除打卡任务。
     * <p>管理员可删除任意任务，普通用户只能删除自己的。</p>
     */
    void deleteTask(Long userId, Long taskId);

    void checkin(Long userId, Long taskId, CheckinRequest request);

    List<CheckinTaskVO> listByDate(Long userId, LocalDate date);

    Map<LocalDate, Long> countByDateRange(Long userId, LocalDate start, LocalDate end);

    HeatmapVO getHeatmap(Long userId, int year, int month);

    List<CheckinTag> listTags(Long userId);

    CheckinTag createTag(Long userId, CreateCheckinTagRequest request);

    void mendCheckin(Long userId, MendCheckinRequest request);

    long getMonthlyMendRemaining(Long userId);

    /**
     * 分页查询打卡记录（管理后台专用）。
     * <p>管理员可查看所有用户的记录，普通用户只查看自己的。</p>
     */
    Page<CheckinRecordVO> listRecords(Long userId, PageQuery pageQuery, String taskName, LocalDate checkinDate);
}
