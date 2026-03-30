# Spring AI 接入与 AI 账单分析说明

## 1. 本次改动范围

本次完成了 `record` 后端和 `miniapp` 小程序前端两部分的 AI 接入，场景主要覆盖：

- AI 聊天
- AI 流式聊天
- AI 账单结构化分析
- AI 账单分析历史
- 账单分析结果页和追问链路

当前整体链路如下：

1. 小程序账本页点击 `AI 分析`
2. 进入 AI 账单分析页
3. 自动调用后端 `/ai/bill-analysis`
4. 展示结构化分析结果
5. 可调整时间范围、补充问题后重新分析
6. 可查看历史分析记录
7. 可点击 `Ask Follow-up` 进入 AI 聊天页继续追问

---

## 2. 后端改动说明

后端工程目录：`record`

### 2.1 Spring AI 依赖与配置

已在 `pom.xml` 中补充：

- `spring-ai-bom`
- `spring-ai-starter-model-openai`
- `spring-ai-starter-model-chat-memory-repository-jdbc`

已在配置文件中补充：

- `spring.ai.openai.*`
- `app.ai.*`

相关文件：

- `record/pom.xml`
- `record/src/main/resources/application.yaml`
- `record/src/main/resources/application-dev.yaml`
- `record/src/main/resources/application-prod.yaml`
- `record/src/main/java/com/record/common/config/AiProperties.java`
- `record/src/main/java/com/record/common/config/AppConfig.java`

### 2.2 后端 AI 接口

控制器：

- `POST /ai/chat`
- `POST /ai/chat/stream`
- `POST /ai/bill-analysis`
- `GET /ai/bill-analysis/history`

主要文件：

- `record/src/main/java/com/record/modules/ai/controller/AiController.java`
- `record/src/main/java/com/record/modules/ai/service/AiService.java`
- `record/src/main/java/com/record/modules/ai/service/impl/AiServiceImpl.java`

### 2.3 账单分析实现原则

账单分析不是把原始账单直接全丢给模型算，而是：

1. 后端先查询用户账单
2. 后端先完成金额统计、收支汇总、分类汇总、样本抽取
3. AI 只负责：
   - 总结
   - 观察
   - 风险提示
   - 建议

这样做的原因：

- 金额计算可控
- 结果稳定
- 前端更容易展示结构化结果
- 模型出错时可以做兜底

### 2.4 后端新增持久化表

已在 `record/src/main/resources/schema.sql` 增加：

- `ai_bill_analysis_record`
- `ai_call_log`

用途：

- `ai_bill_analysis_record`
  - 保存账单分析历史
  - 供 `/ai/bill-analysis/history` 查询

- `ai_call_log`
  - 保存 AI 调用日志
  - 记录用户、场景、耗时、成功失败等基础信息

对应实体与 mapper：

- `record/src/main/java/com/record/modules/ai/model/entity/AiBillAnalysisRecord.java`
- `record/src/main/java/com/record/modules/ai/model/entity/AiCallLog.java`
- `record/src/main/java/com/record/modules/ai/mapper/AiBillAnalysisRecordMapper.java`
- `record/src/main/java/com/record/modules/ai/mapper/AiCallLogMapper.java`

### 2.5 后端当前限制

当前 `ai_call_log.model` 还没有记录真实模型名，暂时写 `null`。

如果后续需要精确统计每次调用的模型，可以在 `app.ai` 配置里补一个显式字段，例如：

- `app.ai.model-name`

然后在日志写入时同步保存。

---

## 3. 前端改动说明

前端工程目录：`miniapp`

### 3.1 新增 AI API 封装

文件：

- `miniapp/src/api/ai.ts`

目前包含：

- `createAiChat`
- `streamAiChat`
- `analyzeBill`
- `fetchBillAnalysisHistory`

其中流式聊天使用的是：

- `uni.request`
- `enableChunked`
- `RequestTask.onChunkReceived`

用于接收后端 `text/event-stream` 数据。

### 3.2 新增页面

#### AI 聊天页

文件：

- `miniapp/src/pages/ai/chat.vue`

功能：

- 流式聊天
- 展示增量回复
- 支持带账本参数进入
- 自动生成默认追问模板

#### AI 账单分析页

文件：

- `miniapp/src/pages/ai/bill-analysis.vue`

功能：

- 自动请求结构化账单分析
- 展示 summary / observations / risks / suggestions
- 展示支出分类
- 展示大额样本账单
- 支持起止日期筛选
- 支持补充问题后重新分析
- 支持查看历史分析
- 支持跳转到 AI 聊天页继续追问

### 3.3 页面注册

文件：

- `miniapp/src/pages.json`

已新增：

- `pages/ai/chat`
- `pages/ai/bill-analysis`

### 3.4 账本页入口

文件：

- `miniapp/src/pages/ledger/index.vue`

已新增 `AI 分析` 按钮。

当前行为：

1. 在账本页点击 `AI 分析`
2. 跳转到 `pages/ai/bill-analysis`
3. 自动带上：
   - `bookId`
   - `bookName`

---

## 4. 当前页面流转

### 4.1 账本分析

`pages/ledger/index`
-> `pages/ai/bill-analysis`

### 4.2 分析后追问

`pages/ai/bill-analysis`
-> `pages/ai/chat`

### 4.3 历史分析复用

`pages/ai/bill-analysis`
-> 点击历史项
-> 回填：

- `bookId`
- `bookName`
- `startDate`
- `endDate`
- `question`

然后重新调用 `/ai/bill-analysis`

---

## 5. 当前接口约定

### 5.1 AI 聊天

#### 请求

`POST /ai/chat`

```json
{
  "conversationId": "ledger-home",
  "message": "Help me summarize my recent spending habits."
}
```

### 5.2 AI 流式聊天

#### 请求

`POST /ai/chat/stream`

后端返回 `text/event-stream`。

事件：

- `message`
- `done`

### 5.3 AI 账单分析

#### 请求

`POST /ai/bill-analysis`

```json
{
  "bookId": "1",
  "startDate": "2026-03-01",
  "endDate": "2026-03-30",
  "question": "Focus on dining and transport expenses."
}
```

### 5.4 AI 账单分析历史

#### 请求

`GET /ai/bill-analysis/history?limit=10`

---

## 6. 启动前需要确认的配置

后端至少需要：

```bash
APP_AI_ENABLED=true
AI_API_KEY=your_key
AI_BASE_URL=https://api.openai.com
AI_CHAT_MODEL=gpt-4o-mini
```

如果使用兼容 OpenAI 协议的平台，只需要改：

- `AI_BASE_URL`
- `AI_API_KEY`
- `AI_CHAT_MODEL`

另外需要确认数据库已执行新增表结构：

- `ai_bill_analysis_record`
- `ai_call_log`

---

## 7. 验证情况

前端已执行：

```bash
pnpm -s exec vue-tsc --noEmit
```

结果：通过。

后端未完成本地 Maven 编译验证，原因不是本次代码逻辑已确认失败，而是当前环境存在：

- `mvnw.cmd` 启动异常
- 默认 `D:\.m2` 写权限问题

因此后端上线前应在可用环境中至少再执行一次：

```bash
mvn -DskipTests compile
```

---

## 8. 后续可选优化

如果后面继续扩展，建议优先考虑：

1. 给 `ai_call_log` 增加查询接口和后台查看页
2. 给账单分析页增加图表展示
3. 在首页增加 AI 入口
4. 给 AI 调用日志补充真实模型名、token、成本信息
5. 为账单分析增加更严格的结构化解析和异常监控

