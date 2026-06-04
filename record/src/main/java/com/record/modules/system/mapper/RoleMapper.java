package com.record.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.record.modules.system.model.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper。
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
