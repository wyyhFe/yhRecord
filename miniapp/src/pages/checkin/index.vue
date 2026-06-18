<template>
  <view class="page-shell-safe checkin-page">
    <!-- Hero 头部 -->
    <view class="checkin-hero">
      <view class="checkin-hero__top">
        <text class="checkin-hero__date">{{ todayDisplay }}</text>
        <!-- 圆形进度 -->
        <view class="checkin-hero__ring">
          <view class="checkin-hero__ring-bg">
            <view class="checkin-hero__ring-text">{{ progressPercent }}%</view>
          </view>
        </view>
      </view>
      <view class="checkin-hero__title">今日打卡</view>
      <view class="checkin-hero__sub">
        已完成 <text class="checkin-hero__highlight">{{ todayDoneIds.size }}</text> / {{ tasks.length }} 个任务
      </view>
    </view>

    <!-- 热力图 -->
    <view v-if="heatmapData" class="checkin-card">
      <HeatmapCalendar
        :data="heatmapData"
        @month-change="onHeatmapMonthChange"
        @day-tap="onHeatmapDayTap"
      />
      <view class="checkin-card__footer">
        <text class="checkin-card__footer-text">🔥 连续 {{ heatmapData.currentStreak }} 天</text>
        <text class="checkin-card__footer-divider">·</text>
        <text class="checkin-card__footer-text">本月剩余补卡 {{ mendRemaining }}/2</text>
      </view>
    </view>

    <!-- 任务列表 -->
    <view class="checkin-card">
      <view class="checkin-card__header">
        <text class="checkin-card__title">我的任务</text>
        <text class="checkin-card__badge">{{ tasks.length }}</text>
      </view>

      <view v-if="tasks.length" class="task-list">
        <u-swipe-action
          v-for="(task, index) in tasks"
          :key="task.id"
          :options="deleteSwipeOptions"
          :index="index"
          btn-width="140"
          @click="onSwipeDelete"
        >
          <view
            class="task-item"
            :class="{ 'task-item--done': todayDoneIds.has(task.id) }"
          >
            <view class="task-item__content">
              <view class="task-item__left">
                <view class="task-item__status" :class="{ 'task-item__status--done': todayDoneIds.has(task.id) }">
                  <text v-if="todayDoneIds.has(task.id)">✓</text>
                </view>
                <view class="task-item__info">
                  <text class="task-item__name">{{ task.name }}</text>
                  <text class="task-item__meta">{{ task.totalCount }} 次 · {{ task.latestCheckedAt || '暂无记录' }}</text>
                </view>
              </view>
              <view class="task-item__right">
                <view
                  v-if="!todayDoneIds.has(task.id)"
                  class="task-item__btn"
                  hover-class="task-item__btn--pressed"
                  @click="handleCheckin(task)"
                >
                  <text class="task-item__btn-text">打卡</text>
                </view>
                <view v-else class="task-item__done-badge">
                  <text class="task-item__done-text">已完成</text>
                </view>
              </view>
            </view>
          </view>
        </u-swipe-action>
      </view>
      <EmptyStateCard
        v-else
        :title="loadFailed ? '加载失败' : '还没有打卡任务'"
        :description="loadFailed ? '请检查网络或登录状态' : '点击上方 ＋ 创建第一条任务'"
      />
    </view>

    <!-- 长按删除提示 -->
    <view v-if="tasks.length" class="checkin-hint">
      <text class="checkin-hint__text">左滑任务可删除</text>
    </view>

    <!-- 打卡弹窗 -->
    <u-popup v-model="showCheckinPopup" mode="bottom" border-radius="28" :safe-area-inset-bottom="true">
      <view class="checkin-popup">
        <view class="checkin-popup__head">
          <view class="checkin-popup__title">{{ checkinTargetTask?.name || '打卡' }}</view>
          <view class="checkin-popup__subtitle">添加心情、标签、备注和图片（可选）</view>
        </view>

        <view class="block-stack">
          <MoodPicker v-model="checkinMood" />
        </view>

        <view class="block-stack">
          <TagPicker :tags="tags" v-model="checkinTagIds" @create-tag="handleCreateTag" />
        </view>

        <view class="block-stack">
          <u-textarea
            v-model="checkinRemark"
            placeholder="一句话记录今天的感受..."
            :border="true"
            border-color="var(--color-border-strong)"
            :custom-style="textareaStyle"
            height="80"
            :maxlength="50"
          />
          <view class="micro-record-counter" :class="{ 'is-over': checkinRemark.length > 50 }">
            {{ checkinRemark.length }}/50
          </view>
        </view>

        <view class="block-stack">
          <PhotoPicker v-model="checkinPhotos" :max-count="9" @retry="retryCheckinPhotoUpload" />
        </view>

        <view class="checkin-popup__actions">
          <u-button shape="circle" plain :hair-line="false" @click="showCheckinPopup = false">取消</u-button>
          <u-button
            shape="circle"
            type="primary"
            color="linear-gradient(135deg, var(--color-checkin) 0%, #B49AD8 100%)"
            :loading="checkinSubmitting"
            @click="confirmCheckin"
          >
            {{ checkinSubmitting ? '提交中...' : '确认打卡' }}
          </u-button>
        </view>
      </view>
    </u-popup>

    <!-- 勋章解锁弹窗 -->
    <MedalUnlockPopup v-model="showMedalPopup" :medal="unlockedMedal" />

    <!-- 补卡弹窗 -->
    <MendCheckinPopup
      v-model="showMendPopup"
      :mend-date="mendTargetDate"
      :display-date="mendDisplayDate"
      :tasks="mendTaskOptions"
      :completed-ids="mendCompletedIds"
      :remaining="mendRemaining"
      @success="onMendSuccess"
    />

    <!-- 悬浮按钮 -->
    <view class="fab" :class="{ 'fab--open': fabOpen }">
      <view class="fab__mask" @tap="fabOpen = false" />
      <!-- 小弧：始终包裹 + 按钮 -->
      <view class="fab__arc-small" />
      <!-- 大弧：展开后覆盖子菜单 -->
      <view class="fab__arc-big" />
      <!-- 子菜单 -->
      <view class="fab__sub fab__sub--1" @tap="goMedals">
        <view class="fab__sub-circle"><text class="fab__sub-emoji">🏆</text></view>
        <text class="fab__sub-text">成就</text>
      </view>
      <view class="fab__sub fab__sub--2" @tap="goCreate">
        <view class="fab__sub-circle"><text class="fab__sub-emoji">＋</text></view>
        <text class="fab__sub-text">新任务</text>
      </view>
      <!-- 主按钮 -->
      <view class="fab__btn" @tap="fabOpen = !fabOpen">
        <text class="fab__btn-icon">＋</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import PhotoPicker, { type SelectedPhoto } from '@/components/business/photo-picker/index.vue'
