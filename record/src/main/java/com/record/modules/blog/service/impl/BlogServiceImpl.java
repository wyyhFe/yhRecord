package com.record.modules.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.config.AiProperties;
import com.record.common.enums.BlogStatus;
import com.record.common.exception.BlogException;
import com.record.common.util.PageQuery;
import com.record.modules.ai.prompt.PromptTemplateLoader;
import com.record.modules.blog.mapper.BlogCommentMapper;
import com.record.modules.blog.mapper.BlogPostMapper;
import com.record.modules.blog.mapper.BlogTagRelMapper;
import com.record.modules.blog.mapper.BlogViewMapper;
import com.record.modules.blog.model.dto.BlogCommentRequest;
import com.record.modules.blog.model.dto.BlogPostRequest;
import com.record.modules.blog.model.entity.BlogComment;
import com.record.modules.blog.model.entity.BlogPost;
import com.record.modules.blog.model.entity.BlogTagRel;
import com.record.modules.blog.model.entity.BlogView;
import com.record.modules.blog.model.vo.BlogCommentVO;
import com.record.modules.blog.model.vo.BlogPostVO;
import com.record.modules.blog.service.BlogService;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.entity.User;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 博客服务实现。
 * 负责文章 CRUD、slug 生成、评论、浏览计数等。
 */
@Service
public class BlogServiceImpl implements BlogService {

    private final BlogPostMapper blogPostMapper;
    private final BlogCommentMapper blogCommentMapper;
    private final BlogTagRelMapper blogTagRelMapper;
    private final BlogViewMapper blogViewMapper;
    private final UserMapper userMapper;
    private final PromptTemplateLoader promptTemplateLoader;
    private final AiProperties aiProperties;
    private final ObjectProvider<ChatClient> chatClientProvider;

    public BlogServiceImpl(BlogPostMapper blogPostMapper,
                           BlogCommentMapper blogCommentMapper,
                           BlogTagRelMapper blogTagRelMapper,
                           BlogViewMapper blogViewMapper,
                           UserMapper userMapper,
                           PromptTemplateLoader promptTemplateLoader,
                           AiProperties aiProperties,
                           ObjectProvider<ChatClient> chatClientProvider) {
        this.blogPostMapper = blogPostMapper;
        this.blogCommentMapper = blogCommentMapper;
        this.blogTagRelMapper = blogTagRelMapper;
        this.blogViewMapper = blogViewMapper;
        this.userMapper = userMapper;
        this.promptTemplateLoader = promptTemplateLoader;
        this.aiProperties = aiProperties;
        this.chatClientProvider = chatClientProvider;
    }

    // ── admin 管理接口 ──

    @Override
    @Transactional
    public BlogPostVO create(BlogPostRequest request) {
        BlogPost post = new BlogPost();
        fillPost(post, request);

        // slug：手动指定 > AI 生成
        if (isNotBlank(request.getSlug())) {
            ensureSlugUnique(request.getSlug(), null);
            post.setSlug(request.getSlug());
        } else {
            post.setSlug(generateSlug(request.getTitle()));
        }

        post.setViewCount(0);
        post.setCommentCount(0);

        if (request.getStatus() == null) {
            post.setStatus(BlogStatus.DRAFT);
        }

        blogPostMapper.insert(post);
        replaceTags(post.getId(), request.getTags());

        return toVO(post);
    }

    @Override
    @Transactional
    public BlogPostVO update(Long postId, BlogPostRequest request) {
        BlogPost post = requirePost(postId);
        String originalTitle = post.getTitle();
        String originalSlug = post.getSlug();

        fillPost(post, request);

        // slug：手动指定覆盖；未手动指定且标题变化 → 重新生成
        if (isNotBlank(request.getSlug())) {
            if (!request.getSlug().equals(originalSlug)) {
                ensureSlugUnique(request.getSlug(), postId);
                post.setSlug(request.getSlug());
            }
        } else if (!request.getTitle().equals(originalTitle)) {
            post.setSlug(generateSlug(request.getTitle()));
        }

        blogPostMapper.updateById(post);
        replaceTags(postId, request.getTags());

        return toVO(post);
    }

