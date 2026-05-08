# lifeRecord 前端 UI 设计规范

## 0. 文档地位

这是 `miniapp` 前端 UI 重构的**单一信息源**：所有页面、组件、样式都按这份规范生产，
不允许在外面重新发明颜色、字号、圆角、间距、阴影。规范本身可以演进，但演进时必须先改这份文档。

文档目的是同时做到：

1. **可主题切换**：用户能在多个预设主题间切换，全站颜色一键替换
2. **设计统一**：圆角、字号、间距、节奏、状态有确定的取值阶梯
3. **生活类小程序调性**：温暖、柔软、留白充足、有呼吸感
4. **结构封装到位**：常见模式抽成组件，页面只描述"用什么"不描述"长什么样"

---

## 1. 设计原则（决定何为"好"）

每条原则都是后面具体规范的依据，遇到取舍时按这五条排序。

1. **温暖而克制**：颜色饱和度中等偏低，不抢戏；强调用主色但不到处铺色
2. **大圆角带来安全感**：界面元素以圆角为主，避免锐角；圆角阶梯统一
3. **节奏分明**：字号、间距、字重都用 4-5 档阶梯，不要无穷多种中间值
4. **留白即设计**：宁可空，不要塞；信息密度要让用户有"喘气"的位置
5. **状态完整**：每个数据展示场景必须有 4 态（加载 / 空 / 错误 / 正常），不能只画正常态

---

## 2. 主题系统

### 2.1 设计

主题切换通过 **CSS 变量** + **`useTheme()` composable** 实现。所有颜色 token 写成 CSS 变量挂在 `page` 上，切换主题 = 替换变量值。

**为什么不用 SCSS 变量？**
SCSS 变量编译期就定型了，运行时无法切换。微信小程序 2020+ 已经支持 CSS 变量，可用。

### 2.2 主题清单

首版提供 **4 套主题**，覆盖典型生活类小程序氛围：

| 主题 ID | 名称 | 关键词 | 主色 | 背景色 | 适合人群 |
|---|---|---|---|---|---|
| `clay` | 暖陶 | 温暖、纸感、复古 | 陶土橙 `#c47c52` | 米白 `#fffaf4` | 默认（当前风格） |
| `mint` | 薄荷 | 清爽、自然、平静 | 薄荷绿 `#5cab86` | 雾白 `#f5f9f5` | 偏好淡雅 |
| `lilac` | 紫雾 | 柔软、梦幻、女性向 | 浅紫 `#9b7cb6` | 雪白 `#faf6fb` | 写日记记录心情 |
| `ink` | 墨夜 | 沉稳、专注、夜间 | 蓝灰 `#7c93a8` | 深灰 `#1d2128` | 暗色模式 |

每套主题都遵守同一个 token 集合，只是 `--color-*` 系列变量值不同，**圆角/间距/字号/字重永远不变**。

### 2.3 CSS 变量定义（节选）

```scss
// styles/themes/_clay.scss（默认主题）
[data-theme='clay'] {
  --color-primary: #c47c52;
  --color-primary-soft: rgba(196, 124, 82, 0.12);
  --color-accent: #d7a648;
  --color-bg: #fffaf4;
  --color-surface: rgba(255, 250, 244, 0.94);
  --color-surface-soft: #fcf5ec;
  --color-border: rgba(196, 124, 82, 0.08);
  --color-text-primary: #2b2118;
  --color-text-secondary: #6b5b4e;
  --color-text-muted: #9b866d;
  --color-success: #4f7a4a;
  --color-warning: #d7a648;
  --color-danger: #b94a3b;
  --shadow-card: 0 18rpx 48rpx rgba(67, 41, 26, 0.08);
}
```

切换时只需要：

```ts
const { setTheme } = useTheme()
setTheme('mint')   // 全站立即生效
```

`useTheme` 内部：写入 `<page>` 的 `data-theme` 属性 + 写入本地存储 + 触发响应式更新。

### 2.4 怎么用

**禁止**：`color: #c47c52`、`background: rgba(196, 124, 82, 0.08)`
**应当**：`color: var(--color-primary)`、`background: var(--color-border)`

---

## 3. 设计 Token

所有 Token 写在 `styles/tokens.scss`。Token 分语义层（业务用）和原子层（不直接用）。

### 3.1 颜色 Token（语义层）

