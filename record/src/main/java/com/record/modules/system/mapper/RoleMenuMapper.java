package com.record.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.record.modules.system.model.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色-菜单关联 Mapper。
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /**
     * 根据角色 ID 列表查询关联的菜单 ID。
     */
    @Select("<script>" +
            "SELECT DISTINCT menu_id FROM role_menu WHERE role_id IN " +
            "<foreach item='id' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> selectMenuIdsByRoleIds(List<Long> roleIds);
}
