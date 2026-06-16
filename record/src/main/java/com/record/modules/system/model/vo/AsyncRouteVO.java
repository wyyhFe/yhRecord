package com.record.modules.system.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * PureAdmin 动态路由格式。
 *
 * <p>后端返回的路由数据必须符合此格式，前端 addAsyncRoutes() 才能正确解析。</p>
 *
 * <p>字段说明：</p>
 * <ul>
 *   <li>path: 路由路径</li>
 *   <li>name: 路由名称（必须唯一）</li>
 *   <li>component: 前端组件路径（对应 src/views 下的路径，如 /business/diary/index）</li>
 *   <li>redirect: 重定向</li>
 *   <li>meta: 路由元信息</li>
 *   <li>children: 子路由</li>
 * </ul>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsyncRouteVO {

    /** 路由路径。 */
    private String path;

    /** 路由名称（唯一标识）。 */
    private String name;

    /** 前端组件路径（如 /business/diary/index，对应 src/views/business/diary/index.vue）。 */
    private String component;

    /** 重定向路径。 */
    private String redirect;

    /** 路由元信息。 */
    private MetaVO meta;

    /** 子路由。 */
    private List<AsyncRouteVO> children;

    /**
     * 路由元信息。
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetaVO {
        /** 菜单标题。 */
        private String title;

        /** 菜单图标。 */
        private String icon;

        /** 排序值。 */
        private Integer rank;

        /** 是否显示在菜单中。 */
        private Boolean showLink;

        /** 是否显示父级菜单。 */
        private Boolean showParent;

        /** 是否缓存。 */
        private Boolean keepAlive;

        /** 内嵌 iframe 地址。 */
        private String frameSrc;

        /** iframe 页面是否开启首次加载动画。 */
        private Boolean frameLoading;

        /** 按钮权限标识列表。 */
        private List<String> auths;

        /** 角色列表（用于前端权限过滤）。 */
        private List<String> roles;

        /** 当前菜单名称禁止添加到标签页。 */
        private Boolean hiddenTag;

        /** 将某个菜单激活（用于 query 或 params 传参路由）。 */
        private String activePath;
    }
}
