# Milvus 接入改造步骤清单

> 路径已更正为 `E:/01-server/lifeRecord/`（旧文档写的是 `D:\1\life-record\`）。
> 当前状态：**未实施**。整体规划见 `plans/知识库与Milvus落地方案.md`。

## 一、先说当前项目现状

后端还没有真正接入向量库，知识库问答只是从 `knowledge_document` 表里取文档元数据和本地可读文本片段，再拼到 prompt 里给大模型。
所以它现在不是标准的 RAG，也没有 Embedding、向量入库、向量检索、TopK 召回、重排这些能力。

当前主要相关位置：

1. `record/pom.xml`
   只有 `spring-ai-starter-model-openai`，还没有 Milvus 依赖。
2. `record/src/main/resources/application.yaml`
   只有 OpenAI Chat 配置（通过 `AiProvider` 多供应商接入），Embedding 配置只在 `AiProvider` 枚举里有默认值，没有 Milvus 配置。
3. `record/src/main/java/com/record/modules/knowledge/service/impl/KnowledgeServiceImpl.java`
   创建文档时只写了 `knowledge_document` 和 `knowledge_chunk_task`，没有真正做切片、Embedding、向量入库。
4. `record/src/main/java/com/record/modules/ai/service/impl/AiServiceImpl.java`
   没有 `buildKnowledgeContext(...)`（已删除），未来要做时直接走向量检索方案。

## 二、Milvus 接入后系统应该变成什么样

标准流程两条链路：

1. **写入链路**
   上传文档 → 解析正文 → 文本切片 → 调 Embedding 模型生成向量 → 写入 Milvus → 回写文档状态
2. **查询链路**
   用户提问 → 生成问题向量 → 去 Milvus 相似度检索 → 拿到 TopK 片段 → 拼到 prompt → 大模型回答 → 返回引用来源

简单理解：**MySQL 管"知识库、文档、任务、元数据"，Milvus 管"chunk 向量索引和召回"**。

## 三、要改的位置

### 第一处：Maven 依赖

`record/pom.xml` 增加：

- `spring-ai-starter-vector-store-milvus`（Spring AI 官方 Milvus VectorStore starter）
- 复用现有 `spring-ai-starter-model-openai` 的 EmbeddingModel（不需要换 SDK）

最终至少要让项目具备两类 Bean：`EmbeddingModel` + `VectorStore`（或 `MilvusVectorStore`）。

### 第二处：应用配置

`record/src/main/resources/application.yaml` 增加：

1. **OpenAI Embedding**（实际由 `AiProviderEnvironmentPostProcessor` 按激活供应商动态注入到 `spring.ai.openai.embedding.options.*`）
   - `model`：智谱 `embedding-3` / OpenAI `text-embedding-3-small`
   - `dimensions`：1536（要和 Milvus collection 维度一致）

2. **Milvus**：`spring.ai.vectorstore.milvus`
   - `client.host` / `client.port` / `client.username` / `client.password`
   - `databaseName` / `collectionName`
   - `embeddingDimension` / `indexType` / `metricType`
   - `initialize-schema`

3. **业务侧 RAG 配置**：`app.ai.rag`
   - `enabled` / `top-k` / `similarity-threshold` / `chunk-size` / `chunk-overlap` / `batch-size`

### 第三处：新增 Milvus 配置类

目录：`record/src/main/java/com/record/modules/ai/config/`

- `AiRagProperties.java` —— 绑 `app.ai.rag.*`
- `MilvusConfiguration.java`（可选）—— 自动装配不够用时手动暴露 `VectorStore` Bean

### 第四处：补 `knowledge_chunk` 分片表

当前已有：`knowledge_base` / `knowledge_document` / `knowledge_chunk_task`
还差：`knowledge_chunk`

字段建议：
- `id` / `knowledge_base_id` / `document_id` / `user_id`
- `chunk_index` / `content` / `token_count`
- `vector_id`（Milvus 向量主键的回写）
- `status`
- `created_at` / `updated_at`

为什么：Milvus 存向量索引，业务还需要一张关系表把"召回的向量"映射回"哪篇文档的哪一段内容"。
推荐：MySQL 存 chunk 正文 + 元数据，Milvus 存 `vector + chunkId + knowledgeBaseId + documentId + userId`。

### 第五处：改造知识库入库流程

核心：`record/src/main/java/com/record/modules/knowledge/service/impl/KnowledgeServiceImpl.java`

当前只是创建一条 PARSE 任务。接 Milvus 后：

1. 文档创建只落库，不直接同步做重活
2. 后台任务消费 `knowledge_chunk_task`
3. 读文档正文
4. 清洗 + 切片
5. 每个 chunk 写入 `knowledge_chunk`
6. 调 `EmbeddingModel.embed(...)` 生成向量
7. 批量写入 Milvus collection
8. 把向量主键回写到 `knowledge_chunk.vector_id`
9. 更新 `knowledge_document.status=VECTORIZED`
10. 任务状态改 `SUCCESS`

关键点：
- 切片不要写死在 Controller 里，封装成独立服务
- 向量写入一定要批量

建议新增服务：
- `KnowledgeChunkingService`
- `KnowledgeEmbeddingService`
- `KnowledgeVectorStoreService`
- `KnowledgeIngestionService`

### 第六处：改造知识库检索流程

核心：`record/src/main/java/com/record/modules/ai/service/impl/AiServiceImpl.java`

要新增方法（之前删的 `buildKnowledgeContext` 不要再回来）：

1. 用 `EmbeddingModel` 给用户问题生成 query vector
2. 去 Milvus 做 similarity search
3. 限定检索范围到当前知识库 + 当前用户
4. 根据召回结果拿 chunkId 列表
5. 去 MySQL 查 `knowledge_chunk` 正文和文档信息
6. 组装 citations
7. topK chunk 拼成最终上下文

### 第七处：返回值升级

API 返回 `citations` 至少包含：

- `chunkId` / `documentId` / `documentTitle`
- `snippet` / `score`
- `knowledgeBaseId`

前端 `AiCitationVO` 已经准备好，等接进来用。

## 四、推荐的最小落地方案

第一次接 Milvus，不要一上来就搞分布式，先做最小可跑版本：

### 第 1 步：把 Milvus 跑起来
开发环境：本地 Docker `milvusdb/milvus:standalone`
- 比 Distributed 简单
- 比 Lite 更接近真实 Java 服务接入方式
- 对 Spring Boot 项目最顺手

### 第 2 步：打通 Embedding + 写入 + 查询
1. 手写一段测试文本
2. 生成 embedding
3. 写入 Milvus
4. 一条测试 query 做相似检索
5. 确认能召回正确 chunk

### 第 3 步：接知识库文档流
文档入库 → 文本切片 → 向量化 → Milvus 入库 → 问答检索 → 引用回显

### 第 4 步：异步任务化
同步流程改成可重试、可重建、可删除向量的任务流。

## 五、配置示例

```yaml
# application.yaml
spring:
  ai:
    openai:
      embedding:
        options:
          model: embedding-3       # 智谱 embedding 模型
          dimensions: 1536
    vectorstore:
      milvus:
        client:
          host: 127.0.0.1
          port: 19530
          username: root
          password: milvus
        databaseName: default
        collectionName: knowledge_chunk_vector
        embeddingDimension: 1536
        indexType: IVF_FLAT
        metricType: COSINE
        initialize-schema: true

