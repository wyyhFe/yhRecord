# TODO

## 优先级 P0 — 影响核心体验

- [x] **Dashboard 接入真实数据** — 新建 DashboardController + DashboardService，前端对接 `/api/dashboard/stats`
- [x] **账户设置页** — 实现 `admin/src/views/account-settings/index.vue`，对接 `/users/profile` 接口

## 优先级 P1 — 功能补全

- [x] **记账账本管理页** — 新建 `admin/src/views/business/ledger/books/` 使用 useTable 模式
- [x] **清理死目录** — 已删除 `miniapp/src/pages/memorial-manage/` 和 `miniapp/src/pages/memorial/`

## 优先级 P2 — 质量保障

- [x] **后端单元测试** — 新增 MemorialDayServiceImpl/DashboardServiceImpl/MenuServiceImpl 共 18 个测试用例
- [x] **CI/CD 流水线** — 新建 `.github/workflows/ci.yml`，包含后端编译+测试、管理端类型检查+构建、小程序类型检查+构建

## 优先级 P3 — 文档清理

- [x] **文档与实际同步** — 过期 docs 目录已在上次提交中删除
- [x] **部门管理标注** — 已在 `admin/src/views/system/dept/index.vue` 顶部添加 el-alert 提示
