package com.record.modules.blog.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.record.modules.blog.model.entity.BlogView;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface BlogViewMapper extends BaseMapper<BlogView> {

    /**
     * 同一用户（或同一 IP）在同一天内对同一文章只算一次浏览。
     */
    default boolean existsByPostAndUserIp(Long postId, Long userId, String viewerIp) {
        // 只看最近 24 小时
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        LambdaQueryWrapper<BlogView> wrapper = new LambdaQueryWrapper<BlogView>()
                .eq(BlogView::getPostId, postId)
                .ge(BlogView::getViewedAt, since);
        if (userId != null) {
            wrapper.eq(BlogView::getUserId, userId);
        } else if (viewerIp != null) {
            wrapper.eq(BlogView::getViewerIp, viewerIp);
        } else {
            return false; // 无法去重，不算新浏览
        }
        return selectCount(wrapper) > 0;
    }
}