import HeatmapCalendar from '@/components/business/heatmap-calendar/index.vue'
import MoodPicker from '@/components/business/mood-picker/index.vue'
import TagPicker from '@/components/business/tag-picker/index.vue'
import { uploadImageToOss } from '@/utils/upload'
import MedalUnlockPopup from '@/components/business/medal-unlock-popup/index.vue'
import MendCheckinPopup from './modules/mend-checkin-popup/index.vue'
import {
  createCheckinTag,
  fetchCheckinDayDetail,
  fetchCheckinHeatmap,
  fetchCheckinTags,
  fetchCheckinTasks,
  fetchMedals,
  fetchMendRemaining,
  submitCheckin
} from '@/api/checkin'
import type { CheckinMediaItem } from '@/api/checkin'
import type { CheckinTag, CheckinTask, HeatmapData, HeatmapDay, Id, Medal } from '@/types/domain'

const tasks = ref<CheckinTask[]>([])
const loadFailed = ref(false)

const showCheckinPopup = ref(false)
const checkinTargetTask = ref<CheckinTask | null>(null)
const checkinRemark = ref('')
const checkinPhotos = ref<SelectedPhoto[]>([])
const checkinSubmitting = ref(false)
const checkinMood = ref<string | undefined>(undefined)
const checkinTagIds = ref<Id[]>([])

