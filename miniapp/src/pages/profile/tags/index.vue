<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">{{ pageTitle }}</view>
          <view class="section-copy__desc">{{ pageDesc }}</view>
        </view>
        <u-tag :text="moduleType === 'LEDGER' ? '记账标签' : '日记标签'" type="warning" plain shape="circle" />
      </view>
    </view>

    <view v-if="moduleType === 'LEDGER'" class="page-section section-shell">
      <view class="block-stack">
        <view class="field-label">标签类型</view>
        <ChoiceChips v-model="ledgerType" :items="ledgerTypeOptions" />
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-copy">
        <view class="section-copy__title">新建自定义标签</view>
        <view class="section-copy__desc">如果模板里没有合适的，可以直接新增自己的标签。</view>
      </view>
      <view class="action-grid-2">
        <u-button type="primary" shape="circle" color="linear-gradient(135deg, #c47c52 0%, #d7a648 100%)" @click="createCustomTag">
          新增标签
        </u-button>
        <u-button shape="circle" plain @click="loadData">刷新列表</u-button>
      </view>
    </view>

    <view class="page-section">
      <view class="section-shell">
        <view class="section-copy">
          <view class="section-copy__title">系统模板</view>
          <view class="section-copy__desc">点击即可基于模板创建自己的标签。</view>
        </view>
      </view>

      <view v-if="templates.length" class="list-stack">
        <view v-for="item in templates" :key="item.id" class="list-card">
          <view class="list-card__head">
            <view>
              <view class="list-card__title">{{ item.name }}</view>
              <view class="list-card__meta">
                {{ item.moduleType === 'LEDGER' ? ledgerTypeText(item.ledgerType) : '日记标签模板' }}
              </view>
            </view>
            <u-button size="small" shape="circle" plain @click="useTemplate(item.id)">使用模板</u-button>
          </view>
        </view>
      </view>
      <EmptyStateCard
        v-else
        class="page-section"
        title="暂无模板"
        description="后台还没有配置可用标签模板。"
      />
    </view>

    <view class="page-section">
      <view class="section-shell">
        <view class="section-copy">
          <view class="section-copy__title">我的标签</view>
          <view class="section-copy__desc">这里展示当前用户已经扩展或新建的标签。</view>
        </view>
      </view>

      <view v-if="tags.length" class="list-stack">
        <view v-for="item in tags" :key="item.id" class="list-card">
          <view class="list-card__head">
            <view>
              <view class="list-card__title">{{ item.name }}</view>
              <view class="list-card__meta">
                {{ item.templateId ? '来源：模板扩展' : '来源：自定义创建' }}
                <text v-if="moduleType === 'LEDGER'"> · {{ ledgerTypeText(item.ledgerType) }}</text>
              </view>
            </view>
            <u-button size="small" shape="circle" plain type="error" @click="removeTag(item.id)">删除</u-button>
          </view>
        </view>
      </view>
      <EmptyStateCard
        v-else
        class="page-section"
        title="还没有标签"
        description="先从模板创建，或者直接新增自己的标签。"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ChoiceChips from '@/components/business/choice-chips'
import EmptyStateCard from '@/components/business/empty-state-card'
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

const templates = ref<TagItem[]>([])
const tags = ref<TagItem[]>([])
const moduleType = ref<TagModuleType>('DIARY')
const ledgerType = ref<LedgerTagType>('EXPENSE')

const ledgerTypeOptions = [
  { label: '支出标签', value: 'EXPENSE' },
  { label: '收入标签', value: 'INCOME' }
]

const pageTitle = computed(() => (moduleType.value === 'LEDGER' ? '记账标签管理' : '日记标签管理'))
const pageDesc = computed(() =>
  moduleType.value === 'LEDGER'
    ? '按支出和收入分别维护记账标签，方便筛选和记一笔时快速选择。'
    : '先使用后台提供的基础模板，再扩展成自己的个性标签。'
)

function currentLedgerType() {
  return moduleType.value === 'LEDGER' ? ledgerType.value : undefined
}

function ledgerTypeText(type?: LedgerTagType) {
  if (type === 'INCOME') return '收入标签'
  return '支出标签'
}

async function loadData() {
  const [templateList, tagList] = await Promise.all([
    fetchTagTemplates(moduleType.value, currentLedgerType()),
    fetchUserTags(moduleType.value, currentLedgerType())
  ])
  templates.value = templateList
  tags.value = tagList
}

async function useTemplate(templateId: number) {
  await createTagFromTemplate(templateId, moduleType.value)
  uni.$feedback.success('已从模板创建')
  await loadData()
}

async function createCustomTag() {
  const result = await uni.showModal({
    title: '新增标签',
    content: '请输入标签名称',
    editable: true,
    placeholderText: moduleType.value === 'LEDGER' ? '例如：餐饮、工资、报销' : '例如：旅行、生活'
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

async function removeTag(id: number) {
  const result = await uni.showModal({
    title: '确认删除',
    content: '删除后该标签不会再出现在你的标签列表中。'
  })
  if (!result.confirm) return

  await deleteTag(id)
  uni.$feedback.success('已删除')
  await loadData()
}

watch(ledgerType, () => {
  if (moduleType.value === 'LEDGER') {
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
