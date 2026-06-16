import { defineComponent, h, ref, computed, type PropType } from "vue";
import { ElImage, ElImageViewer } from "element-plus";

/** 图片预览组件 */
export default defineComponent({
  name: "ImagePreview",
  props: {
    /** 图片地址 */
    src: {
      type: String,
      required: true,
      default: ""
    },
    /** 图片宽度 */
    width: {
      type: [String, Number],
      default: "100px"
    },
    /** 图片高度 */
    height: {
      type: [String, Number],
      default: "100px"
    },
    /** 图片预览列表 */
    previewSrcList: {
      type: Array as PropType<string[]>,
      default: () => []
    },
    /** 是否懒加载 */
    lazy: {
      type: Boolean,
      default: false
    },
    /** 图片填充模式 */
    fit: {
      type: String as PropType<
        | "fill"
        | "contain"
        | "cover"
        | "none"
        | "scale-down"
      >,
      default: "cover"
    },
    /** 圆角 */
    radius: {
      type: [String, Number],
      default: "4px"
    }
  },
  setup(props) {
    const showViewer = ref(false);

    const previewList = computed(() => {
      if (props.previewSrcList?.length > 0) {
        return props.previewSrcList;
      }
      return props.src ? [props.src] : [];
    });

    const handleError = () => {
      console.warn("图片加载失败:", props.src);
    };

    return () =>
      h(
        "div",
        {
          class: "image-preview",
          style: {
            display: "inline-block",
            width: typeof props.width === "number" ? `${props.width}px` : props.width,
            height: typeof props.height === "number" ? `${props.height}px` : props.height
          }
        },
        [
          h(ElImage, {
            src: props.src,
            fit: props.fit,
            lazy: props.lazy,
            previewSrcList: previewList.value,
            previewTeleported: true,
            style: {
              width: "100%",
              height: "100%",
              borderRadius: typeof props.radius === "number" ? `${props.radius}px` : props.radius
            },
            onError: handleError
          })
        ]
      );
  }
});
