package com.record.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-角色关联实体。
 */
@Data
@TableName("sys_user_role")
public class UserRole {

    @TableId
    private Long id;

    private Long userId;

    private Long roleId;
}
