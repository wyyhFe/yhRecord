package com.record.modules.blog.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.blog.model.dto.BlogCommentRequest;
import com.record.modules.blog.model.vo.BlogCommentVO;
import com.record.modules.blog.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 博客评论接口。
 * 发表评论需要登录（如果用户选择匿名评论，则需额外处理）。
 */
@Tag(name = "博客评论")
@RestController
@RequestMapping("/blog/comments")
public class BlogCommentController {

    private final BlogService blogService;

    public BlogCommentController(BlogService blogService) {
        this.blogService = blogService;
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public ApiResponse<BlogCommentVO> comment(@Valid @RequestBody BlogCommentRequest request,
                                              HttpServletRequest servletRequest) {
        Long userId = UserContext.getUserId();
        // 未登录则无法评论
        if (userId == null) {
            return ApiResponse.failure(401, "请先登录后再评论");
        }
        // 自动填充平台信息
        if (request.getPlatform() == null) {
            request.setPlatform("WEB");
        }
        return ApiResponse.success(blogService.comment(userId, request));
    }

    @Operation(summary = "查询评论列表")
    @GetMapping
    public ApiResponse<List<BlogCommentVO>> comments(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return ApiResponse.success(blogService.comments(targetType, targetId));
    }
}
