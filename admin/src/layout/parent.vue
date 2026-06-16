<script setup lang="ts">
/**
 * 三级路由中间层布局组件
 * 用于目录类型路由，渲染子路由
 */
</script>

<template>
  <router-view>
    <template #default="{ Component, route }">
      <transition name="fade-transform" mode="out-in" appear>
        <keep-alive>
          <component :is="Component" :key="route.path" />
        </keep-alive>
      </transition>
    </template>
  </router-view>
</template>