主题相关，跨主题切换：

| Token | 用途 |
|---|---|
| `--color-primary` | 主操作按钮、强调色、激活状态 |
| `--color-primary-soft` | 主色的弱化背景（10-15% 透明度） |
| `--color-accent` | 次要强调，例如警告/突出 |
| `--color-bg` | 整页背景 |
| `--color-surface` | 卡片、面板 |
| `--color-surface-soft` | 弱化卡片（嵌套层 / 空状态） |
| `--color-border` | 边框、分隔线 |
| `--color-text-primary` | 标题、正文主文字（带主题色调，有性格） |
| `--color-text-secondary` | 说明、次级文字（带主题色调） |
| `--color-text-muted` | 时间戳、辅助标签（带主题色调） |
| `--color-text-neutral` | **用户输入内容、代码、原始文本（不带主题色调）**，浅色主题近黑、暗色主题近白 |
| `--color-success` | 成功状态、收入 |
| `--color-warning` | 警告状态 |
| `--color-danger` | 错误状态、删除、支出 |
| `--shadow-card` | 卡片阴影（主题里也定义，配合背景） |

### 3.2 字号阶梯

按用途命名而不是大小命名（避免 `--text-12` 这种无语义）：

| Token | 字号 | line-height | 用途 |
|---|---|---|---|
| `--font-display` | 44rpx | 1.15 | 页面级大标题（首页、登录） |
| `--font-title` | 36rpx | 1.2 | 页面标题、对话标题 |
| `--font-section` | 30rpx | 1.4 | 卡片标题、二级标题 |
| `--font-body` | 28rpx | 1.7 | 主要正文 |
| `--font-caption` | 24rpx | 1.6 | 说明、标签 |
| `--font-meta` | 22rpx | 1.5 | 时间戳、辅助元数据 |
| `--font-tiny` | 20rpx | 1.4 | 角标、徽标 |

字重只有 4 档：`400 / 500 / 600 / 700`。其他字重不允许出现。

### 3.3 间距阶梯

8rpx 为基本单位，6 档够全场景：

| Token | rpx | 用途 |
|---|---|---|
| `--space-1` | 8 | 紧凑元素之间 |
| `--space-2` | 12 | 标签内、icon 与文字间 |
| `--space-3` | 16 | 卡片内字段之间 |
| `--space-4` | 20 | 卡片内分块之间 |
| `--space-5` | 24 | 卡片内边距 |
| `--space-6` | 32 | 大区块之间 |
| `--space-7` | 40 | 页面级分块 |

### 3.4 圆角阶梯（**统一改造重点**）

| Token | rpx | 用途 |
|---|---|---|
| `--radius-tiny` | 8 | 标签、徽标 |
| `--radius-small` | 16 | 输入框、小按钮 |
| `--radius-medium` | 24 | 列表项、嵌套卡片 |
| `--radius-large` | 28 | 主卡片、页头 |
| `--radius-xlarge` | 36 | 弹窗、底部抽屉 |
| `--radius-full` | 999 | 圆形头像、胶囊按钮 |

**禁止**写任何不在阶梯里的圆角值。`border-radius: 22rpx` 这种"差不多"的写法直接拒收。

### 3.5 阴影

| Token | 用途 |
|---|---|
| `--shadow-card` | 主卡片浮起 |
| `--shadow-modal` | 弹窗浮层 |
| `--shadow-press` | 按下态轻微下沉 |
| `--shadow-none` | 平面（无阴影但保留 token 便于切换） |

### 3.6 动画

| Token | 值 | 用途 |
|---|---|---|
| `--motion-fast` | 160ms | 按下、hover 反馈 |
| `--motion-base` | 240ms | 弹窗、抽屉、骨架闪光 |
| `--motion-slow` | 360ms | 页面切换、加载完成淡入 |
| `--ease-standard` | `cubic-bezier(0.4, 0, 0.2, 1)` | 默认曲线 |
| `--ease-bounce` | `cubic-bezier(0.34, 1.56, 0.64, 1)` | 出现/弹出 |

---

## 4. 排版

### 4.0 装饰性 vs 功能性文字

文字按"是否需要主题性格"分两类：

