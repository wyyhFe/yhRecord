# 手机号打通小程序与后台登录

> ✅ **前置条件已满足**：小程序已完成微信认证（有效期至 2027-06-17），`getPhoneNumber` 接口可用。

## Context

当前小程序用户通过 `wx.login → openid` 登录，管理后台通过 `username + password` 登录，两套体系完全独立。需要以**手机号**为跨平台唯一标识，实现：小程序微信授权获取手机号 → 绑定到用户账户 → 管理后台用手机号+密码登录。

微信原生 `<button open-type="getPhoneNumber">` 提供了手机号授权能力（**需企业认证**），无需额外 SDK。

## 改动概览

| 层 | 文件 | 动作 |
|---|---|---|
| DB | Flyway 迁移 `V20260730_25` | 新增 |
| 后端 | `User.java`, `UserProfileVO.java` | 修改 |
| 后端 | `AppProperties.java`, `application.yaml` | 修改 |
| 后端 | `WechatGetPhoneNumberResponse.java` | 新增 |
| 后端 | `WechatPhoneClient.java` | 新增 |
| 后端 | `BindPhoneRequest.java`, `PhoneLoginRequest.java` | 新增 |
| 后端 | `AuthService.java`, `AuthServiceImpl.java` | 修改 |
| 后端 | `AuthController.java`, `SecurityConfig.java` | 修改 |
| 小程序 | `src/api/user.ts` | 修改 |
| 小程序 | `src/pages/profile/security.vue` | 新增 |
| 小程序 | `src/pages/profile/index.vue` | 修改 |
| 小程序 | `src/pages.json` | 修改 |
| 管理后台 | `src/api/user.ts` | 修改 |
| 管理后台 | `src/store/modules/user.ts` | 修改 |
| 管理后台 | `src/views/login/utils/rule.ts` | 修改 |
| 管理后台 | `src/views/login/components/LoginPhone.vue` | 重写 |

## 实现细节

### 1. DB 迁移
**文件**: `record/src/main/resources/db/V20260730_25__add_phone_to_user.sql`
```sql
ALTER TABLE `sys_user`
    ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号（绑定后可用于管理后台登录）',
    ADD UNIQUE KEY `uk_phone` (`phone`);
```

### 2. 后端实体 + VO

**User.java** — 新增字段：
```java
@Schema(description = "手机号（绑定后可用于管理后台登录）", example = "13800138000")
private String phone;
```

**UserProfileVO.java** — 新增字段：
```java
@Schema(description = "手机号")
private String phone;
```

**UserServiceImpl.toVO()** — 追加 `.phone(user.getPhone())`。

### 3. 微信配置

**AppProperties.java** Wechat 内部类新增：
```java
private String getPhoneNumberUrl;
```

**application.yaml** `app.wechat` 下新增：
```yaml
get-phone-number-url: https://api.weixin.qq.com/wxa/business/getuserphonenumber
```

### 4. 微信集成层

**WechatGetPhoneNumberResponse.java** — 对照微信 API 返回结构：
```
{ errcode, errmsg, phone_info: { phoneNumber, purePhoneNumber, countryCode, watermark } }
```
用 `@JsonProperty` 映射 snake_case，对外暴露 `getPurePhoneNumber()`。

**WechatPhoneClient.java** — 复用 `MiniProgramMessageClient`（`record/.../wechat/MiniProgramMessageClient.java:79-103`）的 access_token 缓存模式：
- 依赖 `RestTemplate` + `AppProperties` + `StringRedisTemplate`
- `getAccessToken()` → 读 Redis key `MINI_PROGRAM_ACCESS_TOKEN`（与 MiniProgramMessageClient **共享同一 key**）→ miss 时调 `/cgi-bin/token` → 缓存，TTL = expiresIn - 300s
- `getPhoneNumber(String code)` → POST `{getPhoneNumberUrl}?access_token=...` body `{"code":"..."}` → 返回 `purePhoneNumber`

### 5. 后端 DTO

**BindPhoneRequest.java** — `{ code: String, password: String }`，password 至少 6 位。

**PhoneLoginRequest.java** — `{ phone: String, password: String }`。

### 6. 后端 Service

