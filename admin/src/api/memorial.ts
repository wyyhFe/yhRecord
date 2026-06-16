import { http } from "@/utils/http";

type ApiResult<T> = {
  code: number;
  message: string;
  data: T;
  timestamp: string;
};

type PageResult<T> = ApiResult<{
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}>;

type MemorialVO = {
  id: number;
  title: string;
  type: string;
  memorialDate: string;
  annualRepeat: boolean;
  remark?: string;
  remindAt?: string;
  createdAt: string;
  updatedAt: string;
};

/** 获取纪念日列表 */
export const getMemorialList = (params?: object) => {
  return http.request<PageResult<MemorialVO>>("get", "/memorial/list", {
    params
  });
};

/** 删除纪念日 */
export const deleteMemorial = (id: number) => {
  return http.request<ApiResult<void>>("delete", `/memorial/${id}`);
};
