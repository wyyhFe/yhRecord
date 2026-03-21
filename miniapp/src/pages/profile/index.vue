<template>
  <AppPage>
    <AppHero
      eyebrow="个人中心"
      :title="profile?.nickname || '还没填写昵称'"
      :description="profile?.signature || '把生活记录下来，时间会帮你整理它。'"
      badge="Profile"
    />

    <SectionBlock title="我的记录" subtitle="把当前账号的核心信息放在一个页面里">
      <MetricGrid :items="metrics" />
    </SectionBlock>

    <SectionBlock title="常用入口" subtitle="回收站、提醒设置和标签管理都放在这里">
      <SettingList :items="settings" @select="handleSelect" />
    </SectionBlock>
  </AppPage>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/app-hero'
import SectionBlock from '@/components/business/section-block'
import MetricGrid from '@/components/business/metric-grid'
import SettingList, { type SettingItem } from '@/components/business/setting-list'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const profile = computed(() => appStore.profile)

/**
 * 个人中心概览数据直接复用全局 profile。
 */
const metrics = computed(() => [
  { label: '日记数', value: String(profile.value?.diaryCount ?? 0), hint: '累计记录内容' },
  { label: '生日', value: profile.value?.birthday || '--', hint: '用于年龄展示' },
  { label: '签名', value: profile.value?.signature ? '已设置' : '未设置', hint: '个人状态说明' }
])

/**
 * 设置入口统一放在列表里，便于后续继续扩展。
 */
const settings = computed<SettingItem[]>(() => [
  { key: 'recycle', title: '回收站', description: '删除后的日记会保留 15 天，可恢复或彻底删除。', value: '进入' },
  { key: 'reminder', title: '提醒设置', description: '统一管理小程序订阅消息和公众号模板消息提醒。', value: '配置' },
  { key: 'tag', title: '标签管理', description: '先用系统基类模板，再扩展自己的标签。', value: '管理' }
])

function handleSelect(item: SettingItem) {
  if (item.key === 'recycle') {
    uni.navigateTo({ url: '/pages/profile/recycle' })
    return
  }

  if (item.key === 'reminder') {
    uni.navigateTo({ url: '/pages/profile/reminder' })
    return
  }

  if (item.key === 'tag') {
    uni.navigateTo({ url: '/pages/profile/tags' })
  }
}

onMounted(() => {
  appStore.loadProfile().catch(() => undefined)
})
</script>