const heatmapData = ref<HeatmapData | null>(null)
const heatmapYear = ref(new Date().getFullYear())
const heatmapMonth = ref(new Date().getMonth() + 1)
const tags = ref<CheckinTag[]>([])
const mendRemaining = ref(0)
const showMendPopup = ref(false)
const mendTargetDate = ref('')
const mendDisplayDate = ref('')
const mendTaskOptions = ref<CheckinTask[]>([])
const mendCompletedIds = ref<Set<number>>(new Set())
const showMedalPopup = ref(false)
const unlockedMedal = ref<Medal | null>(null)
const fabOpen = ref(false)

// 左滑删除配置
const deleteSwipeOptions = ref([
  { text: '删除', style: { background: '#f56c6c', color: '#fff', fontSize: '26rpx', height: '100%' } }
])

function onSwipeDelete(index: number, btnIndex: number) {
  const task = tasks.value[index]
  if (task) handleDeleteTask(task)
}

const textareaStyle = {
  background: 'var(--color-surface-soft)',
  borderRadius: '20rpx',
  padding: '18rpx 22rpx',
  fontSize: '26rpx',
  width: '100%',
  boxSizing: 'border-box' as const
}

const WEEK = ['日', '一', '二', '三', '四', '五', '六']
const now = new Date()
const today = now.toISOString().slice(0, 10)
const todayDisplay = `${now.getMonth() + 1}月${now.getDate()}日 周${WEEK[now.getDay()]}`
const todayDoneIds = ref(new Set<Id>())

const progressPercent = computed(() => {
  if (!tasks.value.length) return 0
  return Math.round((todayDoneIds.value.size / tasks.value.length) * 100)
})

function goCreate() {
  uni.navigateTo({ url: '/pages/checkin/editor' })
}

function goMedals() {
  uni.navigateTo({ url: '/pages/checkin/medals' })
}

async function loadTasks() {
  tasks.value = await fetchCheckinTasks()
}

async function loadTodayStatus() {
  const items = await fetchCheckinDayDetail(today)
  todayDoneIds.value = new Set(items.map((item) => item.id))
}

async function loadHeatmap() {
  try {
    heatmapData.value = await fetchCheckinHeatmap(heatmapYear.value, heatmapMonth.value)
  } catch {
    heatmapData.value = null
  }
}

async function loadTags() {
  try {
    tags.value = await fetchCheckinTags()
  } catch {
    tags.value = []
  }
}

function onHeatmapMonthChange(year: number, month: number) {
  heatmapYear.value = year
  heatmapMonth.value = month
  loadHeatmap()
}

async function onHeatmapDayTap(day: HeatmapDay) {
  const dayDate = new Date(day.date)
  const todayDate = new Date(today)
  todayDate.setHours(0, 0, 0, 0)

  // 今天或未来 → 跳历史页
  if (dayDate >= todayDate) {
    uni.navigateTo({ url: `/pages/checkin/history?date=${day.date}` })
    return
  }

  // 全部完成 → 跳历史页
  if (day.completedTasks >= day.totalTasks) {
    uni.navigateTo({ url: `/pages/checkin/history?date=${day.date}` })
    return
  }

  // 超过 7 天 → 跳历史页
  const sevenDaysAgo = new Date(todayDate)
  sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7)
  if (dayDate < sevenDaysAgo) {
    uni.navigateTo({ url: `/pages/checkin/history?date=${day.date}` })
    return
  }

  // 获取该日未完成的任务 → 弹窗展示（无论是否有补卡次数）
  try {
    const completedItems = await fetchCheckinDayDetail(day.date)
    const completedIds = new Set(completedItems.map((item) => item.id))
    // 当天所有有效任务（含已完成 + 未完成）
    const relevant = tasks.value.filter(
      (t) => !t.startDate || t.startDate <= day.date
    )

    if (relevant.length === 0) {
      uni.navigateTo({ url: `/pages/checkin/history?date=${day.date}` })
      return
    }

    mendTargetDate.value = day.date
    const d = new Date(day.date)
    mendDisplayDate.value = `${d.getMonth() + 1}月${d.getDate()}日`
    mendTaskOptions.value = relevant
    mendCompletedIds.value = completedIds
    showMendPopup.value = true
  } catch (error) {
    uni.$feedback.error(error, undefined, '获取任务列表失败')
  }
}

