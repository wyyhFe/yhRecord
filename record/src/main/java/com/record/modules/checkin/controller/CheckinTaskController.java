package com.record.modules.checkin.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.common.model.PageResult;
import com.record.common.util.PageQuery;
import com.record.modules.checkin.model.dto.CheckinRequest;
import com.record.modules.checkin.model.dto.CheckinUpdateRequest;
import com.record.modules.checkin.model.dto.CreateCheckinTagRequest;
import com.record.modules.checkin.model.dto.CreateCheckinTaskRequest;
import com.record.modules.checkin.model.dto.MendCheckinRequest;
import com.record.modules.checkin.model.entity.CheckinTag;
import com.record.modules.checkin.model.vo.CheckinDayDetailVO;
import com.record.modules.checkin.model.vo.CheckinRecordVO;
import com.record.modules.checkin.model.vo.CheckinTaskVO;
import com.record.modules.checkin.model.vo.HeatmapVO;
import com.record.modules.checkin.model.vo.MedalVO;
import com.record.modules.checkin.service.CheckinService;
import com.record.modules.checkin.service.MedalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "打卡")
@RestController
@RequestMapping("/checkin")
public class CheckinTaskController {

    private final CheckinService checkinService;
    private final MedalService medalService;

    public CheckinTaskController(CheckinService checkinService, MedalService medalService) {
        this.checkinService = checkinService;
        this.medalService = medalService;
    }

    @Operation(summary = "创建打卡任务")
    @PostMapping("/tasks/create")
    public ApiResponse<CheckinTaskVO> create(@Valid @RequestBody CreateCheckinTaskRequest request) {
        return ApiResponse.success(checkinService.createTask(UserContext.getUserId(), request));
    }

    @Operation(summary = "分页查询任务列表（管理员返回全量，普通用户返回自己的）")
    @GetMapping("/tasks/list")
    public ApiResponse<PageResult<CheckinTaskVO>> list(PageQuery pageQuery,
                                                  @RequestParam(required = false) String name) {
        return ApiResponse.success(PageResult.from(checkinService.listTasks(UserContext.getUserId(), pageQuery, name)));
    }

    @Operation(summary = "删除打卡任务（管理员可删任意）")
    @DeleteMapping("/tasks/delete/{taskId}")
    public ApiResponse<Void> delete(@PathVariable Long taskId) {
        checkinService.deleteTask(UserContext.getUserId(), taskId);
        return ApiResponse.success();
    }

    @Operation(summary = "完成打卡")
    @PostMapping("/records/check/{taskId}")
    public ApiResponse<Void> checkin(@PathVariable Long taskId, @RequestBody CheckinRequest request) {
        checkinService.checkin(UserContext.getUserId(), taskId, request);
        return ApiResponse.success();
    }

    @Operation(summary = "编辑已打卡记录的备注/心情/标签/附件")
    @PutMapping("/records/update/{recordId}")
    public ApiResponse<Void> updateRecord(@PathVariable Long recordId, @RequestBody CheckinUpdateRequest request) {
        checkinService.updateRecord(UserContext.getUserId(), recordId, request);
        return ApiResponse.success();
    }

    @Operation(summary = "按日期查询打卡")
    @GetMapping("/calendar")
    public ApiResponse<List<CheckinTaskVO>> calendar(@RequestParam LocalDate date) {
        return ApiResponse.success(checkinService.listByDate(UserContext.getUserId(), date));
    }

    @Operation(summary = "查询某日打卡详情")
    @GetMapping("/day-detail")
    public ApiResponse<List<CheckinTaskVO>> dayDetail(@RequestParam LocalDate date) {
        return ApiResponse.success(checkinService.listByDate(UserContext.getUserId(), date));
    }

    @Operation(summary = "查询某日打卡时间线详情（含打卡总次数、按时间排序的记录）")
    @GetMapping("/day/timeline")
    public ApiResponse<CheckinDayDetailVO> dayTimeline(@RequestParam LocalDate date) {
        return ApiResponse.success(checkinService.getDayTimeline(UserContext.getUserId(), date));
    }

    @Operation(summary = "获取热力图数据")
    @GetMapping("/heatmap")
    public ApiResponse<HeatmapVO> heatmap(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.success(checkinService.getHeatmap(UserContext.getUserId(), year, month));
    }

    @Operation(summary = "获取标签列表")
    @GetMapping("/tags")
    public ApiResponse<List<CheckinTag>> listTags() {
        return ApiResponse.success(checkinService.listTags(UserContext.getUserId()));
    }

    @Operation(summary = "创建自定义标签")
    @PostMapping("/tags")
    public ApiResponse<CheckinTag> createTag(@Valid @RequestBody CreateCheckinTagRequest request) {
        return ApiResponse.success(checkinService.createTag(UserContext.getUserId(), request));
    }

    @Operation(summary = "删除自定义标签")
    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        checkinService.deleteTag(UserContext.getUserId(), id);
        return ApiResponse.success();
    }

    @Operation(summary = "补卡")
    @PostMapping("/records/mend")
    public ApiResponse<Void> mend(@Valid @RequestBody MendCheckinRequest request) {
        checkinService.mendCheckin(UserContext.getUserId(), request);
        return ApiResponse.success();
    }

    @Operation(summary = "查询当月剩余补卡次数")
    @GetMapping("/mend-remaining")
    public ApiResponse<Long> mendRemaining() {
        return ApiResponse.success(checkinService.getMonthlyMendRemaining(UserContext.getUserId()));
    }

    @Operation(summary = "获取勋章列表")
    @GetMapping("/medals")
    public ApiResponse<List<MedalVO>> medals() {
        return ApiResponse.success(medalService.listMedals(UserContext.getUserId()));
    }

    @Operation(summary = "分页查询打卡记录（管理后台专用，管理员返回全量）")
    @GetMapping("/records/list")
    public ApiResponse<PageResult<CheckinRecordVO>> listRecords(PageQuery pageQuery,
                                                           @RequestParam(required = false) String taskName,
                                                           @RequestParam(required = false) LocalDate checkinDate) {
        return ApiResponse.success(PageResult.from(checkinService.listRecords(UserContext.getUserId(), pageQuery, taskName, checkinDate)));
    }
}
