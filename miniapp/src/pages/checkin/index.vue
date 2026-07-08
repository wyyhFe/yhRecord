<template>
  <view class="page-shell-safe checkin-page">
    <!-- 顶栏 -->
    <view class="checkin-header">
      <view class="checkin-header__left">
        <text class="checkin-header__title">今日打卡</text>
        <text class="checkin-header__count">
          已完成 {{ todayDoneIds.size }} / {{ tasks.length }} 个任务
        </text>
      </view>
      <view class="checkin-header__ring">
        <text class="checkin-header__ring-text">{{ progressPercent }}%</text>
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
        <text v-if="tasks.length" class="checkin-card__hint">左滑可删除</text>
      </view>

      <view v-if="tasks && tasks.length" class="task-list">
        <u-swipe-action
          v-for="(task, index) in tasks"
          :key="task.id"
          :options="deleteSwipeOptions"
          :index="index"
          btn-width="160"
          bg-color="var(--color-surface-soft)"
          @click="onSwipeDelete"
        >
          <view
            class="task-item"
            :class="{ 'task-item--done': todayDoneIds.has(task.id) }"
          >
            <view class="task-item__left">
              <view class="task-item__status" :class="{ 'task-item__status--done': todayDoneIds.has(task.id) }">
                <text v-if="todayDoneIds.has(task.id)">✓</text>
              </view>
              <view class="task-item__info">
                <text class="task-item__name">{{ task.name }}</text>
                <text class="task-item__meta">{{ task.totalCount }} 次 · {{ formatLatestTime(task.latestCheckedAt) }}</text>
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
              <text v-else class="task-item__done-text">已打卡</text>
            </view>
          </view>
        </u-swipe-action>
      </view>
      <EmptyStateCard
        v-else
        :title="loadFailed ? '加载失败' : '还没有打卡任务'"
        :description="loadFailed ? '请检查网络或登录状态' : '点击上方 ＋ 创建第一条任务'"
        :mode="loadFailed ? 'wifi' : 'data'"
      />
    </view>



    <!-- 打卡弹窗 -->
    <u-popup v-model="showCheckinPopup" mode="bottom" border-radius="28" :safe-area-inset-bottom="false">
      <view class="checkin-popup">
        <scroll-view scroll-y class="checkin-popup__scroll">
          <view class="checkin-popup__head">
            <view class="checkin-popup__title">{{ checkinTargetTask?.name || '打卡' }}</view>
          </view>

          <!-- 心情 + 标签（同一个盒子） -->
          <view class="checkin-popup__card">
            <MoodPicker v-model="checkinMood" />
            <view class="checkin-popup__tag-divider" />
            <view class="checkin-popup__tag-bar" @tap="showTagPickerPopup = true">
              <text class="checkin-popup__tag-bar-label">🏷️ 标签</text>
              <view class="checkin-popup__tag-bar-right">
                <text class="checkin-popup__tag-bar-value">
                  {{ checkinTagIds.length ? `已选 ${checkinTagIds.length}` : '选择标签' }}
                </text>
                <text class="checkin-popup__tag-bar-arrow">›</text>
              </view>
            </view>
            <!-- 已选标签回显 -->
            <view v-if="selectedTags.length" class="checkin-popup__pills">
              <view v-for="tag in selectedTags" :key="tag.id" class="checkin-popup__pill">
                <text class="checkin-popup__pill-text">{{ tag.name }}</text>
                <text class="checkin-popup__pill-x" @tap.stop="removeTag(tag.id)">✕</text>
              </view>
            </view>
          </view>

          <!-- 备注 -->
          <view class="checkin-popup__card">
            <textarea
              v-model="checkinRemark"
              class="checkin-popup__textarea"
              placeholder="写句话..."
              :maxlength="50"
            />
            <view class="checkin-popup__counter" :class="{ 'is-over': checkinRemark.length > 50 }">
              {{ checkinRemark.length }}/50
            </view>
          </view>

          <!-- 图片 -->
          <view class="checkin-popup__card checkin-popup__card--photo">
            <PhotoPicker v-model="checkinPhotos" :max-count="9" @retry="retryCheckinPhotoUpload" />
          </view>
        </scroll-view>

        <!-- 操作按钮（固定在底部，不随内容滚动） -->
        <view class="checkin-popup__actions">
          <view class="checkin-popup__btn checkin-popup__btn--cancel" hover-class="checkin-popup__btn--pressed" @click="showCheckinPopup = false">
            <text>取消</text>
          </view>
          <view class="checkin-popup__btn checkin-popup__btn--confirm" hover-class="checkin-popup__btn--pressed" @click="confirmCheckin">
            <text>{{ checkinSubmitting ? '提交中' : '确认打卡' }}</text>
          </view>
        </view>
      </view>
    </u-popup>

    <!-- 标签选择弹窗 -->
    <u-popup v-model="showTagPickerPopup" mode="bottom" border-radius="28">
      <view class="checkin-tag-popup" style="max-height: 65vh;">
        <view class="checkin-tag-popup__head">
          <text class="checkin-tag-popup__title">选择标签</text>
          <text class="checkin-tag-popup__close" @tap="showTagPickerPopup = false">✕</text>
        </view>
        <scroll-view scroll-y class="checkin-tag-popup__scroll">
          <TagPicker :tags="tags" v-model="checkinTagIds" @create-tag="handleCreateTag" />
        </scroll-view>
        <view class="checkin-tag-popup__foot">
          <view class="checkin-tag-popup__btn" hover-class="checkin-tag-popup__btn--pressed" @tap="showTagPickerPopup = false">
            <text>确定</text>
          </view>
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
      <!-- 子菜单 -->
      <view class="fab__sub fab__sub--1" @tap="goMedals">
        <view class="fab__sub-circle"><text class="fab__sub-emoji">🏆</text></view>
        <text class="fab__sub-text">成就</text>
      </view>
      <view class="fab__sub fab__sub--2" @tap="goCreate">
        <view class="fab__sub-circle"><text class="fab__sub-emoji">＋</text></view>
        <text class="fab__sub-text">新任务</text>
      </view>
      <view class="fab__sub fab__sub--3" @tap="goTags">
        <view class="fab__sub-circle"><text class="fab__sub-emoji">🏷️</text></view>
        <text class="fab__sub-text">标签</text>
      </view>
      <!-- 主按钮 -->
      <view class="fab__btn" @tap="fabOpen = !fabOpen">
        <text class="fab__btn-icon">＋</text>
      </view>
    </view>
  </view>

  <!-- 自TabBar定义 TabBar -->
  <TabBar current="checkin" />