async function handleCreateTag(name: string) {
  try {
    const tag = await createCheckinTag({ name })
    tags.value.push(tag)
    checkinTagIds.value.push(tag.id)
    uni.$feedback.success('标签已创建')
  } catch (error) {
    uni.$feedback.error(error, undefined, '创建标签失败')
  }
}

async function loadMendRemaining() {
  try {
    mendRemaining.value = await fetchMendRemaining()
  } catch {
    mendRemaining.value = 0
  }
}

async function onMendSuccess() {
  await reloadAll()
}

async function reloadAll() {
  try {
    await Promise.all([loadTasks(), loadTodayStatus(), loadHeatmap(), loadTags(), loadMendRemaining()])
    loadFailed.value = false
  } catch (error) {
    tasks.value = []
    todayDoneIds.value = new Set()
    loadFailed.value = true
    uni.$feedback.error(error, undefined, '加载打卡数据失败')
  }
}

function handleCheckin(task: CheckinTask) {
  if (todayDoneIds.value.has(task.id)) {
    uni.$feedback.info('这个任务今天已经打过卡了')
    return
  }
  checkinTargetTask.value = task
  checkinRemark.value = ''
  checkinPhotos.value = []
  checkinMood.value = undefined
  checkinTagIds.value = []
  showCheckinPopup.value = true
}

async function handleDeleteTask(task: CheckinTask) {
  const result = await uni.showModal({
    title: '确认删除',
    content: `确定要删除任务「${task.name}」吗？删除后相关打卡记录也会一并清除。`
  })
  if (!result.confirm) return
  try {
    await deleteCheckinTask(task.id)
    uni.$feedback.success('已删除')
    await reloadAll()
  } catch (error) {
    uni.$feedback.error(error, undefined, '删除失败')
  }
}

async function confirmCheckin() {
  if (!checkinTargetTask.value) return
  checkinSubmitting.value = true
  try {
    for (let i = 0; i < checkinPhotos.value.length; i++) {
      const photo = checkinPhotos.value[i]
      if (photo.status !== 'done') {
        photo.status = 'uploading'
        try {
          const ossPath = photo.ossPath ||
            (await uploadImageToOss({ filePath: photo.localPath, dir: 'checkin/' }))
          photo.ossPath = ossPath
          photo.status = 'done'
        } catch {
          photo.status = 'failed'
          throw new Error('图片上传失败')
        }
      }
    }

    const mediaList: CheckinMediaItem[] = checkinPhotos.value
      .filter((item) => item.ossPath)
      .map((item, index) => ({
        mediaType: 'IMAGE',
        filePath: item.ossPath as string,
        sortOrder: index + 1
      }))

    await submitCheckin(checkinTargetTask.value.id, {
      checkinDate: today,
      remark: checkinRemark.value || undefined,
      mediaList: mediaList.length > 0 ? mediaList : undefined,
      mood: checkinMood.value,
      tagIds: checkinTagIds.value.length > 0 ? checkinTagIds.value : undefined
    })

    showCheckinPopup.value = false
    uni.$feedback.success('打卡成功')

    try {
      const allMedals = await fetchMedals()
      const newlyUnlocked = allMedals.filter((m) => m.unlocked && m.unlockedAt?.startsWith(today))
      if (newlyUnlocked.length > 0) {
        unlockedMedal.value = newlyUnlocked[0]
        showMedalPopup.value = true
      }
    } catch {
      // 静默
    }

    await reloadAll()
  } catch (error) {
    uni.$feedback.error(error, undefined, '打卡失败')
  } finally {
    checkinSubmitting.value = false
  }
}

