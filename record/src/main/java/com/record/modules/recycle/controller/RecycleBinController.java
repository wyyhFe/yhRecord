package com.record.modules.recycle.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.diary.service.DiaryService;
import com.record.modules.recycle.service.RecycleBinService;
import com.record.modules.recycle.vo.RecycleBinItemVO;
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

/**
 * 回收站接口。
 * 当前首版先打通日记资源的恢复和彻底删除。
 */
@Tag(name = "回收站")
@RestController
@RequestMapping("/recycle-bin")
public class RecycleBinController {

    private final RecycleBinService recycleBinService;
    private final DiaryService diaryService;

    public RecycleBinController(RecycleBinService recycleBinService, DiaryService diaryService) {
        this.recycleBinService = recycleBinService;
        this.diaryService = diaryService;
    }

    /**
     * 查询回收站列表。
     */
    @Operation(summary = "回收站列表")
    @GetMapping("/list")
    public ApiResponse<List<RecycleBinItemVO>> list() {
        return ApiResponse.success(recycleBinService.list(UserContext.getUserId()));
    }

    /**
     * 恢复已删除资源。
     */
    @Operation(summary = "恢复资源")
    @PostMapping("/restore/{recycleId}")
    public ApiResponse<Void> restore(@PathVariable Long recycleId, @RequestParam Long resourceId) {
        diaryService.restore(UserContext.getUserId(), resourceId);
        recycleBinService.restore(UserContext.getUserId(), recycleId);
        return ApiResponse.success();
    }

    /**
     * 彻底删除资源。
     */
    @Operation(summary = "彻底删除资源")
    @DeleteMapping("/force-delete/{recycleId}")
    public ApiResponse<Void> forceDelete(@PathVariable Long recycleId, @RequestParam Long resourceId) {
        diaryService.forceDelete(UserContext.getUserId(), resourceId);
        recycleBinService.forceDelete(UserContext.getUserId(), recycleId);
        return ApiResponse.success();
    }
}
