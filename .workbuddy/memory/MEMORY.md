# Web 端关键架构

## 动态路由
- Web 端（Nuxt 3）不强制登录，公开页面直接访问
- 菜单由后端 `GET /api/system/menu/web-routes`（公开接口，platform=WEB）返回，前端 `useMenuStore` 缓存
- admin 端调用 `GET /api/system/menu/get-async-routes`（platform=ADMIN）
- 路由守卫：`middleware/auth.global.ts` 检查当前路由是否需要登录
- **Nuxt 3 不支持运行时 addRoute**，不同于 admin 的 Vue Router 动态注册
- 策略：后端控制菜单配置 + 中间件做权限拦截（非动态注册路由）
- requireAuth 采用前缀匹配（子路由继承父路由设置）
- `sys_menu` 表复用 admin 体系，通过 `meta.roles` + `platform` 列控制权限：
  - platform='ADMIN' → 仅管理后台可见
  - platform='WEB' → 仅 C 端可见
  - platform='ALL' → 双端可见
  - roles 为空 → 公开菜单（未登录可见）
  - roles = ["admin"] → 仅 admin 可见
  - 已登录用户按 user role 精确过滤

## 菜单种子数据
| path | platform | requireAuth |
|------|----------|------------|
| /, /diary, /ledger, /checkin, /memorial, /memory | ALL | false |
| /ai | ALL | true |
| /knowledge | ALL | false |
| /posts | WEB | false |

## 登录
- 侧边栏底部小登录按钮
- 多策略：微信 OAuth / GitHub OAuth / Google OAuth
- OAuth 流程：`/login` → `/api/auth/{provider}/authorize` → 回调 → `/auth/callback` → 存 token → 跳回

## 博客模块 (blog)
- Web 端博客定位：个人公开博客，admin 端管理文章
- slug 生成：中文标题 → AI（Spring AI ChatClient）翻译英文 → 重复加 `-2` 后缀
- Prompt 模板：`resources/prompts/ai/blog/slug-generation.md`，可通过 `app.ai.blog.slug-prompt` 配置覆盖
- 评论：`biz_comment` 通用表，`target_type`='BLOG_POST'/'DIARY'，支持嵌套回复（`parent_id`）
- 标签：`biz_blog_tag_rel` 自由文本标签（非字典表）
- 浏览计数：`biz_blog_view` IP+userId 去重（24h 窗口）
- 公开 API：`/blog/public/**`（permitAll）
- Admin API：`/blog/posts/**`（需认证）
