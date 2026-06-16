import { defineComponent, h, computed, type PropType } from "vue";
import { ElPagination } from "element-plus";

/** 分页组件 */
export default defineComponent({
  name: "Pagination",
  props: {
    /** 总条目数 */
    total: {
      type: Number,
      required: true,
      default: 0
    },
    /** 当前页码 */
    currentPage: {
      type: Number,
      default: 1
    },
    /** 每页显示条目个数 */
    pageSize: {
      type: Number,
      default: 10
    },
    /** 每页显示个数选择器的选项列表 */
    pageSizes: {
      type: Array as PropType<number[]>,
      default: () => [10, 20, 30, 50]
    },
    /** 组件布局 */
    layout: {
      type: String,
      default: "total, sizes, prev, pager, next, jumper"
    },
    /** 是否为分页按钮添加背景色 */
    background: {
      type: Boolean,
      default: true
    },
    /** 是否使用小型分页样式 */
    small: {
      type: Boolean,
      default: false
    },
    /** 是否隐藏只有一页时的分页 */
    hideOnSinglePage: {
      type: Boolean,
      default: false
    },
    /** 是否显示每页条目数选择器 */
    showSizeChanger: {
      type: Boolean,
      default: true
    },
    /** 是否显示跳转页输入框 */
    showJumper: {
      type: Boolean,
      default: true
    }
  },
  emits: ["update:currentPage", "update:pageSize", "currentChange", "sizeChange"],
  setup(props, { emit }) {
    const computedLayout = computed(() => {
      if (!props.showSizeChanger) {
        return props.layout.replace("sizes, ", "");
      }
      return props.layout;
    });

    const handleCurrentChange = (val: number) => {
      emit("update:currentPage", val);
      emit("currentChange", val);
    };

    const handleSizeChange = (val: number) => {
      emit("update:pageSize", val);
      emit("sizeChange", val);
    };

    return () =>
      h("div", { class: "pagination-container", style: { padding: "10px 0" } }, [
        h(ElPagination, {
          currentPage: props.currentPage,
          pageSize: props.pageSize,
          total: props.total,
          pageSizes: props.pageSizes,
          layout: computedLayout.value,
          background: props.background,
          small: props.small,
          hideOnSinglePage: props.hideOnSinglePage,
          onCurrentChange: handleCurrentChange,
          onSizeChange: handleSizeChange
        })
      ]);
  }
});
