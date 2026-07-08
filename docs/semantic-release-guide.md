# semantic-release 使用说明

> 本仓库已接入 **semantic-release** 自动化发版系统。你只需要写好代码 → 提交 → push，版本号和发版流程全自动完成。

---

## 1. 日常开发流程

```
写代码 → git add → git commit → git push
                                          ↓
                               GitHub Actions 自动运行
                               ┌─────────────────────┐
                               │  编译检查 + 类型检查  │
                               │  全部通过后          │
                               │  自动发布新版本       │
                               └─────────────────────┘
```

**你不需要手动改版本号，不需要打 tag，不需要写 Release Notes。全部自动。**

---

## 2. commit 怎么写？（最重要）

commit 信息必须按特定格式写，否则会被拦截。

### 标准格式

```bash
git commit -m "类型: 描述"
```

### 常用类型

| 类型 | 什么时候用 | 示例 |
|---|---|---|
| `feat` | 加了新功能 | `feat: 打卡页面新增排行榜` |
| `fix` | 修了 bug | `fix: 修复打卡热力图表白屏` |
| `perf` | 性能优化 | `perf: 优化首页加载速度` |
| `refactor` | 重构代码（不改功能） | `refactor: 拆分打卡请求逻辑` |
| `docs` | 改文档 | `docs: 更新 README` |
| `style` | 改样式 | `style: 调整打卡按钮圆角` |
| `chore` | 杂项（配置、依赖等） | `chore: 升级 vue 版本` |
| `ci` | 改 CI 配置 | `ci: 添加语义化发版流程` |
| `test` | 加测试 | `test: 添加打卡接口单元测试` |

### 还能写更多信息

```bash
git commit -m "feat: 新增打卡排行榜

- 按连续打卡天数排序
- 支持查看好友排名
- 排行榜页面新增勋章展示"
```

### 错误的写法（会被拦截 ❌）

```bash
git commit -m "修复bug"              # ❌ 缺少类型前缀
git commit -m "fix"                   # ❌ 缺少描述
git commit -m "fix:修复bug"           # ❌ 冒号后缺少空格
git commit -m "Fix: 修复bug"          # ❌ 类型首字母大写
```

### 正确的写法 ✅

```bash
git commit -m "fix: 修复打卡列表加载失败的问题"
git commit -m "feat: 新增周年纪念日提醒功能"
git commit -m "docs: 更新 API 文档"
```

---

## 3. 版本号怎么确定？

commit 的类型决定了版本怎么升：

| commit 类型 | 版本变化 | 示例 |
|---|---|---|
| `feat` | 小版本 +1（minor） | 1.0.0 → 1.1.0 |
| `fix` / `refactor` / `perf` / `docs` / `style` | 补丁 +1（patch） | 1.0.0 → 1.0.1 |
| `chore` / `ci` / `test` | **不触发发版** | 版本不变 |
| commit 说明中写 `BREAKING CHANGE:` | 大版本 +1（major） | 1.0.0 → 2.0.0 |

### 示例场景

```
# 第一次提交
git commit -m "feat: 完成打卡功能"      → 自动发布 v1.0.0

# 修复 bug
git commit -m "fix: 修复打卡崩溃"        → 自动发布 v1.0.1

# 加新功能
git commit -m "feat: 新增勋章系统"       → 自动发布 v1.1.0

# 换个 README
git commit -m "docs: 更新说明"           → 自动发布 v1.1.1

# 改个配置
git commit -m "chore: 升级依赖版本"      → 不发版
```

---

## 4. 发版后会发生什么？

push 到 `main` 分支后，CI 会自动：

1. 编译检查后端、管理后台、小程序
2. 全部通过后 -> 分析最近一次发版以来的所有 commit
3. 决定版本号（major / minor / patch）
4. 更新 `CHANGELOG.md`（自动生成变更记录）
5. 把更新后的文件提交回仓库
6. 在 GitHub 上创建一个 Release
7. 通知你

你可以在 GitHub 仓库的 **Releases** 页面看到所有版本记录。

---

## 5. 小白常见问题

### Q: 我不小心写错了 commit 信息怎么办？

```bash
# 如果还没 push，可以修改最近一次 commit
git commit --amend -m "fix: 正确的描述"

# 如果已经 push 了，就不好改了，下次注意就行
```

### Q: commit 被拦截了，说格式不对？

```bash
# husky 会自动拦截格式不对的 commit，并提示错误。
# 按照提示的格式重新写就行。
```

### Q: 不想触发发版怎么办？

```bash
# 在 commit 信息中加 [skip ci]
git commit -m "docs: 更新 README [skip ci]"

# 这样 push 后不会触发 CI，也就不会发版
```

### Q: 我想本地试一下能不能正常发版？

```bash
# 在项目根目录运行（需要先 npm install）
npm run release:dry
```

---

## 6. 一句话总结

> **正常写代码 → `git commit -m "类型: 描述"` → `git push` → 自动发版**

不用管版本号、不用管 CHANGELOG、不用管 Release。写好 commit 就行。
