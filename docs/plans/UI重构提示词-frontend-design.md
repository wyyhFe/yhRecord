# lifeRecord UI 重构提示词（喂给 frontend-design skill 用）

> ⚠️ **状态（2026-05-27）：本文档 stale，UI 重构暂停**
>
> - 多主题切换已撤回（详见 `docs/standards/UI 设计规范.md` §2），文档内任何涉及「4 套主题」「方向 A/B」「themeClass」「主题切换器」等的描述都已与代码不一致。
> - UI 重构方向待重新确定。在新的指导文档落地之前，不要再按本文档喂给 frontend-design skill 执行重构。
> - 保留此文档作为历史参考，不要据此修改任何代码。

> 用法（历史）：新开一次会话，把这份文件整段贴给 Claude Code，并在末尾加一句"用 /frontend-design 帮我重构 X 页面"。
> 这份文件是**对 skill 的简报**，不是对 Claude 的普通对话，所以语气直接、信息密集，省去寒暄。

---

## 0. 一句话目标

把 `lifeRecord` 微信小程序的视觉层从「合格但平庸」推到「打开就有记忆点」，**保留**现有 token + 4 主题 + 组件分层，**重做**页面结构、版式节奏、动效细节、装饰元素。

---

## 1. 项目背景（让 skill 读懂场景）

### 1.1 产品定位

`lifeRecord` 是个人长期使用的微信小程序，围绕**日记 / 记账 / 打卡 / 纪念日 / 那年今日 / AI 助手**六大场景。用户多为：

- 25–35 岁，独居或小家庭，有"想认真记录生活"的意愿但容易半途而废
- 偏好克制、温暖、有手作感的视觉，**反感**金融 App 那种锐利、信息密集、冷感的风格
- 长期使用，需要颜色舒服、不刺眼、能切换主题适配心情

### 1.2 技术栈与限制（**重要：小程序不是 web**）

| 项 | 限制 |
|---|---|
| 框架 | uni-app + Vue 3 + TypeScript + uView 1.x |
| 单位 | **必须用 rpx**（750rpx = 屏宽）；px 仅用于边框 1px |
| 字体 | **不能引入任意 @font-face**（小程序限制），只能用：系统默认 / PingFang SC / 微信内置中文字体 / 数字用 `tabular-nums`。想要装饰字体只能做**图片切图**或**SVG 文字** |
| 布局 | 不支持原生 CSS Grid 的部分高级特性，Flex 主力；`position: sticky` 部分机型有 bug；伪元素 `::before/::after` 在小程序 view 上不可用，要换成额外 `<view>` |
| 动效 | 支持 CSS animation/transition，不支持 Web Animations API，**不能用 Framer Motion / GSAP**。复杂动效用 `wx.createAnimation` 或 `uni.createAnimation` |
| 图片 | `<image>` 标签，必须给宽高，懒加载用 `lazy-load` |
| SVG | 不直接支持，要么用 base64 内联，要么用 iconfont（uView 自带 `u-icon`） |
| 滚动 | 长列表必须用 `<scroll-view>` 而不是 `view + overflow:auto` |
| 暗色 | 通过 `data-theme="ink"` 切换，不要用 `prefers-color-scheme` |

⚠️ frontend-design skill 默认偏向 Web 富特效，喂这份提示词时**必须先吃掉这一节**，否则会写出小程序跑不起来的东西。

### 1.3 现有 UI 资产（**不要重造**）

下列已建立，重构时直接消费，不要新造：

- `miniapp/src/styles/tokens.scss` — 原子 token（圆角/间距/字号/字重/动画）
- `miniapp/src/styles/themes/{clay,mint,lilac,ink}.scss` — 4 套主题 CSS 变量（暖陶/薄荷/紫雾/墨夜）
- `miniapp/src/styles/globals.scss` — 全局样式
- `miniapp/src/composables/useTheme.ts` — 主题切换 hook
- `miniapp/src/composables/useThemeColors.ts` — canvas/ECharts 拿具体色值
- `miniapp/src/components/business/` — 9 个业务组件（choice-chips、empty-state-card、filter-tabs、location-picker、login-sheet、photo-picker、selector-dialog、ledger-year-charts、echart-panel）

详细 token 表见 `docs/standards/UI 设计规范.md`（**必读**，写代码时 token 名直接抄）。

---

## 2. 美学方向（**让 skill 二选一，不要骑墙**）

frontend-design skill 一直强调"不要在所有项目都用同一种风格"，所以这里给两个候选方向，**每次会话只挑一个**执行：

### 方向 A — 「纸感日记本 · 暖陶风」（推荐默认）

