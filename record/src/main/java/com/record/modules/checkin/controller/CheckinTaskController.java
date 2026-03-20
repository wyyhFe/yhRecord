package com.record.modules.checkin.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.checkin.dto.CheckinRequest;
import com.record.modules.checkin.dto.CreateCheckinTaskRequest;
import com.record.modules.checkin.service.CheckinService;
import com.record.modules.checkin.vo.CheckinTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 打卡任务接口。
 */
@Tag(name = "打卡")
@RestController
@RequestMapping("/checkin")
public class CheckinTaskController {

    private final CheckinService checkinService;

    public CheckinTaskController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    /**
     * 创建打卡任务。
     */
    @Operation(summary = "创建打卡任务")
    @PostMapping("/tasks/create")
    public ApiResponse<CheckinTaskVO> create(@Valid @RequestBody CreateCheckinTaskRequest request) {
        return ApiResponse.success(checkinService.createTask(UserContext.getUserId(), request));
    }

    /**
     * 查询当前用户的打卡任务列表。
     */
    @Operation(summary = "任务列表")
    @GetMapping("/tasks/list")
    public ApiResponse<List<CheckinTaskVO>> list() {
        return ApiResponse.success(checkinService.listTasks(UserContext.getUserId()));
    }

    /**
     * 执行一次打卡。
     */
    @Operation(summary = "执行打卡")
    @PostMapping("/records/check/{taskId}")
    public ApiResponse<Void> checkin(@PathVariable Long taskId, @RequestBody CheckinRequest request) {
        checkinService.checkin(UserContext.getUserId(), taskId, request);
        return ApiResponse.success();
    }

    /**
     * 按日期查询打卡日历详情。
     */
    @Operation(summary = "按日期查询打卡日历")
    @GetMapping("/calendar")
    public ApiResponse<List<CheckinTaskVO>> calendar(@RequestParam LocalDate date) {
        return ApiResponse.success(checkinService.listByDate(UserContext.getUserId(), date));
    }

    /**
     * 查询某一天的打卡详情。
     */
    @Operation(summary = "某天打卡详情")
    @GetMapping("/day-detail")
    public ApiResponse<List<CheckinTaskVO>> dayDetail(@RequestParam LocalDate date) {
        return ApiResponse.success(checkinService.listByDate(UserContext.getUserId(), date));
    }
}