async function retryCheckinPhotoUpload(index: number) {
  const photo = checkinPhotos.value[index]
  if (!photo) return
  photo.status = 'uploading'
  try {
    const ossPath = await uploadImageToOss({ filePath: photo.localPath, dir: 'checkin/' })
    photo.ossPath = ossPath
    photo.status = 'done'
    uni.$feedback.success('重试成功')
  } catch {
    photo.status = 'failed'
    uni.$feedback.error('重试失败')
  }
}

onShow(() => {
  reloadAll()
})
</script>

<style scoped lang="scss">
.checkin-page {
  padding-bottom: var(--space-10);
}

/* ========== Hero 头部 ========== */
.checkin-hero {
  background: var(--color-checkin-gradient);
  border-radius: 0 0 var(--radius-xlarge) var(--radius-xlarge);
  padding: var(--space-5) var(--space-6);
  min-height: 200rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
}

.checkin-hero__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-1);
}

.checkin-hero__date {
  font-size: var(--font-tiny);
  opacity: 0.8;
}

/* 圆形进度 */
.checkin-hero__ring {
  width: 96rpx;
  height: 96rpx;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkin-hero__ring-bg {
  width: 78rpx;
  height: 78rpx;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkin-hero__ring-text {
  font-size: 24rpx;
  font-weight: var(--weight-bold);
}

.checkin-hero__title {
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
  line-height: 1.2;
}

.checkin-hero__sub {
  margin-top: var(--space-2);
  font-size: var(--font-meta);
  opacity: 0.85;
}

.checkin-hero__highlight {
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

/* ========== 悬浮按钮 ========== */
.fab {
  position: fixed;
  right: 32rpx;
  bottom: 120rpx;
  z-index: 50;
}

.fab__mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.2);
  z-index: 49;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.fab--open .fab__mask {
  opacity: 1;
  pointer-events: auto;
}

/* 弧形通用样式 — 固定圆角半径，保证大小弧弧度一致 */
.fab__arc-small,
.fab__arc-big {
  position: fixed;
  border-radius: 500rpx;
  background: rgba(255, 255, 255, 0.97);
  box-shadow: -4rpx -4rpx 24rpx rgba(0, 0, 0, 0.08);
  opacity: 0;
  transform: scale(0);
  transform-origin: bottom right;
  pointer-events: none;
}

/* 小弧 — 包裹 + 按钮 */
.fab__arc-small {
  right: -60rpx;
  bottom: -60rpx;
  width: 300rpx;
  height: 300rpx;
  z-index: 50;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fab--open .fab__arc-small {
  opacity: 1;
  transform: scale(1);
  pointer-events: auto;
}

/* 大弧 — 包裹菜单和 + 按钮 */
.fab__arc-big {
  right: -300rpx;
  bottom: -300rpx;
  width: 700rpx;
  height: 700rpx;
  z-index: 49;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fab--open .fab__arc-big {
  opacity: 1;
  transform: scale(1);
  pointer-events: auto;
  transition-delay: 0.15s;
}

/* 主按钮 */
.fab__btn {
  position: fixed;
  right: 32rpx;
  bottom: 45rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: var(--color-checkin-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(155, 126, 200, 0.4);
  z-index: 52;
  transition: transform 0.3s ease;
}

.fab--open .fab__btn {
  transform: rotate(45deg);
}

.fab__btn-icon {
  font-size: 48rpx;
  color: #fff;
  font-weight: 700;
  line-height: 1;
}

/* 子按钮 — 在弧形内部 */
.fab__sub {
  position: fixed;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  opacity: 0;
  transform: scale(0.3);
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 51;
  pointer-events: none;
}

.fab__sub--1 {
  right: 200rpx;
  bottom: 180rpx;
  transition-delay: 0s;
}

.fab__sub--2 {
  right: 80rpx;
  bottom: 280rpx;
  transition-delay: 0.06s;
}

.fab--open .fab__sub {
  opacity: 1;
  transform: scale(1);
  pointer-events: auto;
}

.fab--open .fab__sub--1 {
  transition-delay: 0.06s;
}

.fab--open .fab__sub--2 {
  transition-delay: 0.12s;
}

.fab__sub-circle {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: var(--color-checkin-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(155, 126, 200, 0.3);
}

.fab__sub-emoji {
  font-size: 36rpx;
  line-height: 1;
}

.fab__sub-text {
  color: #1C1C1E;
  font-size: 22rpx;
  font-weight: 600;
  white-space: nowrap;
}

/* ========== 通用卡片 ========== */
.checkin-card {
  margin: var(--space-4) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
  overflow: hidden;
}

.checkin-card__header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.checkin-card__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.checkin-card__badge {
  padding: 2rpx 14rpx;
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  color: var(--color-checkin);
  font-size: var(--font-tiny);
  font-weight: var(--weight-semibold);
}

.checkin-card__footer {
  margin-top: var(--space-4);
  padding-top: var(--space-3);
  border-top: 1rpx solid var(--color-divider);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.checkin-card__footer-text {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
}

.checkin-card__footer-divider {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

/* ========== 任务列表 ========== */
.task-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.task-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  transition: all var(--motion-fast) var(--ease-standard);
}

.task-item--done {
  opacity: 0.65;
}

.task-item__left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1;
  min-width: 0;
}

.task-item__status {
  width: 48rpx;
  height: 48rpx;
  border-radius: var(--radius-full);
  border: 3rpx solid var(--color-border-strong);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--motion-fast) var(--ease-standard);
}

.task-item__status--done {
  background: var(--color-checkin);
  border-color: var(--color-checkin);
  color: #fff;
  font-size: 24rpx;
  font-weight: var(--weight-bold);
}

.task-item__info {
  flex: 1;
  min-width: 0;
}

.task-item__name {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-item__meta {
  display: block;
  margin-top: 4rpx;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.task-item__right {
  flex-shrink: 0;
  margin-left: var(--space-3);
}

.task-item__btn {
  padding: var(--space-2) var(--space-5);
  border-radius: var(--radius-full);
  background: var(--color-checkin-gradient);
  transition: all var(--motion-fast) var(--ease-standard);
}

.task-item__btn--pressed {
  transform: scale(0.92);
  opacity: 0.85;
}

.task-item__btn-text {
  color: #fff;
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

.task-item__done-badge {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
}

.task-item__done-text {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

/* ========== 提示 ========== */
.checkin-hint {
  margin-top: var(--space-3);
  text-align: center;
}

.checkin-hint__text {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

/* ========== 打卡弹窗 ========== */
.checkin-popup {
  padding: var(--space-6) var(--space-5) calc(var(--space-6) + env(safe-area-inset-bottom));
  background: var(--color-bg);
}

.checkin-popup__head {
  text-align: center;
  margin-bottom: var(--space-4);
}

.checkin-popup__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.checkin-popup__subtitle {
  margin-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.checkin-popup__actions {
  margin-top: var(--space-5);
  display: flex;
  gap: var(--space-3);
}

.checkin-popup__actions .u-button {
  flex: 1;
}

.micro-record-counter {
  text-align: right;
  margin-top: var(--space-1);
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.micro-record-counter.is-over {
  color: var(--color-danger);
}
</style>
