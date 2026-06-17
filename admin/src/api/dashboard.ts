import { http } from "@/utils/http";

type ApiResult<T> = {
  code: number;
  message: string;
  data: T;
  timestamp: string;
};

export type DashboardStatsVO = {
  userCount: number;
  diaryCount: number;
  checkinCount: number;
  ledgerCount: number;
  memorialCount: number;
  knowledgeCount: number;
};

/** 获取仪表盘统计数据 */
export const getDashboardStats = () => {
  return http.request<ApiResult<DashboardStatsVO>>("get", "/dashboard/stats");
};
