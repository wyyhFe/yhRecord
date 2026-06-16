import { defineComponent, h, ref, onMounted, onUnmounted, type PropType } from "vue";

/** 内嵌框架组件 */
export default defineComponent({
  name: "Iframe",
  props: {
    /** 链接地址 */
    src: {
      type: String,
      required: true,
      default: ""
    },
    /** 宽度 */
    width: {
      type: [String, Number],
      default: "100%"
    },
    /** 高度 */
    height: {
      type: [String, Number],
      default: "100%"
    },
    /** 是否显示边框 */
    showBorder: {
      type: Boolean,
      default: false
    },
    /** 是否自适应高度 */
    autoHeight: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const iframeRef = ref<HTMLIFrameElement>();
    const loading = ref(true);

    const handleLoad = () => {
      loading.value = false;
    };

    const handleError = () => {
      loading.value = false;
      console.error("iframe 加载失败:", props.src);
    };

    return () => {
      const w = typeof props.width === "number" ? `${props.width}px` : props.width;
      const h_val = typeof props.height === "number" ? `${props.height}px` : props.height;

      return h("div", {
        class: "iframe-container",
        style: {
          width: w,
          height: h_val,
          position: "relative",
          overflow: "hidden",
          border: props.showBorder ? "1px solid var(--el-border-color)" : "none",
          borderRadius: "4px"
        }
      }, [
        loading.value
          ? h("div", {
              style: {
                position: "absolute",
                top: "50%",
                left: "50%",
                transform: "translate(-50%, -50%)"
              }
            }, [
              h("el-icon", { size: 24, class: "is-loading" }, [
                h("i", { class: "ep:loading" })
              ])
            ])
          : null,
        h("iframe", {
          ref: iframeRef,
          src: props.src,
          style: {
            width: "100%",
            height: "100%",
            border: "none",
            display: loading.value ? "none" : "block"
          },
          onLoad: handleLoad,
          onError: handleError
        })
      ]);
    };
  }
});
