<template>
  <view>
    <!-- Canvas（隐藏） -->
    <canvas
      v-if="showCanvas"
      id="posterCanvas"
      canvas-id="posterCanvas"
      style="width:600px;height:900px;position:fixed;top:-9999px;left:-9999px;"
      type="2d"
    />

    <!-- 海报预览弹窗 -->
    <u-popup v-model="showPopup" mode="center" border-radius="28">
      <view class="poster-preview">
        <image v-if="posterImage" :src="posterImage" mode="widthFix" class="poster-preview__img" />
        <view class="poster-preview__actions">
          <view class="poster-preview__btn" hover-class="poster-preview__btn--pressed" @tap="save">
            <text class="poster-preview__btn-text">保存到相册</text>
          </view>
          <view class="poster-preview__btn poster-preview__btn--share" hover-class="poster-preview__btn--pressed" @tap="share">
            <text class="poster-preview__btn-text">分享给朋友</text>
          </view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup lang="ts">
import { getCurrentInstance, ref } from 'vue'

const props = defineProps<{
  avatarText: string
  nickname: string
  signature: string
  diaryCount: number
  createdAt?: string
}>()

const instance = getCurrentInstance()
const showCanvas = ref(false)
const showPopup = ref(false)
const posterImage = ref('')
const generating = ref(false)

function roundRect(ctx: any, x: number, y: number, w: number, h: number, r: number) {
  ctx.beginPath()
  ctx.moveTo(x + r, y)
  ctx.lineTo(x + w - r, y)
  ctx.quadraticCurveTo(x + w, y, x + w, y + r)
  ctx.lineTo(x + w, y + h - r)
  ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h)
  ctx.lineTo(x + r, y + h)
  ctx.quadraticCurveTo(x, y + h, x, y + h - r)
  ctx.lineTo(x, y + r)
  ctx.quadraticCurveTo(x, y, x + r, y)
  ctx.closePath()
}

