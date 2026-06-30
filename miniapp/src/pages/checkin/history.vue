<template>
  <view class="page-shell-safe checkin-history-page">
    <!-- 日期导航栏 -->
    <view class="checkin-datebar">
      <picker mode="date" :value="date" @change="onDateChange">
        <view class="checkin-datebar__date">
          <text class="checkin-datebar__date-icon">📅</text>
          <text class="checkin-datebar__date-text">{{ date }}</text>
        </view>
      </picker>
      <view class="checkin-datebar__nav">
        <view class="checkin-datebar__arrow" hover-class="checkin-datebar__arrow--pressed" @tap="prevDay">‹</view>
        <view class="checkin-datebar__today" hover-class="checkin-datebar__today--pressed" @tap="goToday">今天</view>
        <view class="checkin-datebar__arrow" hover-class="checkin-datebar__arrow--pressed" @tap="nextDay">›</view>
      </view>
    </view>

    <!-- 加载中骨架 -->
    <view v-if="loading" class="checkin-skeleton">
      <view class="checkin-skeleton__header" />
      <view class="checkin-skeleton__card" />
      <view class="checkin-skeleton__card" />
    </view>

    <!-- 主内容：时间线 -->
    <template v-if="!loading">
      <!-- 空状态（无打卡也无待补卡） -->
      <EmptyStateCard
        v-if="!detail && !incompleteTasks.length"
        title="这一天还没有打卡记录"
        description="切换日期看看过去哪一天完成过任务。"
        mode="history"
      />

      <!-- 有时间线数据 -->
      <template v-if="detail">
        <!-- 日头 -->
        <view class="timeline-hero">
          <view class="timeline-hero__dot" />
          <view class="timeline-hero__label">
            <text class="timeline-hero__day">{{ dayNum }}</text>
            <view class="timeline-hero__meta">
              <text class="timeline-hero__month">{{ monthLabel }} {{ weekdayLabel }}</text>
              <text class="timeline-hero__stats">
                打卡 {{ detail.totalCount }} 次 · {{ detail.taskCount }} 个任务
              </text>
            </view>
          </view>
        </view>

        <!-- 时间线 -->
        <view class="timeline">
          <view
            v-for="(record, idx) in detail.records"
            :key="record.id"
            class="timeline-item"
            :class="{ 'timeline-item--last': idx === detail.records.length - 1 }"
          >
            <!-- 左侧时间 + 竖线 -->
            <view class="timeline-track">
              <view class="timeline-track__dot" />
              <view
                v-if="idx < detail.records.length - 1"
                class="timeline-track__line"
              />
            </view>

            <!-- 右侧卡片 -->
            <view class="timeline-card">
              <!-- 卡片头部：时间 + 补卡标记 + 编辑按钮 -->
              <view class="timeline-card__head">
                <text class="timeline-card__time">{{ formatTime(record.checkedAt) }}</text>
                <view class="timeline-card__head-right">
                  <view v-if="record.isMend" class="timeline-card__mend-badge">补卡</view>
                  <view class="timeline-card__edit" hover-class="timeline-card__edit--pressed" @tap.stop="openEdit(record)">
                    <text class="timeline-card__edit-icon">✎</text>
                  </view>
                </view>
              </view>

              <!-- 任务名 -->
              <text class="timeline-card__title">{{ record.taskName }}</text>

              <!-- 心情 + 备注 -->
              <view v-if="record.mood || record.remark" class="timeline-card__body">
                <text v-if="record.mood" class="timeline-card__mood">{{ record.mood }}</text>
                <text v-if="record.remark" class="timeline-card__remark">{{ record.remark }}</text>
              </view>

              <!-- 标签 -->
              <view v-if="record.tagNames?.length" class="timeline-card__tags">
                <text
                  v-for="tag in record.tagNames"
                  :key="tag"
                  class="timeline-card__tag"
                >#{{ tag }}</text>
              </view>

              <!-- 图片 -->
              <view v-if="record.mediaPaths?.length" class="timeline-card__photos">
                <image
                  v-for="(path, pidx) in record.mediaPaths.slice(0, 9)"
                  :key="pidx"
                  :src="resolveImage(path)"
                  mode="aspectFill"
                  class="timeline-card__photo"
                  @tap="previewImage(record.mediaPaths!, pidx)"
                />
              </view>
            </view>
          </view>
        </view>
      </template>

      <!-- 待补卡 -->
      <view v-if="incompleteTasks.length && canMend" class="mend-section">
        <view class="mend-section__divider">
          <text class="mend-section__divider-text">待补卡</text>
        </view>
        <view class="mend-section__tasks">
          <view
            v-for="task in incompleteTasks"
            :key="task.id"
            class="mend-card"
            hover-class="mend-card--pressed"
            @tap="openMendPopup"
          >
            <view class="mend-card__left">
              <view class="mend-card__icon">○</view>
              <view class="mend-card__info">
                <text class="mend-card__name">{{ task.name }}</text>
                <text v-if="task.description" class="mend-card__desc">{{ task.description }}</text>
              </view>
            </view>
            <view class="mend-card__btn">
              <text class="mend-card__btn-text">补卡</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部间距 -->
      <view class="checkin-history-spacer" />
    </template>

    <!-- 编辑打卡弹窗 -->
    <u-popup v-model="showEditPopup" mode="bottom" border-radius="28" :safe-area-inset-bottom="false">
      <view class="checkin-edit-popup">
        <scroll-view scroll-y class="checkin-edit-popup__scroll">
          <view class="checkin-edit-popup__head">
            <view class="checkin-edit-popup__title">编辑打卡</view>
            <view class="checkin-edit-popup__subtitle">{{ editTarget?.taskName }} · {{ displayDate }}</view>
          </view>

          <!-- 心情 -->
          <view class="checkin-edit-popup__card">
            <MoodPicker v-model="editMood" />
          </view>

          <!-- 标签 -->
          <view class="checkin-edit-popup__card">
            <view class="checkin-edit-popup__tag-row" @tap="showEditTagPicker = true">
              <text class="checkin-edit-popup__tag-row-label">🏷️ 标签</text>
              <view class="checkin-edit-popup__tag-row-right">
                <text class="checkin-edit-popup__tag-row-value">
                  {{ editTagIds.length ? `已选 ${editTagIds.length} 个` : '选择标签' }}
                </text>
                <text class="checkin-edit-popup__tag-row-arrow">›</text>
              </view>
            </view>
          </view>

          <!-- 备注 -->
          <view class="checkin-edit-popup__card">
            <textarea
              v-model="editRemark"
              class="checkin-edit-popup__textarea"
              placeholder="一句话记录今天的感受..."
              :maxlength="50"
            />
            <view class="checkin-edit-popup__counter" :class="{ 'is-over': editRemark.length > 50 }">
              {{ editRemark.length }}/50
            </view>
          </view>

          <!-- 图片 -->
          <view class="checkin-edit-popup__card">
            <PhotoPicker v-model="editPhotos" :max-count="9" @retry="retryEditPhotoUpload" />
          </view>
        </scroll-view>

        <view class="checkin-edit-popup__actions">
          <view class="checkin-edit-popup__btn checkin-edit-popup__btn--cancel" hover-class="checkin-edit-popup__btn--pressed" @tap="showEditPopup = false">
            <text>取消</text>
          </view>
          <view class="checkin-edit-popup__btn checkin-edit-popup__btn--confirm" hover-class="checkin-edit-popup__btn--pressed" @tap="confirmEdit">
            <text>{{ editSubmitting ? '保存中' : '保存修改' }}</text>
          </view>
        </view>
      </view>
    </u-popup>

    <!-- 标签选择弹窗 -->
    <u-popup v-model="showEditTagPicker" mode="bottom" border-radius="28">
      <view class="checkin-edit-tag-popup" style="max-height: 65vh;">
        <view class="checkin-edit-tag-popup__head">
          <text class="checkin-edit-tag-popup__title">选择标签</text>
          <text class="checkin-edit-tag-popup__close" @tap="showEditTagPicker = false">✕</text>
        </view>
        <scroll-view scroll-y class="checkin-edit-tag-popup__scroll">
          <TagPicker :tags="editTags" v-model="editTagIds" @create-tag="handleEditCreateTag" />
        </scroll-view>
        <view class="checkin-edit-tag-popup__foot">
          <view class="checkin-edit-tag-popup__btn" hover-class="checkin-edit-tag-popup__btn--pressed" @tap="showEditTagPicker = false">
            <text>确定</text>
          </view>
        </view>
      </view>
    </u-popup>

    <MendCheckinPopup
      v-model="showMendPopup"
      :mend-date="date"
      :display-date="displayDate"
      :tasks="incompleteTasks"
      :completed-ids="completedTaskIds"
      :remaining="mendRemaining"
      @success="loadHistory"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import MoodPicker from '@/components/business/mood-picker/index.vue'
