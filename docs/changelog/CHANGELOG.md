# lifeRecord 变更记录

> 单一来源的变更日志，**按时间倒序**追加。每条对应一个里程碑式提交或一段集中工作，
> 不与每个微小 commit 一一对应；细节回溯请直接看 git log。
>
> 新增条目格式：`## YYYY-MM-DD · 标题`，正文用「总览 / 主要内容 / 关键决策（可选）/ 相关坑（可选）」四段式。

---

## 2026-05-08 · 主题色补完 + 鉴权无感登录 + 生产环境准备就绪

> Commit: `3db56fb`

### 总览

主题落地的最后一公里 + 鉴权三层兜底实现"无感重登" + 把上线生产所需的配置都准备好。
约 455 行新增、142 行删除，32 个文件。

### 主要内容

#### 1. 主题 token 完善

- 新增 `--color-text-neutral`：用户输入内容、代码等"功能性"文字用，不带主题色调
  - 浅色主题（暖陶/薄荷/紫雾）= `#1d1d1f`（接近黑）
  - 暗色主题（墨夜）= `#f5f5f7`（接近白）
- 新增 `composables/useThemeColors.ts`：给 ECharts 等 canvas 类组件用具体颜色字符串（canvas 不解析 CSS 变量）
- 改造 `ledger-year-charts/index.vue`：所有 chart options（坐标轴/图例/数据色/网格）都用 `colors.value.*`，主题切换时 chart 自动重渲染
- 全站再清 ~400 处剩余硬编码颜色（4 轮批量替换：高频 hex → 父背景 → 文本色/阴影 → 收尾）

#### 2. 鉴权无感登录

`request.ts` 401 拦截器改为**三层兜底**：

1. 第一层：本地有 refreshToken → `/auth/refresh` 换新 access
2. 第二层：refresh 失败 → 自动 `wx.login()` + `/auth/wx-login` 重新拿 token
3. 第三层：彻底失败 → toast "登录已失效，请重新登录" + reLaunch `/pages/auth/login`

前两层用户完全无感，原请求自动重放。`redirectToLogin()` 跳的目标改为登录页（之前是首页）。

#### 3. 生产环境准备

- `.env.production` 域名 → `https://recordlife.top/api`
- `manifest.json` mp-weixin 加 `enhance: true`（处理 ES2020 可选链等）+ `debug: true`（真机弹 vConsole）
- `vite.config.ts` 加 `esbuild.target: 'es2017'` —— 修真机的 `Unexpected token ?` 报错（vendor.js 里 ES2020 `??` 真机 JS 引擎不认）
- `application-prod.yaml` 新增完整 `logging` 段：写文件 `./logs/record.log`、滚动（10MB × 14 份）、级别细分

#### 4. 定位调试与精度

- `wx.getLocation` 默认 `isHighAccuracy: true`：走真 GPS，精度到米级（之前默认是大致区域）
- `LocationPicker` 加"🔍 显示原始经纬度（调试）"按钮：直接调 `wx.getLocation` 不走后端，对比真实位置 vs 后端逆地址解析

#### 5. 文档更新

- `UI 设计规范.md` 加 §4.0 区分"装饰性 vs 功能性文字"
- `todo.md` 标记"无感登录"已实现

### 关键发现

- 微信小程序真机 ES2020 支持不完整，`??` 和 `?.` 在某些版本会报 `SyntaxError: Unexpected token ?`，必须在 vite + manifest 两层降级
- 之前 401 → 跳首页是设计漏洞：用户体验上没有"自动重登"的反馈
- WeChat dev tool 的 `enhance` 编译跟 vite 的 target 设置是两层独立的 transform，要同时生效

---

## 2026-05-08 · 线上部署到腾讯云 + nginx 反向代理

> 运维侧操作，无对应 commit；过程中 docker-compose / Dockerfile / nginx 配置都做了同步整理。

### 总览

把后端服务从本地开发环境真正部署到腾讯云 4 核 4G 服务器（IP `123.207.210.150`），
通过 nginx 容器反向代理给 `https://recordlife.top` 提供 HTTPS API 入口。

### 部署架构

```text
微信小程序                          腾讯云 ECS（123.207.210.150）
─────────────                      ────────────────────────────────
HTTPS 请求    ────────►  nginx 容器（443 端口）
  recordlife.top                    │
                                    │  proxy_pass http://record-backend:8080/
                                    ▼
                                  record-backend 容器（Spring Boot 8080）
                                    │
                                    ├──► mysql 容器（3306）
                                    └──► redis 容器（6379）

图片直接打 OSS                    阿里云 OSS（不在这台服务器）
```

### docker-compose.yml 关键约定

