<template>
  <view class="flex flex-wrap gap-[14rpx]">
    <view
      v-for="item in items"
      :key="item.value"
      class="rounded-full px-[20rpx] py-[12rpx] text-[24rpx]"
      :class="isSelected(item.value) ? 'bg-[#c47c52] text-white' : 'glass-panel text-[#7d6c5b]'"
      @tap="toggle(item.value)"
    >
      {{ item.label }}
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
    if (index >= 0) current.splice(index, 1)
    else current.push(value)
    emit('update:modelValue', current)
    return
  }
  emit('update:modelValue', props.modelValue === value ? undefined : value)
}
</script>
