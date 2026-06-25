# View Transitions API — 主题切换波纹效果

## 效果

点击侧边栏底部主题切换按钮时，新主题色从按钮位置以圆形波纹向外扩散覆盖旧主题色，文字/边框/背景同步过渡，无闪烁。

## 最终实现

### JS（`layouts/sidebar.vue`）

```ts
function toggleTheme(e: MouseEvent) {
  const btn = e.currentTarget as HTMLElement
  const rect = btn.getBoundingClientRect()
  const x = rect.left + rect.width / 2
  const y = rect.top + rect.height / 2

  // 降级：不支持 View Transitions API 的浏览器直接切换
  if (!document.startViewTransition) {
    toggleDark()
    return
  }

  // 浏览器原生截图新旧状态 → 文字/边框/背景全部同步过渡，不闪
  const transition = document.startViewTransition(() => {
    toggleDark()
  })

  transition.ready.then(() => {
    document.documentElement.animate(
      {
        clipPath: [
          `circle(0px at ${x}px ${y}px)`,
          `circle(${Math.hypot(Math.max(x, innerWidth - x), Math.max(y, innerHeight - y))}px at ${x}px ${y}px)`,
        ],
      },
      {
        duration: 300,
        easing: 'ease-out',
        pseudoElement: '::view-transition-new(root)',
      },
    )
  })
}
```

### CSS（`layouts/sidebar.vue` 全局 style 块）

```css
/* 禁用默认 cross-fade 动画，只保留 JS animate() 驱动的 clip-path 扩散 */
::view-transition-old(root),
::view-transition-new(root) {
  animation: none;
  mix-blend-mode: normal;
}
::view-transition-new(root) { z-index: 9999; }
::view-transition-old(root) { z-index: 1; }
```

## 关键点

1. **`document.startViewTransition(callback)`** — 浏览器在执行 callback 前截图旧状态，执行后截图新状态，生成 `::view-transition-old(root)` 和 `::view-transition-new(root)` 两个伪元素
2. **`animation: none`** — 必须禁用默认的 cross-fade 动画，否则淡入淡出会和 clip-path 扩散叠加，波纹看不清
3. **`transition.ready`** — 等浏览器准备好伪元素后再用 `element.animate()` 驱动 clip-path 扩散
4. **`pseudoElement: '::view-transition-new(root)'`** — animate 作用在新状态伪元素上，旧状态 z-1 静止衬底，新状态从按钮位置圆形扩散覆盖
5. **降级** — `!document.startViewTransition` 时直接 `toggleDark()`，无动画但不影响功能

## 参考站点

- https://blog.grtsinry43.com/moments/ — SvelteKit 内置 View Transitions API 实现的主题切换

---

## 踩坑记录（手动 overlay 方案 → 已废弃）

### 旧方案原理

手动创建 `<div>` 作为波纹层，`clip-path: circle()` 从按钮位置扩散，配合 DOM 操作保证视觉效果。

### 遇到的问题及解决过程

| 问题 | 根因 | 临时修法 |
|---|---|---|
| 快速点击后背景卡死 | overlay 的 `transitionend` 不触发，div 累积在 DOM 里 | 创建前清理残留 + setTimeout 兜底移除 |
| 波纹不可见 | overlay `z-index` 低于侧边栏/内容区 | 调 z-index |
| 过渡不触发 | rAF 里同时设 transition + 终值，浏览器没记录初始状态 | `void offsetWidth` 强制 reflow |
| 文本被挡住 | overlay z-index 太高盖住文字 | 降 z-index + 侧边栏/卡片去背景改 backdrop-blur |
| 切换后边框还是黑的 | toggleDark 延迟到波纹完成后才执行 | toggleDark 提前到开头 |
| 文字闪动 | body transition-colors 与 overlay 扩散叠加 | 全局 freeze style 禁所有 transition |
| 仍然闪 | freeze 导致 snap 太生硬 | sync style 统一 transition-duration |

### 为什么手动方案最终被放弃

核心矛盾：手动方案需要同时管理「背景波纹扩散」和「文字/边框颜色过渡」两个独立动画系统。clip-path transition 和 CSS class transition 由不同机制驱动，时序永远对不齐——要么文字太快（snap 闪），要么文字太慢（跟背景不匹配）。

### View Transitions API 为什么能解决

- 浏览器对整页**像素截图**，新旧两张图通过 clip-path 圆形扩散过渡
- 截图包含了所有文字/边框/背景的最终状态，整体切换，不存在各元素时序不一致
- 不需要手动管理 overlay DOM 生命周期、z-index 层级、元素背景透明度
- 代码量从 ~50 行降到 ~15 行，无 edge case

## 浏览器兼容性

- Chrome 111+（2023.03）
- Edge 111+
- Safari 17.4+（2024.03）
- Firefox 暂不支持（降级为直接切换）

## 相关文件

- `layouts/sidebar.vue` — 主题切换按钮 + toggleTheme 实现
- `app.vue` — Body 背景色 `bg-white dark:bg-gray-950`
- `tailwind.config.ts` — primary 色定义
