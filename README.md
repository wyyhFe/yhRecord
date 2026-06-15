# lifeRecord

个人生活记录微信小程序，覆盖日记、记账、打卡、纪念日、AI 辅助等场景。

---

## 项目结构

| 目录 | 技术栈 | 说明 |
|---|---|---|
| `miniapp/` | uni-app 3 + Vue 3 + TS + Pinia + uView Pro | 微信小程序前端 |
| `record/` | Spring Boot 3.5 + Java 21 + MyBatis-Plus + Spring Security + JWT + Redis + Spring AI | 后端服务 |
| `admin/` | vue-pure-admin 7（Element Plus） | 管理后台 |

---

## 快速启动

### 后端

```bash
cd record
./mvnw.cmd spring-boot:run
```

### 小程序

```bash
cd miniapp
npm install
npm run dev:mp-weixin
```

### 管理后台

```bash
cd admin
pnpm install
pnpm dev
```

---

## 常用命令

```bash
# 后端编译检查
cd record && ./mvnw.cmd -q -DskipTests compile

# 前端类型检查
cd miniapp && npm run type-check

# 前端构建
cd miniapp && npm run build:mp-weixin
```

---

## 核心功能

| 模块 | 说明 |
|---|---|
| 📝 日记 | CRUD + 回收站 + 天气心情 + 图片位置 + 标签 + 点赞评论 |
| 💰 记账 | 多账本 + 收支流水 + 月度年度统计 + 标签统计 |
| ✅ 打卡 | 任务管理 + 每日打卡 + 热力图 + 勋章系统 + 心情标签 + 补卡机制 |
| 📅 纪念日 | CRUD + 每年重复 + 提醒配置 |
| 🤖 AI | 流式聊天 + 账单分析 + RAG 知识库（Milvus） |
| 🔔 提醒 | 微信订阅消息：日记/记账/纪念日 |
| 📍 定位 | 腾讯地图逆地理编码 + 位置选择 |
| 🗑️ 回收站 | 日记 + 记账流水软删除恢复 |

---

## 技术架构

```
微信小程序 (uni-app)
    ↓
Nginx (HTTPS 反代 + SSE 支持)
    ↓
Spring Boot 后端
    ├── MySQL（业务数据）
    ├── Redis（缓存 + OAuth state + AI 上下文）
    ├── 阿里云 OSS（文件存储）
    ├── Milvus（向量数据库）
    └── 微信/腾讯地图/智谱 AI（外部服务）
```

---

## 部署

腾讯云 ECS + Docker Compose：mysql + redis + nginx + backend 四容器。

- 域名：`https://recordlife.top`
- JVM 参数：`-Xmx1g -Xms512m -XX:+UseG1GC`

---

## 文档

| 文档 | 说明 |
|---|---|
| [项目总结](docs/项目总结.md) | 已完成功能、技术栈、架构详情 |
| [迭代记录](docs/功能迭代/迭代记录.md) | 功能迭代变更日志 |
| [打卡 V2 设计](docs/设计文档/打卡V2/设计文档.md) | 打卡模块 V2 产品设计 |
| [AGENTS.md](AGENTS.md) | AI Agent 开发指南 |
| [开发规范](开发规范.md) | 编码与提交规范 |
