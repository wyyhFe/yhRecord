package com.record.modules.blog.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.record.modules.blog.model.entity.BlogTagRel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BlogTagRelMapper extends BaseMapper<BlogTagRel> {

    default List<BlogTagRel> selectByPostId(Long postId) {
        return selectList(new LambdaQueryWrapper<BlogTagRel>()
                .eq(BlogTagRel::getPostId, postId));
    }

    default void deleteByPostId(Long postId) {
        delete(new LambdaQueryWrapper<BlogTagRel>()
                .eq(BlogTagRel::getPostId, postId));
    }
}