| 场景 | 用什么 |
|---|---|
| **装饰性**：标题、说明、内容、meta、提示 | `--color-text-primary` / `--color-text-secondary` / `--color-text-muted`（**带**主题色调，浅色主题里偏暖棕/暖绿/暖紫，墨夜里偏冷白） |
| **功能性**：用户输入内容、代码片段、文本编辑器、原样回显 | `--color-text-neutral`（**不带**主题色调，浅色主题里近黑 `#1d1d1f`，墨夜里近白 `#f5f5f7`） |

为什么要区分？
- 装饰性文字让界面"有气质"，主题色调强化整体氛围
- 用户输入的字是"用户的字"，不应该被主题渲染 —— 切到墨夜也希望看到"我打的字就是黑底白字"的稳定体感
- 输入框、textarea、代码、可编辑文本等都用 `--color-text-neutral`

### 4.1 标题层级

最多 3 级标题：

- **Display**（44rpx / 700）：每页只出现 1 次，登录页 / 首页问候语等
- **Title**（36rpx / 700）：当前模块的最高标题（"我的日记"、"AI 账单分析"）
- **Section**（30rpx / 600）：卡片或子模块的标题

### 4.2 中英文混排

中文优先 PingFang SC / 系统默认，英文优先 SF Pro / system-ui，数字优先用 tabular-nums（避免小数点对不齐）。

### 4.3 行高

- 单行场景（标题、按钮）：`1.15-1.2`
- 多行正文：`1.6-1.8`
- 紧凑列表项：`1.4-1.5`

---

## 5. 组件库

### 5.1 分层

```text
components/
├─ ui/              # 设计系统层（无业务）
│  ├─ AppButton/
│  │  ├─ index.vue
│  │  └─ README.md            # （可选）组件说明、props、用法
│  ├─ AppCard/
│  │  └─ index.vue
│  ├─ AppListItem/
│  ├─ AppEmpty/
│  ├─ AppLoading/
│  ├─ AppSkeleton/
│  ├─ AppToast/
│  └─ AppBadge/
└─ business/        # 业务组件（已存在，保留）
   ├─ choice-chips/
   ├─ ledger-year-charts/
   └─ ...
```

ui/ 层只用 token，不写硬编码颜色/数值；business/ 层基于 ui/ 层组合。

### 5.2 必备组件清单

| 组件 | 必须支持的 props | 替代谁 |
|---|---|---|
| `AppButton` | `type` (primary/secondary/ghost/danger) / `size` (mini/normal/large) / `block` / `loading` / `disabled` / `icon` | `u-button`（项目内部封装） |
| `AppCard` | `padding` (compact/normal/loose) / `elevated` (boolean) | 现有 `.section-shell` |
| `AppListItem` | `title` / `desc` / `aside` / `clickable` | 现有 `.list-card` |
| `AppEmpty` | `title` / `description` / `mode` (default/history/error) / `action` slot | `empty-state-card` |
| `AppLoading` | `mode` (spinner/dots/inline) / `text` | 各页面零散 loading |
| `AppSkeleton` | `rows` / `avatar` (boolean) / `paragraph` (boolean) | **缺失，新增** |
| `AppPageHeader` | `eyebrow` / `title` / `desc` / actions slot | 现有 `.page-head` |
| `AppSection` | `title` / `desc` / actions slot / 默认 slot | 现有 `.section-shell` + `.section-head` 组合 |

每个组件必须写：

- TS 类型定义
- props 默认值
- emits 列表
- `<style scoped>` 全部用 token

### 5.3 命名规范

- **每个组件一个独立文件夹**，主文件统一命名 `index.vue`
- 组件文件夹名：PascalCase，如 `AppButton/index.vue`、`AppCard/index.vue`
- 业务组件文件夹保留 kebab-case（已有惯例），如 `choice-chips/index.vue`
- 全局注册前缀：`App`（避免和 uview 的 `u-` 冲突）

为什么不允许 `AppButton.vue` 这种平铺写法？一旦后续要加 README、单测、CHANGELOG、TS 类型，平铺会迅速污染外层目录；先用文件夹起步，加新文件直接放进去就行，不会动到任何引用路径。

---

## 6. 状态系统（4 态都要画）

每个展示数据的页面/区块**必须**实现 4 个状态：

### 6.1 加载（Loading）

