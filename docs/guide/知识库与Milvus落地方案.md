# 知识库与 Milvus 落地方案

## 1. 目标

本方案用于后续在当前项目中落地：

- 知识库管理
- 文档导入
- 文档切片
- 向量写入 Milvus
- 基于知识库的 RAG 检索

当前原则：

- `MySQL` 负责知识库元数据与任务状态
- `OSS` 负责原始文档存储
- `Milvus` 负责切片向量检索
- `Redis` 负责缓存

---

## 2. 存储分层

### 2.1 MySQL 存什么

MySQL 负责“可管理、可审计、可追踪”的结构化数据：

- 知识库
- 文档元数据
- 切片任务
- 向量化任务状态
- 失败原因
- 上传人与权限

本次已在 `record/src/main/resources/schema.sql` 中新增三张表：

- `knowledge_base`
- `knowledge_document`
- `knowledge_chunk_task`

### 2.2 OSS 存什么

对象存储负责原始文件：

- pdf
- docx
- txt
- md
- html 导出文件

建议目录：

- `knowledge/raw/{knowledgeBaseId}/{documentId}/原始文件`

### 2.3 Milvus 存什么

Milvus 负责向量检索数据：

- 文档切片文本
- embedding 向量
- 用于过滤的 metadata

Milvus 不作为文档主真相源，只做检索层。

---

## 3. MySQL 表职责

### 3.1 knowledge_base

用途：

- 知识库基本信息
- 可见性
- 启停状态

核心字段：

- `id`
- `user_id`
- `name`
- `code`
- `description`
- `status`
- `visibility`

### 3.2 knowledge_document

用途：

- 某个知识库下的文档
- 原始文件路径
- 文档解析状态
- 向量化状态

核心字段：

- `id`
- `knowledge_base_id`
- `title`
- `source_type`
- `file_name`
- `file_path`
- `mime_type`
- `content_hash`
- `status`
- `chunk_count`
- `last_error`

### 3.3 knowledge_chunk_task

用途：

- 管理文档解析与向量化任务
- 支持失败重试

核心字段：

- `document_id`
- `task_type`
- `status`
- `retry_count`
- `last_error`
- `started_at`
- `finished_at`

---

## 4. Milvus 设计建议

## 4.1 collection 建议

建议先使用单 collection 方案，名称例如：

- `knowledge_chunk`

适合当前阶段，简单直接，便于统一检索。

### 4.2 Milvus 字段建议

建议至少包含：

- `id`
  - chunk 主键
- `knowledge_base_id`
  - 所属知识库
- `document_id`
  - 所属文档
- `user_id`
  - 所属用户
- `chunk_index`
  - 切片序号
- `chunk_text`
  - 切片正文
- `embedding`
  - 向量字段
- `source_type`
  - 文档来源
- `created_at`
  - 创建时间

### 4.3 为什么这样设计

原因：

- 检索后需要回溯到知识库和文档
- 后续支持按用户过滤
- 后续支持按知识库过滤
- 后续删除文档时，可按 `document_id` 删除对应向量

---

## 5. 文档导入流程

建议流程如下：

1. 用户上传文件
2. 文件进入 OSS
3. MySQL 创建 `knowledge_document`
4. MySQL 创建 `knowledge_chunk_task`
5. 后台任务开始解析文档
6. 解析成纯文本
7. 文本切片
8. 调用 embedding 模型生成向量
9. 批量写入 Milvus
10. 更新文档状态为 `VECTORIZED`

---

## 6. 切片策略建议

第一版建议：

- `chunk size`: 500 到 800 字符
- `chunk overlap`: 80 到 120 字符

为什么：

- 太小会碎片化严重
- 太大检索精度下降
- 这个范围适合作为第一版默认值

后续可按文档类型微调：

- FAQ / 规则说明：更小
- 长文档章节：更大

---

## 7. 检索流程建议

用户提问后：

1. 根据当前用户和知识库范围确定过滤条件
2. 对用户问题做 embedding
3. 在 Milvus 召回 topK 切片
4. 组装上下文
5. 将上下文和问题交给 Spring AI
6. 返回生成结果

建议：

- `topK` 第一版先用 `5`
- 控制上下文总长度
- 不要把过多切片全部塞给模型

---

## 8. 当前推荐实现顺序

建议按这个顺序推进：

### 第一阶段

- 完成 MySQL 表
- 完成文档上传接口
- 完成知识库基础管理接口

### 第二阶段

- 接文档解析
- 接文本切片
- 接 embedding
- 接 Milvus 写入

### 第三阶段

- 做检索接口
- 做 RAG 问答接口
- 做知识库后台管理页

---

## 9. 当前已执行内容

本次已执行：

- 在 `record/src/main/resources/schema.sql` 中新增知识库相关 MySQL 表

包括：

- `knowledge_base`
- `knowledge_document`
- `knowledge_chunk_task`

---

## 10. 下一步建议

如果继续实施，建议优先做：

1. 后端知识库表对应的 entity / mapper / service
2. 文档上传接口
3. 文档解析任务骨架
4. Milvus 接入配置

---

## 11. 一句话结论

当前项目的知识库落地方式应当是：

**MySQL 管元数据，OSS 管原始文件，Milvus 管向量检索。**

