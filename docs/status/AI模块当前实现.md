# AI 模块当前实现

> 截至 2026-05-08。
> 替代旧文档 `guide/SpringAI接入与AI账单分析说明.md`（说"账单分析已下线"已过期）。

## 1. 当前已实现的接口（8 个）

| Endpoint | 用途 | 入口 |
|---|---|---|
| `POST /ai/chat/stream` | 流式聊天（SSE） | `pages/ai/index.vue` |
| `GET /ai/conversations` | 会话列表 | `pages/ai/index.vue` |
| `POST /ai/conversations` | 创建会话 | `pages/ai/index.vue` |
| `GET /ai/conversations/{id}/messages` | 查会话消息 | `pages/ai/index.vue` |
| `DELETE /ai/conversations/{id}` | 删会话 | `pages/ai/index.vue` |
| `POST /ai/bill-analysis` | 账单分析 | `pages/ai/bill-analysis.vue` |
| `GET /ai/bill-analysis/history` | 账单分析历史（分页） | `pages/ai/bill-analysis.vue` |

`POST /ai/function-call/demo` 之前删掉了，未来做 Function Calling 体系会重新设计。

## 2. 多供应商切换

支持 4 家 OpenAI 兼容供应商，运行时 class 切换：

| Provider id | 默认 base-url | 默认 chat 模型 | embedding |
|---|---|---|---|
| `openai` | https://api.openai.com | gpt-4o-mini | text-embedding-3-small |
| `zhipu` | https://open.bigmodel.cn/api/paas/v4 | glm-4-flash | embedding-3 |
| `deepseek` | https://api.deepseek.com | deepseek-chat | (不支持) |
| `dashscope` | https://dashscope.aliyuncs.com/compatible-mode | qwen-turbo | text-embedding-v3 |

切换：改 `app.ai.active`（或环境变量 `APP_AI_ACTIVE`）+ 设对应 `{NAME}_API_KEY` 环境变量。

实现机制：`AiProvider` 枚举 + `AiProperties.providers` Map + `AiProviderEnvironmentPostProcessor`（`META-INF/spring.factories` 注册），启动期把激活供应商参数注入到 `spring.ai.openai.*`。

## 3. 流式聊天（chat/stream）

- Spring AI `ChatClient.prompt().stream()` 实现
- SSE 协议（`text/event-stream`），前端用 `uni.request` 的 `enableChunked` 接 chunk
- Redis 持久化会话上下文 + 摘要（Pinia 在前端只缓存当前会话）
- 多轮对话上下文从 `app.ai.chat.max-messages`（默认 20）读取
- 调用日志记到 `ai_call_log` 表，scene = `CHAT_STREAM`

## 4. 账单分析（bill-analysis）

完整 P0 链路（同步调用，5-10 秒返回）：

1. **入参规整**：日期窗口（默认前 30 天）、单次最多 200 条账单（`app.ai.bill-analysis.max-entries`）
2. **聚合统计**：账本归属校验 → 拉账单 → 算总收入/支出/结余 → 按标签分类（多标签每个都计） → Top 12 高金额样本
3. **Prompt 构造**：用结构化文本拼，**不**用 JSON 包裹（避免模型把输入当输出格式照抄）
4. **调用模型**：`prompts/ai/bill-analysis/system.md` 强制模型返回严格 JSON
5. **JSON 解析**：抠 `{...}` 主体，容错代码块包裹和首尾杂言，失败时降级为"原文塞 summary、其余三段空数组"
6. **持久化**：写 `ai_bill_analysis_record`（仅 summary，不存 observations/risks/suggestions 明细） + 写 `ai_call_log` scene = `BILL_ANALYSIS`
7. **响应**：`BillAnalysisResponse`（summary / observations / risks / suggestions + 全部聚合数据）

历史接口分页倒序，最大 page size 50。

## 5. 配置位置

- 公共：`application.yaml` 的 `app.ai` 段
- 模板：`resources/prompts/ai/{system,bill-analysis/system,knowledge/...}.md`
- 客户端：`AiClientConfig` —— 当前只是 `ChatClient.builder.build()`，所有定制走 properties

## 6. 还没实现的（在 plans/）

- 知识库 RAG（切片 + Embedding + 向量检索 + 引用回答）—— 见 `plans/知识库与Milvus落地方案.md`
- Function Calling 工具体系
- Agent 编排
- Prompt 版本管理 / 灰度

## 7. 相关 changelog

- `changelog/2026-05-07-P0账单分析与多供应商脚手架.md`
- `changelog/2026-04-01-AI模块改造.md`（含老版下线 / 重构）