- **首屏全页加载** → 骨架屏（`AppSkeleton`）
- **二次加载/分页加载** → 顶部进度条 + 末尾 `AppLoading mode="dots"`
- **按钮提交中** → `AppButton` 自身 `loading` 态（替换文字为 spinner，按钮保持 disabled）

骨架屏要**模拟真实内容形状**，不能用一坨灰色矩形糊弄。比如日记列表的骨架要有"标题条 + 两行正文条"的形状。

### 6.2 空（Empty）

- 用 `AppEmpty` 组件
- 必须有：插画或大 emoji（不要什么都没有）+ 标题 + 一句话说明 + 主操作按钮
- 文案要鼓励行动："还没有日记，写下今天发生的事吧" ✅；"暂无数据" ❌

### 6.3 错误（Error）

- 用 `AppEmpty mode="error"`
- 必须有：错误描述（不要 stack trace）+ 重试按钮
- 网络错误优先用文案"网络好像走神了"等温和措辞，避免吓人

### 6.4 正常

- 实际内容渲染
- 注意切换时的过渡：从 skeleton → 正常态用 fade-in（`--motion-base`）

---

## 7. 页面布局规范

### 7.0 页面文件结构

**每个页面也用文件夹包裹**，主文件统一命名 `index.vue`。和组件保持一致，便于以后追加该页面专属的子组件、说明文档、类型定义而不污染外层。

```text
pages/
├─ diary/
│  ├─ index/             # 日记列表页
│  │  └─ index.vue
│  ├─ editor/            # 编辑页
│  │  └─ index.vue
│  └─ detail/            # 详情页
│     └─ index.vue
├─ ledger/
│  ├─ index/
│  │  └─ index.vue
│  └─ books/
│     └─ index.vue
└─ ...
```

`pages.json` 里的 path 写到文件夹层级即可（如 `pages/diary/editor`），uni-app 会自动按 `index.vue` 解析；现有 `pages/profile/recycle/`、`pages/profile/tags/` 已经是这种结构，统一推广到所有页面。

每个页面文件夹下后续可以追加：

- `index.vue`（必须）
- `composables.ts`（页面级 composable）
- `types.ts`（页面专属类型）
- `components/`（仅本页用的子组件）
- `README.md`（该页设计说明，复杂页面建议有）

### 7.1 安全区

页面根容器必须包：

```html
<view class="page-shell-safe">
  <AppPageHeader ... />
  <view class="page-section">
    <AppSection ...>...</AppSection>
  </view>
  ...
</view>
```

`page-shell-safe` 自动处理顶部状态栏、底部 home indicator 安全区。

### 7.2 卡片排版

- 卡片之间间距固定 `--space-6`（32rpx）
- 卡片内顶层元素之间 `--space-4`（20rpx）
- 卡片内字段之间 `--space-3`（16rpx）

### 7.3 操作按钮区

- 主操作单按钮 → 卡片内底部，全宽，`AppButton block size="large"`
- 双按钮 → 主次组合，主按钮在右，次按钮 `type="secondary"` 在左
- 三按钮以上 → 改用底部固定 toolbar 或菜单 dropdown

---

## 8. 动效规范

### 8.1 出现

- 页面跳转：用 uni-app 默认转场
- 卡片懒加载出现：fade-in + 微微 translateY 6rpx，时长 `--motion-slow`
- 弹窗/抽屉：从对应方向滑入 + 透明度，时长 `--motion-base`，曲线 `--ease-bounce`

### 8.2 反馈

- 按下：scale(0.97)，时长 `--motion-fast`
- 加载完成：fade-out skeleton + fade-in 正常内容
- 提交成功：在按钮上短暂出现 ✓（`--motion-base`）后回到正常状态

### 8.3 禁忌

- 禁止超过 400ms 的过渡
- 禁止页面级抖动（shake）等强反馈
- 长列表禁止整列同时动画（性能考虑）

---

## 9. 图片懒加载与性能

### 9.1 图片

封装 `AppImage` 组件，统一处理：

- 用 `lazy-load` 属性（uni-app 原生支持）
- 加载中显示骨架占位
- 失败显示降级图标
- 默认 `mode="aspectFill"` + 圆角

```html
<AppImage src="..." :radius="--radius-medium" mode="aspectFill" />
```

### 9.2 长列表