import PhotoPicker from '@/components/business/photo-picker/index.vue'
import type { SelectedPhoto } from '@/components/business/photo-picker/index.vue'
import TagPicker from '@/components/business/tag-picker/index.vue'
import MendCheckinPopup from './modules/mend-checkin-popup/index.vue'
import { fetchCheckinDayTimeline, fetchCheckinTasks, fetchCheckinTags, fetchMendRemaining, updateCheckinRecord } from '@/api/checkin'
import { uploadImageToOss } from '@/utils/upload'
import { OSS_BASE_URL } from '@/config/app'
import type { CheckinDayDetail, CheckinRecordItem, CheckinTag, CheckinTask } from '@/types/domain'

const loading = ref(false)
const detail = ref<CheckinDayDetail | null>(null)
const allTasks = ref<CheckinTask[]>([])
const mendRemaining = ref(0)
const showMendPopup = ref(false)
const date = ref(new Date().toISOString().slice(0, 10))

// 编辑状态
const showEditPopup = ref(false)
const showEditTagPicker = ref(false)
const editTarget = ref<CheckinRecordItem | null>(null)
const editMood = ref<string | undefined>(undefined)
const editRemark = ref('')
const editTagIds = ref<string[]>([])
const editPhotos = ref<SelectedPhoto[]>([])
const editSubmitting = ref(false)
const editTags = ref<CheckinTag[]>([])

