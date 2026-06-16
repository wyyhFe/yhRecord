import { defineComponent, h, ref, computed, type PropType } from "vue";
import { ElUpload, ElButton, ElIcon, ElMessage } from "element-plus";
import { UploadFilled, Delete, Download } from "@element-plus/icons-vue";

/** 文件上传组件 */
export default defineComponent({
  name: "FileUpload",
  props: {
    /** 文件地址 */
    modelValue: {
      type: [String, Array] as PropType<string | string[]>,
      default: ""
    },
    /** 上传地址 */
    action: {
      type: String,
      default: "/api/common/upload"
    },
    /** 文件类型限制 */
    accept: {
      type: String,
      default: ""
    },
    /** 文件大小限制(MB) */
    fileSize: {
      type: Number,
      default: 5
    },
    /** 最大上传数量 */
    limit: {
      type: Number,
      default: 5
    },
    /** 是否多选 */
    multiple: {
      type: Boolean,
      default: true
    },
    /** 是否显示提示 */
    showTip: {
      type: Boolean,
      default: true
    },
    /** 提示文字 */
    tip: {
      type: String,
      default: ""
    },
    /** 请求头 */
    headers: {
      type: Object as PropType<Record<string, string>>,
      default: () => ({})
    },
    /** 附加参数 */
    data: {
      type: Object as PropType<Record<string, any>>,
      default: () => ({})
    }
  },
  emits: ["update:modelValue", "success", "error"],
  setup(props, { emit }) {
    const fileList = ref<any[]>([]);

    /** 解析文件列表 */
    const parseFileList = () => {
      if (!props.modelValue) {
        fileList.value = [];
        return;
      }
      const urls = Array.isArray(props.modelValue)
        ? props.modelValue
        : props.modelValue.split(",").filter(Boolean);
      fileList.value = urls.map((url, index) => ({
        name: url.split("/").pop() || `文件${index + 1}`,
        url
      }));
    };

    parseFileList();

    /** 上传前校验 */
    const beforeUpload = (file: File) => {
      const isAccept = props.accept
        ? props.accept.split(",").some(type => file.type.includes(type.trim()))
        : true;
      if (!isAccept) {
        ElMessage.error(`文件类型不正确，请上传 ${props.accept} 格式文件`);
        return false;
      }
      const isLimit = file.size / 1024 / 1024 < props.fileSize;
      if (!isLimit) {
        ElMessage.error(`文件大小不能超过 ${props.fileSize}MB`);
        return false;
      }
      return true;
    };

    /** 上传成功 */
    const handleSuccess = (response: any, file: any, files: any[]) => {
      if (response.code === 0) {
        const url = response.data?.url || response.url;
        emit("success", url, file);
        updateModelValue(files);
      } else {
        ElMessage.error(response.message || "上传失败");
        emit("error", response);
      }
    };

    /** 删除文件 */
    const handleRemove = (file: any) => {
      const index = fileList.value.findIndex(item => item.url === file.url);
      if (index > -1) {
        fileList.value.splice(index, 1);
        updateModelValue(fileList.value);
      }
    };

    /** 下载文件 */
    const handleDownload = (file: any) => {
      if (file.url) {
        window.open(file.url, "_blank");
      }
    };

    /** 更新modelValue */
    const updateModelValue = (files: any[]) => {
      const urls = files
        .map(file => file.response?.data?.url || file.url)
        .filter(Boolean);
      const value = Array.isArray(props.modelValue) ? urls : urls.join(",");
      emit("update:modelValue", value);
    };

    /** 文件超限提示 */
    const handleExceed = () => {
      ElMessage.warning(`最多只能上传 ${props.limit} 个文件`);
    };

    return () => {
      const tipText = props.tip || `支持 ${props.accept || "任意"} 格式，单个文件不超过 ${props.fileSize}MB`;

      return h("div", { class: "file-upload" }, [
        h(
          ElUpload,
          {
            action: props.action,
            headers: props.headers,
            data: props.data,
            fileList: fileList.value,
            accept: props.accept,
            multiple: props.multiple,
            limit: props.limit,
            beforeUpload,
            onSuccess: handleSuccess,
            onRemove: handleRemove,
            onExceed: handleExceed,
            showFileList: true
          },
          {
            default: () =>
              h(
                ElButton,
                { type: "primary", icon: UploadFilled },
                { default: () => "选择文件" }
              ),
            tip: () =>
              props.showTip
                ? h(
                    "div",
                    { class: "el-upload__tip", style: { color: "#999" } },
                    { default: () => tipText }
                  )
                : null
          }
        )
      ]);
    };
  }
});
