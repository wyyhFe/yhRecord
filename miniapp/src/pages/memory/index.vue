<template>
  <AppPage>
    <AppHero
      eyebrow="回忆"
      title="去年今日，不只是翻旧账"
      description="把同一天的日记、打卡和纪念日并排展示，回忆会更具体。"
      badge="Memory"
    />

    <SectionBlock title="去年今天" subtitle="同一天发生过什么，会重新连成线">
      <DiaryCard
        title="春天第一次去植物园"
        content="那天太阳很好，拍了很多树影，回去路上还买了面包。"
        mood="开心"
        record-date="2025-03-20"
        footer="2 个打卡 · 1 个纪念日"
      />
    </SectionBlock>

    <SectionBlock title="回忆提示" subtitle="这里以后会接后端真实的去年今日接口">
      <EmptyState
        icon="📕"
        title="回忆页已预留联调位置"
        description="等后端去年今日接口联通后，这里会展示同一天的日记、打卡和纪念日。"
      />
    </SectionBlock>
  </AppPage>
</template>

<script setup lang="ts">
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/app-hero'
import SectionBlock from '@/components/business/section-block'
import DiaryCard from '@/components/business/diary-card'
import EmptyState from '@/components/business/empty-state'
</script>