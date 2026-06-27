import { http } from "@/utils/http";

type ApiResult<T> = {
  code: number;
  message: string;
  data: T;
};

type UploadResult = {
  url: string;
  fileName: string;
};

/**
 * 上传文件（通用）
 *
 * 当前为占位实现，需要后端提供 `/files/upload` multipart 接口。
 * 如果后端使用 OSS 直传（upload-policy 模式），需要：
 * 1. 先调 /files/upload-policy 获取临时凭证
 * 2. 再直传 OSS
 *
 * TODO: 根据后端实际上传方案替换实现
 */
export const uploadFile = async (file: File): Promise<ApiResult<UploadResult>> => {
  const formData = new FormData();
  formData.append("file", file);

  return http.request<ApiResult<UploadResult>>("post", "/files/upload", {
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};
