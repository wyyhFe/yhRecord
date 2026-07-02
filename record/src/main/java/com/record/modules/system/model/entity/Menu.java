package com.record.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.CommonStatus;
import com.record.common.model.BaseEntity;
import com.record.common.model.JsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 菜单实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_menu", autoResultMap = true)
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

    /** 路由元信息（JSON 格式）。 */
    @TableField(typeHandler = JsonTypeHandler.class)
    private Map<String, Object> meta;

    /** 类型：DIRECTORY / PAGE / BUTTON。 */
    private String menuType;

    /** 状态。 */
    private CommonStatus status;

    // ============ 便捷方法，从 meta 中获取常用字段 ============

    public String getTitle() {
        return meta != null ? (String) meta.get("title") : null;
    }

    public String getIcon() {
        return meta != null ? (String) meta.get("icon") : null;
    }

    public Integer getRank() {
        if (meta == null) return 0;
        Object value = meta.get("rank");
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    public Boolean getShowLink() {
        return meta != null ? (Boolean) meta.get("showLink") : true;
    }

    public Boolean getShowParent() {
        return meta != null ? (Boolean) meta.get("showParent") : false;
    }

    public Boolean getKeepAlive() {
        return meta != null ? (Boolean) meta.get("keepAlive") : false;
    }

    public String getFrameSrc() {
        return meta != null ? (String) meta.get("frameSrc") : null;
    }

    public String getAuths() {
        return meta != null ? (String) meta.get("auths") : null;
    }

    public Object getRoles() {
        return meta != null ? meta.get("roles") : null;
    }
}
