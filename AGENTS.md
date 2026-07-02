# lifeRecord · Agent Guide

个人生活记录类项目，单仓三子项目：

| 目录 | 技术栈 | 启动命令 |
|---|---|---|
| `miniapp/` | uni-app 3 + Vue 3 + TS + Pinia + uView Pro + UnoCSS | `npm run dev:mp-weixin` |
| `record/` | Spring Boot 3.5.3 + Java 21 + MyBatis-Plus 3.5.6 + Spring Security + JWT + Redis + Spring AI 1.1.2 | `./mvnw.cmd spring-boot:run` |
| `admin/` | vue-pure-admin 7（Element Plus，fork 模板） | `pnpm dev` |

---

## 命令

### 后端（`record/`）
```
./mvnw.cmd -q -DskipTests compile          # 编译检查
./mvnw.cmd -q clean package                 # 构建 JAR
./mvnw.cmd spring-boot:run                  # 启动开发服务
```

### 前端（`miniapp/`）
```
npm run dev:mp-weixin                        # 微信小程序开发
npm run build:mp-weixin                      # 构建
npm run type-check                           # vue-tsc --noEmit
```
构建后自动执行 `scripts/sanitize-mp-output.mjs` 清理 WXSS 产物。

### 管理后台（`admin/`）
```
pnpm dev                                     # 开发
pnpm build                                   # 构建
pnpm typecheck                               # vue-tsc --noEmit --skipLibCheck
pnpm lint                                    # eslint + prettier + stylelint
```
Node >= 22.22.1, pnpm >= 11。

---

## CI

`.github/workflows/ci.yml` — push 到 `dev-ai`/`main` 分支或 PR 到 `main` 时触发：

| Job | 步骤 |
|---|---|
| backend | JDK 21 compile + `mvn test` |
| admin | Node 20, pnpm install --frozen-lockfile, typecheck, build |
| miniapp | Node 20, npm ci, type-check, build:mp-weixin |

---

## 架构

### 后端模块（`record/src/main/java/com/record/`）

```
modules/<name>/
  controller/     # REST 接口，前缀 /api/<module>
  service/        # 接口
  service/impl/   # 实现
  mapper/         # MyBatis-Plus Mapper
  model/
    dto/          # 请求参数
    vo/           # 响应
    entity/       # 数据库实体
```

| 模块 | 包 | 职责 |
|---|---|---|
| diary | `modules/diary` | 日记 CRUD + 回收/恢复 + 天气/心情 + 图片/位置 + 点赞/评论 |
| ledger | `modules/ledger` | 多账本 + 收入/支出流水 + 月/年统计 |
| checkin | `modules/checkin` | 打卡任务 + 每日签到 |
| memorial | `modules/memorial` | 纪念日 CRUD + 每年重复 + 提醒配置 |
| calendar | `modules/calendar` | 那年今日回忆 |
| dashboard | `modules/dashboard` | 首页仪表盘统计（admin 用） |
| tag | `modules/tag` | 跨日记/记账标签 |
| recycle | `modules/recycle` | 日记 + 记账流水回收站（打卡/纪念日/账本不进入） |
| reminder | `modules/reminder` | 微信订阅消息调度 |
| auth | `modules/auth` | 微信登录 + OAuth（GitHub/Google） |
| user | `modules/user` | 个人资料 + 身份绑定 |
| ai | `modules/ai` | 流式聊天 + 账单分析 + RAG |
| knowledge | `modules/knowledge` | 知识库（Milvus RAG）—— 骨架已完成 |
| file | `modules/file` | 阿里云 OSS 上传 |
| location | `modules/location` | 腾讯地图逆地理编码 |
| system | `modules/system` | 管理后台：用户/角色/菜单管理 |
| common | `common/` | 缓存、配置、常量、枚举、异常、工具 |
| security | `security/` | JWT 过滤器 + 启动引导 + SecurityConfig |
| scheduler | `scheduler/` | 定时任务 |
| integration | `integration/` | 地图、OAuth、微信客户端 |

### 小程序页面（`miniapp/src/pages/`）

| 目录 | 模块 |
|---|---|
| `pages/home/` | 首页 |
| `pages/diary/` | 日记列表/编辑/详情 |
| `pages/ledger/` | 记账列表 + 账本 |
| `pages/checkin/` | 打卡任务 |
| `pages/memorialPage/` | 纪念日 |
| `pages/memory/` | 那年今日 |
| `pages/auth/` | 登录 |
| `pages/profile/` | 个人中心、设置、回收站、标签 |
| `pages/ai/` | AI 聊天 + 账单分析 |

### 前端分层（`miniapp/`）
```
src/api/          # Axios API 调用
src/stores/       # Pinia 状态管理（app.ts, reminder.ts）
src/composables/  # useGreeting, useThemeColors
src/components/   # ui/（AppButton, AppCard…）+ business/
src/styles/       # tokens.scss, themes/clay.scss, globals.scss
src/utils/        # request.ts（鉴权拦截器）, validators
src/config/       # 应用配置
```

