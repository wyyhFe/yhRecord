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
  /** accessToken 过期时间（秒），用于前端主动续签判断 */
  expiresIn: number;
  /** 是否需要强制修改密码（初始密码登录时返回 true） */
  requirePasswordChange?: boolean;
}>;

/** 刷新 token 响应 */
export type RefreshTokenResult = ApiResult<{
  accessToken: string;
  refreshToken: string;
  /** accessToken 过期时间（秒） */
  expiresIn: number;
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

/** 管理后台注册 */
export const registerAccount = (data?: object) => {
  return http.request<UserResult>("post", "/auth/register", { data });
};

/** 微信登录（小程序端使用，后台管理暂不使用） */
export const getWxLogin = (data?: object) => {
  return http.request<UserResult>("post", "/auth/wx-login", { data });
};

/** 手机号 + 密码登录管理后台 */
export const phoneLogin = (data?: object) => {
  return http.request<UserResult>("post", "/auth/phone-login", { data });
};

/** 修改密码（需已登录） */
export const changePassword = (data?: object) => {
  return http.request<void>("put", "/auth/change-password", { data });
};

/** 获取二维码扫码登录 token */
export const getQrToken = () => {
  return http.request<{ qrToken: string; expireSeconds: number }>("get", "/auth/qr-token");
};

/** 轮询二维码扫码状态 */
export const getQrStatus = (qrToken: string) => {
  return http.request<UserResult["data"]>("get", "/auth/qr-status", { params: { qrToken } });
};

/** 刷新 token */
export const refreshTokenApi = (data?: object) => {
  return http.request<RefreshTokenResult>("post", "/auth/refresh", { data });
};

/** 获取当前用户信息 */
export const getMine = (data?: object) => {
  return http.request<UserInfoResult>("get", "/auth/me", { data });
};

/** 用户资料 */
export type UserProfileVO = {
  id: number;
  openid: string;
  nickname: string;
  avatarPath: string;
  gender: string;
  officialAccountOpenid: string;
  birthday: string;
  signature: string;
  diaryCount: number;
};

type UserProfileResult = ApiResult<UserProfileVO>;

/** 获取当前用户资料 */
export const getUserProfile = () => {
  return http.request<UserProfileResult>("get", "/users/profile");
};

/** 更新当前用户资料 */
export const updateUserProfile = (data?: object) => {
  return http.request<UserProfileResult>("put", "/users/profile/update", {
    data
  });
};