app:
  ai:
    rag:
      enabled: true
      top-k: 5
      similarity-threshold: 0.65
      chunk-size: 800
      chunk-overlap: 120
      batch-size: 20
```

⚠️ Embedding 维度必须和 Milvus collection 维度一致。

## 六、代码落地顺序

| 阶段 | 内容 |
|---|---|
| 1. 底座 | pom 加依赖 → yaml 加配置 → `AiRagProperties` → 启动确认 EmbeddingModel + VectorStore Bean 注入成功 |
| 2. 分片模型 | 建 `knowledge_chunk` 表 → entity / mapper / VO |
| 3. 入库链路 | 读正文 → 切片 → 批 embed → 批写 Milvus → 更新 status |
| 4. 查询链路 | 问题向量化 → Milvus topK → 回查正文 → 组装 citations → 拼 prompt |
| 5. 运维能力 | 删除文档同步删向量 → 更新文档重建索引 → 失败任务重试 → 监控耗时和命中率 |

## 七、最容易踩的坑

1. **Embedding 维度不一致** —— 模型维度必须和 Milvus collection 维度一致
2. **切片太大或太小** —— 太大召回钝，太小语义碎，先试 500-1000 字符
3. **没有 metadata 过滤** —— 不按 `knowledgeBaseId` / `userId` 过滤会串库
4. **入库同步阻塞接口** —— 必须异步任务化
5. **只存向量不存 chunk 正文** —— 引用来源、排错、重建索引都难做
6. **逻辑堆在 `AiServiceImpl` 里** —— 切片、入库、检索要拆服务

## 八、官方资料

- [Spring AI Milvus Vector Store](https://docs.spring.io/spring-ai/reference/api/vectordbs/milvus.html)
- [Milvus 部署方式总览](https://milvus.io/docs/install-overview.md)

最推荐的路线：**Spring AI EmbeddingModel + Spring AI Milvus VectorStore + MySQL 维护 chunk 元数据**。
