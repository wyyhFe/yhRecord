package com.record.modules.dashboard.controller;

import com.record.common.model.ApiResponse;
import com.record.modules.dashboard.model.vo.DashboardStatsVO;
import com.record.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "仪表盘")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "获取统计数据")
    @GetMapping("/stats")
    public ApiResponse<DashboardStatsVO> getStats() {
        return ApiResponse.success(dashboardService.getStats());
    }
}
