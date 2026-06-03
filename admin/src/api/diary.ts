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

type DiaryVO = {
  id: number;
  recordDate: string;
  content: string;
  mood: string;
  tags: string[];
  imageUrls: string[];
  createdAt: string;
  updatedAt: string;
};

/** 获取日记列表 */
export const getDiaryList = (params?: object) => {
  return http.request<PageResult<DiaryVO>>("get", "/diary/list", { params });
};

/** 获取日记详情 */
export const getDiaryDetail = (id: number) => {
  return http.request<ApiResult<DiaryVO>>("get", `/diary/${id}`);
};

/** 删除日记 */
export const deleteDiary = (id: number) => {
  return http.request<ApiResult<void>>("delete", `/diary/${id}`);
};
