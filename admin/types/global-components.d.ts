import type { IconifyIcon } from "@iconify/types";
import type { iconType } from "../src/components/ReIcon/src/types";
import type { Component, DefineComponent, HTMLAttributes } from "vue";

type IconifyIconOfflineProps = {
  icon?: IconifyIcon | string | Component;
} & iconType &
  HTMLAttributes;

type IconifyIconOnlineProps = {
  icon?: string;
} & iconType &
  HTMLAttributes;

type FontIconProps = {
  icon?: string;
  /** `unicode` 引用模式 */
  uni?: boolean;
  /** `svg symbol` 引用模式 */
  svg?: boolean;
  iconType?: "uni" | "svg";
} & HTMLAttributes;

/** 权限控制组件公共 `Props` */
type AuthorityProps = {
  value?: string | string[];
};

/** 分页组件 Props */
type PaginationProps = {
  total: number;
  currentPage?: number;
  pageSize?: number;
  pageSizes?: number[];
  layout?: string;
  background?: boolean;
  small?: boolean;
  hideOnSinglePage?: boolean;
};

/** 字典标签组件 Props */
type DictTagProps = {
  options: Array<{
    label: string;
    value: string | number;
    elTagType?: string;
    elTagClass?: string;
  }>;
  value: string | number | Array<string | number>;
  multiple?: boolean;
};

/** 图片预览组件 Props */
type ImagePreviewProps = {
  src: string;
  width?: string | number;
  height?: string | number;
  previewSrcList?: string[];
  lazy?: boolean;
  fit?: "fill" | "contain" | "cover" | "none" | "scale-down";
  radius?: string | number;
};

/** 文件上传组件 Props */
type FileUploadProps = {
  modelValue?: string | string[];
  action?: string;
  accept?: string;
  fileSize?: number;
  limit?: number;
  multiple?: boolean;
  showTip?: boolean;
  tip?: string;
  headers?: Record<string, string>;
  data?: Record<string, any>;
};

/** 图片上传组件 Props */
type ImageUploadProps = {
  modelValue?: string | string[];
  action?: string;
  fileSize?: number;
  limit?: number;
  width?: string | number;
  height?: string | number;
  headers?: Record<string, string>;
  data?: Record<string, any>;
};

/** 内嵌框架组件 Props */
type IframeProps = {
  src: string;
  width?: string | number;
  height?: string | number;
  showBorder?: boolean;
  autoHeight?: boolean;
};

/** 树形选择组件 Props */
type TreeSelectProps = {
  modelValue?: string | number | Array<string | number>;
  data: any[];
  props?: {
    label?: string;
    value?: string;
    children?: string;
    disabled?: string;
  };
  placeholder?: string;
  clearable?: boolean;
  multiple?: boolean;
  disabled?: boolean;
  filterable?: boolean;
};

declare module "vue" {
  /**
   * 自定义全局组件获得 `Volar` 提示
   */
  export interface GlobalComponents {
    IconifyIconOffline: DefineComponent<IconifyIconOfflineProps>;
    IconifyIconOnline: DefineComponent<IconifyIconOnlineProps>;
    FontIcon: DefineComponent<FontIconProps>;
    Auth: DefineComponent<AuthorityProps>;
    Perms: DefineComponent<AuthorityProps>;
    /** 分页组件 */
    Pagination: DefineComponent<PaginationProps>;
    /** 字典标签组件 */
    DictTag: DefineComponent<DictTagProps>;
    /** 图片预览组件 */
    ImagePreview: DefineComponent<ImagePreviewProps>;
    /** 文件上传组件 */
    FileUpload: DefineComponent<FileUploadProps>;
    /** 图片上传组件 */
    ImageUpload: DefineComponent<ImageUploadProps>;
    /** 内嵌框架组件 */
    IframeComponent: DefineComponent<IframeProps>;
    /** 树形选择组件 */
    TreeSelect: DefineComponent<TreeSelectProps>;
  }
}

export {};
