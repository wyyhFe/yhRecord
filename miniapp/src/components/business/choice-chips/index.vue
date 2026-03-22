<template>
  <view class="choice-chip-group">
    <view
      v-for="item in items"
      :key="item.value"
      class="choice-chip-group__item"
      @tap="toggle(item.value)"
    >
      <u-tag
        :text="item.label"
        shape="circle"
        :plain="!isSelected(item.value)"
        :type="isSelected(item.value) ? 'warning' : 'info'"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  items: Array<{ label: string; value: string | number }>
  modelValue: Array<string | number> | string | number | undefined
  multiple?: boolean
}>(), {
  multiple: false
})

const emit = defineEmits<{
  'update:modelValue': [value: Array<string | number> | string | number | undefined]
}>()

function isSelected(value: string | number) {
  if (props.multiple) {
    return Array.isArray(props.modelValue) && props.modelValue.includes(value)
  }
  return props.modelValue === value
}

function toggle(value: string | number) {
  if (props.multiple) {
    const current = Array.isArray(props.modelValue) ? [...props.modelValue] : []
    const index = current.indexOf(value)
    if (index >= 0) {
      current.splice(index, 1)
    } else {
      current.push(value)
    }
    emit('update:modelValue', current)
    return
  }

  emit('update:modelValue', props.modelValue === value ? undefined : value)
}
</script>

<style scoped lang="scss">
.choice-chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.choice-chip-group__item {
  display: inline-flex;
}
</style>
