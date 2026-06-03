import { http } from "@/utils/http";

/** 获取后端动态路由 */
export const getAsyncRoutes = (data?: Record<string, any>) => {
  return http.request<any>("get", "/system/menu/get-async-routes", { data });
};
