package com.record.scheduler;

import com.record.modules.recycle.service.RecycleBinService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecycleBinCleanupScheduler {

    private final RecycleBinService recycleBinService;

    public RecycleBinCleanupScheduler(RecycleBinService recycleBinService) {
        this.recycleBinService = recycleBinService;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanup() {
        recycleBinService.purgeExpired();
    }
}
