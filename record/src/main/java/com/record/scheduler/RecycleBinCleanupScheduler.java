package com.record.scheduler;

import com.record.modules.recycle.service.RecycleBinService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 回收站清理定时任务。
 * 该任务会按固定时间扫描回收站，并清理已经超过保留期限的数据。
 */
@Component
public class RecycleBinCleanupScheduler {

    private final RecycleBinService recycleBinService;

    public RecycleBinCleanupScheduler(RecycleBinService recycleBinService) {
        this.recycleBinService = recycleBinService;
    }

    /**
     * 每天凌晨 03:00 执行一次回收站清理。
     * `0 0 3 * * ?` 表示每天 03:00:00 触发一次。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanup() {
        recycleBinService.purgeExpired();
    }
}

