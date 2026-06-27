export function useScroll() {
  const y = ref(0)
  const ratio = ref(0)
  const scrolled = ref(false)
  const atBottom = ref(false)
  let ticking = false
  const update = () => {
    const sy = window.scrollY
    const sh = document.documentElement.scrollHeight
    const ch = window.innerHeight
    y.value = sy
    ratio.value = sh > ch ? sy / (sh - ch) : 0
    scrolled.value = sy > ch
    atBottom.value = sh - sy - ch < 100
    ticking = false
  }
  onMounted(() =>
    window.addEventListener(
      'scroll',
      () => {
        if (!ticking) {
          requestAnimationFrame(update)
          ticking = true
        }
      },
      { passive: true },
    ),
  )
  return {
    y: readonly(y),
    ratio: readonly(ratio),
    scrolled: readonly(scrolled),
    atBottom: readonly(atBottom),
  }
}
