import { defineComponent, h, ref, watch, computed, type PropType } from "vue";
import { ElTreeSelect } from "element-plus";

/** 树形选择组件 */
export default defineComponent({
  name: "TreeSelect",
  props: {
    /** 选中值 */
    modelValue: {
      type: [String, Number, Array] as PropType<
        string | number | Array<string | number>
      >,
      default: ""
    },
    /** 数据源 */
    data: {
      type: Array as PropType<any[]>,
      required: true,
      default: () => []
    },
    /** 配置选项 */
    props: {
      type: Object as PropType<{
        label?: string;
        value?: string;
        children?: string;
        disabled?: string;
      }>,
      default: () => ({
        label: "label",
        value: "value",
        children: "children",
        disabled: "disabled"
      })
    },
    /** 占位符 */
    placeholder: {
      type: String,
      default: "请选择"
    },
    /** 是否可清空 */
    clearable: {
      type: Boolean,
      default: true
    },
    /** 是否多选 */
    multiple: {
      type: Boolean,
      default: false
    },
    /** 是否禁用 */
    disabled: {
      type: Boolean,
      default: false
    },
    /** 是否可搜索 */
    filterable: {
      type: Boolean,
      default: true
    },
    /** 搜索过滤方法 */
    filterNodeMethod: {
      type: Function as PropType<(value: string, data: any) => boolean>,
      default: undefined
    }
  },
  emits: ["update:modelValue", "change"],
  setup(props, { emit }) {
    const treeRef = ref();

    const handleChange = (val: any) => {
      emit("update:modelValue", val);
      emit("change", val);
    };

    return () =>
      h(ElTreeSelect, {
        ref: treeRef,
        modelValue: props.modelValue,
        data: props.data,
        props: props.props,
        placeholder: props.placeholder,
        clearable: props.clearable,
        multiple: props.multiple,
        disabled: props.disabled,
        filterable: props.filterable,
        filterNodeMethod: props.filterNodeMethod,
        onChange: handleChange,
        style: { width: "100%" }
      });
  }
});
