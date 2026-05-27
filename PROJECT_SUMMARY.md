# lifeRecord 项目总结

> 截至 2026-05-27 的实现盘点：已完成功能、进行中模块、未完成事项与后续扩展方向。

---

## 1. 项目定位

`lifeRecord` 是一款面向个人长期使用的微信小程序，围绕「**记录、回看、提醒、统计**」四条主线，覆盖日记、记账、打卡、纪念日、AI 辅助等场景。

- **前端**：`miniapp/` —— uni-app + Vue 3 + TypeScript + Pinia + uView
- **后端**：`record/` —— Spring Boot 3 + Spring Security + MyBatis-Plus + MySQL + Redis + Spring AI
- **基础设施**：阿里云 OSS（文件）+ 腾讯地图（定位/逆地理）+ 微信订阅消息（提醒）+ 腾讯云 ECS + Docker Compose 部署

---

## 2. 已完成（稳定上线）

### 2.1 核心业务模块

| 模块 | 关键能力 | 后端包 | 前端入口 |
|---|---|---|---|
| **日记** | 增删改、回收/恢复、天气心情、图片/位置、标签、可见范围、点赞评论、年龄文案 | `modules/diary` | `pages/diary/` |
| **记账** | 多账本、收入支出流水、月度/年度统计、标签统计、回收站 | `modules/ledger` | `pages/ledger/` |
| **打卡** | 任务创建、每日打卡、历史查看 | `modules/checkin` | `pages/checkin/` |
| **纪念日** | 增删改、每年重复、提醒时间配置 | `modules/memorial` | `pages/memorialPage/` |
| **那年今日** | 历史同期日记/账单回顾 | `modules/calendar` | `pages/memory/` |
| **标签** | 跨日记/账单复用 | `modules/tag` | 内嵌 |
| **回收站** | 日记、账单流水进入回收站；可恢复/彻底删除 | `modules/recycle` | 各模块内 |
| **提醒** | 微信订阅消息：日记/每日记账/每月记账/纪念日 | `modules/reminder` | `stores/reminder.ts` |
| **定位/媒体** | wx.getLocation、腾讯地图选点、服务端逆地理、OSS 上传 | `modules/location`、`modules/file` | `components/business/location-picker/` |
| **用户/鉴权** | 微信登录、JWT + refreshToken 三层无感重登 | `modules/auth`、`modules/user` | `pages/auth/login.vue` |

### 2.2 AI 模块（P0 已完成）

| 接口 | 用途 | 状态 |
|---|---|---|
| `POST /ai/chat/stream` | SSE 流式聊天（Spring AI ChatClient + Redis 上下文持久化） | ✅ |
| `GET /ai/conversations` | 会话列表 | ✅ |
| `POST /ai/conversations` | 创建会话 | ✅ |
| `GET /ai/conversations/{id}/messages` | 会话消息 | ✅ |
| `DELETE /ai/conversations/{id}` | 删除会话 | ✅ |
| `POST /ai/bill-analysis` | **P0 账单分析**：聚合 → prompt → 模型 JSON → 解析 → 落库 | ✅ |
| `GET /ai/bill-analysis/history` | 账单分析历史分页 | ✅ |

**多供应商切换脚手架**：`AiProvider` 枚举（OPENAI / ZHIPU / DEEPSEEK / DASHSCOPE）+ `AiProviderEnvironmentPostProcessor`，改 `app.ai.active` 一个字段即可切换，所有调用走 Spring AI OpenAI 兼容入口。当前生产用智谱 `glm-4-flash`。

**调用日志**：所有 AI 调用落 `ai_call_log` 表，按 `scene`（`CHAT_STREAM` / `BILL_ANALYSIS`）+ `provider` 维度可审计。

### 2.3 UI 主题系统

- 4 套主题：**暖陶 / 薄荷 / 紫雾 / 墨夜**（最后一套为暗色）
- Token 三件套：`styles/tokens.scss`（原子）+ `styles/themes/*.scss`（主题色）+ `styles/globals.scss`（语义）
- `stores/theme.ts` + `composables/useTheme.ts` 持久化主题选择
- "我的"页内置外观主题切换器（4 张色卡所见即所得）
- ECharts 类 canvas 通过 `composables/useThemeColors.ts` 拿具体色值，主题切换自动重渲染

### 2.4 生产部署

- 腾讯云 ECS（4 核 4G，`123.207.210.150`） + Docker Compose 编排（`mysql` / `redis` / `nginx` / `record-backend` 四容器）
- nginx 容器作 HTTPS 反代，域名 `https://recordlife.top`，证书来自腾讯云免费 SSL
- 后端 `openjdk:21-jdk-slim` + `-Xmx1g -Xms512m -XX:+UseG1GC`
- 日志写文件 `./logs/record.log`，按 10MB × 14 份滚动
- nginx 已为 SSE 流式聊天关闭 `proxy_buffering`，`proxy_read_timeout 300s`

