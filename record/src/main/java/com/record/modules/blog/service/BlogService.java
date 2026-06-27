package com.record.modules.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.enums.BlogStatus;
import com.record.common.util.PageQuery;
import com.record.modules.blog.model.dto.BlogCommentRequest;
import com.record.modules.blog.model.dto.BlogPostRequest;
import com.record.modules.blog.model.vo.BlogCommentVO;
import com.record.modules.blog.model.vo.BlogPostVO;

import java.util.List;

public interface BlogService {

    // ── admin 管理接口 ──

    /** 创建文章（admin/editor），返回完整 VO 含 slug */
    BlogPostVO create(BlogPostRequest request);

    /** 更新文章 */
    BlogPostVO update(Long postId, BlogPostRequest request);

    /** 删除文章 */
    void delete(Long postId);

    /** 分页查询文章列表 */
    Page<BlogPostVO> list(PageQuery pageQuery, BlogStatus status, String category, String keyword);

    /** 查询文章详情（含 Markdown 原文，admin 用） */
    BlogPostVO detail(Long postId);

    // ── 公开接口（WEB C 端） ──

    /** 根据 slug 获取已发布文章详情（含 HTML，公开访问） */
    BlogPostVO getBySlug(String slug);

    /** 分页查询已发布文章列表（公开访问） */
    Page<BlogPostVO> listPublic(PageQuery pageQuery, String category, String keyword);

    /** 记录一次浏览（IP + 用户去重，异步更新 view_count） */
    void recordView(Long postId, Long userId, String viewerIp);

    // ── 评论 ──

    /** 发表评论 */
    BlogCommentVO comment(Long userId, BlogCommentRequest request);

    /** 查询评论列表（树形嵌套） */
    List<BlogCommentVO> comments(String targetType, Long targetId);
}
