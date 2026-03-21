<template>
  <AppPage>
    <AppHero
      eyebrow="标签管理"
      title="系统模板 + 我的标签"
      description="先使用后台提供的基类模板，再扩展成自己的个性标签。"
      badge="Tag"
    />

    <SectionBlock title="新建自定义标签" subtitle="如果模板里没有合适的，可以直接新增自己的标签">
      <view class="glass-panel px-[24rpx] py-[24rpx]">
        <view class="grid grid-cols-2 gap-[16rpx]">
          <BaseButton @tap="createCustomTag">新增日记标签</BaseButton>
          <BaseButton @tap="loadData">刷新列表</BaseButton>
        </view>
      </view>
    </SectionBlock>

    <SectionBlock title="系统模板" subtitle="点击即可基于模板创建自己的标签">
      <view v-if="templates.length" class="space-y-[16rpx]">
        <view v-for="item in templates" :key="item.id" class="glass-panel px-[24rpx] py-[24rpx]">
          <view class="flex items-center justify-between gap-[16rpx]">
            <view>
              <view class="text-[28rpx] font-semibold text-ink">{{ item.name }}</view>
              <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">模块：{{ item.moduleType }} {{ item.color ? `· 颜色：${item.color}` : '' }}</view>
            </view>
            <BaseButton @tap="useTemplate(item.id)">使用模板</BaseButton>
          </view>
        </view>
      </view>
      <EmptyState v-else icon="🏷️" title="暂无模板" description="后台还没有配置可用标签模板。" />
    </SectionBlock>

    <SectionBlock title="我的标签" subtitle="这里展示当前用户已经扩展或新建的标签">
      <view v-if="tags.length" class="space-y-[16rpx]">
        <view v-for="item in tags" :key="item.id" class="glass-panel px-[24rpx] py-[24rpx]">
          <view class="flex items-center justify-between gap-[16rpx]">
            <view>
              <view class="text-[28rpx] font-semibold text-ink">{{ item.name }}</view>
              <view class="mt-[8rpx] text-[22rpx] text-[#7f7366]">
                {{ item.templateId ? '来源：模板扩展' : '来源：自定义创建' }}
              </view>
            </view>
            <BaseButton @tap="removeTag(item.id)">删除</BaseButton>
          </view>
        </view>
      </view>
      <EmptyState v-else icon="🧷" title="还没有标签" description="先从模板创建，或者直接新增自己的标签。" />
    </SectionBlock>
  </AppPage>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppPage from '@/layouts/AppPage.vue'
import AppHero from '@/components/business/app-hero'
import SectionBlock from '@/components/business/section-block'
import EmptyState from '@/components/business/empty-state'
import BaseButton from '@/components/base/base-button'
import {
  createTag,
  createTagFromTemplate,
  deleteTag,
  fetchTagTemplates,
  fetchUserTags,
  type TagItem
} from '@/api/tag'

const templates = ref<TagItem[]>([])
const tags = ref<TagItem[]>([])

async function loadData() {
  const [templateList, tagList] = await Promise.all([
    fetchTagTemplates('DIARY'),
    fetchUserTags('DIARY')
  ])
  templates.value = templateList
  tags.value = tagList
}

async function useTemplate(templateId: number) {
  await createTagFromTemplate(templateId, 'DIARY')
  uni.showToast({ title: '已从模板创建', icon: 'success' })
  await loadData()
}

async function createCustomTag() {
  const result = await uni.showModal({
    title: '新增标签',
    content: '请输入标签名称',
    editable: true,
    placeholderText: '例如：旅行'
  })

  const name = result.content?.trim()
  if (!result.confirm || !name) return

  await createTag({
    name,
    moduleType: 'DIARY'
  })
  uni.showToast({ title: '标签已创建', icon: 'success' })
  await loadData()
}

async function removeTag(id: number) {
  const result = await uni.showModal({
    title: '确认删除',
    content: '删除后该标签不会再出现在你的标签列表中。'
  })
  if (!result.confirm) return

  await deleteTag(id)
  uni.showToast({ title: '已删除', icon: 'success' })
  await loadData()
}

onMounted(() => {
  loadData().catch(() => undefined)
})
</script>