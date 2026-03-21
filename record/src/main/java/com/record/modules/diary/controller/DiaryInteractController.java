package com.record.modules.diary.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.diary.model.dto.DiaryCommentRequest;
import com.record.modules.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 日记互动控制器。
 * 负责点赞、评论新增和评论列表查询。
 */
@Tag(name = "日记互动")
@RestController
@RequestMapping("/diaries")
public class DiaryInteractController {

    private final DiaryService diaryService;

    public DiaryInteractController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    /**
     * 同一接口同时承担点赞和取消点赞两个动作。
     */
    @Operation(summary = "切换点赞")
    @PostMapping("/{id}/like")
    public ApiResponse<Void> like(@PathVariable Long id) {
        diaryService.toggleLike(UserContext.getUserId(), id);
        return ApiResponse.success();
    }

    @Operation(summary = "发表评论")
    @PostMapping("/{id}/comments")
    public ApiResponse<Void> comment(@PathVariable Long id, @Valid @RequestBody DiaryCommentRequest request) {
        diaryService.comment(UserContext.getUserId(), id, request);
        return ApiResponse.success();
    }

    @Operation(summary = "评论列表")
    @GetMapping("/{id}/comments")
    public ApiResponse<List<String>> comments(@PathVariable Long id) {
        return ApiResponse.success(diaryService.comments(UserContext.getUserId(), id));
    }
}
