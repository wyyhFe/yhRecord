import { defineComponent, h, type PropType } from "vue";
import { ElTag } from "element-plus";

/** 字典标签组件 */
export default defineComponent({
  name: "DictTag",
  props: {
    /** 字典数据数组 */
    options: {
      type: Array as PropType<
        Array<{
          label: string;
          value: string | number;
          elTagType?: string;
          elTagClass?: string;
        }>
      >,
      required: true,
      default: () => []
    },
    /** 当前值 */
    value: {
      type: [String, Number, Array] as PropType<
        string | number | Array<string | number>
      >,
      default: ""
    },
    /** 是否多选 */
    multiple: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const getOption = (val: string | number) => {
      return props.options.find(item => item.value === val);
    };

    const getLabel = (val: string | number) => {
      const option = getOption(val);
      return option ? option.label : val;
    };

    const getTagType = (val: string | number) => {
      const option = getOption(val);
      return option?.elTagType || "";
    };

    const getTagClass = (val: string | number) => {
      const option = getOption(val);
      return option?.elTagClass || "";
    };

    return () => {
      // 多选情况
      if (props.multiple && Array.isArray(props.value)) {
        return h(
          "div",
          { class: "dict-tag-multiple" },
          props.value.map(val =>
            h(
              ElTag,
              {
                type: getTagType(val) as any,
                class: getTagClass(val),
                style: { marginRight: "4px", marginBottom: "4px" }
              },
              { default: () => getLabel(val) }
            )
          )
        );
      }

      // 单选情况
      return h(
        ElTag,
        {
          type: getTagType(props.value as string | number) as any,
          class: getTagClass(props.value as string | number)
        },
        { default: () => getLabel(props.value as string | number) }
      );
    };
  }
});
