package com.record.modules.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.record.modules.blog.model.entity.BlogPost;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogPostMapper extends BaseMapper<BlogPost> {
}
