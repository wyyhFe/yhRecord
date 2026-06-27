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

/** 博客文章 VO */
export type BlogPostVO = {
  id: number;
  userId: number;
  authorNickname: string;
  authorAvatar: string;
  title: string;
  slug: string;
  markdownContent: string;
  htmlContent: string;
  summary: string;
  category: string;
  tags: string[];
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  viewCount: number;
  commentCount: number;
  publishedAt: string;
  createdAt: string;
  updatedAt: string;
};

/** 创建/更新文章请求 */
export type BlogPostRequest = {
  title: string;
  markdownContent: string;
  htmlContent: string;
  summary: string;
  category: string;
  tags: string[];
  slug: string;
  status: string;
};

/** 获取文章列表（admin） */
export const getBlogList = (params?: object) => {
  return http.request<PageResult<BlogPostVO>>("get", "/blog/posts", { params });
};

/** 获取文章详情 */
export const getBlogDetail = (id: number) => {
  return http.request<ApiResult<BlogPostVO>>("get", `/blog/posts/${id}`);
};

/** 创建文章 */
export const createBlog = (data: BlogPostRequest) => {
  return http.request<ApiResult<BlogPostVO>>("post", "/blog/posts", { data });
};

/** 更新文章 */
export const updateBlog = (id: number, data: BlogPostRequest) => {
  return http.request<ApiResult<BlogPostVO>>("put", `/blog/posts/${id}`, { data });
};

/** 删除文章 */
export const deleteBlog = (id: number) => {
  return http.request<ApiResult<void>>("delete", `/blog/posts/${id}`);
};