- 4 个服务：`mysql / redis / nginx / record-backend`，全部走 compose 默认网络互联
- nginx 用容器名 `record-backend` 解析后端，不写硬编码 IP（容器重启 IP 会变）
- 后端 `REDIS_HOST: redis`、`DB_URL: jdbc:mysql://mysql:3306/...`（不能用 `127.0.0.1`，那是容器自己）
- nginx 挂载：`./docker/nginx/conf.d` + `./docker/nginx/ssl:/etc/nginx/ssl:ro` + 项目根的 `recordFront/`（其实小程序产物不需要 nginx 托管，这条历史遗留可清）

### SSL 证书

- 来源：腾讯云免费证书（一年期）
- 路径：宿主机 `/www/wwwroot/myproject/docker/nginx/ssl/recordlife.top.{pem,key}`
- 证书 `chmod 644`，私钥 `chmod 600`

### nginx 关键配置

- 80 强转 443（HTTP 不允许）
- `location ^~ /api/` 反代 + `proxy_pass http://record-backend:8080/;` **末尾斜线必填**，剥掉 `/api/` 前缀（后端没 `/api` context-path）
- SSE 流式聊天：`proxy_buffering off; proxy_cache off; proxy_request_buffering off;` + `proxy_read_timeout 300s`
- 浏览器访问根路径返回 friendly 提示（防止扫到默认 nginx 欢迎页）

### 后端 Dockerfile

- 基础镜像 `openjdk:21-jdk-slim`
- jar 路径 `recordJar/record-0.0.1-SNAPSHOT.jar`
- `JAVA_OPTS=-Xmx1g -Xms512m -XX:+UseG1GC`
- 时区 `Asia/Shanghai`
- 日志写到 `/app/logs/record.log`，宿主机挂出来 `tail -f`

### 路径前缀约定

- 前端 `.env.production` 的 API base：`https://recordlife.top/api`
- 前端代码 `request({ url: '/auth/wx-login' })` → 实际访问 `https://recordlife.top/api/auth/wx-login`
- nginx 剥 `/api/` → 后端拿到 `/auth/wx-login` → controller `@RequestMapping("/auth")` 命中

### 微信公众平台必须配置

后台 → 开发管理 → 服务器域名：

| 类型 | 值 |
|---|---|
| request 合法域名 | `https://recordlife.top` |
| uploadFile 合法域名 | `https://wyhosskey.oss-cn-hangzhou.aliyuncs.com` |
| downloadFile 合法域名 | `https://wyhosskey.oss-cn-hangzhou.aliyuncs.com` |

### 踩过的坑

- `docker compose up -d` 不会重建已有容器，加新 volume 必须 `down + up` 或 `--force-recreate`
- `docker exec nginx nginx -s reload` 在容器 restart 状态下不能用，要先看 `docker logs nginx` 找崩溃原因
- 容器里 `127.0.0.1` 是容器自己，不是宿主机；要用容器名通信
- 旧 `nginx http2` 写法 `listen 443 ssl http2;` 在新版 nginx 上 deprecated，改为 `listen 443 ssl;\n http2 on;`
- `nginx.conf` 里 `proxy_pass http://record-backend:8080/;` **末尾斜线**决定是否剥前缀，丢了就 404

---

## 2026-05-07 · UI 设计 token 体系 + 多主题切换基础

> Commit: `031fefa`
> ⚠️ 注：多主题切换功能已于 2026-05-27 撤回（详见 git log `2069d0a`），本条仅作为历史记录保留。

### 总览

把硬编码颜色从 `theme.scss` 抽离成完整 CSS 变量 token，
建立 4 套主题切换机制，并把切换器接到"我的"页面。
约 1500 行新增、420 行删除，37 个文件。

### 主要内容

#### 1. 设计规范文档

新增 `docs/standards/UI 设计规范.md`，13 节内容：
- 设计原则、token 取值阶梯
- 主题切换实现机制
- 页面/组件/页面文件夹约定
- loading / 空 / 错误 / 正常 4 态系统
- 重构路线（7 步）

#### 2. Token 三件套

- `styles/tokens.scss` —— 原子 token：圆角/间距/字号/字重/动画时长（跨主题不变）
- `styles/themes/{clay,mint,lilac,ink}.scss` —— 4 套主题色变量
- `styles/globals.scss` —— 老 `theme.scss` 改名 + 把所有硬编码颜色替换为 `var(--color-*)`

#### 3. 主题切换机制

- `stores/theme.ts` —— Pinia 状态 + 本地存储持久化
- `composables/useTheme.ts` —— 暴露 `active / themeClass / setTheme / isDark`
- `App.vue` `onLaunch` 时 `themeStore.bootstrap()`，从 storage 恢复上次选的主题
- 页面级背景渐变从 `<page>` 移到 `.page-shell-safe`（`<page>` 元素不能绑 class，主题切换才能生效）