### 2.5 鉴权无感登录

`miniapp/src/utils/request.ts` 401 拦截器三层兜底：
1. 本地有 refreshToken → `/auth/refresh` 换新 access
2. 失败 → 自动 `wx.login()` + `/auth/wx-login` 重发
3. 仍失败 → 提示并跳登录页

前两层用户完全无感，原请求自动重放。

---

## 3. 进行中（部分完成 / 骨架）

### 3.1 知识库模块（骨架已完成）

**已完成**：
- 三张表 `knowledge_base` / `knowledge_document` / `knowledge_chunk_task`
- Entity / Mapper / DTO / VO / Service / Controller 分层完整
- 6 个基础接口：知识库 CRUD、文档元数据创建、任务列表查询
- 用户维度数据隔离 + 字段校验（`visibility`、`sourceType` 枚举校验）
- 创建文档时自动写 `PARSE` 任务

**未完成**（见下一节）。

---

## 4. 未完成（已规划，未实现）

### 4.1 AI / RAG 路线（按 P0-P3 推进）

- **P1 知识库 RAG（待启动）**
  - 文档上传接口
  - OSS 文件写入
  - 文档解析任务执行器（PDF / DOCX / TXT / MD）
  - 文本切片（建议 500–800 字符，overlap 80–120）
  - Embedding 调用
  - 检索接口
  - 基于知识库的 RAG 问答接口
  - **轻方案兜底**：先用 pgvector 或 MySQL 拼 prompt，再上 Milvus
- **P2 Milvus 接入**：`docs/plans/Milvus接入改造步骤清单.md` 已就绪，`pom.xml` 还没加依赖
- **P3 Agent 编排层**：prompt 模板已备好（`prompts/ai/agent/general.md`、`life-record.md`），Service / Controller 路由不存在
- **Function Calling 工具体系**：上一版 `POST /ai/function-call/demo` 已删除，等重新设计
- **Prompt 版本管理 / 灰度**

### 4.2 README 中标注的 TODO

- 补充正式页面截图
- 完善评论互动体验
- 完善提醒模板配置指引
- 增加更多统计分析能力
- 部署说明和环境变量模板（已有部分内容散落在 changelog 中，未汇成模板）

### 4.3 V1.2 计划

- 更多统计分析页面
- 分享与公开展示能力
- 整体使用体验与页面完成度提升

---

## 5. 后续扩展方向（建议）

### 5.1 短期（1–2 周）

1. **知识库 P1 闭环**：先把文档上传 → OSS → 解析 → 切片 → MySQL 存切片（无向量）→ MySQL 全文检索拼 prompt 跑通，验证产品形态
2. **AI 账单分析 P0.5**：把 `observations / risks / suggestions` 明细也持久化，做"分析对比"页（这个月 vs 上个月 AI 视角的变化）
3. **页面截图 + 部署模板**：补 README 标注的两项 TODO

### 5.2 中期（1–2 月）

1. **知识库 P2**：接 Milvus，迁出 MySQL 兜底方案
2. **Function Calling**：定义工具体系（查日记 / 查账单 / 创建提醒），让聊天可以触发实际操作
3. **统计分析增强**：年度报告、消费趋势、心情曲线（基于已有数据 + AI 洞察）
4. **分享能力**：日记/年度报告生成图片分享（参考小红书年度总结）

### 5.3 长期

1. **Agent 编排 P3**：基于 prompts/ 下已备好的 `general.md` / `life-record.md`，做"私人秘书"形态
2. **多端**：当前只跑微信小程序，uni-app 可较低成本扩到 H5 / App
3. **Prompt 版本管理**：A/B 灰度、效果对比看板（依赖 `ai_call_log` 表）
4. **跨用户公开知识库**：当前 `visibility` 字段已留 `PUBLIC`，但还未消费

---

## 6. 文档索引

- 架构：`docs/architecture/architecture.md`
- 规范：`docs/standards/开发规范.md`、`docs/standards/提交规范.md`、`docs/standards/UI 设计规范.md`
- 指南：`docs/guides/发布配置清单.md`、`docs/guides/新手发布步骤.md`、`docs/guides/微信登录说明.md`、`docs/guides/小程序订阅消息和地图配置步骤.md`
- 当前状态：`docs/status/AI模块当前实现.md`、`docs/status/知识库模块当前实现清单.md`
- 规划：`docs/plans/AI大模型应用选型清单.md`、`docs/plans/知识库与Milvus落地方案.md`、`docs/plans/Milvus接入改造步骤清单.md`
- 变更日志：`docs/changelog/2026-04-01-AI模块改造记录.md` 起

---

## 7. 一句话结论

**主链路（日记 / 记账 / 打卡 / 纪念日 / 提醒）+ AI P0（账单分析、流式聊天）+ 多主题 + 生产部署已完成，焦点是 AI 知识库 RAG（P1）落地，长期向 Agent 化演进。**
