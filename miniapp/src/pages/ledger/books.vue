<template>
  <view class="page-shell-safe books-page">
    <!-- Hero -->
    <view class="books-hero">
      <text class="books-hero__icon">📚</text>
      <text class="books-hero__title">账本管理</text>
      <text class="books-hero__sub">创建账本，分类管理你的收支记录</text>
    </view>

    <!-- 账本列表 -->
    <view v-if="books.length" class="books-list">
      <view v-for="item in books" :key="item.id" class="book-item" hover-class="book-item--pressed" @tap="enterBook(item)">
        <view class="book-item__icon">
          <text class="book-item__icon-text">📖</text>
        </view>
        <view class="book-item__info">
          <text class="book-item__name">{{ item.name }}</text>
          <text class="book-item__desc">{{ item.description || '暂未填写说明' }}</text>
        </view>
        <text class="book-item__arrow">›</text>
      </view>
    </view>
    <EmptyStateCard v-else title="还没有账本" description="先创建一个账本，后面新增收支时就能直接选择" />

    <!-- 新建按钮 -->
    <view class="books-action">
      <u-button shape="circle" type="primary" color="var(--color-ledger-gradient)" @click="showCreate = true">
        ＋ 新建账本
      </u-button>
    </view>

    <!-- 新建弹窗 -->
    <u-popup v-model="showCreate" mode="center" width="86%" border-radius="28">
      <view class="books-popup">
        <view class="books-popup__head">
          <text class="books-popup__title">新建账本</text>
        </view>
        <view class="books-popup__field">
          <text class="books-popup__label">账本名称</text>
          <input v-model="form.name" class="books-popup__input" placeholder="例如：生活开销、旅行账本" />
        </view>
        <view class="books-popup__field">
          <text class="books-popup__label">账本说明</text>
          <textarea v-model="form.description" class="books-popup__textarea" placeholder="可选，用一句话说明用途" />
        </view>
        <view class="books-popup__actions">
          <u-button shape="circle" plain :hair-line="false" @click="showCreate = false">取消</u-button>
          <u-button shape="circle" type="primary" color="var(--color-ledger-gradient)" :loading="submitting" @click="submit">
            {{ submitting ? '创建中...' : '创建' }}
          </u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import { createBook, fetchBooks, type LedgerBook } from '@/api/books'
import { setLastLedgerBook } from '@/utils/ledger-book'

const books = ref<LedgerBook[]>([])
const showCreate = ref(false)
const submitting = ref(false)
const form = ref({ name: '', description: '' })

async function loadBooks() { books.value = await fetchBooks() }

function enterBook(book: LedgerBook) {
  setLastLedgerBook({ id: book.id, name: book.name })
  uni.navigateTo({ url: `/pages/ledger/index?bookId=${book.id}&bookName=${encodeURIComponent(book.name)}` })
}

async function submit() {
  if (!form.value.name.trim()) { uni.$feedback.error('请先填写账本名称'); return }
  submitting.value = true
  try {
    const created = await createBook({ name: form.value.name.trim(), description: form.value.description.trim() })
    setLastLedgerBook({ id: created.id, name: created.name })
    uni.$feedback.success(`已创建：${created.name}`)
    form.value.name = ''
    form.value.description = ''
    showCreate.value = false
    await loadBooks()
  } catch (error) {
    uni.$feedback.error(error, undefined, '创建失败')
  } finally { submitting.value = false }
}

onShow(() => { loadBooks().catch((e) => uni.$feedback.error(e, undefined, '加载失败')) })
</script>

<style scoped lang="scss">
.books-page {
  padding-bottom: var(--space-10);
}

/* ========== Hero ========== */
.books-hero {
  background: var(--color-ledger-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-8) var(--space-6) var(--space-7);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  color: #fff;
}

.books-hero__icon {
  font-size: 56rpx;
  margin-bottom: var(--space-3);
}

.books-hero__title {
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.books-hero__sub {
  margin-top: var(--space-2);
  font-size: var(--font-meta);
  opacity: 0.85;
}

/* ========== 列表 ========== */
.books-list {
  margin: var(--space-4) var(--space-4) 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.book-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
  transition: all var(--motion-fast) var(--ease-standard);
}

.book-item--pressed {
  transform: scale(0.98);
}

.book-item__icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: var(--radius-medium);
  background: var(--color-ledger-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.book-item__icon-text {
  font-size: 40rpx;
}

.book-item__info {
  flex: 1;
  min-width: 0;
}

.book-item__name {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
}

.book-item__desc {
  display: block;
  margin-top: 4rpx;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-item__arrow {
  color: var(--color-text-muted);
  font-size: 32rpx;
  flex-shrink: 0;
}

/* ========== 操作 ========== */
.books-action {
  margin: var(--space-6) var(--space-4) 0;
}

/* ========== 弹窗 ========== */
.books-popup {
  padding: var(--space-6);
  background: var(--color-bg);
}

.books-popup__head {
  text-align: center;
  margin-bottom: var(--space-5);
}

.books-popup__title {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.books-popup__field {
  margin-bottom: var(--space-4);
}

.books-popup__label {
  display: block;
  margin-bottom: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
}

.books-popup__input {
  width: 100%;
  height: 88rpx;
  padding: 0 var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-body);
  box-sizing: border-box;
}

.books-popup__textarea {
  width: 100%;
  min-height: 120rpx;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-body);
  box-sizing: border-box;
}

.books-popup__actions {
  margin-top: var(--space-5);
  display: flex;
  gap: var(--space-3);
}

.books-popup__actions .u-button {
  flex: 1;
}
</style>
