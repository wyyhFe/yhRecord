package com.record.modules.blog.controller;

import com.record.common.context.UserContext;
import com.record.common.enums.BlogStatus;
import com.record.common.model.ApiResponse;
import com.record.common.model.PageResult;
import com.record.common.util.PageQuery;
import com.record.modules.blog.model.dto.BlogPostRequest;
import com.record.modules.blog.model.vo.BlogPostVO;
import com.record.modules.blog.service.BlogService;
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
 * 博客管理接口（admin/editor 用）。
 */
@Tag(name = "博客管理")
@RestController
@RequestMapping("/blog/posts")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @Operation(summary = "创建文章")
    @PostMapping
    public ApiResponse<BlogPostVO> create(@Valid @RequestBody BlogPostRequest request) {
        return ApiResponse.success(blogService.create(request));
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    public ApiResponse<BlogPostVO> update(@PathVariable Long id, @Valid @RequestBody BlogPostRequest request) {
        return ApiResponse.success(blogService.update(id, request));
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        blogService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "分页查询文章（admin）")
    @GetMapping
    public ApiResponse<PageResult<BlogPostVO>> list(
            PageQuery pageQuery,
            @RequestParam(required = false) BlogStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(PageResult.from(blogService.list(pageQuery, status, category, keyword)));
    }

    @Operation(summary = "查询文章详情（含 Markdown 原文）")
    @GetMapping("/{id}")
    public ApiResponse<BlogPostVO> detail(@PathVariable Long id) {
        return ApiResponse.success(blogService.detail(id));
    }
}
