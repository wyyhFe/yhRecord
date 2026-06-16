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

type CheckinTaskVO = {
  id: number;
  name: string;
  description?: string;
  startDate?: string;
  totalCount: number;
  latestCheckedAt?: string;
  createdAt: string;
  updatedAt: string;
};

type CheckinRecordVO = {
  id: number;
  taskId: number;
  taskName: string;
  checkinDate: string;
  remark?: string;
  mood?: string;
  mediaPaths?: string[];
  tagNames?: string[];
  createdAt: string;
};

type CheckinTagVO = {
  id: number;
  name: string;
  icon?: string;
  isSystem: boolean;
};

/** 获取打卡任务列表 */
export const getCheckinTaskList = (params?: object) => {
  return http.request<PageResult<CheckinTaskVO>>(
    "get",
    "/checkin/tasks/list",
    { params }
  );
};

/** 获取打卡记录列表 */
export const getCheckinRecordList = (params?: object) => {
  return http.request<PageResult<CheckinRecordVO>>(
    "get",
    "/checkin/records/list",
    { params }
  );
};

/** 删除打卡任务 */
export const deleteCheckinTask = (id: number) => {
  return http.request<ApiResult<void>>("delete", `/checkin/tasks/delete/${id}`);
};

/** 删除打卡记录 */
export const deleteCheckinRecord = (id: number) => {
  return http.request<ApiResult<void>>("delete", `/checkin/records/${id}`);
};

/** 获取打卡标签列表 */
export const getCheckinTagList = () => {
  return http.request<ApiResult<CheckinTagVO[]>>("get", "/checkin/tags");
};

/** 创建打卡标签 */
export const createCheckinTag = (data: { name: string; icon?: string }) => {
  return http.request<ApiResult<CheckinTagVO>>("post", "/checkin/tags", {
    data
  });
};

/** 删除打卡标签 */
export const deleteCheckinTag = (id: number) => {
  return http.request<ApiResult<void>>("delete", `/checkin/tags/${id}`);
};