**AuthService 接口** 新增两个方法签名：
- `void bindPhone(Long userId, String code, String password)`
- `AuthTokenVO phoneLogin(String phone, String password)`

**AuthServiceImpl** 实现：
- `bindPhone()`: 校验用户存在 → 校验 phone 未绑定 → 调 `wechatPhoneClient.getPhoneNumber(code)` → 校验 phone 唯一性 → 设 `phone` + BCrypt 编码 `password` → `updateById`
- `phoneLogin()`: 查 `userMapper.selectOne(phone)` → null 则抛 `AuthException("手机号或密码错误")` → 密码 null 则抛错 → BCrypt 验证 → 禁用检查 → `issueToken(user)`（复用已有方法，自动查询 roles）

### 7. 后端 Controller

**AuthController** 新增两个端点：
- `POST /auth/bind-phone` — 需登录态（JWT），调 `authService.bindPhone(UserContext.getUserId(), ...)`
- `POST /auth/phone-login` — 无需登录态

**SecurityConfig** — `/auth/phone-login` 加入 `permitAll()` 列表。

### 8. 小程序 — API

**src/api/user.ts** 新增：
```typescript
export function bindPhone(data: { code: string; password: string }) {
  return request<void>({ url: '/auth/bind-phone', method: 'POST', data })
}
```

### 9. 小程序 — 账号安全页

**`src/pages/profile/security.vue`** (新页面)：
- 展示绑定状态：从 `appStore.profile.phone` 判空
- 未绑定时：微信原生 `<button open-type="getPhoneNumber" @getphonenumber="onGetPhoneNumber">` → 获取 `detail.code` → 用户输入密码 + 确认密码 → 调 `bindPhone()` → 刷新 profile → 提示成功
- 已绑定时：展示脱敏手机号（`phone.slice(0,3) + '****' + phone.slice(-4)`）
- 样式复用 profile 页面现有卡片风格

**`src/pages/profile/index.vue`** — `menuItems` 数组新增：
```typescript
{ key: 'security', icon: '🔐', title: '账号安全', description: '绑定手机号，设置管理后台登录' }
```
`handleSelect()` 路由映射新增 `security: '/pages/profile/security'`。

**`src/pages.json`** — 注册路由 `pages/profile/security`。

### 10. 管理后台 — API

**admin/src/api/user.ts** 新增：
```typescript
export const phoneLogin = (data?: object) => {
  return http.request<UserResult>("post", "/auth/phone-login", { data });
};
```

### 11. 管理后台 — Store

**admin/src/store/modules/user.ts** 新增 `loginByPhone` action，完全复用 `loginByUsername` 的模式（调 API → 设 token → resolve/reject）。

### 12. 管理后台 — 手机登录页

**admin/src/views/login/utils/rule.ts** — 新增 `phoneLoginRules`：
```typescript
export const phoneLoginRules: FormRules = {
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少6位", trigger: "blur" }
  ]
};
```

**admin/src/views/login/components/LoginPhone.vue** — 从当前 stub（phone + verifyCode + setTimeout）重写为：
- 两个字段：`phone`（`prefix-icon=Iphone`）+ `password`（`prefix-icon=Lock`，`show-password`）
- `onLogin` 调 `useUserStoreHook().loginByPhone({ phone, password })`
- 成功后 `initRouter()` + `router.push(getTopMenu(true).path)`
- 保留 "返回" 按钮调 `SET_CURRENTPAGE(0)`

## 验证方案

1. **后端编译**: `cd record && ./mvnw.cmd -q -DskipTests compile`
2. **后端测试**: `./mvnw.cmd test`（确保现有测试不回归）
3. **小程序 type-check**: `cd miniapp && npm run type-check`
4. **小程序构建**: `npm run build:mp-weixin`
5. **管理后台 typecheck**: `cd admin && pnpm typecheck`
6. **管理后台构建**: `pnpm build`
7. **端到端验证**（需启动后端 + 微信开发者工具）：
   - 小程序登录 → 进入个人中心 → 账号安全 → 点击微信手机号授权 → 设置密码 → 绑定成功
   - 浏览器打开管理后台 → 切到手机号登录 tab → 输入手机号 + 密码 → 登录成功
