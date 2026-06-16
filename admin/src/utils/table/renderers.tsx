import dayjs from "dayjs";
import { h } from "vue";
import { ElTag, ElImage } from "element-plus";

/** 心情映射表 */
export const moodMap = {
  HAPPY: { text: "开心", type: "success" as const },
  SAD: { text: "难过", type: "info" as const },
  ANGRY: { text: "生气", type: "danger" as const },
  CALM: { text: "平静", type: "" as const },
  EXCITED: { text: "兴奋", type: "warning" as const }
};

/** 格式化日期时间 */
export function formatDateTime(val: string, format = "YYYY-MM-DD HH:mm:ss") {
  return val ? dayjs(val).format(format) : "";
}

/** 格式化日期 */
export function formatDate(val: string) {
  return val ? dayjs(val).format("YYYY-MM-DD") : "-";
}

/** 渲染心情标签 */
export function renderMoodTag(mood: string) {
  const m = moodMap[mood] || { text: mood || "-", type: "" };
  return <el-tag type={m.type}>{m.text}</el-tag>;
}

/** 渲染标签列表 */
export function renderTags(tags: Array<{ id?: string; name?: string } | string>) {
  if (!tags?.length) return "-";
  return (
    <div class="flex flex-wrap gap-1">
      {tags.map(tag => (
        <el-tag size="small" key={typeof tag === "string" ? tag : tag.id}>
          {typeof tag === "string" ? tag : tag.name}
        </el-tag>
      ))}
    </div>
  );
}

/** 渲染图片预览 */
export function renderImagePreview(urls: string[], size = 40) {
  if (!urls?.length) return "-";
  return (
    <el-image
      src={urls[0]}
      preview-teleported
      preview-src-list={urls}
      fit="cover"
      style={{ width: `${size}px`, height: `${size}px`, borderRadius: "4px" }}
    />
  );
}

/** 渲染带数字的标签 */
export function renderCountTag(count: number, suffix = "次") {
  return <el-tag type="success">{count}{suffix}</el-tag>;
}

/** 通用表格列工厂 */
export function createColumns(config: {
  /** 是否显示选择框 */
  selectable?: boolean;
  /** 是否显示ID列 */
  showId?: boolean;
  /** 额外的列 */
  extra?: any[];
  /** 操作列 */
  operation?: { width?: number };
}) {
  const cols: any[] = [];

  if (config.selectable) {
    cols.push({ type: "selection", width: 55, align: "center" });
  }

  if (config.showId) {
    cols.push({ label: "ID", prop: "id", width: 80 });
  }

  if (config.extra) {
    cols.push(...config.extra);
  }

  if (config.operation) {
    cols.push({
      label: "操作",
      fixed: "right",
      width: config.operation.width || 120,
      slot: "operation"
    });
  }

  return cols;
}
