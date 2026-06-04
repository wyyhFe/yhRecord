package com.record.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.record.modules.system.model.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联 Mapper。
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
