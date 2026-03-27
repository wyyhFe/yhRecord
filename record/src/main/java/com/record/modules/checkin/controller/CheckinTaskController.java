package com.record.modules.checkin.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.checkin.model.dto.CheckinRequest;
import com.record.modules.checkin.model.dto.CreateCheckinTaskRequest;
import com.record.modules.checkin.model.vo.CheckinTaskVO;
import com.record.modules.checkin.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    public CheckinTaskController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @Operation(summary = "创建打卡任务")
    @PostMapping("/tasks/create")
    public ApiResponse<CheckinTaskVO> create(@Valid @RequestBody CreateCheckinTaskRequest request) {
        return ApiResponse.success(checkinService.createTask(UserContext.getUserId(), request));
    }

    @Operation(summary = "查询任务列表")
    @GetMapping("/tasks/list")
    public ApiResponse<List<CheckinTaskVO>> list() {
        return ApiResponse.success(checkinService.listTasks(UserContext.getUserId()));
    }

    @Operation(summary = "删除打卡任务")
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
}
