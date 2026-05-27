<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">我的打卡任务</view>
          <view class="section-copy__desc">先建任务，再在这里完成今日打卡，并查看指定日期的历史记录。</view>
        </view>
      </view>

      <view class="action-grid-2">
        <u-button type="primary" shape="circle" color="linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)" @click="goCreate">
          创建打卡任务
        </u-button>
        <u-button shape="circle" plain @click="reloadAll">刷新</u-button>
      </view>
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">今日进度</view>
          <view class="section-copy__desc">今天已经完成 {{ todayDoneIds.size }} / {{ tasks.length }} 个任务。</view>
        </view>
        <view class="checkin-date-chip">{{ today }}</view>
      </view>

      <view v-if="tasks.length" class="list-stack">
        <u-swipe-action
          v-for="task in tasks"
          :key="task.id"
          :index="task.id"
          :options="swipeOptions"
          btn-width="140"
          @click="handleSwipeAction"
        >
          <view class="list-card checkin-task-card">
            <view class="list-card__head">
              <view>
                <view class="list-card__title">{{ task.name }}</view>
                <view class="list-card__meta">{{ task.description || '未填写任务描述' }}</view>
              </view>
              <view class="checkin-count">{{ task.totalCount }} 次</view>
            </view>

            <view class="list-card__meta">
              开始日期：{{ task.startDate || '--' }} | 最近完成：{{ task.latestCheckedAt || '还没有打卡记录' }}
            </view>

            <view class="action-grid-2 checkin-actions">
              <u-button
                shape="circle"
                :plain="todayDoneIds.has(task.id)"
                :hair-line="false"
                :type="todayDoneIds.has(task.id) ? 'default' : 'primary'"
                :color="todayDoneIds.has(task.id) ? undefined : 'linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%)'"
                @click="handleCheckin(task)"
              >
                {{ todayDoneIds.has(task.id) ? '今天已打卡' : '今日打卡' }}
              </u-button>
              <u-button shape="circle" plain :hair-line="false" @click="viewTaskHistory(task)">
                查看历史
              </u-button>
            </view>
          </view>
        </u-swipe-action>
      </view>
      <EmptyStateCard
        v-else
        class="page-section"
        :title="loadFailed ? '打卡数据加载失败' : '还没有打卡任务'"
        :description="
          loadFailed
            ? '这次没有拿到真实接口数据，请检查后端、登录状态或网络连接。'
            : '先创建一条任务，后面就能在这里直接打卡。'
        "
      />
    </view>

    <view class="page-section section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">历史记录</view>
          <view class="section-copy__desc">按日期查看当天完成了哪些任务。</view>
        </view>
      </view>

      <view class="checkin-history-toolbar">
        <picker mode="date" :value="historyDate" @change="onHistoryDateChange">
          <view class="checkin-history-toolbar__date">{{ historyDate }}</view>
        </picker>
        <u-button shape="circle" plain size="mini" :hair-line="false" @click="historyDate = today; loadHistory()">
          回到今天
        </u-button>
      </view>

      <view v-if="historyItems.length" class="list-stack">
        <view v-for="item in filteredHistoryItems" :key="item.id" class="list-card">
          <view class="list-card__head">
            <view>
              <view class="list-card__title">{{ item.name }}</view>
              <view class="list-card__meta">{{ item.description || '未填写任务描述' }}</view>
            </view>
            <view class="history-tag">已完成</view>
          </view>
          <view class="list-card__meta">累计打卡：{{ item.totalCount }} 次</view>
        </view>
      </view>
      <EmptyStateCard
        v-else
        title="这一天还没有打卡记录"
        description="切换日期看看过去哪一天完成过任务。"
        mode="history"
      />
    </view>

    <u-popup v-model="showTaskHistoryPopup" mode="bottom" border-radius="28" :safe-area-inset-bottom="true">
      <view class="task-history-popup">
        <view class="task-history-popup__head">
          <view class="task-history-popup__title">{{ focusedTask?.name || '任务历史' }}</view>
          <view class="task-history-popup__subtitle">当前先展示任务最近一次完成时间和累计次数。</view>
        </view>

        <view v-if="focusedTask" class="list-card">
          <view class="list-card__meta">任务描述：{{ focusedTask.description || '未填写任务描述' }}</view>
          <view class="list-card__meta">开始日期：{{ focusedTask.startDate || '--' }}</view>
          <view class="list-card__meta">累计打卡：{{ focusedTask.totalCount }} 次</view>
          <view class="list-card__meta">最近完成：{{ focusedTask.latestCheckedAt || '还没有打卡记录' }}</view>
        </view>

        <u-button class="task-history-popup__action" shape="circle" plain :hair-line="false" @click="showTaskHistoryPopup = false">
          关闭
        </u-button>
      </view>
    </u-popup>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyStateCard from '@/components/business/empty-state-card'