---

## 关键注意事项

### 构建/配置
- **esbuild target 必须是 `es2017`**（`miniapp/vite.config.ts`）—— 微信小程序 JS 引擎不支持 ES2020 可选链（`?.`）和空值合并（`??`）。绝不能提升此 target。
- **UnoCSS 快捷类 `page-shell-safe`** = `min-h-screen box-border px-6 pb-40 pt-6`。所有小程序页面的根容器样式标准。
- **Admin 是 vue-pure-admin 模板**，仅做了最小定制。避免深度重构，作为模板消费者使用。

### 鉴权流程（小程序）
- `request.ts` 401 拦截器：3 层静默重试 → 1) refreshToken 换新 access，2) `wx.login()` 重新登录换 token，3) 跳转登录页
- JWT + refreshToken 双 token 方案，存于 `wx` 本地存储，无 session 机制
- OAuth 登录/绑定共用同一个回调 URL，通过 Redis `state` 区分意图（LOGIN vs BIND）。一次性消费，10 分钟 TTL

### 主题系统（小程序）
- 当前**固定使用 "clay"（暖陶）**主题。多主题切换已于 2026-05-27 下线，但 CSS 变量 + 语义命名的 token 体系保留，未来可恢复
- 所有颜色走 CSS 变量（`var(--color-primary)` 等）—— 禁止硬编码 hex
- Canvas 组件（ECharts）通过 `composables/useThemeColors.ts` 获取具体色值
- Token 定义：`styles/tokens.scss`（间距/圆角/字号）+ `styles/themes/clay.scss`（颜色/阴影）

### 数据库
- 用户体系：`sys_user` + `sys_user_identity`（一对多，支持多平台绑定）
- 软删除：`ledger_entry` 使用 `deleted_at` 列；回收站模块处理日记 + 记账流水
- 约束：`(provider, provider_user_id)` 全局唯一
- `schema.sql` 每个字段必须有 `COMMENT`

### AI 模块
- 多供应商通过 Spring AI OpenAI 兼容入口切换：改 `app.ai.active` 即可（ZHIPU/DEEPSEEK/OPENAI/DASHSCOPE）
- 生产环境使用智谱 `glm-4-flash`
- 向量库用 Milvus（开发环境：`docker compose -f docker-compose.dev.yml up -d` 启动 etcd+minio+standalone+attu）
- AI 调用记录到 `ai_call_log` 表，按 `scene` + `provider` 维度审计
- RAG 流程：Milvus 相似度检索 → LLM 拼接 prompt → 带引用的回答

### 用户合并
- `POST /system/user/merge` body `{sourceUserId, targetUserId}`
- 在 `@Transactional` 事务中执行：13 张业务表迁移用户数据，5 张 UK 冲突表用 `UPDATE IGNORE` 处理
- 源用户设为 DISABLED 状态，记录 `merged_into_user_id` 形成审计链路

### 部署
- 腾讯云 ECS + Docker Compose：mysql + redis + nginx + backend 四容器
- 域名 `https://recordlife.top`，腾讯云免费 SSL 证书
- nginx 已为 SSE 流式关闭 `proxy_buffering`，`proxy_read_timeout 300s`
- 后端 JVM 参数：`-Xmx1g -Xms512m -XX:+UseG1GC`，日志 10MB × 14 份滚动
---

## 编码约定

- UTF-8 + LF 换行（Windows：`.bat` 用 CRLF，`.cmd` 用 CRLF）
- Java 缩进 4 空格，其余 2 空格
- DTO/VO/Entity 必须有 `@Schema` 注解（Knife4j/OpenAPI）
- 中文注释可接受，但必须确保编码稳定；乱码时改英文注释
- 禁止 `as any`、`@ts-ignore`、`@ts-expect-error`
- 提交格式：1 行中文标题 + body 带 `feat:`/`fix:`/`refactor:`/`perf:`/`docs:`/`chore:`/`test:` 前缀
- 管理后台所有路由加 `/api/` 前缀以兼容 nginx 代理
- 后端 API 路由：`/api/<module>/...`（在 SecurityConfig 中配置）

## 提交前验证顺序

1. `cd record && ./mvnw.cmd -q -DskipTests compile`
2. `cd miniapp && npm run type-check`
3. `cd miniapp && npm run build:mp-weixin`
4. `cd admin && pnpm typecheck`
5. `cd admin && pnpm build`

---

## 测试

`record/src/test/` 有 4 个测试文件：`RecordApplicationTests.java`（空）、`DashboardServiceImplTest`、`MemorialDayServiceImplTest`、`MenuServiceImplTest`。CI 运行 `./mvnw -q test`。
