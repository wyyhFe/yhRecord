package com.record.modules.recycle.controller;

import com.record.common.context.UserContext;
import com.record.common.enums.ResourceType;
import com.record.common.model.ApiResponse;
import com.record.modules.diary.service.DiaryService;
import com.record.modules.ledger.service.LedgerService;
import com.record.modules.recycle.model.entity.RecycleBinRecord;
import com.record.modules.recycle.model.vo.RecycleBinItemVO;
import com.record.modules.recycle.service.RecycleBinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "回收站")
@RestController
@RequestMapping("/recycle-bin")
public class RecycleBinController {

    private final RecycleBinService recycleBinService;
    private final DiaryService diaryService;
    private final LedgerService ledgerService;

    public RecycleBinController(RecycleBinService recycleBinService,
                                DiaryService diaryService,
                                LedgerService ledgerService) {
        this.recycleBinService = recycleBinService;
        this.diaryService = diaryService;
        this.ledgerService = ledgerService;
    }

    @Operation(summary = "查询回收站列表")
    @GetMapping("/list")
    public ApiResponse<List<RecycleBinItemVO>> list() {
        return ApiResponse.success(recycleBinService.list(UserContext.getUserId()));
    }

    @Operation(summary = "恢复回收站资源")
    @PostMapping("/restore/{recycleId}")
    public ApiResponse<Void> restore(@PathVariable Long recycleId, @RequestParam Long resourceId) {
        RecycleBinRecord record = recycleBinService.getOwnedRecord(UserContext.getUserId(), recycleId);
        if (record == null || !record.getResourceId().equals(resourceId)) {
            return ApiResponse.failure(-1, "回收站记录不存在");
        }

        if (record.getResourceType() == ResourceType.DIARY) {
            diaryService.restore(UserContext.getUserId(), resourceId);
        } else if (record.getResourceType() == ResourceType.LEDGER_ENTRY) {
            ledgerService.restoreEntry(UserContext.getUserId(), resourceId);
        } else {
            return ApiResponse.failure(-1, "当前资源暂不支持恢复");
        }

        return ApiResponse.success();
    }

    @Operation(summary = "彻底删除回收站资源")
    @DeleteMapping("/force-delete/{recycleId}")
    public ApiResponse<Void> forceDelete(@PathVariable Long recycleId, @RequestParam Long resourceId) {
        RecycleBinRecord record = recycleBinService.getOwnedRecord(UserContext.getUserId(), recycleId);
        if (record == null || !record.getResourceId().equals(resourceId)) {
            return ApiResponse.failure(-1, "回收站记录不存在");
        }

        if (record.getResourceType() == ResourceType.DIARY) {
            diaryService.forceDelete(UserContext.getUserId(), resourceId);
        } else if (record.getResourceType() == ResourceType.LEDGER_ENTRY) {
            ledgerService.forceDeleteEntry(UserContext.getUserId(), resourceId);
        } else {
            return ApiResponse.failure(-1, "当前资源暂不支持彻底删除");
        }

        return ApiResponse.success();
    }
}