const today = new Date().toISOString().slice(0, 10)

const WEEKDAY_CN = ['日', '一', '二', '三', '四', '五', '六']

const weekdayLabel = computed(() => {
  const d = new Date(date.value)
  return `周${WEEKDAY_CN[d.getDay()]}`
})

const monthLabel = computed(() => {
  const d = new Date(date.value)
  return `${d.getMonth() + 1}月`
})

const dayNum = computed(() => {
  return Number(date.value.slice(8, 10))
})

const displayDate = computed(() => {
  const d = new Date(date.value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const canMend = computed(() => {
  if (date.value >= today) return false
  const d = new Date(date.value)
  const limit = new Date()
  limit.setDate(limit.getDate() - 7)
  return d >= limit
})

const completedTaskIds = computed(() => {
  return new Set(detail.value?.records.map((r) => r.taskId) || [])
})

const incompleteTasks = computed(() => {
  return allTasks.value.filter(
    (t) => !completedTaskIds.value.has(Number(t.id)) && (!t.startDate || t.startDate <= date.value)
  )
})

function formatTime(iso: string) {
  const d = new Date(iso)
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}

function resolveImage(path: string) {
  return path.startsWith('http') ? path : `${OSS_BASE_URL}/${path}`
}

function previewImage(urls: string[], current: number) {
  const resolved = urls.map((p) => resolveImage(p))
  uni.previewImage({ urls: resolved, current })
}

async function loadHistory() {
  loading.value = true
  try {
    const [timeline, tasks, remaining] = await Promise.all([
      fetchCheckinDayTimeline(date.value),
      fetchCheckinTasks(),
      fetchMendRemaining()
    ])
    detail.value = timeline
    allTasks.value = tasks.list || []
    mendRemaining.value = remaining
  } catch {
    detail.value = null
    allTasks.value = []
    mendRemaining.value = 0
  } finally {
    loading.value = false
  }
}

function openMendPopup() {
  showMendPopup.value = true
}

function openEdit(record: CheckinRecordItem) {
  editTarget.value = record
  editMood.value = record.mood || undefined
  editRemark.value = record.remark || ''
  editTagIds.value = (record.tagIds || []).map(String)
  editPhotos.value = (record.mediaPaths || []).map((p) => ({
    localPath: p,
    ossPath: p,
    status: 'done' as const
  }))
  // 加载标签列表供选择
  fetchCheckinTags().then((tags) => {
    editTags.value = tags
  }).catch(() => {
    editTags.value = []
  })
  showEditPopup.value = true
}

async function confirmEdit() {
  if (!editTarget.value) return
  editSubmitting.value = true
  try {
    // 上传未上传的图片
    for (let i = 0; i < editPhotos.value.length; i++) {
      const photo = editPhotos.value[i]
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

    const mediaList = editPhotos.value
      .filter((item) => item.ossPath)
      .map((item, index) => ({
        mediaType: 'IMAGE' as const,
        filePath: item.ossPath as string,
        sortOrder: index + 1
      }))

    await updateCheckinRecord(editTarget.value.id, {
      remark: editRemark.value || undefined,
      mood: editMood.value,
      tagIds: editTagIds.value.length > 0 ? editTagIds.value.map(Number) : [],
      mediaList: mediaList.length > 0 ? mediaList : []
    })

    showEditPopup.value = false
    uni.$feedback.success('修改已保存')
    await loadHistory()
  } catch (error) {
    uni.$feedback.error(error, undefined, '保存失败')
  } finally {
    editSubmitting.value = false
  }
}

async function handleEditCreateTag(name: string) {
  try {
    const { createCheckinTag } = await import('@/api/checkin')
    const tag = await createCheckinTag({ name })
    editTags.value.push(tag)
    editTagIds.value.push(String(tag.id))
    uni.$feedback.success('标签已创建')
  } catch (error) {
    uni.$feedback.error(error, undefined, '创建标签失败')
  }
}

async function retryEditPhotoUpload(index: number) {
  const photo = editPhotos.value[index]
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

function onDateChange(e: { detail: { value: string } }) {
  date.value = e.detail.value
  loadHistory()
}

function prevDay() {
  const d = new Date(date.value)
  d.setDate(d.getDate() - 1)
  date.value = d.toISOString().slice(0, 10)
  loadHistory()
}

function nextDay() {
  const d = new Date(date.value)
  d.setDate(d.getDate() + 1)
  date.value = d.toISOString().slice(0, 10)
  loadHistory()
}

function goToday() {
  date.value = today
  loadHistory()
}

onLoad((query) => {
  if (query?.date) {
    date.value = query.date
  }
  loadHistory()
})
</script>

<style scoped lang="scss">
.checkin-history-page {
  padding-bottom: var(--bottom-padding);
}

/* ========== 日期导航栏 ========== */
.checkin-datebar {
  margin: var(--space-3) var(--space-4) 0;
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkin-datebar__date {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
}

.checkin-datebar__date-icon {
  font-size: 24rpx;
  line-height: 1;
}

.checkin-datebar__date-text {
  color: var(--color-text-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

.checkin-datebar__nav {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.checkin-datebar__arrow {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
  font-size: 32rpx;
  transition: all var(--motion-fast) var(--ease-standard);
}

.checkin-datebar__arrow--pressed {
  transform: scale(0.9);
  opacity: 0.6;
}

.checkin-datebar__today {
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  color: var(--color-checkin);
  font-size: 22rpx;
  font-weight: var(--weight-semibold);
  transition: all var(--motion-fast) var(--ease-standard);
}

.checkin-datebar__today--pressed {
  transform: scale(0.92);
  opacity: 0.75;
}

/* ========== 骨架屏 ========== */
.checkin-skeleton {
  margin: var(--space-4) var(--space-4) 0;
}

.checkin-skeleton__header {
  height: 80rpx;
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  margin-bottom: var(--space-4);
  animation: pulse 1.5s ease-in-out infinite;
}

.checkin-skeleton__card {
  height: 200rpx;
  border-radius: var(--radius-large);
  background: var(--color-surface-soft);
  margin-bottom: var(--space-3);
  animation: pulse 1.5s ease-in-out infinite;
  animation-delay: 0.2s;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* ========== 日头 ========== */
.timeline-hero {
  margin: var(--space-5) var(--space-4) 0;
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding-left: 4rpx;
}

.timeline-hero__dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: var(--radius-full);
  background: var(--color-checkin);
  flex-shrink: 0;
  box-shadow: 0 0 0 8rpx var(--color-checkin-soft);
}

.timeline-hero__label {
  display: flex;
  align-items: flex-end;
  gap: var(--space-3);
}

.timeline-hero__day {
  color: var(--color-text-primary);
  font-size: var(--font-hero);
  font-weight: var(--weight-bold);
  line-height: 1;
}

.timeline-hero__meta {
  display: flex;
  flex-direction: column;
  gap: 2rpx;
  padding-bottom: 4rpx;
}

.timeline-hero__month {
  color: var(--color-checkin);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

.timeline-hero__stats {
  color: var(--color-text-muted);
  font-size: 20rpx;
}

/* ========== 时间线 ========== */
.timeline {
  margin: 0 var(--space-4);
  padding-left: 20rpx;
  padding-top: var(--space-4);
}

.timeline-item {
  display: flex;
  gap: var(--space-3);
}

.timeline-item--last {
  margin-bottom: 0;
}

/* 左侧轨道 */
.timeline-track {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 20rpx;
  flex-shrink: 0;
  padding-top: 10rpx;
}

.timeline-track__dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: var(--radius-full);
  background: var(--color-checkin);
  flex-shrink: 0;
  border: 4rpx solid var(--color-checkin-soft);
  box-sizing: content-box;
  z-index: 1;
}

.timeline-track__line {
  width: 2rpx;
  flex: 1;
  background: linear-gradient(180deg, var(--color-checkin-soft) 0%, var(--color-divider) 100%);
  min-height: 40rpx;
}

/* 右侧卡片 */
.timeline-card {
  flex: 1;
  min-width: 0;
  padding: var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  margin-bottom: var(--space-4);
  transition: all var(--motion-fast) var(--ease-standard);
  position: relative;
  overflow: hidden;
}

/* 卡片顶部装饰线条 */
.timeline-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3rpx;
  background: var(--color-checkin-gradient);
  opacity: 0.4;
}

/* 卡片头部 */
.timeline-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.timeline-card__time {
  color: var(--color-text-muted);
  font-size: 20rpx;
  font-weight: var(--weight-medium);
}

.timeline-card__mend-badge {
  padding: 2rpx 14rpx;
  border-radius: var(--radius-full);
  background: var(--color-warning);
  color: #fff;
  font-size: 18rpx;
  font-weight: var(--weight-semibold);
}

/* 任务名 */
.timeline-card__title {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-bold);
  line-height: var(--leading-snug);
}

/* 心情 + 备注 */
.timeline-card__body {
  margin-top: var(--space-2);
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
}

.timeline-card__mood {
  font-size: 32rpx;
  line-height: 1.4;
  flex-shrink: 0;
}

.timeline-card__remark {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  line-height: var(--leading-relaxed);
  flex: 1;
  min-width: 0;
}

/* 标签 */
.timeline-card__tags {
  margin-top: var(--space-2);
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.timeline-card__tag {
  padding: 4rpx 14rpx;
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  color: var(--color-checkin);
  font-size: 18rpx;
  font-weight: var(--weight-medium);
}

/* 图片 */
.timeline-card__photos {
  margin-top: var(--space-3);
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8rpx;
  border-radius: var(--radius-small);
  overflow: hidden;
}

.timeline-card__photo {
  width: 100%;
  height: 160rpx;
  background: var(--color-surface-soft);
}

/* ========== 补卡区 ========== */
.mend-section {
  margin: var(--space-2) var(--space-4) 0;
}

.mend-section__divider {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.mend-section__divider::before,
.mend-section__divider::after {
  content: '';
  flex: 1;
  height: 1rpx;
  background: var(--color-divider);
}

.mend-section__divider-text {
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  font-weight: var(--weight-medium);
  white-space: nowrap;
}

.mend-section__tasks {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.mend-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  transition: all var(--motion-fast) var(--ease-standard);
}

.mend-card--pressed {
  transform: scale(0.97);
  opacity: 0.85;
}

.mend-card__left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1;
  min-width: 0;
}

.mend-card__icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: var(--radius-full);
  border: 3rpx solid var(--color-border-strong);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--color-text-muted);
  font-size: 22rpx;
}

.mend-card__info {
  flex: 1;
  min-width: 0;
}

.mend-card__name {
  color: var(--color-text-primary);
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mend-card__desc {
  margin-top: 4rpx;
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mend-card__btn {
  flex-shrink: 0;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
}

.mend-card__btn-text {
  color: var(--color-checkin);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

/* ========== 编辑按钮 ========== */
.timeline-card__head-right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.timeline-card__edit {
  width: 44rpx;
  height: 44rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--motion-fast) var(--ease-standard);
}

.timeline-card__edit--pressed {
  transform: scale(0.85);
  opacity: 0.6;
}

.timeline-card__edit-icon {
  color: var(--color-text-muted);
  font-size: 24rpx;
  line-height: 1;
}

/* ========== 编辑弹窗 ========== */
.checkin-edit-popup {
  background: var(--color-bg);
}

.checkin-edit-popup__scroll {
  padding: var(--space-5);
  max-height: 80vh;
  box-sizing: border-box;
}

.checkin-edit-popup__head {
  text-align: center;
  margin-bottom: var(--space-5);
}

.checkin-edit-popup__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.checkin-edit-popup__subtitle {
  margin-top: var(--space-1);
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.checkin-edit-popup__card {
  background: var(--color-surface);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-card);
  padding: var(--space-4);
  margin-bottom: var(--space-3);
}

/* 标签行 */
.checkin-edit-popup__tag-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkin-edit-popup__tag-row-label {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}

.checkin-edit-popup__tag-row-right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.checkin-edit-popup__tag-row-value {
  color: var(--color-checkin);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
}

.checkin-edit-popup__tag-row-arrow {
  color: var(--color-text-muted);
  font-size: 28rpx;
}

/* 备注 */
.checkin-edit-popup__textarea {
  width: 100%;
  min-height: 88rpx;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-medium);
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: var(--font-body);
  box-sizing: border-box;
}

.checkin-edit-popup__counter {
  text-align: right;
  margin-top: var(--space-1);
  color: var(--color-text-muted);
  font-size: var(--font-tiny);
}

.checkin-edit-popup__counter.is-over {
  color: var(--color-danger);
}

/* 操作按钮 */
.checkin-edit-popup__actions {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5) calc(var(--space-5) + env(safe-area-inset-bottom));
}

.checkin-edit-popup__btn {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: var(--radius-full);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
  transition: all var(--motion-fast) var(--ease-standard);
}

.checkin-edit-popup__btn--cancel {
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
}

.checkin-edit-popup__btn--confirm {
  background: var(--color-checkin-gradient);
  color: #fff;
}

.checkin-edit-popup__btn--pressed {
  transform: scale(0.95);
  opacity: 0.85;
}

/* 标签选择弹窗 */
.checkin-edit-tag-popup {
  background: var(--color-bg);
}

.checkin-edit-tag-popup__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-5) 0;
}

.checkin-edit-tag-popup__title {
  color: var(--color-text-primary);
  font-size: var(--font-section);
  font-weight: var(--weight-bold);
}

.checkin-edit-tag-popup__close {
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

.checkin-edit-tag-popup__scroll {
  padding: var(--space-4) var(--space-5);
  max-height: calc(65vh - 140rpx);
}

.checkin-edit-tag-popup__foot {
  padding: 0 var(--space-5) calc(var(--space-5) + env(safe-area-inset-bottom));
}

.checkin-edit-tag-popup__btn {
  text-align: center;
  padding: var(--space-3) 0;
  border-radius: var(--radius-full);
  background: var(--color-checkin-gradient);
  color: #fff;
  font-size: var(--font-body);
  font-weight: var(--weight-semibold);
  transition: all var(--motion-fast) var(--ease-standard);
}

.checkin-edit-tag-popup__btn--pressed {
  transform: scale(0.95);
  opacity: 0.85;
}

/* ========== 间距 ========== */
.checkin-history-spacer {
  height: 48rpx;
}
</style>
