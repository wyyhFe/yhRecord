<template>
  <view :class="['page-shell-safe', themeClass]">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">账本管理</view>
          <view class="section-copy__desc">先创建账本，再进入对应账本查看收支流水和统计。</view>
        </view>
        <u-button
          size="mini"
          type="primary"
          shape="circle"
          color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
          @click="showCreate = true"
        >
          + 新建账本
        </u-button>
      </view>
    </view>

    <view v-if="books.length" class="page-section list-stack">
      <view v-for="item in books" :key="item.id" class="section-shell book-card">
        <view class="list-card__head">
          <view>
            <view class="list-card__title">{{ item.name }}</view>
            <view class="list-card__meta">{{ item.description || '暂未填写账本说明' }}</view>
          </view>
          <view class="book-card__id">#{{ item.id }}</view>
        </view>

        <view class="book-card__actions">
          <u-button plain shape="circle" :hair-line="false" size="mini" @click="enterBook(item)">进入</u-button>
        </view>
      </view>
    </view>

    <EmptyStateCard
      v-else
      class="page-section"
      title="还没有账本"
      description="先创建一个账本，后面新增收支时就能直接选择。"
    >
      <template #actions>
        <u-button
          type="primary"
          shape="circle"
          size="mini"
          color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
          @click="showCreate = true"
        >
          新建账本
        </u-button>
      </template>
    </EmptyStateCard>

    <u-popup v-model="showCreate" mode="center" width="86%" border-radius="28">
      <view class="book-popup">
        <view class="book-popup__head">
          <view class="book-popup__title">新建账本</view>
        </view>

        <view class="block-stack">
          <view class="field-label">账本名称</view>
          <u-input
            v-model="form.name"
            placeholder="例如：生活开销、旅行账本"
            :border="true"
            border-color="#eadfd0"
            :custom-style="fieldStyle"
          />
        </view>

        <view class="block-stack">
          <view class="field-label">账本说明</view>
          <u-textarea
            v-model="form.description"
            placeholder="可选，用一句话说明这个账本的用途"
            :border="true"
            border-color="#eadfd0"
            :custom-style="textareaStyle"
            height="150"
          />
        </view>

        <view class="action-grid-2">
          <u-button plain shape="circle" :hair-line="false" @click="showCreate = false">取消</u-button>
          <u-button
            type="primary"
            shape="circle"
            color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)"
            :loading="submitting"
            @click="submit"
          >
            {{ submitting ? '创建中...' : '创建' }}
          </u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
const { themeClass } = useTheme()

import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import { createBook, fetchBooks, type LedgerBook } from '@/api/books'
import { setLastLedgerBook } from '@/utils/ledger-book'

const books = ref<LedgerBook[]>([])
const showCreate = ref(false)
const submitting = ref(false)
const form = ref({
  name: '',
  description: ''
})

const fieldStyle = {
  background: 'var(--color-surface-soft)',
  borderRadius: '20rpx',
  padding: '0 22rpx',
  fontSize: '28rpx',
  minHeight: '84rpx'
}

const textareaStyle = {
  background: 'var(--color-surface-soft)',
  borderRadius: '20rpx',
  padding: '18rpx 22rpx',
  fontSize: '26rpx',
  width: '100%',
  boxSizing: 'border-box' as const
}

async function loadBooks() {
  books.value = await fetchBooks()
}

function enterBook(book: LedgerBook) {
  setLastLedgerBook({ id: book.id, name: book.name })
  uni.navigateTo({
    url: `/pages/ledger/index?bookId=${book.id}&bookName=${encodeURIComponent(book.name)}`
  })
}

async function submit() {
  if (!form.value.name.trim()) {
    uni.$feedback.error('请先填写账本名称')
    return
  }

  submitting.value = true
  try {
    const created = await createBook({
      name: form.value.name.trim(),
      description: form.value.description.trim()
    })
    setLastLedgerBook({ id: created.id, name: created.name })
    uni.$feedback.success(`已创建账本：${created.name}`)
    form.value.name = ''
    form.value.description = ''
    showCreate.value = false
    await loadBooks()
  } catch (error) {
    uni.$feedback.error(error, undefined, '创建账本失败')
  } finally {
    submitting.value = false
  }
}

onShow(() => {
  loadBooks().catch((error) => {
    uni.$feedback.error(error, undefined, '加载账本失败')
  })
})
</script>

<style scoped lang="scss">
.book-card__id {
  color: var(--color-text-muted);
  font-size: 24rpx;
}

.book-card__actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20rpx;
}

.book-popup {
  padding: 28rpx;
  background: var(--color-bg);
}

.book-popup__head {
  margin-bottom: 20rpx;
  text-align: center;
}

.book-popup__title {
  color: var(--color-text-primary);
  font-size: 34rpx;
  font-weight: 700;
}
</style>