- **关键词**：手账本质感、奶油纸背景、温暖陶土橙、墨水蓝点缀、轻纸纹
- **灵感**：MUJI 手帐、Hobonichi、Day One 日记 App、苹果手记
- **typography**：中文 PingFang SC 600（标题）+ 系统衬线（细节装饰用图片切）；数字用 `tabular-nums` 等宽；标题字号大胆放大到 48–56rpx
- **色彩主导**：陶土橙 `#c47c52` 为绝对主角（不是均匀分布的"装饰色"），背景米白 `#fffaf4` 占 70%，墨水蓝 `#3a5169` 偶尔出现作对比
- **装饰元素**：纸张噪点（极浅 SVG pattern 转 base64 当背景）、手绘风分隔波浪线、卡片左上角"装订针"小圆点、日期用印章风胶囊
- **动效**：卡片出现用 `translateY(8rpx) + opacity` 渐入；按下 `scale(0.97)` + 加深阴影；翻页用左滑入
- **氛围**：像在翻一本被翻旧了的笔记本

### 方向 B — 「极简编辑器风 · 墨夜默认」

- **关键词**：编辑器、专注、留白、单色、无装饰
- **灵感**：Linear、Bear、iA Writer、Things 3
- **typography**：中文系统默认（实际是 PingFang SC）400 + 标题 700 形成强对比；行高拉到 1.75；段落字距宽
- **色彩主导**：墨夜 `#1d2128` 背景占 85%，淡蓝灰 `#7c93a8` 为单一主色，整页几乎黑白
- **装饰元素**：**极致克制** —— 没有图标只有点和短横线（`·` / `—`）；卡片边框 1px 极细线条，不要阴影；分割用空行而非线条
- **动效**：几乎没有，只在按下时 200ms 背景色微变；列表出现一律 fade，时长 240ms
- **氛围**：像在用一支高级钢笔写字

> 你给 skill 喂的时候，**在结尾明确指定方向 A 或 B**，比如：
> "用 frontend-design skill 按方向 A（纸感日记本）重做首页"

---

## 3. 页面级简报（按重构优先级排）

每页给：**用途 / 关键信息层级 / 当前痛点 / 期望视觉记忆点**。

### 3.1 首页 `pages/home/index.vue`（优先级 P0）

- **用途**：打开 App 第一屏，今日概览 + 快速入口
- **信息层级**：① 问候/今日状态 → ② 今日记录概览（3 个 metric）→ ③ 最近七天日记/打卡状态带 → ④ 快速入口宫格
- **痛点**：现在四个区块平铺、视觉权重一致，没层次；"最近七天"是横向状态点，但视觉上像普通色块没辨识度
- **记忆点期望**：方向 A → 首屏顶部带"今天是 5 月 27 日 星期二，雨"的手账标签 + 大字号问候；最近七天做成"日历胶囊带"，写过日记的胶囊填实陶土色

### 3.2 日记列表 `pages/diary/index.vue`（P0）

- **用途**：浏览历史日记，进入详情或新建
- **信息层级**：① 顶部筛选/搜索 → ② 时间倒序卡片流（每张：日期 + 心情 + 标题 + 正文摘要 + 图片缩略）
- **痛点**：卡片是统一矩形，缺少"翻开本子"感；日期不够醒目；心情图标小且不突出
- **记忆点期望**：方向 A → 每张卡片左侧贴一个手写感的"日期 + 心情"垂直竖条；图片缩略圆角更大；列表底部留"今天还没写"的引导卡

### 3.3 日记编辑 `pages/diary/editor.vue`（P0）

- **用途**：写新日记或编辑
- **信息层级**：标题输入 → 天气/心情/日期选择 → 正文 textarea → 图片上传区 → 位置 → 标签 → 保存
- **痛点**：表单像 OA 系统，缺少"在写"的氛围；textarea 是普通灰框
- **记忆点期望**：textarea 改成无边框 + 浅纸纹背景 + 自动换行的"稿纸"感；保存按钮放在浮动 sticky 底栏

### 3.4 记账主页 `pages/ledger/index.vue`（P1）

- **用途**：浏览本月账单、统计、跳转
- **信息层级**：① 月份切换 → ② 本月总览（收入/支出/结余） → ③ 流水按日分组列表 → ④ FAB 新增
- **痛点**：数字字号不够大、没有视觉权重；分组日期头平庸
- **记忆点期望**：本月支出做成超大数字（80rpx+ tabular-nums），辅以小字"本月已花费"；流水条目左侧贴标签色块

### 3.5 那年今日 `pages/memory/index.vue`（P1）