#### 4. UI 集成

- "我的"页面新增**外观主题**模块：4 张色卡（暖陶/薄荷/紫雾/墨夜），每张卡用自己的 `.theme-{id}` class 局部覆盖 CSS 变量 → 预览即所见
- 18 个页面根 view 加 `themeClass` 绑定 + `useTheme()` 导入

#### 5. 批量替换硬编码

- 业务页面 + 业务组件 **216 处**高频硬编码 hex/rgba 替换为 token 变量

### 关键决策

- **架构选择**：4 套主题全部预编译，class 切换（行业标准，VS Code/Linear 都这样）。SCSS 引入"当前主题"的方案做不到运行时无缝切换，pass。
- **页面应用模式**：选 C 模式（每页根 view 自己绑 themeClass）而非 A 模式（`<AppPage>` 包裹组件）。理由：项目目前只有主题这一个全局行为，C 不引入抽象；等到第二个全局功能再升级到 A。
- **u-button 事件名**：项目约定 `@click`（不是 `@tap`），uview 不转发原生 `@tap`。

### 相关坑

- `<page>` 元素小程序里不能绑 class，所以页面级背景必须搬到 `.page-shell-safe` 之类的根 view
- 加 token 的同一个 PR 必须把所有相关样式文件的硬编码同步替换为 `var()`，否则切了主题没人消费变量看不到效果

---

## 2026-05-07 · P0 账单分析重新上线 + AI 多供应商脚手架

> Commit: `8f95527`

### 总览

这次提交把之前"已下线"的 AI 账单分析 P0 全链路接回，并搭好了多供应商切换脚手架。
约 1500 行新增、500 行删除，覆盖前后端 29 个文件。

### 主要内容

#### 1. P0 账单分析（新增 2 个接口）

- `POST /ai/bill-analysis` —— 同步分析
  - 后端先聚合（金额、分类占比、Top 样本）
  - 拼 prompt 让模型返回严格 JSON：`summary / observations / risks / suggestions`
  - 模型失败时降级，仍返回完整聚合结果 + 错误描述
  - 落库 `ai_bill_analysis_record` + 写 `ai_call_log`
- `GET /ai/bill-analysis/history` —— 倒序分页历史

#### 2. AI 多供应商切换脚手架

- 新增 `AiProvider` 枚举：`OPENAI / ZHIPU / DEEPSEEK / DASHSCOPE`（每个内置 base-url、chat 模型、embedding 模型、API path）
- 新增 `AiProperties.active` + `providers` Map，`yaml` 里只填 `app.ai.active` 和各 provider 的 api-key
- 新增 `AiProviderEnvironmentPostProcessor`（注册在 `META-INF/spring.factories`），启动期把激活供应商参数注入到 `spring.ai.openai.*`，Spring AI auto-config 透明使用
- 切换供应商 = 改 `app.ai.active` 一个字段，不动代码

#### 3. 前端

- 新增 `pages/ai/bill-analysis.vue`：日期 / 账本 / 补充问题表单 + 概览卡 + AI 总结/观察/风险/建议 + 历史列表
- `api/ai.ts` 加类型 + 2 个方法：`requestBillAnalysis`、`fetchBillAnalysisHistory`
- `pages/ledger/index.vue` 的"AI 帮我整理账单"按钮改为跳到新页（之前跳聊天页）

#### 4. 清理孤儿

删除 6 个后端 DTO/VO（`AgentChatRequest` / `KnowledgeBaseChatRequest` / `AiAgentVO` / `AiKnowledgeBaseVO` / `AiChatResponse` / `AiFunctionCallResponse`）和 3 个前端孤儿文件（`pages/ai/chat.vue`、`pages/ai/knowledge-base.vue`、`components/business/knowledge-chat-panel/`）。

#### 5. 鉴权诊断日志

`AuthServiceImpl.refreshToken` 加 `[refresh]` 系列日志，401 排查可以看到具体哪一步挂（JWT 解析 / Redis session / DB 记录 / refreshToken 不一致 / 过期）。

### 端到端验证

- `mvn compile` ✅
- 前端 `npm run type-check` ✅
- curl 真打智谱 GLM：6.7 秒返回完整 JSON 响应，结构 100% 对齐前端
- 历史接口分页 + 落库验证通过

### 相关坑

- 智谱 OpenAI 兼容路径不带 `/v1`（`/chat/completions`），需要 `AiProvider` 枚举里 `defaultChatCompletionsPath` 单独覆盖
- `EnvironmentPostProcessor` 必须注册在 `META-INF/spring.factories`，**不是** `META-INF/spring/...imports`（后者只服务 `@AutoConfiguration`）
- JVM 默认不读系统代理，调 OpenAI/智谱前必须在 IDE Run Configuration 加 `-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7897`（Clash 端口）
