package com.record.modules.blog.controller;

import com.record.common.model.ApiResponse;
import com.record.common.model.PageResult;
import com.record.common.util.PageQuery;
import com.record.modules.blog.model.vo.BlogPostVO;
import com.record.modules.blog.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 博客公开接口（C 端无需登录）。
 */
@Tag(name = "博客公开")
@RestController
@RequestMapping("/blog/public")
public class BlogPublicController {

    private final BlogService blogService;

    public BlogPublicController(BlogService blogService) {
        this.blogService = blogService;
    }

    @Operation(summary = "根据 slug 获取文章详情")
    @GetMapping("/posts/slug/{slug}")
    public ApiResponse<BlogPostVO> getBySlug(@PathVariable String slug) {
        return ApiResponse.success(blogService.getBySlug(slug));
    }

    @Operation(summary = "分页查询已发布文章列表")
    @GetMapping("/posts")
    public ApiResponse<PageResult<BlogPostVO>> listPublic(
            PageQuery pageQuery,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(PageResult.from(blogService.listPublic(pageQuery, category, keyword)));
    }

    @Operation(summary = "记录文章浏览")
    @PostMapping("/posts/{postId}/view")
    public ApiResponse<Void> recordView(@PathVariable Long postId, HttpServletRequest request) {
        // 未登录用户尝试获取 userId；JWT 过滤器放行时 UserContext 为 null
        Long userId = getUserIdOrNull();
        String viewerIp = getClientIp(request);
        blogService.recordView(postId, userId, viewerIp);
        return ApiResponse.success();
    }

    private Long getUserIdOrNull() {
        try {
            return com.record.common.context.UserContext.getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