async function generate() {
  if (generating.value) return
  generating.value = true
  showCanvas.value = true

  await new Promise((r) => setTimeout(r, 300))

  try {
    const canvasNode = await new Promise<any>((resolve) => {
      uni.createSelectorQuery()
        .in(instance)
        .select('#posterCanvas')
        .fields({ node: true, size: true }, () => {})
        .exec((res) => resolve(res?.[0]?.node))
    })
    if (!canvasNode) throw new Error('canvas not found')

    const ctx = canvasNode.getContext('2d')
    const W = 600, H = 900
    canvasNode.width = W
    canvasNode.height = H

    // 背景
    const grad = ctx.createLinearGradient(0, 0, 0, H)
    grad.addColorStop(0, '#F8F5F1')
    grad.addColorStop(0.5, '#F0EDEA')
    grad.addColorStop(1, '#E8D5C4')
    ctx.fillStyle = grad
    ctx.fillRect(0, 0, W, H)

    // 装饰圆
    ctx.beginPath(); ctx.arc(520, 80, 220, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(191, 123, 94, 0.05)'; ctx.fill()
    ctx.beginPath(); ctx.arc(80, 820, 160, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(91, 141, 190, 0.04)'; ctx.fill()

    // 标题
    ctx.fillStyle = '#1C1C1E'
    ctx.font = 'bold 32px sans-serif'
    ctx.textAlign = 'center'
    ctx.fillText('Life Record', 300, 80)
    ctx.strokeStyle = 'rgba(191, 123, 94, 0.15)'
    ctx.lineWidth = 2
    ctx.beginPath(); ctx.moveTo(220, 98); ctx.lineTo(380, 98); ctx.stroke()

    // 头像首字
    ctx.beginPath(); ctx.arc(300, 195, 55, 0, Math.PI * 2)
    ctx.fillStyle = '#BF7B5E'; ctx.fill()
    ctx.fillStyle = '#FFFFFF'
    ctx.font = 'bold 48px sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(props.avatarText, 300, 195)
    ctx.beginPath(); ctx.arc(300, 195, 58, 0, Math.PI * 2)
    ctx.strokeStyle = 'rgba(191, 123, 94, 0.15)'
    ctx.lineWidth = 3; ctx.stroke()

    // 昵称
    const nk = props.nickname || 'User'
    ctx.fillStyle = '#BF7B5E'
    ctx.font = 'bold 44px sans-serif'
    ctx.fillText(nk, 300, 280)

    // 签名
    const sg = props.signature || ''
    if (sg) {
      ctx.fillStyle = '#6E6E73'
      ctx.font = '22px sans-serif'
      ctx.fillText(sg, 300, 315)
    }

    // 统计卡片
    const cardY = 360, cardH = 150, cardW = 480, cardX = 60
    ctx.fillStyle = '#FFFFFF'
    ctx.shadowColor = 'rgba(0,0,0,0.05)'
    ctx.shadowBlur = 16; ctx.shadowOffsetY = 4
    roundRect(ctx, cardX, cardY, cardW, cardH, 24); ctx.fill()
    ctx.shadowColor = 'transparent'; ctx.shadowBlur = 0

    ctx.strokeStyle = 'rgba(0,0,0,0.04)'
    ctx.lineWidth = 1
    ctx.beginPath(); ctx.moveTo(300, cardY + 25); ctx.lineTo(300, cardY + cardH - 25); ctx.stroke()

    ctx.textAlign = 'center'
    const dc = String(props.diaryCount)
    ctx.fillStyle = '#5B8DBE'; ctx.font = 'bold 38px sans-serif'
    ctx.fillText(dc, 180, cardY + 75)
    ctx.fillStyle = '#6E6E73'; ctx.font = '20px sans-serif'
    ctx.fillText('Diaries', 180, cardY + 110)

    const join = props.createdAt
      ? Math.floor((Date.now() - new Date(props.createdAt).getTime()) / 86400000) : '--'
    ctx.fillStyle = '#BF7B5E'; ctx.font = 'bold 38px sans-serif'
    ctx.fillText(String(join), 420, cardY + 75)
    ctx.fillStyle = '#6E6E73'; ctx.font = '20px sans-serif'
    ctx.fillText('Days', 420, cardY + 110)

    // 二维码
    const qrY = 560, qrSize = 160, qrX = 220
    try {
      const info = await new Promise<any>((resolve, reject) => {
        uni.getImageInfo({ src: './static/qrcode.jpg', success: resolve, fail: reject })
      })
      const img = canvasNode.createImage()
      await new Promise((resolve, reject) => {
        img.onload = resolve
        img.onerror = reject
        img.src = info.path
      })
      ctx.fillStyle = '#FFFFFF'
      roundRect(ctx, qrX - 8, qrY - 8, qrSize + 16, qrSize + 16, 16); ctx.fill()
      ctx.save()
      ctx.beginPath()
      roundRect(ctx, qrX, qrY, qrSize, qrSize, 8); ctx.clip()
      ctx.drawImage(img, qrX, qrY, qrSize, qrSize)
      ctx.restore()
    } catch {
      ctx.fillStyle = '#FFFFFF'
      roundRect(ctx, qrX - 8, qrY - 8, qrSize + 16, qrSize + 16, 16); ctx.fill()
      ctx.fillStyle = '#AEAEB2'; ctx.font = '20px sans-serif'
      ctx.textAlign = 'center'
      ctx.fillText('QR Code', qrX + qrSize / 2, qrY + qrSize / 2 + 7)
    }
    ctx.fillStyle = '#AEAEB2'; ctx.font = '18px sans-serif'
    ctx.fillText('Scan to open mini program', 300, qrY + qrSize + 35)
    ctx.fillStyle = 'rgba(191, 123, 94, 0.06)'; ctx.font = '15px sans-serif'
    ctx.fillText('-- Every day matters --', 300, 840)

    const tempFilePath = await new Promise<string>((resolve, reject) => {
      uni.canvasToTempFilePath({
        canvas: canvasNode,
        canvasId: 'posterCanvas',
        x: 0, y: 0, width: W, height: H,
        destWidth: W * 2, destHeight: H * 2,
        success: (res: any) => resolve(res.tempFilePath),
        fail: reject
      } as any)
    })

    posterImage.value = tempFilePath
    showCanvas.value = false
    showPopup.value = true
  } catch (err) {
    console.error('[poster]', err)
    uni.showModal({ title: '生成失败', content: '海报生成出错，可截图分享代替', showCancel: false })
    showCanvas.value = false
  } finally {
    generating.value = false
  }
}

async function save() {
  if (!posterImage.value) return
  try {
    await uni.saveImageToPhotosAlbum({ filePath: posterImage.value })
    uni.$feedback.success('已保存到相册')
  } catch {
    uni.showModal({ title: '保存失败', content: '需要开启相册权限才能保存', confirmText: '去设置' })
  }
}

function share() {
  if (!posterImage.value) return
  showPopup.value = false
  uni.showModal({
    title: '分享海报',
    content: '已生成海报，可截图分享给朋友',
    showCancel: false
  })
}

defineExpose({ generate })
</script>

<style scoped lang="scss">
.poster-preview {
  width: 540rpx;
  padding: var(--space-5);
  background: var(--color-bg);
  border-radius: var(--radius-large);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
}

.poster-preview__img {
  width: 100%;
  border-radius: var(--radius-medium);
}

.poster-preview__actions {
  display: flex;
  gap: var(--space-3);
  width: 100%;
}

.poster-preview__btn {
  flex: 1;
  text-align: center;
  padding: var(--space-3) 0;
  border-radius: var(--radius-full);
  background: var(--color-surface-soft);
  transition: all var(--motion-fast) var(--ease-standard);
}

.poster-preview__btn--share {
  background: var(--color-primary-gradient);
}

.poster-preview__btn--pressed {
  transform: scale(0.95);
  opacity: 0.85;
}

.poster-preview__btn-text {
  color: var(--color-text-primary);
  font-size: var(--font-meta);
  font-weight: var(--weight-semibold);
}

.poster-preview__btn--share .poster-preview__btn-text {
  color: #fff;
}
</style>