import { deleteCheckinTask, fetchCheckinDayDetail, fetchCheckinTasks, submitCheckin } from '@/api/checkin'
import type { CheckinTask, Id } from '@/types/domain'

const tasks = ref<CheckinTask[]>([])
const historyItems = ref<CheckinTask[]>([])
const loadFailed = ref(false)
const focusedTask = ref<CheckinTask>()
const showTaskHistoryPopup = ref(false)

const today = new Date().toISOString().slice(0, 10)
const historyDate = ref(today)
const todayDoneIds = ref(new Set<Id>())

const swipeOptions = [
  {
    text: '删除',
    style: {
      backgroundColor: '#d35d56',
      color: 'var(--color-bg)'
    }
  }
]

const filteredHistoryItems = computed(() => historyItems.value)

function goCreate() {
  uni.navigateTo({ url: '/pages/checkin/editor' })
}

function onHistoryDateChange(event: { detail: { value: string } }) {
  historyDate.value = event.detail.value
  loadHistory()
}

function viewTaskHistory(task: CheckinTask) {
  focusedTask.value = task
  showTaskHistoryPopup.value = true
}

async function loadTasks() {
  tasks.value = await fetchCheckinTasks()
}

async function loadTodayStatus() {
  const items = await fetchCheckinDayDetail(today)
  todayDoneIds.value = new Set(items.map((item) => item.id))
}

async function loadHistory() {
  try {
    historyItems.value = await fetchCheckinDayDetail(historyDate.value)
  } catch (error) {
    historyItems.value = []
    uni.$feedback.error(error, undefined, '加载历史打卡失败')
  }
}

async function reloadAll() {
  try {
    await Promise.all([loadTasks(), loadTodayStatus(), loadHistory()])
    loadFailed.value = false
  } catch (error) {
    tasks.value = []
    historyItems.value = []
    todayDoneIds.value = new Set()
    loadFailed.value = true
    uni.$feedback.error(error, undefined, '加载打卡数据失败')
  }
}

async function handleCheckin(task: CheckinTask) {
  if (todayDoneIds.value.has(task.id)) {
    uni.$feedback.info('这个任务今天已经打过卡了')
    return
  }

  try {
    await submitCheckin(task.id, {
      checkinDate: today
    })
    uni.$feedback.success('打卡成功')
    await reloadAll()
  } catch (error) {
    uni.$feedback.error(error, undefined, '打卡失败')
  }
}

async function removeTask(taskId: Id) {
  const result = await uni.showModal({
    title: '确认删除',
    content: '删除任务后，相关历史打卡记录也会一起删除。'
  })
  if (!result.confirm) return

  try {
    await deleteCheckinTask(taskId)
    uni.$feedback.success('任务已删除')
    await reloadAll()
  } catch (error) {
    uni.$feedback.error(error, undefined, '删除失败')
  }
}

function handleSwipeAction(taskId: Id) {
  removeTask(taskId)
}

onShow(() => {
  reloadAll()
})
</script>

<style scoped lang="scss">
.checkin-task-card {
  margin-bottom: 0;
}

.checkin-count {
  border-radius: 999rpx;
  background: #edf4ef;
  padding: 10rpx 20rpx;
  color: #4b6b57;
  font-size: 22rpx;
}

.checkin-date-chip,
.history-tag {
  padding: 10rpx 18rpx;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
}

.checkin-actions {
  margin-top: 16rpx;
}

.checkin-history-toolbar {
  margin-top: 16rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.checkin-history-toolbar__date {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 68rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: var(--color-surface-soft);
  color: var(--color-text-primary);
  font-size: 24rpx;
  font-weight: 600;
}

.task-history-popup {
  padding: 28rpx 24rpx calc(28rpx + env(safe-area-inset-bottom));
  background: var(--color-bg);
}

.task-history-popup__head {
  text-align: center;
}

.task-history-popup__title {
  color: var(--color-text-primary);
  font-size: 34rpx;
  font-weight: 700;
}

.task-history-popup__subtitle {
  margin-top: 8rpx;
  color: var(--color-text-muted);
  font-size: 24rpx;
}

.task-history-popup__action {
  margin-top: 20rpx;
}
</style>
