<template>
  <view class="app-tabbar-wrap">
    <view class="app-tabbar">
      <!-- 首页 -->
      <view
        class="app-tabbar__pill"
        :class="{ 'app-tabbar__pill--active': current === 'home' }"
        hover-class="app-tabbar__pill--pressed"
        :hover-stay-time="120"
        @tap="goHome"
      >
        <text class="app-tabbar__emoji">{{ current === 'home' ? '🏠' : '🏡' }}</text>
        <text class="app-tabbar__text">首页</text>
      </view>

      <!-- 日记 -->
      <view
        class="app-tabbar__pill"
        :class="{ 'app-tabbar__pill--active': current === 'diary' }"
        hover-class="app-tabbar__pill--pressed"
        :hover-stay-time="120"
        @tap="goDiary"
      >
        <text class="app-tabbar__emoji">{{ current === 'diary' ? '📖' : '📕' }}</text>
        <text class="app-tabbar__text">日记</text>
      </view>

      <!-- 打卡 -->
      <view
        class="app-tabbar__pill"
        :class="{ 'app-tabbar__pill--active': current === 'checkin' }"
        hover-class="app-tabbar__pill--pressed"
        :hover-stay-time="120"
        @tap="goCheckin"
      >
        <text class="app-tabbar__emoji">{{ current === 'checkin' ? '✅' : '✓' }}</text>
        <text class="app-tabbar__text">打卡</text>
      </view>

      <!-- 我的 -->
      <view
        class="app-tabbar__pill"
        :class="{ 'app-tabbar__pill--active': current === 'profile' }"
        hover-class="app-tabbar__pill--pressed"
        :hover-stay-time="120"
        @tap="goProfile"
      >
        <text class="app-tabbar__emoji">{{ current === 'profile' ? '👤' : '🧑' }}</text>
        <text class="app-tabbar__text">我的</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  current: 'home' | 'diary' | 'checkin' | 'profile'
}>()

function goHome() {
  uni.switchTab({ url: '/pages/home/index' })
}

function goDiary() {
  uni.switchTab({ url: '/pages/diary/index' })
}

function goCheckin() {
  uni.switchTab({ url: '/pages/checkin/index' })
}

function goProfile() {
  uni.switchTab({ url: '/pages/profile/index' })
}
</script>

<style scoped lang="scss">
/* ============================================================
 * 胶囊 TabBar — 浮动式
 * 所有选项统一：emoji 在上、文字在下（垂直布局）
 * 发现选中时暖陶色渐变填充
 * ========================================================= */

/* 外层容器：固定底部，背景遮住下方内容，留出安全区 */
.app-tabbar-wrap {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background: var(--color-bg);
  padding: var(--space-1) var(--space-5);
  padding-bottom: calc(var(--space-1) + env(safe-area-inset-bottom));
}

/* 内层胶囊条 */
.app-tabbar {
  display: flex;
  align-items: stretch;
  justify-content: space-around;
  gap: var(--space-1);
  background: var(--color-surface);
  border-radius: var(--radius-xlarge);
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.06), 0 4rpx 24rpx rgba(0, 0, 0, 0.03);
  padding: var(--space-3) var(--space-2);
}

/* 胶囊选项：统一垂直布局 emoji 上 + 文字下 */
.app-tabbar__pill {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding: var(--space-2) var(--space-5);
  border-radius: var(--radius-large);
  min-width: 130rpx;
  transition: all var(--motion-fast) var(--ease-standard);
}

.app-tabbar__pill--pressed {
  transform: scale(0.94);
  opacity: 0.85;
}

/* 选中态：暖陶色填充 */
.app-tabbar__pill--active {
  background: var(--color-primary-soft);
}

/* Emoji 图标 */
.app-tabbar__emoji {
  font-size: 36rpx;
  line-height: 1;
}

/* 文字标签 */
.app-tabbar__text {
  color: var(--color-text-muted);
  font-size: var(--font-meta);
  font-weight: var(--weight-medium);
  line-height: 1;
  transition: all var(--motion-fast) var(--ease-standard);
}

/* 选中态文字高亮 */
.app-tabbar__pill--active .app-tabbar__text {
  color: var(--color-primary-strong);
  font-weight: var(--weight-semibold);
}
</style>
