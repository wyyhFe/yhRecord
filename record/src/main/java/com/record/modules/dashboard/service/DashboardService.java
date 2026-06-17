package com.record.modules.dashboard.service;

import com.record.modules.dashboard.model.vo.DashboardStatsVO;

/**
 * 仪表盘服务。
 */
public interface DashboardService {

    /**
     * 获取各模块的统计数据。
     */
    DashboardStatsVO getStats();
}
