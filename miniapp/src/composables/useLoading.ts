import { ref } from 'vue'

const visible = ref(false)
const text = ref('')
let timer: ReturnType<typeof setTimeout> | null = null

/**
 * 全局 loading 管理。
 *
 * 用法：
 *   const { show, hide, wrap } = useLoading()
 *   show('加载中...')
 *   // 或用 wrap 包裹异步操作
 *   await wrap(fetchData(), '加载中...')
 */
export function useLoading() {
  function show(msg?: string) {
    if (timer) clearTimeout(timer)
    text.value = msg || ''
    visible.value = true
  }

  function hide() {
    if (timer) clearTimeout(timer)
    // 延迟 200ms 关闭，避免闪烁
    timer = setTimeout(() => {
      visible.value = false
      text.value = ''
    }, 200)
  }

  /**
   * 包裹一个 Promise，自动显示/隐藏 loading。
   */
  async function wrap<T>(promise: Promise<T>, msg?: string): Promise<T> {
    show(msg)
    try {
      return await promise
    } finally {
      hide()
    }
  }

  return { visible, text, show, hide, wrap }
}
