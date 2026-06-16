import { defineComponent, h, ref, computed, type PropType } from "vue";
import { ElUpload, ElIcon, ElMessage, ElImageViewer } from "element-plus";
import { Plus, ZoomIn, Delete } from "@element-plus/icons-vue";

/** 图片上传组件 */
export default defineComponent({
  name: "ImageUpload",
  props: {
    /** 图片地址 */
    modelValue: {
      type: [String, Array] as PropType<string | string[]>,
      default: ""
    },
    /** 上传地址 */
    action: {
      type: String,
      default: "/api/common/upload"
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
    /** 图片宽度 */
    width: {
      type: [String, Number],
      default: "120px"
    },
    /** 图片高度 */
    height: {
      type: [String, Number],
      default: "120px"
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
    const showViewer = ref(false);
    const previewIndex = ref(0);

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
        name: url.split("/").pop() || `图片${index + 1}`,
        url
      }));
    };

    parseFileList();

    /** 图片列表 */
    const imageUrls = computed(() => {
      return fileList.value
        .map(file => file.response?.data?.url || file.url)
        .filter(Boolean);
    });

    /** 上传前校验 */
    const beforeUpload = (file: File) => {
      const isImage = file.type.startsWith("image/");
      if (!isImage) {
        ElMessage.error("只能上传图片文件");
        return false;
      }
      const isLimit = file.size / 1024 / 1024 < props.fileSize;
      if (!isLimit) {
        ElMessage.error(`图片大小不能超过 ${props.fileSize}MB`);
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

    /** 删除图片 */
    const handleRemove = (file: any) => {
      const index = fileList.value.findIndex(item => item.url === file.url);
      if (index > -1) {
        fileList.value.splice(index, 1);
        updateModelValue(fileList.value);
      }
    };

    /** 预览图片 */
    const handlePreview = (file: any) => {
      const url = file.response?.data?.url || file.url;
      previewIndex.value = imageUrls.value.indexOf(url);
      showViewer.value = true;
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
      ElMessage.warning(`最多只能上传 ${props.limit} 张图片`);
    };

    return () => {
      const w = typeof props.width === "number" ? `${props.width}px` : props.width;
      const h_val = typeof props.height === "number" ? `${props.height}px` : props.height;

      return h("div", { class: "image-upload" }, [
        h(
          ElUpload,
          {
            action: props.action,
            headers: props.headers,
            data: props.data,
            fileList: fileList.value,
            accept: "image/*",
            multiple: true,
            limit: props.limit,
            listType: "picture-card",
            beforeUpload,
            onSuccess: handleSuccess,
            onRemove: handleRemove,
            onPreview: handlePreview,
            onExceed: handleExceed
          },
          {
            default: () =>
              fileList.value.length >= props.limit
                ? null
                : h(ElIcon, { size: 20, color: "#8c939d" }, {
                    default: () => h(Plus)
                  })
          }
        ),
        showViewer.value
          ? h(ElImageViewer, {
              urlList: imageUrls.value,
              initialIndex: previewIndex.value,
              onClose: () => (showViewer.value = false),
              teleported: true
            })
          : null
      ]);
    };
  }
});
