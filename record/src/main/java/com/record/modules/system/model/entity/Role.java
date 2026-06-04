package com.record.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.CommonStatus;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class Role extends BaseEntity {

    @TableId
    private Long id;

    /** 角色名称（如 admin、editor）。 */
    private String name;

    /** 角色显示名（如 管理员、编辑者）。 */
    private String label;

    /** 状态。 */
    private CommonStatus status;

    /** 备注。 */
    private String remark;
}
