<template>
  <view class="page-shell-safe">
    <view class="section-shell">
      <view class="section-head">
        <view class="section-copy">
          <view class="section-copy__title">🏆 我的成就</view>
          <view class="section-copy__desc">
            已解锁 {{ unlockedCount }} / {{ medals.length }} 枚勋章
          </view>
        </view>
      </view>
    </view>

    <view v-for="group in groupedMedals" :key="group.category" class="page-section section-shell">
      <view class="medal-group__title">{{ group.label }}</view>
      <view class="medal-grid">
        <MedalCard
          v-for="medal in group.items"
          :key="medal.id"
          :medal="medal"
          @tap="onMedalTap"
        />
      </view>
    </view>

    <!-- 勋章详情弹窗 -->
    <u-popup v-model="showDetail" mode="bottom" border-radius="28" :safe-area-inset-bottom="true">
      <view class="medal-detail">
        <view class="medal-detail__icon-wrap">
          <text class="medal-detail__icon">{{ selectedMedal?.icon }}</text>
        </view>
        <view class="medal-detail__name">{{ selectedMedal?.name }}</view>
        <view class="medal-detail__desc">{{ selectedMedal?.description }}</view>
        <view v-if="selectedMedal?.unlocked" class="medal-detail__status unlocked">
          ✅ 已解锁于 {{ selectedMedal?.unlockedAt?.slice(0, 10) }}
        </view>
        <view v-else class="medal-detail__status locked">🔒 尚未解锁</view>
        <view class="medal-detail__difficulty">
          难度：
          <text v-for="i in (selectedMedal?.difficulty || 0)" :key="i">⭐</text>
        </view>
        <u-button class="medal-detail__close" shape="circle" plain :hair-line="false" @click="showDetail = false">
          关闭
        </u-button>
      </view>
    </u-popup>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import MedalCard from '@/components/business/medal-card/index.vue'
import { fetchMedals } from '@/api/checkin'
import type { Medal } from '@/types/domain'

const medals = ref<Medal[]>([])
const showDetail = ref(false)
const selectedMedal = ref<Medal | null>(null)

const CATEGORY_MAP: Record<string, string> = {
  STREAK: '连续坚持',
  TOTAL: '累计总量',
  TIME: '时间习惯',
  SPECIAL: '特殊成就'
}

const unlockedCount = computed(() => medals.value.filter((m) => m.unlocked).length)

const groupedMedals = computed(() => {
  const groups: Record<string, Medal[]> = {}
  for (const medal of medals.value) {
    if (!groups[medal.category]) groups[medal.category] = []
    groups[medal.category].push(medal)
  }
  return Object.entries(groups).map(([category, items]) => ({
    category,
    label: CATEGORY_MAP[category] || category,
    items
  }))
})

function onMedalTap(medal: Medal) {
  selectedMedal.value = medal
  showDetail.value = true
}

onLoad(async () => {
  try {
    medals.value = await fetchMedals()
  } catch {
    medals.value = []
  }
})
</script>

<style scoped lang="scss">
.medal-group__title {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
  margin-bottom: var(--space-3);
}

.medal-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
}

.medal-detail {
  padding: var(--space-6) var(--space-5) calc(var(--space-6) + env(safe-area-inset-bottom));
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.medal-detail__icon-wrap {
  width: 128rpx;
  height: 128rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--color-checkin-soft);
  margin-bottom: var(--space-4);
}

.medal-detail__icon {
  font-size: 72rpx;
}

.medal-detail__name {
  color: var(--color-text-primary);
  font-size: var(--font-title);
  font-weight: var(--weight-bold);
  margin-bottom: var(--space-2);
}

.medal-detail__desc {
  color: var(--color-text-secondary);
  font-size: var(--font-body);
  margin-bottom: var(--space-3);
}

.medal-detail__status {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  font-size: var(--font-meta);
  margin-bottom: var(--space-3);
}

.medal-detail__status.unlocked {
  background: rgba(52, 199, 89, 0.12);
  color: var(--color-success);
}

.medal-detail__status.locked {
  background: var(--color-surface-soft);
  color: var(--color-text-muted);
}

.medal-detail__difficulty {
  color: var(--color-text-secondary);
  font-size: var(--font-meta);
  margin-bottom: var(--space-5);
}

.medal-detail__close {
  width: 100%;
}
</style>