    @Override
    @Transactional
    public void delete(Long postId) {
        requirePost(postId);
        blogCommentMapper.delete(new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getTargetType, "BLOG_POST")
                .eq(BlogComment::getTargetId, postId));
        blogViewMapper.delete(new LambdaQueryWrapper<BlogView>()
                .eq(BlogView::getPostId, postId));
        blogTagRelMapper.deleteByPostId(postId);
        blogPostMapper.deleteById(postId);
    }

    @Override
    public Page<BlogPostVO> list(PageQuery pageQuery, BlogStatus status, String category, String keyword) {
        LambdaQueryWrapper<BlogPost> wrapper = new LambdaQueryWrapper<BlogPost>()
                .eq(status != null, BlogPost::getStatus, status)
                .eq(isNotBlank(category), BlogPost::getCategory, category)
                .and(isNotBlank(keyword),
                        w -> w.like(BlogPost::getTitle, keyword).or().like(BlogPost::getSummary, keyword))
                .orderByDesc(BlogPost::getCreatedAt);

        Page<BlogPost> page = blogPostMapper.selectPage(pageQuery.toPage(), wrapper);
        return toVOPage(page);
    }

    @Override
    public BlogPostVO detail(Long postId) {
        return toVO(requirePost(postId));
    }

    // ── 公开接口 ──

    @Override
    public BlogPostVO getBySlug(String slug) {
        BlogPost post = blogPostMapper.selectOne(new LambdaQueryWrapper<BlogPost>()
                .eq(BlogPost::getSlug, slug)
                .eq(BlogPost::getStatus, BlogStatus.PUBLISHED));
        if (post == null) {
            throw new BlogException("文章不存在或未发布");
        }
        return toVO(post);
    }

    @Override
    public Page<BlogPostVO> listPublic(PageQuery pageQuery, String category, String keyword) {
        LambdaQueryWrapper<BlogPost> wrapper = new LambdaQueryWrapper<BlogPost>()
                .eq(BlogPost::getStatus, BlogStatus.PUBLISHED)
                .eq(isNotBlank(category), BlogPost::getCategory, category)
                .and(isNotBlank(keyword),
                        w -> w.like(BlogPost::getTitle, keyword).or().like(BlogPost::getSummary, keyword))
                .orderByDesc(BlogPost::getPublishedAt)
                .orderByDesc(BlogPost::getCreatedAt);

        Page<BlogPost> page = blogPostMapper.selectPage(pageQuery.toPage(), wrapper);
        Page<BlogPostVO> resultPage = toVOPage(page);

        // 公开列表不返回 Markdown/HTML 正文
        if (!resultPage.getRecords().isEmpty()) {
            resultPage.getRecords().forEach(vo -> {
                vo.setMarkdownContent(null);
                vo.setHtmlContent(null);
            });
        }
        return resultPage;
    }

    @Override
    @Transactional
    public void recordView(Long postId, Long userId, String viewerIp) {
        if (blogViewMapper.existsByPostAndUserIp(postId, userId, viewerIp)) {
            return;
        }

        BlogView view = new BlogView();
        view.setPostId(postId);
        view.setUserId(userId);
        view.setViewerIp(viewerIp);
        view.setViewedAt(LocalDateTime.now());
        blogViewMapper.insert(view);

        BlogPost post = new BlogPost();
        post.setId(postId);
        post.setViewCount(blogViewMapper.selectCount(
                new LambdaQueryWrapper<BlogView>().eq(BlogView::getPostId, postId)).intValue());
        blogPostMapper.updateById(post);
    }

    // ── 评论 ──

    @Override
    @Transactional
    public BlogCommentVO comment(Long userId, BlogCommentRequest request) {
        BlogComment comment = new BlogComment();
        comment.setTargetType(request.getTargetType());
        comment.setTargetId(request.getTargetId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setDeviceModel(request.getDeviceModel());
        comment.setPlatform(request.getPlatform());
        blogCommentMapper.insert(comment);

        if ("BLOG_POST".equals(request.getTargetType())) {
            BlogPost post = requirePost(request.getTargetId());
            post.setCommentCount(blogCommentMapper.selectCount(
                    new LambdaQueryWrapper<BlogComment>()
                            .eq(BlogComment::getTargetType, "BLOG_POST")
                            .eq(BlogComment::getTargetId, request.getTargetId())).intValue());
            blogPostMapper.updateById(post);
        }

        return toCommentVO(comment);
    }

    @Override
    public List<BlogCommentVO> comments(String targetType, Long targetId) {
        List<BlogComment> all = blogCommentMapper.selectList(
                new LambdaQueryWrapper<BlogComment>()
                        .eq(BlogComment::getTargetType, targetType)
                        .eq(BlogComment::getTargetId, targetId)
                        .orderByAsc(BlogComment::getCreatedAt));

        if (all.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = all.stream().map(BlogComment::getUserId).distinct().toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<BlogCommentVO> voList = all.stream()
                .map(c -> toCommentVO(c, userMap.get(c.getUserId())))
                .toList();

        // 构建树形结构
        Map<Long, List<BlogCommentVO>> childrenMap = voList.stream()
                .filter(v -> v.getParentId() != null)
                .collect(Collectors.groupingBy(BlogCommentVO::getParentId));

        return voList.stream()
                .filter(v -> v.getParentId() == null)
                .peek(v -> v.setChildren(childrenMap.getOrDefault(v.getId(), List.of())))
                .toList();
    }

    // ── 内部方法 ──

    private BlogPost requirePost(Long postId) {
        BlogPost post = blogPostMapper.selectById(postId);
        if (post == null) {
            throw new BlogException("文章不存在");
        }
        return post;
    }

    private void fillPost(BlogPost post, BlogPostRequest request) {
        post.setTitle(request.getTitle());
        post.setMarkdownContent(request.getMarkdownContent());
        post.setHtmlContent(request.getHtmlContent());
        post.setSummary(request.getSummary());
        post.setCategory(request.getCategory());
        post.setStatus(request.getStatus());
        // 首次发布记录时间
        if (request.getStatus() == BlogStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
    }

    private Page<BlogPostVO> toVOPage(Page<BlogPost> page) {
        List<BlogPost> postList = page.getRecords();
        if (postList.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }
        List<BlogPostVO> voList = postList.stream().map(this::toVO).toList();
        Page<BlogPostVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    private BlogPostVO toVO(BlogPost post) {
        User user = userMapper.selectById(post.getUserId());
        List<String> tags = blogTagRelMapper.selectByPostId(post.getId()).stream()
                .map(BlogTagRel::getTagName)
                .toList();

        return BlogPostVO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .authorNickname(user != null ? user.getNickname() : null)
                .authorAvatar(user != null ? user.getAvatarPath() : null)
                .title(post.getTitle())
                .slug(post.getSlug())
                .markdownContent(post.getMarkdownContent())
                .htmlContent(post.getHtmlContent())
                .summary(post.getSummary())
                .category(post.getCategory())
                .tags(tags)
                .status(post.getStatus())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private BlogCommentVO toCommentVO(BlogComment comment) {
        User user = userMapper.selectById(comment.getUserId());
        return toCommentVO(comment, user);
    }

    private BlogCommentVO toCommentVO(BlogComment comment, User user) {
        return BlogCommentVO.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .userAvatar(user != null ? user.getAvatarPath() : null)
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .deviceModel(comment.getDeviceModel())
                .platform(comment.getPlatform())
                .createdAt(comment.getCreatedAt())
                .children(new ArrayList<>())
                .build();
    }

    private void replaceTags(Long postId, List<String> tags) {
        blogTagRelMapper.deleteByPostId(postId);
        if (tags == null || tags.isEmpty()) {
            return;
        }
        tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .forEach(tagName -> {
                    BlogTagRel rel = new BlogTagRel();
                    rel.setPostId(postId);
                    rel.setTagName(tagName.trim());
                    blogTagRelMapper.insert(rel);
                });
    }

    private void ensureSlugUnique(String slug, Long excludePostId) {
        LambdaQueryWrapper<BlogPost> wrapper = new LambdaQueryWrapper<BlogPost>()
                .eq(BlogPost::getSlug, slug);
        if (excludePostId != null) {
            wrapper.ne(BlogPost::getId, excludePostId);
        }
        if (blogPostMapper.selectCount(wrapper) > 0) {
            throw new BlogException("URL 标识 " + slug + " 已被占用，请换一个");
        }
    }

    // ── AI Slug 生成 ──

    private String generateSlug(String title) {
        // 纯英文标题 → 直接转小写 slug
        if (title.matches("^[a-zA-Z0-9\\s\\-]+$") && !title.matches(".*[\\u4e00-\\u9fff]+.*")) {
            return title.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        }

        try {
            ChatClient chatClient = chatClientProvider.getIfAvailable();
            if (chatClient == null) {
                return fallbackSlug();
            }

            String prompt = resolveSlugPrompt() + "\n\nTitle: " + title;
            String raw = chatClient.prompt().user(prompt).call().content();
            if (raw == null || raw.isBlank()) {
                return fallbackSlug();
            }

            String slug = raw.trim().toLowerCase().replaceAll("[^a-z0-9\\-]", "")
                    .replaceAll("-+", "-").replaceAll("^-|-$", "");
            if (slug.isBlank() || slug.length() < 2) {
                return fallbackSlug();
            }

            return ensureUniqueSlug(slug);
        } catch (Exception e) {
            return fallbackSlug();
        }
    }

    private String resolveSlugPrompt() {
        return promptTemplateLoader.resolve(
                aiProperties.getBlog().getSlugPrompt(),
                "prompts/ai/blog/slug-generation.md");
    }

    private String fallbackSlug() {
        return "post-" + System.currentTimeMillis() % 100000;
    }

    private String ensureUniqueSlug(String slug) {
        String candidate = slug;
        int suffix = 2;
        while (blogPostMapper.selectCount(
                new LambdaQueryWrapper<BlogPost>().eq(BlogPost::getSlug, candidate)) > 0) {
            candidate = slug + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
