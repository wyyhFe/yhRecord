package com.record.modules.diary.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.context.UserContext;
import com.record.common.enums.VisibilityType;
import com.record.common.model.ApiResponse;
import com.record.modules.diary.model.dto.CreateDiaryRequest;
import com.record.modules.diary.model.dto.UpdateDiaryRequest;
import com.record.modules.diary.model.vo.DiaryVO;
import com.record.modules.diary.service.DiaryService;
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

/**
 * 日记基础接口。
 */
@Tag(name = "日记管理")
@RestController
@RequestMapping("/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @Operation(summary = "创建日记")
    @PostMapping("/create")
    public ApiResponse<DiaryVO> create(@Valid @RequestBody CreateDiaryRequest request) {
        return ApiResponse.success(diaryService.create(UserContext.getUserId(), request));
    }

    @Operation(summary = "更新日记")
    @PutMapping("/update/{id}")
    public ApiResponse<DiaryVO> update(@PathVariable Long id, @Valid @RequestBody UpdateDiaryRequest request) {
        return ApiResponse.success(diaryService.update(UserContext.getUserId(), id, request));
    }

    @Operation(summary = "分页查询日记")
    @GetMapping("/list")
    public ApiResponse<Page<DiaryVO>> list(@RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) VisibilityType visibility,
                                           @RequestParam(required = false) Long tagId,
                                           @RequestParam(required = false) String keyword) {
        return ApiResponse.success(diaryService.list(UserContext.getUserId(), current, size, visibility, tagId, keyword));
    }

    @Operation(summary = "查询日记详情")
    @GetMapping("/detail/{id}")
    public ApiResponse<DiaryVO> detail(@PathVariable Long id) {
        return ApiResponse.success(diaryService.detail(UserContext.getUserId(), id));
    }

    @Operation(summary = "删除日记")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        diaryService.delete(UserContext.getUserId(), id);
        return ApiResponse.success();
    }

    @Operation(summary = "恢复日记")
    @PostMapping("/restore/{id}")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        diaryService.restore(UserContext.getUserId(), id);
        return ApiResponse.success();
    }

    @Operation(summary = "强制删除日记")
    @DeleteMapping("/force-delete/{id}")
    public ApiResponse<Void> forceDelete(@PathVariable Long id) {
        diaryService.forceDelete(UserContext.getUserId(), id);
        return ApiResponse.success();
    }

    @Operation(summary = "搜索日记")
    @GetMapping("/search")
    public ApiResponse<Page<DiaryVO>> search(@RequestParam String keyword,
                                             @RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.success(diaryService.list(UserContext.getUserId(), current, size, null, null, keyword));
    }
}
