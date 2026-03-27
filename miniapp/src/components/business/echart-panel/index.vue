<template>
  <view class="echart-panel" :style="{ height }">
    <UniEcharts
      class="echart-panel__canvas"
      :option="option || {}"
      :update-options="{ notMerge: true }"
      :autoresize="true"
      :custom-style="{
        width: '100%',
        height: '100%'
      }"
    />
  </view>
</template>

<script setup lang="ts">
import * as echarts from 'echarts/dist/echarts.esm.js'
import UniEcharts from 'uni-echarts'
import type { ChartOption } from 'uni-echarts/shared'
import { provideEcharts } from 'uni-echarts/shared'

withDefaults(
  defineProps<{
    option?: ChartOption
    height?: string
  }>(),
  {
    option: undefined,
    height: '360rpx'
  }
)

provideEcharts(echarts)
</script>

<style scoped lang="scss">
.echart-panel {
  width: 100%;
  min-height: 1px;
}

.echart-panel__canvas {
  width: 100%;
  height: 100%;
}
</style>
