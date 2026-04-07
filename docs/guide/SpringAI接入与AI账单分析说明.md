# Spring AI 接入说明

## 1. 当前目标

当前项目先只保留最基础的 AI 聊天能力，目的是：

- 代码更容易看懂
- 前后端更容易跑通
- 方便后续继续学习 Spring AI 和 Spring AI Alibaba

这次已经移除或下线的内容：

- Agent 聊天
- 知识库聊天
- AI 账单分析
- 多入口 AI 页面

当前只保留：

- 流式聊天
- 会话列表
- 创建会话
- 查询会话消息
- 删除会话

---

## 2. 后端实现

后端目录：`record`

### 2.1 对外接口

控制器文件：

- `record/src/main/java/com/record/modules/ai/controller/AiController.java`

当前接口只有 5 个：

- `POST /ai/chat/stream`
- `GET /ai/conversations`
- `POST /ai/conversations`
- `GET /ai/conversations/{conversationId}/messages`
- `DELETE /ai/conversations/{conversationId}`

### 2.2 流式聊天实现

服务实现文件：

- `record/src/main/java/com/record/modules/ai/service/impl/AiServiceImpl.java`

当前实现方式：

1. 基于 `ChatClient.prompt(...).stream().content()`
2. 后端返回 `Flux<ServerSentEvent<String>>`
3. 事件只保留两种：
   - `message`
   - `done`

这套实现更贴近 Spring AI 官方示例，也更适合后续迁移到 Spring AI Alibaba。

### 2.3 会话存储

当前会话数据仍然使用 Redis 保存，包含：

- 会话索引
- 会话摘要
- 会话消息
- 简单上下文记忆

这样可以保留多轮会话能力，同时不再引入知识库、Agent 这些复杂分支。

---

## 3. 前端实现

前端目录：`miniapp`

### 3.1 当前页面

主页面：

- `miniapp/src/pages/ai/index.vue`

现在页面只保留：

- 会话列表
- 聊天消息区
- 输入框
- 发送按钮

已经移除：

- Agent 选择
- 知识库选择
- 工具弹窗
- 引用来源面板
- 账单分析入口逻辑

### 3.2 流式接收方式

前端不是 WebSocket。

当前采用：

- `uni.request`
- `enableChunked: true`
- `onChunkReceived`

对应文件：

- `miniapp/src/api/ai.ts`

实现思路：

1. 请求 `/ai/chat/stream`
2. 按 chunk 接收后端返回内容
3. 手动解析 SSE 风格文本
4. 逐段追加到最后一条 AI 消息

---

## 4. 当前请求模型

聊天请求 DTO：

- `record/src/main/java/com/record/modules/ai/model/dto/AiChatRequest.java`

当前只保留两个字段：

- `message`
- `conversationId`

创建会话 DTO：

- `record/src/main/java/com/record/modules/ai/model/dto/CreateConversationRequest.java`

当前只保留：

- `title`

---

## 5. 运行配置

至少需要这些配置：

```bash
APP_AI_ENABLED=true
AI_API_KEY=你的密钥
AI_BASE_URL=你的兼容接口地址
AI_CHAT_MODEL=你的模型名
```

如果这些配置缺失，后端虽然能启动，但聊天请求不会成功。

---

## 6. 当前目录建议

如果你接下来是为了学习 Java AI，建议优先看这几个文件：

- `record/src/main/java/com/record/modules/ai/controller/AiController.java`
- `record/src/main/java/com/record/modules/ai/service/AiService.java`
- `record/src/main/java/com/record/modules/ai/service/impl/AiServiceImpl.java`
- `miniapp/src/api/ai.ts`
- `miniapp/src/pages/ai/index.vue`

先把这条最短链路看明白：

1. 前端发送消息
2. 后端返回 Flux 流
3. 前端按 chunk 追加显示
4. Redis 保存会话

把这条链路跑通以后，再继续加：

- 知识库
- Agent
- 工具调用
- 账单分析

---

## 7. 验证结果

前端已执行：

```bash
npm run type-check
```

结果：通过

后端已执行：

```bash
mvn -q -DskipTests compile
```

结果：通过
