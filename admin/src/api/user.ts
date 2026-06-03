import { http } from "@/utils/http";

/** 后端统一响应结构 */
type ApiResult<T> = {
  code: number;
  message: string;
  data: T;
  timestamp: string;
};

/** 登录响应 */
export type UserResult = ApiResult<{
  userId: number;
  openid: string;
  accessToken: string;
  refreshToken: string;
  sessionId: string;
}>;

/** 刷新 token 响应 */
export type RefreshTokenResult = ApiResult<{
  accessToken: string;
  refreshToken: string;
}>;

/** 当前用户信息 */
export type UserInfo = {
  /** 头像 */
  avatar: string;
  /** 用户名 */
  username: string;
  /** 昵称 */
  nickname: string;
};

export type UserInfoResult = ApiResult<UserInfo>;

/** 管理员登录（后续对接后端 admin 登录接口） */
export const getLogin = (data?: object) => {
  return http.request<UserResult>("post", "/auth/login", { data });
};

/** 微信登录（小程序端使用，后台管理暂不使用） */
export const getWxLogin = (data?: object) => {
  return http.request<UserResult>("post", "/auth/wx-login", { data });
};

/** 刷新 token */
export const refreshTokenApi = (data?: object) => {
  return http.request<RefreshTokenResult>("post", "/auth/refresh", { data });
};

/** 获取当前用户信息 */
export const getMine = (data?: object) => {
  return http.request<UserInfoResult>("get", "/auth/me", { data });
};
