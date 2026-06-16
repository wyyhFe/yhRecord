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

type LedgerEntryVO = {
  id: number;
  bookId?: number;
  type: "INCOME" | "EXPENSE";
  amount: number;
  entryDate: string;
  remark?: string;
  imagePath?: string;
  tagIds?: number[];
  tags?: Array<{ id: number; name: string; color?: string }>;
  createdAt: string;
  updatedAt: string;
};

type LedgerBookVO = {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
};

/** 获取记账账本列表 */
export const getLedgerBookList = (params?: object) => {
  return http.request<PageResult<LedgerBookVO>>("get", "/ledger/books", {
    params
  });
};

/** 删除记账账本 */
export const deleteLedgerBook = (id: number) => {
  return http.request<ApiResult<void>>("delete", `/ledger/books/${id}`);
};

/** 获取记账流水列表 */
export const getLedgerEntryList = (params?: object) => {
  return http.request<PageResult<LedgerEntryVO>>("get", "/ledger/entries", {
    params
  });
};

/** 删除记账流水 */
export const deleteLedgerEntry = (id: number) => {
  return http.request<ApiResult<void>>("delete", `/ledger/entries/${id}`);
};
