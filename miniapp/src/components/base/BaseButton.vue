<template>
  <button class="brand-button border-0 text-[28rpx]" :class="{ 'opacity-70': disabled }" :disabled="disabled">
    <slot />
  </button>
</template>

<script setup lang="ts">
defineProps<{
  disabled?: boolean
}>()
</script>
