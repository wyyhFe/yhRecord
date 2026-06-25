<template>
  <view class="page-shell-safe tags-page">
    <!-- 顶栏 -->
    <view class="tags-header">
      <text class="tags-header__title">{{ pageTitle }}</text>
    </view>

    <!-- 记账标签类型切换 -->
    <view v-if="isLedger" class="tags-switch">
      <view
        v-for="opt in ledgerTypeOptions"
        :key="opt.value"
        class="tags-switch__item"
        :class="{ 'tags-switch__item--active': ledgerType === opt.value }"
        @click="ledgerType = opt.value"
      >
        <text class="tags-switch__text">{{ opt.label }}</text>
      </view>
    </view>

    <!-- 我的标签 -->
    <view class="tags-card">
      <view class="tags-card__header">
        <text class="tags-card__title">我的标签</text>
        <text class="tags-card__badge">{{ tags.length }}</text>
      </view>

      <view v-if="tags.length" class="tags-grid">
        <view v-for="item in tags" :key="item.id" class="tag-chip tag-chip--user">
          <text class="tag-chip__color" :style="{ background: item.color || 'var(--color-checkin)' }" />
          <text class="tag-chip__name">{{ item.name }}</text>
          <text class="tag-chip__delete" @click.stop="removeTag(item.id)">×</text>
        </view>
      </view>
      <view v-else class="tags-empty">
        <text class="tags-empty__text">还没有标签，从下方模板添加吧</text>
      </view>
    </view>

    <!-- 系统模板 -->
    <view class="tags-card">
      <view class="tags-card__header">
        <text class="tags-card__title">系统模板</text>
        <text class="tags-card__badge">{{ templates.length }}</text>
      </view>

      <view v-if="templates.length" class="tags-grid">
        <view
          v-for="item in templates"
          :key="item.id"
          class="tag-chip tag-chip--template"
          @click="useTemplate(item.id)"
        >
          <text class="tag-chip__color" :style="{ background: item.color || 'var(--color-text-muted)' }" />
          <text class="tag-chip__name">{{ item.name }}</text>
          <text class="tag-chip__add">+</text>
        </view>
      </view>
      <view v-else class="tags-empty">
        <text class="tags-empty__text">暂无可用模板</text>
      </view>
    </view>

    <!-- 新增标签 -->
    <view class="tags-action">
      <u-button
        shape="circle"
        type="primary"
        :color="isLedger ? 'var(--color-ledger-gradient)' : 'var(--color-diary-gradient)'"
        @click="createCustomTag"
      >
        ＋ 新增自定义标签
      </u-button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  createTag,
  createTagFromTemplate,
  deleteTag,
  fetchTagTemplates,
  fetchUserTags,
  type LedgerTagType,
  type TagItem,
  type TagModuleType
} from '@/api/tag'
import type { Id } from '@/types/domain'

const templates = ref<TagItem[]>([])
const tags = ref<TagItem[]>([])
const moduleType = ref<TagModuleType>('DIARY')
const ledgerType = ref<LedgerTagType>('EXPENSE')

const isLedger = computed(() => moduleType.value === 'LEDGER')

const ledgerTypeOptions = [
  { label: '支出', value: 'EXPENSE' as LedgerTagType },
  { label: '收入', value: 'INCOME' as LedgerTagType }
]

const pageTitle = computed(() => (isLedger.value ? '记账标签' : '日记标签'))
const pageDesc = computed(() =>
  isLedger.value
    ? '按支出和收入分别管理，记账时快速选择'
    : '管理日记标签，记录生活分类'
)

function currentLedgerType() {
  return isLedger.value ? ledgerType.value : undefined
}

async function loadData() {
  const [templateList, tagList] = await Promise.all([
    fetchTagTemplates(moduleType.value, currentLedgerType()),
    fetchUserTags(moduleType.value, currentLedgerType())
  ])
  templates.value = templateList
  tags.value = tagList
}

async function useTemplate(templateId: Id) {
  await createTagFromTemplate(templateId, moduleType.value)
  uni.$feedback.success('已添加')
  await loadData()
}

async function createCustomTag() {
  const result = await uni.showModal({
    title: '新增标签',
    content: '请输入标签名称',
    editable: true,
    placeholderText: isLedger.value ? '例如：餐饮、工资' : '例如：旅行、生活'
  })

  const name = result.content?.trim()
  if (!result.confirm || !name) return

  await createTag({
    name,
    moduleType: moduleType.value,
    ledgerType: currentLedgerType()
  })
  uni.$feedback.success('标签已创建')
  await loadData()
}

async function removeTag(id: Id) {
  const result = await uni.showModal({
    title: '确认删除',
    content: '删除后该标签不会再出现在标签列表中。'
  })
  if (!result.confirm) return

  await deleteTag(id)
  uni.$feedback.success('已删除')
  await loadData()
}

watch(ledgerType, () => {
  if (isLedger.value) {
    loadData().catch(() => undefined)
  }
})

onLoad((query) => {
  if (query?.moduleType === 'LEDGER') {
    moduleType.value = 'LEDGER'
  }
  if (query?.ledgerType === 'INCOME') {
    ledgerType.value = 'INCOME'
  }
  loadData().catch(() => undefined)
})
</script>

<style scoped lang="scss">
.tags-page {
  padding-bottom: var(--bottom-padding);
}

/* ========== 顶栏 ========== */
.tags-header {
  padding: var(--space-5) var(--space-6) var(--space-3);
}

.tags-header__title {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

/* ========== 类型切换 ========== */
.tags-switch {
  margin: var(--space-4) var(--space-4) 0;
  display: flex;
  gap: var(--space-2);
  background: var(--color-surface);
  border-radius: var(--radius-full);
  padding: 6rpx;
  box-shadow: var(--shadow-card);
}

.tags-switch__item {
  flex: 1;
  text-align: center;
  padding: var(--space-3) 0;
  border-radius: var(--radius-full);
  transition: all var(--motion-fast) var(--ease-standard);
}

.tags-switch__item--active {
  background: var(--color-ledger);
  box-shadow: var(--shadow-btn);
}

.tags-switch__text {
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
  color: var(--color-text-secondary);
}

.tags-switch__item--active .tags-switch__text {
  color: #fff;
}

/* ========== 标签卡片 ========== */
.tags-card {
  margin: var(--space-4) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.tags-card__header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.tags-card__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.tags-card__badge {
  padding: 2rpx 14rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

/* ========== 标签网格 ========== */
.tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  transition: all var(--motion-fast) var(--ease-standard);
}

.tag-chip--user {
  padding-right: var(--space-2);
}

.tag-chip--template {
  cursor: pointer;
}

.tag-chip--template:active {
  transform: scale(0.95);
  opacity: 0.8;
}

.tag-chip__color {
  width: 16rpx;
  height: 16rpx;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.tag-chip__name {
  color: var(--color-text-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}

.tag-chip__delete {
  width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  text-align: center;
  border-radius: var(--radius-full);
  background: rgba(255, 59, 48, 0.12);
  color: var(--color-danger);
  font-size: 24rpx;
  margin-left: 4rpx;
}

.tag-chip__add {
  width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  text-align: center;
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  color: var(--color-checkin);
  font-size: 24rpx;
  margin-left: 4rpx;
}

/* ========== 空状态 ========== */
.tags-empty {
  padding: var(--space-5) 0;
  text-align: center;
}

.tags-empty__text {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

/* ========== 底部操作 ========== */
.tags-action {
  margin: var(--space-6) var(--space-4) 0;
}
</style>