</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShareAppMessage, onShareTimeline, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import PhotoPicker, { type SelectedPhoto } from '@/components/business/photo-picker/index.vue'
import HeatmapCalendar from '@/components/business/heatmap-calendar/index.vue'
import MoodPicker from '@/components/business/mood-picker/index.vue'
import TagPicker from '@/components/business/tag-picker/index.vue'
import { uploadImageToOss } from '@/utils/upload'
import { formatLatestTime } from '@/utils/format'
import EmptyStateCard from '@/components/business/empty-state-card'
import TabBar from '@/components/business/tab-bar/index.vue'
import MedalUnlockPopup from '@/components/business/medal-unlock-popup/index.vue'
import MendCheckinPopup from './modules/mend-checkin-popup/index.vue'
import {
  createCheckinTag,
  deleteCheckinTask,
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

onShareAppMessage(() => ({ title: '今日打卡' }))
onShareTimeline(() => ({ title: '今日打卡' }))

const tasks = ref<CheckinTask[]>([])
const loadFailed = ref(false)

const showCheckinPopup = ref(false)
const showTagPickerPopup = ref(false)
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

const deleteSwipeOptions = ref([
  { text: '删除', style: { background: 'var(--color-danger)', color: '#fff', fontSize: '26rpx' } }
])

// 已选标签（回显用）
const selectedTags = computed(() =>
  tags.value.filter((t) => checkinTagIds.value.includes(t.id))
)

function removeTag(tagId: Id) {
  checkinTagIds.value = checkinTagIds.value.filter((id) => id !== tagId)
}

function onSwipeDelete(index: number) {
  const task = tasks.value[index]
  if (task) handleDeleteTask(task)
}

const WEEK = ['日', '一', '二', '三', '四', '五', '六']
const now = new Date()
const today = now.toISOString().slice(0, 10)
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

function goTags() {
  uni.navigateTo({ url: '/pages/profile/tags/index?moduleType=CHECKIN' })
}

async function loadTasks() {
  const result = await fetchCheckinTasks()
  tasks.value = result.list || []
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

onPullDownRefresh(() => {
  reloadAll().finally(() => uni.stopPullDownRefresh())
})
</script>

<style scoped lang="scss">
.checkin-page {
  padding-bottom: var(--bottom-padding-with-tabbar);
}

/* ========== 顶栏 ========== */
.checkin-header {
  padding: var(--space-5) var(--space-6) var(--space-3);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkin-header__left {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.checkin-header__title {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
}

.checkin-header__count {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

/* 圆形进度 */
.checkin-header__ring {
  width: 88rpx;
  height: 88rpx;
  border-radius: var(--radius-full);
  background: var(--color-checkin-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx var(--color-checkin-shadow);
}

.checkin-header__ring-text {
  font-size: 24rpx;
  font-weight: var(--weight-bold);
  color: #fff;
}

/* ========== 卡片（热力图用） ========== */
.checkin-card {
  margin: var(--space-4) var(--space-4) 0;
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}

.checkin-card:last-child {
  margin-bottom: var(--space-3);
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

/* ========== 悬浮按钮 ========== */
.fab {
  position: fixed;
  right: 32rpx;
  bottom: calc(120rpx + var(--tabbar-total));
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

/* 主按钮 */
.fab__btn {
  position: fixed;
  right: 32rpx;
  bottom: calc(45rpx + var(--tabbar-total));
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: var(--color-checkin-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx var(--color-checkin-shadow-strong);
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

/* 子按钮 — 垂直弹出 */
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
  right: 32rpx;
  bottom: calc(165rpx + var(--tabbar-total));
  transition-delay: 0s;
}

.fab__sub--2 {
  right: 32rpx;
  bottom: calc(290rpx + var(--tabbar-total));
  transition-delay: 0.06s;
}

.fab__sub--3 {
  right: 32rpx;
  bottom: calc(415rpx + var(--tabbar-total));
  transition-delay: 0.12s;
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

.fab--open .fab__sub--3 {
  transition-delay: 0.18s;
}

.fab__sub-circle {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: var(--color-checkin-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx var(--color-checkin-shadow);
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
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  transition: all var(--motion-fast) var(--ease-standard);
}

.task-item--done {
  opacity: 0.6;
}

.task-item--pressed {
  background: var(--color-surface-hover);
}

.task-item__left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1;
  min-width: 0;
}

.task-item__status {
  width: 40rpx;
  height: 40rpx;
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
  font-size: 20rpx;
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
  margin-top: 2rpx;
  color: var(--color-text-muted);
  font-size: 20rpx;
}

.task-item__right {
  flex-shrink: 0;
  margin-left: var(--space-2);
}

.task-item__btn {
  padding: 8rpx 20rpx;
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
  font-size: 22rpx;
  font-weight: var(--weight-semibold);
}

.task-item__done-text {
  color: var(--color-checkin);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}

/* ========== 提示 ========== */
.checkin-card__hint {
  margin-left: auto;
  color: var(--color-text-muted);
  font-size: 18rpx;
}

/* ========== 打卡弹窗 ========== */
.checkin-popup {
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
  max-height: 65vh;
  border-radius: 28rpx 28rpx 0 0;
  overflow: hidden;
}

.checkin-popup__scroll {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-3) var(--space-4) 0;
  box-sizing: border-box;
}

.checkin-popup__head {
  text-align: center;
  margin-bottom: var(--space-2);
}

.checkin-popup__title {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-bold);
}

.checkin-popup__subtitle {
  margin-top: 2rpx;
  color: var(--color-text-muted);
  font-size: 16rpx;
}

/* 通用卡片 — 紧凑 */
.checkin-popup__card {
  background: var(--color-surface);
  border-radius: var(--radius-small);
  box-shadow: var(--shadow-card);
  padding: var(--space-2) var(--space-3);
  margin-bottom: var(--space-2);
}

/* 心情 */
.checkin-popup__card .mood-picker {
  margin-bottom: 0;
}

.checkin-popup__card .mood-picker__label {
  display: block;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
  margin-bottom: var(--space-1);
}

.checkin-popup__card .mood-picker__items {
  display: flex;
  gap: var(--space-1);
}

.checkin-popup__card .mood-picker__item {
  width: 52rpx;
  height: 52rpx;
}

.checkin-popup__card .mood-picker__emoji {
  font-size: 24rpx;
}

/* 心情 + 标签分割线 */
.checkin-popup__tag-divider {
  height: 1rpx;
  background: var(--color-divider);
  margin: var(--space-2) 0;
}

/* 标签 */
.checkin-popup__tag-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkin-popup__tag-bar-label {
  color: var(--color-text-secondary);
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
}

.checkin-popup__tag-bar-right {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.checkin-popup__tag-bar-value {
  color: var(--color-checkin);
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
}

.checkin-popup__tag-bar-arrow {
  color: var(--color-text-muted);
  font-size: 22rpx;
}

/* 已选标签药丸 */
.checkin-popup__pills {
  display: flex;
  flex-wrap: wrap;
  gap: 6rpx;
  margin-top: var(--space-2);
  padding-top: var(--space-2);
  border-top: 1rpx solid var(--color-divider);
}

.checkin-popup__pill {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 12rpx;
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
}

.checkin-popup__pill-text {
  color: var(--color-checkin);
  font-size: 18rpx;
  font-weight: var(--weight-medium);
  line-height: 1.2;
}

.checkin-popup__pill-x {
  color: var(--color-checkin);
  font-size: 16rpx;
  line-height: 1;
  opacity: 0.5;
  padding: 2rpx;
}

.checkin-popup__tag-row-arrow {
  color: var(--color-text-muted);
  font-size: 22rpx;
}

/* 备注 — 更紧凑 */
.checkin-popup__textarea {
  width: 100%;
  min-height: 48rpx;
  max-height: 80rpx;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-tiny);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-meta);
  box-sizing: border-box;
}

.checkin-popup__counter {
  text-align: right;
  margin-top: 2rpx;
  color: var(--color-text-muted);
  font-size: 12rpx;
}

.checkin-popup__counter.is-over {
  color: var(--color-danger);
}

/* 图片 — 紧凑行内排列 */
.checkin-popup__card--photo {
  padding: var(--space-1) var(--space-2);
}

.checkin-popup__card .photo-picker .section-shell {
  padding: 0;
  background: none;
  box-shadow: none;
  border-radius: 0;
}

/* 图片标题与描述同一行 */
.checkin-popup__card .photo-picker .section-head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.checkin-popup__card .photo-picker .section-copy {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex-direction: row;
}

.checkin-popup__card .photo-picker .section-copy__title {
  color: var(--color-text-secondary);
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
  line-height: 1;
}

.checkin-popup__card .photo-picker .section-copy__desc {
  color: var(--color-text-muted);
  font-size: 14rpx;
  line-height: 1;
}

.checkin-popup__card .photo-picker .photo-picker__count {
  margin-left: auto;
  color: var(--color-text-muted);
  font-size: 14rpx;
}

.checkin-popup__card .photo-picker .photo-picker__grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6rpx;
}

.checkin-popup__card .photo-picker .photo-picker__card {
  width: 100rpx;
  height: 100rpx;
  border-radius: var(--radius-tiny);
}

.checkin-popup__card .photo-picker .photo-picker__image {
  width: 100rpx;
  height: 100rpx;
  border-radius: var(--radius-tiny);
}

.checkin-popup__card .photo-picker .photo-picker__add {
  width: 100rpx;
  height: 100rpx;
  border-radius: var(--radius-tiny);
}

/* 操作按钮 */
.checkin-popup__actions {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  padding-bottom: calc(var(--space-3) + env(safe-area-inset-bottom) + 20rpx);
}

.checkin-popup__btn {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border-radius: var(--radius-full);
  font-size: var(--font-tiny);
  font-weight: var(--weight-semibold);
  transition: all var(--motion-fast) var(--ease-standard);
}

.checkin-popup__btn--cancel {
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
}

.checkin-popup__btn--confirm {
  background: var(--color-checkin-gradient);
  color: #fff;
}

.checkin-popup__btn--pressed {
  transform: scale(0.95);
  opacity: 0.85;
}

/* ========== 标签选择弹窗 ========== */
.checkin-tag-popup {
  background: var(--color-bg);
}

.checkin-tag-popup__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-5) 0;
}

.checkin-tag-popup__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.checkin-tag-popup__close {
  width: 48rpx;
  height: 48rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  font-size: var(--font-meta);
}

.checkin-tag-popup__scroll {
  padding: var(--space-4) var(--space-5);
  max-height: calc(65vh - 140rpx);
}

.checkin-tag-popup__foot {
  padding: 0 var(--space-5) calc(var(--space-5) + env(safe-area-inset-bottom));
}

.checkin-tag-popup__btn {
  text-align: center;
  padding: var(--space-3) 0;
  border-radius: var(--radius-full);
  background: var(--color-checkin-gradient);
  color: #fff;
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
  transition: all var(--motion-fast) var(--ease-standard);
}

.checkin-tag-popup__btn--pressed {
  transform: scale(0.95);
  opacity: 0.85;
}
</style>