- 用 `scroll-view` + `lazy-load` + 虚拟列表（>50 项时考虑 `@dcloudio/uni-virtual-list`）
- 分页拉取每次 20 条，下拉加载更多
- 滚动到底加载下一页

### 9.3 字体子集化

后期如果引入自定义字体，必须做子集化（CJK 全集 4MB+，必须裁到当前页用到的字符）。一期不引入。

---

## 10. 文档与实现的强一致

每个 token 在以下位置同时存在，缺一不可：

1. 本文档：定义和说明
2. `styles/tokens.scss`：CSS 变量定义
3. `styles/themes/*.scss`：每套主题的覆盖
4. `composables/useTheme.ts`：切换逻辑
5. `components/ui/*.vue`：消费 token

加 token 时必须 5 处同步更新，PR 中漏一处都要打回。

---

## 11. 重构路线（执行步骤）

按顺序推进，每一步独立可验证、独立可提交：

### 第 1 步 — 地基

- [ ] 创建 `styles/tokens.scss` 定义所有原子 token
- [ ] 创建 `styles/themes/clay.scss` 等 4 套主题
- [ ] 创建 `composables/useTheme.ts` + 在 `App.vue` 启动时读取本地存储应用
- [ ] `theme.scss` 改造：把硬编码颜色全部替换为 `var(--color-*)`
- [ ] 验收：现有页面表面看不出变化，但 DevTools 里能看到所有颜色都是 CSS 变量

### 第 2 步 — UI 组件层

- [ ] 创建 `components/ui/` 目录与 8 个核心组件
- [ ] 全局注册（`main.ts`）
- [ ] 在 1 个原型页面（推荐"个人中心"）替换为新组件
- [ ] 验收：原型页面所有视觉元素都来自 ui 组件，无内联样式

### 第 3 步 — 主题切换功能

- [ ] 在"个人中心 → 设置"加主题切换器
- [ ] 验收：切换后整页颜色立即变，刷新后保持

### 第 4 步 — 骨架屏与状态完善

- [ ] `AppSkeleton` 组件实现（含闪光动画）
- [ ] 把 5 个常用页面（首页、日记列表、记账列表、打卡、AI）的 loading 都换成骨架屏
- [ ] 把所有空状态都改成 `AppEmpty` 组件

### 第 5 步 — 批量页面迁移

按使用频次推进：日记 → 记账 → 打卡 → 记忆/纪念日 → AI → 设置类。每页一个独立提交。

### 第 6 步 — 清理 + 视觉走查

- [ ] 删除老的 `theme.scss` 里被新 token 体系替代的部分
- [ ] 全站视觉走查（每页截图对比）
- [ ] 移除未使用的样式类

### 第 7 步 — 页面文件夹化

- [ ] 把 `pages/{module}/{page}.vue` 全部迁移成 `pages/{module}/{page}/index.vue`
- [ ] 同步更新 `pages.json` 的 path（保持原路径不变，uni-app 自动按 `index.vue` 解析）
- [ ] 验收：每个页面都是一个文件夹，外层不再有散落的 .vue 文件

> 这一步可以与第 5 步合并：迁移每个页面时顺手把它移进自己的文件夹。

---

## 12. 反例（明确"不要这样"）

| 场景 | ❌ 反例 | ✅ 正例 |
|---|---|---|
| 颜色 | `color: #c47c52` | `color: var(--color-primary)` |
| 圆角 | `border-radius: 22rpx` | `border-radius: var(--radius-medium)` |
| 字号 | `font-size: 26rpx` | `font-size: var(--font-caption)` |
| 间距 | `margin-top: 18rpx` | `margin-top: var(--space-3)` |
| 加载状态 | `<view>加载中...</view>` | `<AppSkeleton :rows="3" />` |
| 空状态 | `<view>暂无数据</view>` | `<AppEmpty title="..." description="..." />` |
| 按钮 | `<u-button @tap="...">...` | `<AppButton @click="...">...` |
| 分隔线 | `<view style="height:1rpx;background:#eee">` | `<view class="divider">`（在 token 里定义） |
| 组件 | `components/ui/AppButton.vue` | `components/ui/AppButton/index.vue` |
| 页面 | `pages/diary/editor.vue` | `pages/diary/editor/index.vue` |

---

## 13. 一句话总结

**所有视觉决策都先来这份文档查 token 和组件，找不到就先来补这份文档，再去写代码。**
