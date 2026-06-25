# lifeRecord · Web 端 设计文档

> 状态：Phase 0 已完成（项目脚手架 + 布局）  
> 基底：`nuxt3-awesome-starter`（1800+ ⭐）  
> 定位：面向 C 端的博客风格生活记录全功能应用

---

## 目录

1. [产品全景](#1-产品全景)
2. [技术选型](#2-技术选型)
3. [设计系统](#3-设计系统)
4. [导航架构](#4-导航架构)
5. [模块设计](#5-模块设计)
6. [组件树](#6-组件树)
7. [路由设计](#7-路由设计)
8. [API 对接](#8-api-对接)
9. [鉴权方案](#9-鉴权方案)
10. [后端缺口](#10-后端缺口)
11. [实施路线](#11-实施路线)

---

## 1. 产品全景

### 1.1 定位

生活记录 Web 端 —— 不只是日记。是一个集日记、记账、打卡、纪念日、相册、学习笔记于一体的个人生活管理平台。博客风格的 UI 设计，模块化的功能架构。

### 1.2 与现有项目的关系

```
                    ┌─────────────────┐
                    │  Spring Boot API │
                    │  record/         │
                    └───┬──────┬───────┘
                        │      │
          ┌─────────────┘      └──────────────┐
          ▼                                   ▼
 ┌────────────────┐                  ┌────────────────┐
 │ 小程序 (miniapp)│                  │  Web 端 (web/)  │
 │ uni-app         │                  │ Nuxt 3          │
 └────────────────┘                  └────────────────┘

共用：用户体系、全部业务 API、文件上传
独立：UI 设计、前端路由、部署域名
```

### 1.3 模块清单

| 模块 | 小程序已有 | Web 端 | 状态 |
|------|:---------:|:------:|------|
| 📝 日记 | ✅ | ✅ | 后端就绪 |
| 💰 记账 | ✅ | ✅ | 后端就绪 |
| ✅ 打卡 | ✅ | ✅ | 后端就绪 |
| 📅 纪念日 | ✅ | ✅ | 后端就绪 |
| 🏷 标签 | ✅ | ✅ | 后端就绪 |
| 📖 回忆（那年今日） | ✅ | ✅ | 后端就绪 |
| 🤖 AI 日记分析 & 摘要 | ❌ | 🆕 | 需新建后端 |
| 🖼 相册 | ❌ | 🆕 | 需新建后端 |
| 📚 学习笔记 | ❌ | 🆕 | 需新建后端 |
| 🤖 AI 对话 | ✅ | ⏳ | 后端就绪，可后期加 |
| 👤 用户中心 | ✅ | ✅ | 后端就绪 |

---

## 2. 技术选型

### 2.1 基底：nuxt3-awesome-starter

基于 [viandwi24/nuxt3-awesome-starter](https://github.com/viandwi24/nuxt3-awesome-starter)（1800+ ⭐）二开，自带：

| 自带能力 | 说明 |
|----------|------|
| Nuxt 3 文件路由 | `pages/` 目录自动生成路由，无需手写 router |
| Tailwind CSS 3 | 已配好暗色模式 + 自定义主题色 |
| Headless UI | 无样式 Dialog/Menu/Listbox/Switch 等 |
| Pinia | 状态管理，stores 自动导入 |
| VueUse | 工具 composables |
| Nuxt Icon | Iconify 图标，100+ 图标集可用 |
| 暗色模式 | `@nuxtjs/color-mode`，系统感知 + 手动切换 |
| Awesome UI 组件 | Button/Card/Form/Tabs/ActionSheet/AlertBanner 等 |
| Page 布局 | Navbar + Content + Footer 的标准页面布局 |
| SCSS 支持 | + 全局滚动条样式 + 页面过渡动画 |

### 2.2 我们加的

| 类别 | 选型 | 说明 |
|------|------|------|
| HTTP | `$http` (fetch 封装) | Nuxt SSR 兼容，不依赖 axios 的浏览器端 API |
| Token 管理 | `utils/auth.ts` | getToken/setToken/removeToken |
| 滚动 | `useScroll()` composable | scrollY / ratio / scrolled / atBottom |
| 侧边栏布局 | `layouts/sidebar.vue` | 固定侧边栏 + FloatingHeader + 内容区 |

### 2.3 后续加的（按 Phase）

| Phase | 加什么 |
|-------|--------|
| P2 | Tiptap 富文本编辑器、motion-v 滚动动画 |
| P3 | ECharts 图表 |
| P1 | tsParticles 粒子（登录页） |

### 2.4 不用什么

| 不用 | 原因 |
|------|------|
| Element Plus | C 端博客风格不需要中后台组件库 |
| Nuxt Content (`@nuxt/content`) | 数据来自后端 API，不用 markdown 文件管理 |
| Quill / TinyMCE | 太重，Tiptap 更现代 |

---

## 3. 设计系统

### 3.1 设计理念

**「极简 · 纸张感 · 呼吸感」**

- 色彩：白底黑字，蓝色点缀，大量留白
- 排版：宽行距、大字号正文、清晰的视觉层级
- 动效：克制的微交互，不喧宾夺主
- 布局：侧边栏导航 + 内容区，适配宽屏

### 3.2 色彩

```
品牌色
  primary:    #2563EB  主按钮、选中态、链接
  primary-50: #EFF6FF  浅蓝背景

中性色
  bg:         #F8FAFC  页面背景
  surface:    #FFFFFF  卡片背景
  border:     #E2E8F0  分割线

文字
  text-main:  #0F172A  标题
  text-body:  #334155  正文
  text-muted: #94A3B8  辅助信息

模块色（侧边栏图标 + 页面 Hero accent）
  日记:      #3B82F6  蓝
  记账:      #10B981  绿
  打卡:      #8B5CF6  紫
  纪念日:    #F59E0B  琥珀
  相册:      #EC4899  粉
  学习:      #06B6D4  青

状态色
  success:   #10B981
  warning:   #F59E0B
  danger:    #EF4444
```

### 3.3 字体

| 用途 | Font Family |
|------|------------|
| 中文正文 | `"Noto Sans SC", "PingFang SC", sans-serif` |
| 英文/数字 | `"Inter", system-ui, sans-serif` |
| 代码 | `"JetBrains Mono", monospace` |

### 3.4 动效规范

| 场景 | 实现 | 时长 |
|------|------|------|
| 卡片入场 | motion-v `:initial="{ y: 20, opacity: 0 }"` `:visible` | 400ms |
| hover 浮起 | CSS `hover:scale-[1.02] shadow-lg` | 200ms |
| 路由切换 | Vue `<Transition name="page">` fade | 300ms |
| Header 显隐 | scroll 监听 → 滚动超过一屏 → fade-in + translateY(0) | 300ms |
| 粒子背景 | tsParticles | 持续 |
| 点赞爆发 | CSS `@keyframes` + `scale` | 400ms |
| 侧边栏收起 | CSS transition width | 200ms |
| 进度条滚动 | CSS `scaleX` + JS scroll 百分比 | 实时 |

---

## 4. 导航架构

### 4.1 整体布局

```
┌──────┬──────────────────────────────────────────────────┐
│      │  ┌─ FloatingHeader ────────────────────────┐     │
│      │  │  半透明毛玻璃 · 滚动进度条 · scroll 触发    │     │
│ 📝 日记│  └────────────────────────────────────────────┘     │
│ 💰 记账│                                                  │
│ ✅ 打卡│              模块内容区                             │
│ 📅 纪念日│             (NuxtPage)                           │
│ 📖 回忆│                                                  │
│ ──────│                                                  │
│ 🌓 主题│                                                  │
│ ◀ 收起│                                                  │
└──────┴──────────────────────────────────────────────────┘
 240px                        flex-1
```

**实现：`layouts/sidebar.vue`**

- 固定左侧导航（可折叠为 64px 仅图标）
- Iconify 图标集（Heroicons）
- 暗色模式切换（系统感知 + 手动）
- FloatingHeader：默认隐藏，滚动超一屏出现，毛玻璃背景 + 进度条
- useScroll() composable 驱动所有滚动联动

### 4.2 两种布局并存

| 布局 | 文件 | 用途 |
|------|------|------|
| `sidebar` | `layouts/sidebar.vue` | 我们的业务页面（首页/日记/记账...） |
| `page` | `layouts/page.vue` | 原模板页面（post/ 等 demo 页保留） |

页面通过 `definePageMeta({ layout: 'sidebar' })` 选择布局。

---

### 4.6 首页（Dashboard）

不直接进日记列表，而是做一个仪表盘首页：

```
┌──────────────────────────────────────────────────┐
│  👋 下午好，小林                                    │
│  今天是 2026年6月24日 · 周三                         │
├──────────┬──────────┬──────────┬─────────────────┤
│  📝 日记  │  💰 记账  │  ✅ 打卡  │  📅 纪念日       │
│  3 篇     │  本月支出   │  2/4 完成 │  最近: 生日      │
│  最近:    │  ¥2,340  │  连续3天  │  还有3天        │
│  「海边」  │  收入 ¥500│          │                │
├──────────┴──────────┴──────────┴─────────────────┤
│  最近日记                            [写日记 →]   │
│  ┌──────────────────────────────────────────┐    │
│  │ 6.23 · 今天去了深圳湾 · 😊 ☀️ #旅行        │    │
│  └──────────────────────────────────────────┘    │
│  ┌──────────────────────────────────────────┐    │
│  │ 6.22 · 深夜的思考 · 😌 🌙 #成长           │    │
│  └──────────────────────────────────────────┘    │
├──────────────────────────────────────────────────┤
│  最近照片                            [查看更多 →] │
│  [图1] [图2] [图3] [图4] [图5]                   │
└──────────────────────────────────────────────────┘
```

---

### 4.7 触底加载（无限滚动）

**原则：全站所有列表页统一使用触底加载，不使用传统分页器。**

**实现方式：**

```
useInfiniteScroll composable:
  监听 ScrollProvider 的 isAtBottom
  isAtBottom = true 且 !loading 且 hasMore
    → pageNum++
    → fetchList({ pageNum, pageSize: 12 })
    → 追加到 list 尾部（非替换）
    → hasMore = (返回数据量 === pageSize)
```

**各模块列表页触底参数：**

| 页面 | pageSize | 触发条件 |
|------|----------|----------|
| 日记广场 / 我的日记 | 12 | isAtBottom + hasMore |
| 记账流水 | 20 | isAtBottom + hasMore |
| 打卡记录 | 20 | isAtBottom + hasMore |
| 纪念日列表 | 20 | isAtBottom + hasMore |
| 相册照片墙 | 24 | isAtBottom + hasMore |
| 学习笔记 | 12 | isAtBottom + hasMore |

**列表底部状态组件：**

```
加载中：   ┌─────────────────────┐
          │      ⟳ 加载中...     │
          └─────────────────────┘

没有更多： ┌─────────────────────┐
          │     — 到底了 —       │
          └─────────────────────┘
```

**ScrollProvider 触底检测：**
```
isAtBottom = (scrollHeight - scrollTop - clientHeight) < 100px
debounce 200ms，防止重复触发
```

---

## 5. 模块设计

### 5.1 📝 日记

**子页面：**

| 路由 | 页面 | 说明 |
|------|------|------|
| `/diary` | 日记列表 | 卡片网格 + 筛选（全部/公开/私密） + 搜索 + **触底加载** |
| `/diary/:id` | 日记详情 | **顶部 AI 分析卡片** → 全文阅读 → 图片 lightbox → 标签 → 位置 → 点赞评论 |
| `/diary/write` | 写日记 | Tiptap 编辑器，天气/心情，图片上传，标签，可见性 |
| `/diary/edit/:id` | 编辑日记 | 复用编辑器组件，预填数据 |

**日记卡片：**
```
┌─────────────────────┐
│                     │
│  [封面图片/渐变色]    │
│                     │
│  今天去了深圳湾公园    │
│  今天天气特别好，      │
│  下午出发去了深圳湾... │
│                     │
│  😊 ☀️  2天前   #旅行  │
└─────────────────────┘
```

**日记详情页（含 AI 分析）：**
```
┌─────────────────────────────────────────────────────────┐
│  [Header]  ← 返回                                       │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─ 🤖 AI 智能分析 ─────────────────────────────────┐   │
│  │  [分析中...]  或  [分析结果]                       │   │
│  │                                                    │   │
│  │  📋 摘要：今天在海边度过了一个愉快的下午...           │   │
│  │  🎭 情绪基调：愉悦、平静                             │   │
│  │  🏷 关键主题：旅行、自然、摄影、生活感悟              │   │
│  │  📊 字数 486 · 图片 3 张                             │   │
│  │                                        [重新分析]   │   │
│  └────────────────────────────────────────────────────┘   │
│                                                         │
│  2026年6月23日 · 周一                                     │
│  今天去了深圳湾公园                                        │
│  ☀️ 晴天  😊 开心  👁 公开                                │
│                                                         │
│  [正文内容 · prose 排版]                                  │
│  ...                                                    │
│                                                         │
│  [图片1] [图片2] [图片3] ← 点击放大                       │
│                                                         │
│  #旅行  #摄影  #生活                                     │
│  📍 深圳湾公园                                           │
│  ❤️ 12  💬 3                                            │
└─────────────────────────────────────────────────────────┘
```

**AI 分析卡片状态：**

```
加载中：
  ┌─ 🤖 AI 智能分析 ──────────────┐
  │  ⟳ 正在分析日记内容...          │
  │  [骨架屏闪烁]                   │
  └────────────────────────────────┘

完成：
  ┌─ 🤖 AI 智能分析 ──────────────┐
  │  📋 摘要：一段简洁的总结...      │
  │  🎭 情绪：高兴 / 平静 / 悲伤    │
  │  🏷 主题：标签1、标签2、标签3    │
  │  📊 字数 · 图片数              │
  │                    [重新分析]  │
  └────────────────────────────────┘

失败：
  ┌─ 🤖 AI 智能分析 ──────────────┐
  │  ⚠️ 分析失败，请稍后重试        │
  │  [重试]                        │
  └────────────────────────────────┘

未分析：
  ┌─ 🤖 AI 智能分析 ──────────────┐
  │  💡 让 AI 帮你读懂这篇日记       │
  │  自动生成摘要、分析情绪和主题    │
  │  [开始分析]                    │
  └────────────────────────────────┘
```

---

### 5.1b 🤖 AI 日记分析 & 摘要

**功能说明：** 打开任意日记详情页时，可在顶部看到 AI 对这篇日记的分析。AI 会阅读日记全文（包括图片描述），生成摘要、识别情绪基调、提取关键主题。

**流程图：**
```
用户打开日记详情
  └→ 检查该日记是否已有缓存的 AI 分析
       ├── 有缓存 → 直接展示
       └── 无缓存 → 展示"开始分析"按钮
              └→ 用户点击 [开始分析]
                   └→ POST /ai/diary-analysis
                        ├── 后端提取日记内容 + 图片 URL
                        ├── 调 AI 模型分析
                        └── 返回 { summary, mood, themes, stats }
                             └→ 前端展示 + 缓存到后端 DB
```

**后端 API 设计：**

```
POST /ai/diary-analysis
  Request:  { diaryId: Long }
  Response: {
    summary: String,       // 2-3 句话的摘要
    mood: String,          // 情绪基调（愉悦/平静/感伤/兴奋...）
    themes: List<String>,  // 关键主题（3-5 个词）
    wordCount: int,        // 正文字数
    imageCount: int,       // 图片数量
    analyzedAt: DateTime   // 分析时间
  }
```

**AI Prompt 设计：**
```
你是一位善于理解人心的日记分析助手。

请分析这篇日记，返回 JSON：

1. summary：用 2-3 句话总结这篇日记的内容（中文）
2. mood：判断作者的情绪基调（一个词：愉悦/平静/感伤/兴奋/焦虑/感动/疲惫/期待）
3. themes：提取 3-5 个关键主题词

日记标题：{title}
日记正文：{content}
包含图片：{imageCount} 张

请严格返回 JSON 格式。
```

**缓存策略：**
- 分析结果存到 `ai_diary_analysis` 表：`diary_id` + `summary` + `mood` + `themes` + `analyzed_at`
- 日记内容更新后，缓存失效，需要重新分析
- 用户可以手动点击"重新分析"强制刷新

---

### 5.2 💰 记账

**子页面：**

| 路由 | 页面 | 说明 |
|------|------|------|
| `/ledger` | 记账首页 | 月度汇总卡片 + 流水列表 + 记一笔入口 |
| `/ledger/books` | 账本管理 | 多账本列表 + 创建/编辑 |
| `/ledger/statistics` | 统计 | ECharts 年度统计、分类饼图、月度趋势 |

**记账首页布局：**
```
┌─────────────────────────────────────────────┐
│  2026 年 6 月                                │
│  ┌──────────┐ ┌──────────┐ ┌─────────────┐  │
│  │ 支出 ¥2,340│ │ 收入 ¥500 │ │ 结余 -¥1,840 │  │
│  └──────────┘ └──────────┘ └─────────────┘  │
├─────────────────────────────────────────────┤
│  6月23日 · 周一                               │
│  🍜 餐饮    午餐              -¥35           │
│  🚇 交通    地铁通勤           -¥8            │
│  ☕ 咖啡    星巴克              -¥38           │
├─────────────────────────────────────────────┤
│  6月22日 · 周日                               │
│  ...                                        │
└─────────────────────────────────────────────┘
```

---

### 5.3 ✅ 打卡

**子页面：**

| 路由 | 页面 | 说明 |
|------|------|------|
| `/checkin` | 打卡首页 | 今日任务列表 + 热力图 + 连续天数 |
| `/checkin/history` | 打卡历史 | 按日期查看历史记录 |
| `/checkin/medals` | 勋章墙 | 成就勋章展示 |

**打卡首页布局：**
```
┌─────────────────────────────────────────────┐
│  🔥 连续打卡 12 天          本月打卡 18/30 天   │
│  ┌─────────────────────────────────────┐    │
│  │  6月热力图（颜色深度 = 打卡完成度）     │    │
│  └─────────────────────────────────────┘    │
├─────────────────────────────────────────────┤
│  今日任务                        2 / 4 完成   │
│  ✅ 俯卧撑 20 个              已坚持 30 天    │
│  ✅ 阅读 30 分钟              已坚持 12 天    │
│  ⬜ 冥想 10 分钟              还剩 2 天      │
│  ⬜ 写日记                    今天未完成      │
│                              [创建任务 +]    │
└─────────────────────────────────────────────┘
```

---

### 5.4 📅 纪念日

**子页面：**

| 路由 | 页面 | 说明 |
|------|------|------|
| `/memorial` | 纪念日列表 | 卡片列表，倒计时显示 |
| `/memorial/write` | 创建纪念日 | 标题、日期、类型、每年重复、提醒 |

**纪念日卡片：**
```
┌──────────────────────┐
│  🎂                    │
│  女朋友的生日           │
│  还有 45 天             │
│  每年重复 · 8月8日      │
│  [提醒已开启]           │
└──────────────────────┘
```

---

### 5.5 📖 回忆（那年今日）

| 路由 | 页面 | 说明 |
|------|------|------|
| `/memory` | 那年今日 | 去年/前年同一天的日记、打卡、纪念日回顾 |

```
┌─────────────────────────────────────────────┐
│  2025 年的今天                                │
│  ┌──────────────────────────────────────┐    │
│  │  📝 1 篇日记                          │    │
│  │  「天气真好，去爬山了」😊 ☀️              │    │
│  └──────────────────────────────────────┘    │
│  ┌──────────────────────────────────────┐    │
│  │  ✅ 打卡了 3 项任务                    │    │
│  └──────────────────────────────────────┘    │
│                                             │
│  2024 年的今天                                │
│  ┌──────────────────────────────────────┐    │
│  │  📝 2 篇日记                          │    │
│  └──────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

---

### 5.6 🖼 相册（新建）

| 路由 | 页面 | 说明 |
|------|------|------|
| `/album` | 相册列表 | 瀑布流照片墙，按日期/标签分组 |
| `/album/:id` | 相册详情 | 单张/多张照片查看，lightbox 浏览 |
| `/album/upload` | 上传照片 | 批量拖拽上传，添加描述/标签/日期 |

**相册瀑布流：**
```
┌──────┐ ┌────────┐ ┌──────┐
│      │ │        │ │      │
│ 照片1 │ │ 照片2   │ │ 照片3 │
│      │ │        │ │      │
│ 6.23 │ │        │ │ 6.20 │
└──────┘ │ 6.22   │ └──────┘
         └────────┘
┌────────┐ ┌──────┐
│        │ │      │
│ 照片4   │ │ 照片5 │
│        │ │      │
│ 6.19   │ │ 6.18 │
└────────┘ └──────┘
```

---

### 5.7 📚 学习笔记（新建）

| 路由 | 页面 | 说明 |
|------|------|------|
| `/learn` | 笔记列表 | 笔记卡片列表，按分类/标签筛选 |
| `/learn/:id` | 笔记详情 | 富文本阅读 |
| `/learn/write` | 写笔记 | Tiptap 编辑器（复用） |
| `/learn/edit/:id` | 编辑笔记 | |

**笔记卡片：**
```
┌──────────────────────┐
│  📘 学习笔记           │
│                      │
│  React 18 源码解析     │
│  Fiber 架构的核心思想..│
│                      │
│  #前端 #React  3天前   │
└──────────────────────┘
```

---

### 5.8 👤 用户中心

| 路由 | 页面 | 说明 |
|------|------|------|
| `/settings` | 个人设置 | 资料编辑 + 账号绑定 + 密码修改 |
| `/settings/tags` | 标签管理 | 日记/记账/学习标签管理 |

**标签管理：**
```
┌─────────────────────────────────────────────┐
│  标签管理                                     │
│  ┌──────────────────────────────────────┐    │
│  │  日记标签                              │    │
│  │  #旅行  #美食  #读书  #工作  [+ 新建]    │    │
│  ├──────────────────────────────────────┤    │
│  │  记账标签                              │    │
│  │  #餐饮  #交通  #购物  #娱乐  [+ 新建]    │    │
│  ├──────────────────────────────────────┤    │
│  │  学习标签                              │    │
│  │  #前端  #后端  #设计  #算法  [+ 新建]    │    │
│  └──────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

### 5.9 🤖 AI 助手（后期）

| 路由 | 页面 | 说明 |
|------|------|------|
| `/ai` | AI 对话 | 流式聊天，账单分析，日记润色 |

---

## 6. 组件树

```
App.vue
├── AppLayout.vue                    ← 布局壳：侧边栏 + 内容区
│   ├── AppSidebar.vue               ← 左侧固定导航
│   │   ├── SidebarUser.vue          ← 用户头像 + 名称
│   │   ├── SidebarNav.vue           ← 模块导航列表
│   │   │   └── SidebarItem.vue ×N   ← 图标 + 名称 + 角标
│   │   └── SidebarFooter.vue        ← 设置/退出
│   └── <RouterView v-slot>          ← 内容区，嵌套 ScrollProvider
│       └── FloatingShell.vue        ← 全局浮层容器
│           ├── FloatingHeader.vue    ← 滚动触发悬浮 Header（磨砂玻璃）
│           │   └── ScrollProgressBar.vue  ← 滚动进度条
│           └── ScrollNavigator.vue   ← 右下角 ↑↓ 按钮
│       ├── DashboardView.vue       ← 仪表盘首页
│       │   ├── GreetingCard.vue
│       │   ├── ModuleCards.vue     ← 4 模块快捷卡片
│       │   ├── RecentDiaries.vue
│       │   └── RecentPhotos.vue
│       │
│       ├── DiaryListView.vue       ← 日记列表（触底加载）
│       │   ├── DiaryFilter.vue
│       │   ├── DiaryCard.vue ×N
│       │   └── LoadMoreSentinel.vue  ← 触底加载状态
│       ├── DiaryDetailView.vue
│       │   ├── DiaryAiAnalysis.vue  ← AI 分析卡片（4 态）
│       │   ├── DiaryHeader.vue      ← 视差标题
│       │   ├── ProseContent.vue    ← Tiptap 渲染 + typography
│       │   ├── ImageGallery.vue    ← lightbox
│       │   ├── TagChips.vue
│       │   └── LikeButton.vue      ← 粒子爆发
│       ├── DiaryWriteView.vue
│       │   ├── MoodPicker.vue
│       │   ├── WeatherPicker.vue
│       │   ├── VisibilityToggle.vue
│       │   ├── TagSelector.vue
│       │   ├── RichEditor.vue      ← Tiptap 封装（日记+学习复用）
│       │   └── ImageUploader.vue   ← 拖拽+粘贴（日记+学习+相册复用）
│       ├── DiaryEditView.vue
│       │
│       ├── LedgerIndexView.vue
│       │   ├── MonthlySummary.vue
│       │   ├── LedgerEntryList.vue
│       │   └── LedgerEntryForm.vue  ← 记一笔 Popover
│       ├── LedgerBooksView.vue
│       ├── LedgerStatsView.vue      ← ECharts
│       │
│       ├── CheckinIndexView.vue
│       │   ├── StreakBanner.vue
│       │   ├── HeatmapCalendar.vue
│       │   └── TaskItem.vue ×N
│       ├── CheckinHistoryView.vue
│       ├── CheckinMedalsView.vue
│       │
│       ├── MemorialListView.vue
│       │   └── MemorialCard.vue ×N
│       ├── MemorialWriteView.vue
│       │
│       ├── MemoryView.vue           ← 那年今日
│       │
│       ├── AiView.vue               ← AI 对话（后期）
│       ├── AlbumListView.vue        ← 瀑布流
│       │   └── PhotoCard.vue ×N
│       ├── AlbumDetailView.vue
│       ├── AlbumUploadView.vue
│       │
│       ├── LearnListView.vue        ← 笔记列表
│       │   └── NoteCard.vue ×N
│       ├── LearnDetailView.vue
│       ├── LearnWriteView.vue
│       ├── LearnEditView.vue
│       │
│       ├── SettingsView.vue
│       ├── TagsManageView.vue
│       │
│       ├── LoginView.vue
│       │   ├── ParticleBackground.vue
│       │   ├── OAuthButtons.vue
│       │   └── LoginForm.vue
│       │
│       └── NotFoundView.vue
│
├── <Toast />                        ← 全局通知
├── <ImageLightbox />                ← 全局 lightbox
│
composables/
├── useScroll.ts           ← 全局滚动状态（ScrollProvider 底层）
├── useInfiniteScroll.ts   ← 触底加载逻辑（pageNum, hasMore, loadMore）
└── useAuth.ts             ← 登录态
```

---

## 7. 路由设计

```
/                           DashboardView   仪表盘首页
/login                      LoginView       登录
/auth/callback              OAuth 回调

日记
/diary                      DiaryListView   日记列表
/diary/:id                  DiaryDetailView 日记详情
/diary/write                DiaryWriteView  写日记
/diary/edit/:id             DiaryEditView   编辑日记

记账
/ledger                     LedgerIndexView 记账首页
/ledger/books               LedgerBooksView 账本管理
/ledger/stats               LedgerStatsView 统计图表

打卡
/checkin                    CheckinIndexView   打卡首页
/checkin/history            CheckinHistoryView 历史
/checkin/medals             CheckinMedalsView  勋章

纪念日
/memorial                   MemorialListView   列表
/memorial/write             MemorialWriteView  创建

回忆
/memory                     MemoryView         那年今日

AI
/ai                         AiView             AI 对话
（日记分析入口在日记详情页顶部，不单独路由）

相册（新建后端）
/album                      AlbumListView      照片墙
/album/:id                  AlbumDetailView    详情
/album/upload               AlbumUploadView    上传

学习笔记（新建后端）
/learn                      LearnListView      笔记列表
/learn/:id                  LearnDetailView    笔记详情
/learn/write                LearnWriteView     写笔记
/learn/edit/:id             LearnEditView      编辑

用户
/settings                   SettingsView       个人设置
/settings/tags              TagsManageView     标签管理

兜底
/:pathMatch(.*)*            NotFoundView       404
```

**路由守卫：**

```typescript
const publicPages = ['/', '/login']
const publicDiary = /^\/diary\/\d+$/  // 公开日记详情可看

router.beforeEach((to) => {
  if (publicPages.includes(to.path)) return true
  if (publicDiary.test(to.path)) return true
  if (!authStore.isLoggedIn) return `/login?redirect=${to.fullPath}`
  return true
})
```

---

## 8. API 对接

### 8.1 已有后端接口（直接复用）

| 模块 | 接口 | 方法 |
|------|------|------|
| 认证 | `/auth/login` `/auth/register` `/auth/refresh` `/auth/logout` | POST |
| OAuth | `/auth/github/authorize` `/auth/google/authorize` `/auth/{provider}/callback` | GET |
| 用户 | `/users/profile` `/users/profile/update` `/users/identity/bind` | GET/PUT/POST |
| 日记 | `/diaries/list` `/diaries/create` `/diaries/detail/{id}` `/diaries/update/{id}` `/diaries/delete/{id}` | GET/POST/GET/PUT/DELETE |
| AI 分析 | `/ai/diary-analysis` | POST（新增） |
| AI 对话 | `/ai/chat/stream` `/ai/conversations` | POST/GET |
| 记账 | `/ledger/entries/month` `/ledger/entries/create` `/books/list` `/ledger/statistics/year` | GET/POST/GET/GET |
| 打卡 | `/checkin/tasks/list` `/checkin/records/list` `/checkin/records/check` `/checkin/heatmap` | GET/GET/POST/GET |
| 纪念日 | `/memorial-days/list` `/memorial-days/create` `/memorial-days/update/{id}` `/memorial-days/delete/{id}` | GET/POST/PUT/DELETE |
| 回忆 | `/memories/on-this-day` | GET |
| 标签 | `/tags/list` `/tags/create` | GET/POST |
| 文件 | `/files/upload` | POST |

### 8.2 需要后端改动

| 需求 | 改动 |
|------|------|
| 公开日记匿名访问 | `GET /diaries/list`、`GET /diaries/detail/{id}` → `permitAll()` |
| 日记广场返回公开日记 | 无 token 时强制 `visibility=PUBLIC` |
| OAuth 回调跳转 Web 端 | `frontendCallbackUrl` 改指 Web 端地址 |
| **相册模块** | 新建 `album` 模块：`/albums/list`、`/albums/create`、`/albums/detail/{id}`、`/albums/delete/{id}` |
| **学习笔记模块** | 新建 `learn` 模块：`/notes/list`、`/notes/create`、`/notes/detail/{id}`、`/notes/update/{id}`、`/notes/delete/{id}` |

---

## 9. 鉴权方案

```
登录方式：
  ├── GitHub OAuth ──→ /auth/github/authorize ──→ 回调 → 前端 /auth/callback
  ├── Google OAuth ──→ /auth/google/authorize ──→ 同上
  └── 账号密码    ──→ POST /auth/login

Token 存储：
  Cookie: authorized-token = { accessToken, expires, refreshToken }
  localStorage: user-info = { roles, username, avatar }

请求鉴权：
  Axios interceptor:
    ├── 自动拼 Authorization: Bearer {accessToken}
    ├── 过期自动调 /auth/refresh 换新 token
    └── 401 → 清 token → 跳 /login

路由保护：
  /、/login、/diary/:id          → 无需登录
  其他所有页面                     → 需登录
```

---

## 10. 后端缺口

### 10.1 需新建的模块

**AI 日记分析（AiService 新增方法）：**

```
POST /ai/diary-analysis
  Request:  { diaryId: Long }
  Response: { summary, mood, themes, wordCount, imageCount, analyzedAt }

实现要点：
  - 查 Diary 内容 + mediaPaths
  - 拼接 prompt（标题 + 正文 + 图片数量）
  - 调现有 AiService 的 LLM 客户端
  - 结果写入 ai_diary_analysis 表（缓存）
  - 日记更新时通过 diary update 链路清除缓存
```

**相册（Album）：**

```
Entity: Album
  id, user_id, title, description, cover_path, visibility, created_at

Entity: AlbumPhoto
  id, album_id, file_path, sort_order, description, taken_at, created_at

API:
  GET    /albums/list         分页查相册
  POST   /albums/create       创建相册
  GET    /albums/detail/{id}  相册详情（含照片列表）
  PUT    /albums/update/{id}  更新相册
  DELETE /albums/delete/{id}  删除相册
  POST   /albums/{id}/photos  上传照片到相册
  DELETE /albums/photos/{id}  删除单张照片
```

**学习笔记（Learn）：**

```
Entity: Note
  id, user_id, title, content (富文本HTML/JSON),
  category, visibility, created_at, updated_at

Entity: NoteTag
  note_id, tag_id

API:
  GET    /notes/list          分页查笔记（支持分类/标签筛选）
  POST   /notes/create        创建笔记
  GET    /notes/detail/{id}   笔记详情
  PUT    /notes/update/{id}   更新笔记
  DELETE /notes/delete/{id}   删除笔记
```

### 10.2 需修改的配置

```
SecurityConfig:
  .requestMatchers(GET, "/diaries/list").permitAll()
  .requestMatchers(GET, "/diaries/detail/**").permitAll()

application-*.yaml:
  app.oauth.frontend-callback-url: https://xxx.com/auth/callback
```

---

## 11. 实施路线

### 总览

| Phase | 内容 | 时间 | 依赖 |
|-------|------|------|------|
| **P0** | 项目地基 | 1 天 | — |
| **P1** | 登录系统 | 1 天 | P0 |
| **P2** | 日记模块（列表+详情+编辑+AI分析） | 2.5 天 | P1 |
| **P3** | 记账模块 | 1.5 天 | P1 |
| **P4** | 打卡模块 | 1 天 | P1 |
| **P5** | 纪念日 + 回忆 | 1 天 | P1 |
| **P6** | 仪表盘首页 | 0.5 天 | P2-P5 |
| **P7** | 用户中心 + 标签管理 | 0.5 天 | P1 |
| **P8** | 后端：相册模块 + 学习模块 | 2 天 | — |
| **P9** | 相册 Web 端 | 1 天 | P8 |
| **P10** | 学习笔记 Web 端 | 1 天 | P8 |
| **P11** | AI 助手（可选） | 1 天 | P1 |
| **P12** | 部署上线 | 0.5 天 | P2-P11 |

### P0 — 项目地基（1 天）

```
□ Vite + Vue 3 + TS 脚手架
□ Tailwind CSS + typography 配置
□ 全局 Layout（Sidebar + Header + <RouterView>）
□ 路由 scaffold（所有模块路由 + 守卫）
□ Axios 层（复用 admin 的拦截器模式）
□ auth store（login/logout/refreshToken）
□ 公共组件：AppSidebar, AppHeader, Toast, ImageLightbox
□ motion-v + tsParticles + ECharts 安装配置
```

### P1 — 登录系统（1 天）

```
□ LoginView（粒子背景 + OAuth 按钮 + 账号登录）
□ OAuth 回调页
□ 账号密码登录/注册表单
□ Header 登录态切换
```

### P2 — 日记模块（2.5 天）

```
□ DiaryCard 组件（封面/纯文字两种模式 + hover 浮起动画）
□ DiaryListView（筛选 + 搜索 + 分页 + 卡片网格）
□ DiaryDetailView（顶部 AI 分析卡片 + prose 排版 + lightbox + 点赞动效）
□ DiaryAiAnalysis 组件（4 态：未分析/加载中/完成/失败）
□ AI 分析缓存逻辑（首次手动触发，已有缓存自动展示）
□ DiaryWriteView（Tiptap 编辑器 + 天气/心情 + 图片上传 + 标签）
□ DiaryEditView（复用编辑器组件）
□ 后端：POST /ai/diary-analysis（AiService 新增方法 + ai_diary_analysis 表）
```

### P3 — 记账模块（1.5 天）

```
□ MonthlySummary 卡片
□ LedgerEntryList（按日期分组）
□ LedgerEntryForm（记一笔 Popover/Dialog）
□ LedgerBooksView（账本管理）
□ LedgerStatsView（ECharts 图表）
```

### P4 — 打卡模块（1 天）

```
□ TaskItem 组件（勾选动画）
□ HeatmapCalendar 组件
□ StreakBanner 组件
□ CheckinIndexView
□ CheckinMedalsView（勋章墙）
```

### P5 — 纪念日 + 回忆（1 天）

```
□ MemorialCard 组件（倒计时显示）
□ MemorialListView + MemorialWriteView
□ MemoryView（那年今日）
```

### P6 — 仪表盘首页（0.5 天）

```
□ DashboardView
□ ModuleCards（4 模块概览卡片）
□ RecentDiaries + RecentPhotos
```

### P7 — 用户中心（0.5 天）

```
□ SettingsView（资料编辑 + 账号绑定）
□ TagsManageView（标签管理）
```

### P8 — 后端新模块（2 天，后端）

```
□ Album 模块（Entity + Mapper + Service + Controller）
□ Learn/Note 模块（同上）
□ SecurityConfig 公开日记白名单
□ OAuth 回调适配 Web 端
```

### P9-P10 — 相册 + 学习 Web 端（各 1 天）

```
□ AlbumListView（瀑布流照片墙）
□ AlbumUploadView（批量上传 + 拖拽）
□ LearnListView（笔记卡片列表）
□ LearnWriteView（复用 Tiptap）
```

---

> **本文档是 Web 端的唯一设计真相源。页面、组件、路由、API 均以此为准。任何偏离需先更新本文档。**