- **用途**：展示历史同期的日记/账单
- **痛点**：纯列表，没有"翻看回忆"的仪式感
- **记忆点期望**：每条历史卡片做成"老相片"风格，带年份大字+轻微旋转角度+浅黄色滤镜（方向 A）

### 3.6 个人中心 `pages/profile/index.vue`（P2）

- **用途**：用户信息 + 主题切换 + 入口菜单
- **现状**：已经做过一版 token 化，可以保留主结构，**只重做 hero 头部和主题色卡**
- **记忆点期望**：4 张主题色卡做成"打开的颜料罐"或"色票"风格，hover/激活时有手感反馈

### 3.7 打卡 / 纪念日（P2）

- 现状可用，重构时统一向方向 A 的纸感靠拢即可，不必单独大改

### 3.8 不动的页面

- `pages/ai/*` — 个人开发者不能上线 AI 类目，入口已屏蔽，**不重构**
- `pages/auth/login.vue` — 登录页低频，重构最后再考虑

---

## 4. 给 skill 的明确产出要求

每次会话只重构 **1 个页面**，产出：

1. **完整可跑的 `.vue` 文件**（template + script setup TS + style scoped lang="scss"）
2. **样式只能引用 token**：颜色 `var(--color-*)`、圆角 `var(--radius-*)`、间距 `var(--space-*)`、字号 `var(--font-*)`、动效 `var(--motion-*)`。如果需要新增 token，**先在响应里列出来要加什么，等用户确认再写**
3. **小程序兼容性自查**：
   - 没用 `::before/::after` 在 `view` 上
   - 没用 `position: sticky`（除非加注释说明已测试机型）
   - 图片都有宽高 + `mode` 属性
   - 没引入任何 @font-face 或外部字体
4. **动效写在 CSS keyframes 里**，不要用 JS 动画库
5. **保留所有现有功能** —— template 里现有的事件绑定、数据绑定、组件调用都不能丢
6. **末尾附 30-60 字的"视觉记忆点说明"**：解释这个页面 1 个最关键的设计决策

---

## 5. 反面清单（**skill 默认会犯这些错，提前堵死**）

| 别做 | 原因 |
|---|---|
| 用 Inter / Space Grotesk / Roboto | 小程序根本加载不了，且这是 skill 自己的禁令 |
| 紫色渐变 + 白底（generic AI 风） | skill 自己禁令 |
| @import 任何外部 CSS / Google Fonts | 小程序加载不到 |
| 用 SVG 图标内联到 template | 小程序 view 不直接渲染 SVG，要么 image+base64 要么 u-icon |
| 用 `position: fixed` 不加 z-index 且不测安全区 | 真机底部会被遮 |
| 用 `box-shadow` 颜色硬编码（如 `rgba(0,0,0,0.1)`） | 切到墨夜主题会有问题，必须用 `var(--shadow-card)` |
| 给 `<page>` 元素绑 class | 小程序 `<page>` 不能加 class，主题切换会失效 |
| 用 `u-button` 的 `@tap` | uView 不转发，必须 `@click` |
| 改 `pages.json` 路径结构 | 这是路由配置，重构 UI 不动它 |
| 一次重构超过 1 个页面 | skill 会失焦，记忆点稀释 |

---

## 6. 怎么开始（用户操作）

新开一次会话，按这个模板贴：

```
我要重构 lifeRecord 微信小程序的 [页面名]。

请先读这两份文件：
1. docs/plans/UI重构提示词-frontend-design.md（项目背景 + 限制 + 美学方向）
2. docs/standards/UI 设计规范.md（token 和组件命名）

然后用 /frontend-design 按【方向 A 纸感日记本】重构 miniapp/src/pages/[路径].vue。
保留所有现有功能，只动视觉层。

如果需要新增 token，先列出来等我确认。
```

> 替换 `[页面名]` 和 `[路径]` 即可。每次只做一页。

---

## 7. 验收清单（每个重构 PR 自检）

- [ ] `npm run type-check` 通过
- [ ] 微信开发者工具真机预览不报错
- [ ] 切换 4 套主题视觉都成立（不只在 clay 下好看）
- [ ] 所有颜色/圆角/间距/字号都是 `var(--*)`
- [ ] 没有引入新依赖（除非提前讨论）
- [ ] 现有事件绑定、props、emits 全部保留
- [ ] 视觉记忆点说明已写

---

## 8. 一句话给 skill 听

**这是一个温暖、克制、长期陪伴用户的生活记录小程序。技术上是 uni-app 微信小程序，不是 Web，不能用 Web 字体和 JS 动画库。视觉上请按指定的「纸感日记本」或「极简编辑器」方向坚定执行，不要折中。每次只做一页，做出"打开就记得住"的细节。**
