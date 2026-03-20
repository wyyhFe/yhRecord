<template>
  <AppPage>
    <AppHero
      eyebrow="个人中心"
      :title="profile?.nickname || '还没填写昵称'"
      :description="profile?.signature || '把生活留下来，时间会帮你整理它。'"
      badge="Profile"
    />

    <SectionBlock title="我的记录" subtitle="把当前账号的核心信息放在一个页面里">
      <MetricGrid :items="metrics" />
    </SectionBlock>

    <SectionBlock title="常用入口" subtitle="先把提醒配置和内容管理接起来">
      <SettingList :items="settings" @select="handleSelect" />
    </SectionBlock>
  </AppPage>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/AppHero.vue'
import SectionBlock from '@/components/business/SectionBlock.vue'
import MetricGrid from '@/components/business/MetricGrid.vue'
import SettingList, { type SettingItem } from '@/components/business/SettingList.vue'
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
 * 设置入口统一放在列表里，后续补页面时只需要扩展 key 分发。
 */
const settings = computed<SettingItem[]>(() => [
  { key: 'recycle', title: '回收站', description: '删除后的内容保留 15 天，可恢复或彻底删除。', value: '进入' },
  { key: 'reminder', title: '提醒设置', description: '支持小程序订阅消息提醒，公众号模板消息作为扩展方案保留。', value: '配置' },
  { key: 'tag', title: '标签管理', description: '先用后台模板，再逐步加上个人自定义标签管理。', value: '管理' }
])

/**
 * 当前只有提醒页已落地，其他入口先给出明确占位反馈。
 */
function handleSelect(item: SettingItem) {
  if (item.key === 'reminder') {
    uni.navigateTo({ url: '/pages/profile/reminder' })
    return
  }

  uni.showToast({
    title: item.key === 'recycle' ? '回收站页面待补充' : '标签管理页面待补充',
    icon: 'none'
  })
}

onMounted(() => {
  appStore.loadProfile().catch(() => undefined)
})
</script>
