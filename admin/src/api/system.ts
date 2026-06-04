import { http } from "@/utils/http";

// ==================== 菜单管理 ====================

/** 获取菜单列表（一维数组，含 parentId，前端自行构建树） */
export const getMenuList = () => {
  return http.request<any>("get", "/system/menu/list");
};

/** 新增或更新菜单 */
export const saveMenu = (data?: object) => {
  return http.request<any>("post", "/system/menu/save", { data });
};

/** 删除菜单 */
export const deleteMenu = (id: number) => {
  return http.request<any>("delete", `/system/menu/${id}`);
};

// ==================== 角色管理 ====================

/** 获取角色列表 */
export const getRoleList = (data?: object) => {
  return http.request<any>("get", "/system/role/list", { params: data });
};

/** 新增或更新角色 */
export const saveRole = (data?: object) => {
  return http.request<any>("post", "/system/role/save", { data });
};

/** 删除角色 */
export const deleteRole = (id: number) => {
  return http.request<any>("delete", `/system/role/${id}`);
};

/** 获取角色已分配的菜单 ID 列表 */
export const getRoleMenuIds = (data?: object) => {
  return http.request<any>("get", "/system/role/menu-ids", { params: data });
};

/** 分配角色菜单权限 */
export const assignRoleMenus = (roleId: number, menuIds: number[]) => {
  return http.request<any>("post", `/system/role/assign-menus?roleId=${roleId}`, {
    data: menuIds
  });
};

/** 获取所有角色（用户管理-分配角色用） */
export const getAllRoleList = () => {
  return http.request<any>("get", "/system/role/list");
};

// ==================== 用户管理 ====================

/** 获取用户列表（分页） */
export const getUserList = (data?: object) => {
  return http.request<any>("get", "/system/user/list", { params: data });
};

/** 获取用户已分配的角色 ID 列表 */
export const getRoleIds = (data?: object) => {
  return http.request<any>("get", "/system/user/role-ids", { params: data });
};

/** 分配用户角色 */
export const assignUserRoles = (userId: number, roleIds: number[]) => {
  return http.request<any>("post", `/system/user/${userId}/assign-roles`, {
    data: roleIds
  });
};

/** 修改用户状态 */
export const updateUserStatus = (userId: number, status: string) => {
  return http.request<any>("put", `/system/user/${userId}/status`, { data: { status } });
};

// ==================== 部门管理（暂不实现，保留占位） ====================

/** 获取部门列表 */
export const getDeptList = () => {
  // 暂无部门模块，返回空数组
  return Promise.resolve({ code: 0, data: [] });
};
