package com.record.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.CommonStatus;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("menu")
public class Menu extends BaseEntity {

    @TableId
    private Long id;

    /** 父菜单 ID（NULL 为顶级）。 */
    private Long parentId;

    /** 路由 name（唯一标识）。 */
    private String name;

    /** 路由 path。 */
    private String path;

    /** 前端组件路径。 */
    private String component;

    /** 重定向路径。 */
    private String redirect;

    /** 菜单显示标题。 */
    private String title;

    /** 菜单图标。 */
    private String icon;

    /** 排序值。 */
    private Integer rank;

    /** 类型：DIRECTORY / PAGE / BUTTON。 */
    private String menuType;

    /** 内嵌 iframe 地址。 */
    private String frameSrc;

    /** 是否在菜单中显示。 */
    private Boolean showLink;

    /** 是否缓存。 */
    private Boolean keepAlive;

    /** 按钮权限标识。 */
    private String auths;

    /** 状态。 */
    private CommonStatus status;
}
