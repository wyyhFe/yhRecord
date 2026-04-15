# Spring AI Alibaba 多模型落地方案（从零版）

## 1. 先回答你现在的状态
你现在**是 Spring AI**：
- `record/pom.xml` 已有 `org.springframework.ai:spring-ai-bom`
- 已有 `org.springframework.ai:spring-ai-starter-model-openai`
- `application.yaml` 已配置 `spring.ai.openai.*`
- 代码里 `AiClientConfig` + `ChatClient.Builder` 已在使用 Spring AI 抽象

结论：你已经在 Spring AI 体系里，只是当前主要是 OpenAI 兼容方式，尚未完整进入 Spring AI Alibaba 的“多模型治理模式”。

---

## 2. 目标架构（建议）
目标不是“能调一个模型”，而是“同一业务按场景自动选模型，并可灰度、回退、限流、审计”。

建议分 4 层：
1. **Provider 层**：OpenAI / DashScope(Qwen) / DeepSeek 等模型接入
2. **Model Registry 层**：统一注册多个 `ChatClient`，每个模型一个 Bean
3. **Routing 层**：按场景路由（chat、bill-analysis、rag、tool-call）
4. **Governance 层**：超时、重试、降级、审计日志、成本统计

---

## 3. 你项目需要改什么（重点清单）

### 3.1 `pom.xml`
保留 Spring AI BOM，同时引入 Spring AI Alibaba 对应 starter（以你选的模型为准）。

示例（请按官方兼容矩阵替换版本号）：
```xml
<properties>
    <spring-ai.version>1.1.2</spring-ai.version>
    <spring-ai-alibaba.version>1.1.2.2</spring-ai-alibaba.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-bom</artifactId>
            <version>${spring-ai-alibaba.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- 已有：OpenAI 兼容 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>

    <!-- 新增：DashScope / 通义（Spring AI Alibaba） -->
    <dependency>
        <groupId>com.alibaba.cloud.ai</groupId>
        <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
    </dependency>
</dependencies>
```

### 3.2 `application.yaml`
不要只配一个 `spring.ai.openai.chat.options.model`，改为“多模型 + 场景路由”。

示例：
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.2
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY:}
      chat:
        options:
          model: qwen-plus
          temperature: 0.3

app:
  ai:
    enabled: true
    default-model: qwen-plus
    routing:
      chat: qwen-plus
      bill-analysis: deepseek-chat
      rag: qwen-max
      tool-call: qwen-plus
    models:
      qwen-plus:
        provider: dashscope
        model: qwen-plus
        timeout-ms: 20000
        retry: 1
      qwen-max:
        provider: dashscope
        model: qwen-max
        timeout-ms: 25000
        retry: 1
      deepseek-chat:
        provider: openai-compatible
        model: deepseek-chat
        base-url: https://api.deepseek.com
        api-key-env: DEEPSEEK_API_KEY
        timeout-ms: 20000
        retry: 1
```

### 3.3 新增配置类（建议新增）
建议新增：
- `com.record.modules.ai.config.AiModelProperties`
- `com.record.modules.ai.config.MultiChatClientConfig`
- `com.record.modules.ai.service.ModelRouterService`

职责：
- `AiModelProperties`：接收 `app.ai.models` 和 `app.ai.routing`
- `MultiChatClientConfig`：创建 `Map<String, ChatClient>`（key=模型别名）
- `ModelRouterService`：根据场景返回目标 `ChatClient`

### 3.4 修改现有类
1. `com.record.common.config.AiProperties`
- 增加：`defaultModel`、`routing`、`models`

2. `com.record.modules.ai.config.AiClientConfig`
- 从“单一 `ChatClient` Bean”改为“多 `ChatClient` 注册”

3. `com.record.modules.ai.service.impl.AiServiceImpl`
- 现在是 `requireChatClient()` 固定单模型
- 改为：`modelRouterService.getClient(scene)`
- 在 `saveAiCallLog` 增加 `modelName/provider/latencyMs/tokenUsage/cost`

---

## 4. 代码实现骨架（可直接照着落）

### 4.1 路由接口
```java
public interface ModelRouterService {
    ChatClient getClient(String scene);
    String resolveModelAlias(String scene);
}
```

### 4.2 在 `AiServiceImpl` 按场景选模型
```java
String scene = "chat";
ChatClient client = modelRouterService.getClient(scene);

return client.prompt()
        .system(resolveSystemPrompt())
        .user(buildChatPrompt(message, historyText))
        .stream()
        .content();
```

### 4.3 Function Calling / RAG 单独模型
- `functionCallDemo` 用 `scene=tool-call`
- 账单分析用 `scene=bill-analysis`
- 知识库问答用 `scene=rag`

这样可以把“便宜模型处理普通对话，强模型处理复杂推理”固定下来。

---

## 5. 生产级必须补齐的能力
1. **超时与重试**：按模型粒度配置，避免慢模型拖垮接口
2. **降级链路**：主模型失败时 fallback 到备模型
3. **限流与熔断**：对高成本模型单独限流
4. **审计与成本**：记录 provider/model/tokens/cost/requestId/userId
5. **提示词隔离**：不同场景独立 prompt 模板
6. **密钥管理**：全部通过环境变量，不写入仓库

---

## 6. 分阶段实施（推荐顺序）
1. **Phase 1（1-2天）**：接入 DashScope，跑通双模型（OpenAI + Qwen）
2. **Phase 2（2-3天）**：完成路由层，按场景切模型
3. **Phase 3（2-3天）**：补齐 fallback、重试、超时、日志字段
4. **Phase 4（持续）**：做成本看板、A/B 评测、自动路由策略

---

## 7. 你现在最小可行改造（MVP）
如果你想最快上线，先做这 4 件事：
1. `pom.xml` 增加 `spring-ai-alibaba-starter-dashscope`
2. `application.yaml` 增加 `spring.ai.dashscope.*`
3. 把 `AiClientConfig` 改成多 `ChatClient`
4. 把 `AiServiceImpl` 的 `requireChatClient()` 改为“按场景路由”

完成这 4 步，你就从“单模型可用”升级到“多模型可运营”。

---

## 8. 参考资料（官方）
- Spring AI Alibaba 快速开始：<https://sca.aliyun.com/en/docs/ai/get-started/>
- Spring AI Alibaba GitHub：<https://github.com/alibaba/spring-ai-alibaba>

> 注：版本号请以你落地当日的兼容矩阵为准，优先保证 `spring-ai` 与 `spring-ai-alibaba` 主版本兼容。
