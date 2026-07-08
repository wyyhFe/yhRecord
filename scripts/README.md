# 部署脚本说明

## 项目结构

```
myproject/                    # 服务器上 /www/wwwroot/myproject
├── record/                   # 后端源码（本地，不部署到服务器）
├── admin/                    # 管理后台源码（本地）
├── scripts/                  # 部署脚本
│   ├── deploy-backend-docker.bat    # 部署后端
│   └── deploy-admin.bat             # 部署管理后台
└── record/Dockerfile         # 后端镜像打包配置
```

---

## deploy-backend-docker.bat — 部署后端

### 流程

```
┌─ 本机 ─────────────────────────────────────────────────┐
│  1. mvn clean package -DskipTests                       │
│     → record/target/record-0.0.1-SNAPSHOT.jar（编译）    │
│                                                         │
│  2. scp 上传到服务器                                      │
│     → /www/wwwroot/myproject/recordJar/*.jar            │
│     → /www/wwwroot/myproject/Dockerfile                 │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─ 服务器 ─────────────────────────────────────────────────┐
│  3. docker compose up -d --build                         │
│     → Docker 读取 Dockerfile                             │
│     → 把 recordJar/*.jar 打包成镜像                       │
│     → 重启 record-backend 容器                            │
└─────────────────────────────────────────────────────────┘
```

### 本质

**Bat = 自动登录服务器 + 自动执行 docker 命令。**

Dockerfile 在服务器上被 Docker 读取和执行，bat 只是替你做了「上传文件 + SSH 敲命令」的重复劳动。

### 使用

```bash
# 直接双击，或用命令行
scripts\deploy-backend-docker.bat
```

---

## deploy-admin.bat — 部署管理后台

### 流程

```
┌─ 本机 ─────────────────────────────────────────────────┐
│  1. pnpm build                                          │
│     → admin/dist/（纯静态文件：HTML + JS + CSS）          │
│                                                         │
│  2. tar 打包                                             │
│     → admin/dist.tar.gz                                 │
│                                                         │
│  3. scp 上传到服务器                                      │
│     → /tmp/lifeRecord-admin-dist.tar.gz                 │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─ 服务器 ─────────────────────────────────────────────────┐
│  4. 解压到 /www/wwwroot/myproject/recordFront-admin/     │
│     → nginx 直接读取这个目录，提供网页服务                  │
│     → 旧目录自动备份（加时间戳）                           │
└─────────────────────────────────────────────────────────┘
```

### 跟 Docker 的关系

**完全无关。** 管理后台是纯前端静态文件，不需要 Docker。nginx 直接 serve 这个目录。

### 使用

```bash
# 直接双击
scripts\deploy-admin.bat
```

---

## deploy-all.bat — 一键部署全部

按顺序执行：先后端 → 再管理后台。

```bash
scripts\deploy-all.bat
```

---

## 常见问题

### Dockerfile 在哪执行？

**在服务器上。** 当 `docker compose up -d --build` 执行时，Docker 引擎读取服务器上的 Dockerfile，把 jar 打包成镜像。

### 本机需要装 Docker 吗？

**不需要。** 本机只需要 JDK + Maven（编译后端）、pnpm（编译管理后台）、ssh + scp（上传文件）。

### 服务器要装什么？

| 软件 | 用途 |
|---|---|
| Docker + Docker Compose | 运行后端容器 |
| git（可选） | 如果想在服务器上 git pull 拉代码 |
| Maven（可选，不推荐） | 如果在服务器上编译（服务器内存通常不够） |

### 更新代码后怎么部署？

```bash
# 1. 本地 git push
# 2. 双击 deploy-backend-docker.bat（或 deploy-all.bat）
# 搞定，不需要登录服务器
```
