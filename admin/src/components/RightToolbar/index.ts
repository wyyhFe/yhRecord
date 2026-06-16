import { defineComponent, h, type PropType } from "vue";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";

export default defineComponent({
  name: "RightToolbar",
  props: {
    /** 是否显示搜索 */
    showSearch: {
      type: Boolean,
      default: true
    },
    /** 是否显示刷新 */
    showRefresh: {
      type: Boolean,
      default: true
    },
    /** 是否显示列设置 */
    showColumns: {
      type: Boolean,
      default: false
    },
    /** 列设置数据 */
    columns: {
      type: Array as PropType<any[]>,
      default: () => []
    },
    /** 搜索事件 */
    onSearch: {
      type: Function as PropType<() => void>,
      default: () => {}
    },
    /** 刷新事件 */
    onRefresh: {
      type: Function as PropType<() => void>,
      default: () => {}
    },
    /** 列设置变更事件 */
    onColumnChange: {
      type: Function as PropType<(column: any) => void>,
      default: () => {}
    }
  },
  emits: ["update:showSearch", "search", "refresh", "columnChange"],
  setup(props, { emit }) {
    const toggleSearch = () => {
      emit("update:showSearch", !props.showSearch);
      emit("search");
    };

    const handleRefresh = () => {
      emit("refresh");
    };

    const handleColumnChange = (column: any) => {
      column.visible = !column.visible;
      emit("columnChange", column);
    };

    return () =>
      h("div", { class: "right-toolbar" }, [
        props.showSearch
          ? h(
              "el-tooltip",
              { content: "搜索", placement: "top" },
              {
                default: () =>
                  h("el-button", {
                    icon: useRenderIcon("ep/search"),
                    circle: true,
                    onClick: toggleSearch
                  })
              }
            )
          : null,
        props.showRefresh
          ? h(
              "el-tooltip",
              { content: "刷新", placement: "top" },
              {
                default: () =>
                  h("el-button", {
                    icon: useRenderIcon("ep/refresh"),
                    circle: true,
                    onClick: handleRefresh
                  })
              }
            )
          : null,
        props.showColumns
          ? h(
              "el-tooltip",
              { content: "列设置", placement: "top" },
              {
                default: () =>
                  h(
                    "el-dropdown",
                    {
                      trigger: "click",
                      onCommand: handleColumnChange
                    },
                    {
                      default: () =>
                        h("el-button", {
                          icon: useRenderIcon("ep/set-up"),
                          circle: true
                        }),
                      dropdown: () =>
                        h("el-dropdown-menu", null, {
                          default: () =>
                            props.columns.map(column =>
                              h(
                                "el-dropdown-item",
                                {
                                  command: column,
                                  class: { "is-active": column.visible }
                                },
                                {
                                  default: () => column.label
                                }
                              )
                            )
                        })
                    }
                  )
              }
            )
          : null
      ]);
  }
});
