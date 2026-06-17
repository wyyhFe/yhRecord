# App 打包开发清单

> 本文档基于 `miniapp` 项目源码分析生成，覆盖从微信小程序打包为 Android/iOS App 的全部适配工作。
>
> **生成日期**：2026-06-16
>
> **项目技术栈**：uni-app 3 + Vue 3 + TypeScript + Pinia + uview-pro

---

## 一、可行性结论

| 项目 | 说明 |
|------|------|
| **能否打包** | ✅ 可以，uni-app 原生支持多端 |
| **代码复用率** | ~80%，大部分业务代码无需改动 |
| **主要适配点** | 登录系统、地图/定位、头像选择、订阅消息 |
| **推荐打包方式** | HBuilderX 云打包 |
| **预计总工期** | 8-11 个工作日 |

---

## 二、环境准备

### 2.1 开发工具

- [ ] 安装 [HBuilderX 正式版](https://www.dcloud.io/hbuilderx.html)
- [ ] 注册 [DCloud 开发者账号](https://dev.dcloud.net.cn)
- [ ] 在 DCloud 开发者中心创建应用，获取 `__UNI__XXXXXXX` 格式的 AppID

### 2.2 项目配置

当前 `miniapp/src/manifest.json` 的 `appid` 为微信小程序 AppID（`wxc881594b9650474f`），打包 App 需要切换为 DCloud AppID。

```json
{
  "name": "Life Record",
  "appid": "__UNI__XXXXXXX",
  "description": "Life record app",
  "versionName": "1.0.0",
  "versionCode": "100"
}
```

### 2.3 构建脚本

在 `miniapp/package.json` 的 `scripts` 中新增：

```json
{
  "dev:app": "uni -p app",
  "build:app-android": "uni build -p app-android",
  "build:app-ios": "uni build -p app-ios"
}
```

---

## 三、manifest.json App 端配置

当前 `manifest.json` 只有 `mp-weixin` 节点，需新增 `app-plus` 配置：

### 3.1 基础配置

```json
{
  "app-plus": {
    "distribute": {
      "android": {
        "permissions": [
          "<uses-permission android:name=\"android.permission.INTERNET\"/>",
          "<uses-permission android:name=\"android.permission.ACCESS_FINE_LOCATION\"/>",
          "<uses-permission android:name=\"android.permission.ACCESS_COARSE_LOCATION\"/>",
          "<uses-permission android:name=\"android.permission.CAMERA\"/>",
          "<uses-permission android:name=\"android.permission.READ_EXTERNAL_STORAGE\"/>",
          "<uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\"/>"
        ],
        "minSdkVersion": 21
      },
      "ios": {
        "dSYMs": false
      },
      "sdkConfigs": {}
    },
    "modules": {},
    "splashscreen": {
      "alwaysShowBeforeRender": true,
      "autoclose": true,
      "waiting": true
    },
    "screenOrientation": [
      "portrait-primary"
    ]
  }
}
```

### 3.2 iOS 权限描述

```json
{
  "app-plus": {
    "distribute": {
      "ios": {
        "privacyDescription": {
          "NSLocationWhenInUseUsageDescription": "用于获取当前位置和在地图中选择地点，给日记记录补充位置信息",
          "NSCameraUsageDescription": "用于拍摄照片作为日记配图或更换头像",
          "NSPhotoLibraryUsageDescription": "用于选择照片作为日记配图或更换头像"
        }
      }
    }
  }
}
```

### 3.3 应用图标

需准备以下尺寸的 PNG 图标（无透明通道）：

| 尺寸 | 用途 |
|------|------|
| 1024x1024 | App Store 图标 |
| 512x512 | Android 启动图标 |
| 192x192 | Android 高清图标 |
| 144x144 | Android 中高清图标 |
| 96x96 | Android 标清图标 |
| 72x72 | Android 低清图标 |

在 HBuilderX 中通过「manifest.json 可视化界面 → App 图标配置」上传自动生成。

---

## 四、登录系统适配 ⚠️ 高优先级

### 4.1 现状分析

当前登录链路完全依赖微信小程序 API：

| 文件 | 问题 |
|------|------|
| `pages/auth/login.vue:35` | 调用 `uni.login({ provider: 'weixin' })` |
| `utils/session.ts:13` | 调用 `uni.login({ provider: 'weixin' })` |
| `components/business/login-sheet/index.vue:77` | 调用 `uni.login({ provider: 'weixin' })` |
| `utils/request.ts:217` | 401 恢复链调用 `silentReLogin()` |

App 端没有微信登录环境，`uni.login({ provider: 'weixin' })` 会直接失败。

### 4.2 适配方案

**推荐方案**：App 端使用手机号 + 验证码登录，后端新增对应接口。

#### 4.2.1 后端新增接口

```
POST /auth/phone-login
Body: { "phone": "13800138000", "code": "123456" }
Response: { "code": 0, "data": { "accessToken": "...", "refreshToken": "..." } }

POST /auth/send-sms-code
Body: { "phone": "13800138000" }
Response: { "code": 0, "data": null }
```

#### 4.2.2 前端改造 `pages/auth/login.vue`

```vue
<template>
  <view class="page-shell-safe login-page">
    <view class="login-page__hero">
      <view class="login-page__brand">Life Record</view>
      <view class="login-page__title">让记录变成一种长期关系</view>
    </view>

    <view class="login-page__actions">
      <!-- #ifdef MP-WEIXIN -->
      <u-button type="primary" shape="circle" :loading="loading" @click="handleWxLogin">
        {{ loading ? '登录中...' : '微信登录' }}
      </u-button>
      <!-- #endif -->

      <!-- #ifdef APP-PLUS -->
      <view class="login-form">
        <u-input v-model="phone" placeholder="请输入手机号" type="number" maxlength="11" />
        <u-input v-model="smsCode" placeholder="验证码" type="number" maxlength="6">
          <template #suffix>
            <u-button size="mini" :disabled="countdown > 0" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </u-button>
          </template>
        </u-input>
        <u-button type="primary" shape="circle" :loading="loading" @click="handlePhoneLogin">
          {{ loading ? '登录中...' : '登录' }}
        </u-button>
      </view>
      <!-- #endif -->
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { wxLogin, phoneLogin as phoneLoginApi, sendSmsCode } from '@/api/auth'
import { tokenStorage } from '@/utils/storage'

const loading = ref(false)
const phone = ref('')
const smsCode = ref('')
const countdown = ref(0)

// 小程序登录（原有逻辑）
async function handleWxLogin() {
  loading.value = true
  try {
    const loginRes = await uni.login({ provider: 'weixin' })
    const result = await wxLogin(loginRes.code || '')
    tokenStorage.setAccessToken(result.accessToken)
    tokenStorage.setRefreshToken(result.refreshToken)
    uni.$feedback.success('登录成功')
    setTimeout(() => uni.reLaunch({ url: '/pages/home/index' }), 300)
  } catch (error) {
    uni.$feedback.error(error)
  } finally {
    loading.value = false
  }
}

// App 手机号登录
async function handlePhoneLogin() {
  if (!phone.value || !smsCode.value) return
  loading.value = true
  try {
    const result = await phoneLoginApi(phone.value, smsCode.value)
    tokenStorage.setAccessToken(result.accessToken)
    tokenStorage.setRefreshToken(result.refreshToken)
    uni.$feedback.success('登录成功')
    setTimeout(() => uni.reLaunch({ url: '/pages/home/index' }), 300)
  } catch (error) {
    uni.$feedback.error(error)
  } finally {
    loading.value = false
  }
}

// 发送验证码
async function sendCode() {
  if (!phone.value || countdown.value > 0) return
  try {
    await sendSmsCode(phone.value)
    uni.$feedback.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (error) {
    uni.$feedback.error(error)
  }
}
</script>
```

#### 4.2.3 新增 API `api/auth.ts`

```typescript
// 新增 App 端登录接口
export function phoneLogin(phone: string, code: string) {
  return request<AuthToken>({
    url: '/auth/phone-login',
    method: 'POST',
    data: { phone, code },
    withAuth: false
  })
}

export function sendSmsCode(phone: string) {
  return request<null>({
    url: '/auth/send-sms-code',
    method: 'POST',
    data: { phone },
    withAuth: false
  })
}
```

#### 4.2.4 改造 `utils/session.ts`

App 端静默重登不能走 `uni.login({ provider: 'weixin' })`，需要跳过或走其他逻辑：

```typescript
export async function ensureSession(): Promise<boolean> {
  try {
    // #ifdef MP-WEIXIN
    const loginRes = await uni.login({ provider: 'weixin' })
    const result = await wxLogin(loginRes.code || '')
    tokenStorage.setAccessToken(result.accessToken)
    tokenStorage.setRefreshToken(result.refreshToken)
    return true
    // #endif

    // #ifdef APP-PLUS
    // App 端无法静默重登，返回 false 让 request.ts 跳转登录页
    return false
    // #endif
  } catch {
    return false
  }
}
```

#### 4.2.5 改造 `components/business/login-sheet/index.vue`

同上逻辑，增加条件编译分支。

### 4.3 工作量评估

| 任务 | 耗时 |
|------|------|
| 后端新增手机号登录接口 + 短信服务对接 | 1.5 天 |
| 前端登录页改造 + 条件编译 | 0.5 天 |
| session.ts / request.ts 适配 | 0.5 天 |
| 联调测试 | 0.5 天 |
| **小计** | **3 天** |

---

## 五、地图/定位系统适配 ⚠️ 高优先级

### 5.1 现状分析

当前地图系统基于腾讯地图 QQ Map SDK：

| 文件 | 问题 |
|------|------|
| `utils/qqmap/sdk/qqmap-wx-jssdk.js` | 内部调用 `wx.request` 和 `wx.getLocation`，App 端会报错 |
| `utils/qqmap/client.ts` | 实例化 QQ Map SDK，依赖上述文件 |
| `utils/qqmap/location.ts` | `searchLocations()` 直接调用 SDK 的 `search()` 方法 |
| `components/business/location-picker/index.vue` | `#ifndef MP-WEIXIN` 分支只显示"不支持"提示 |

`uni.getLocation` 和 `uni.chooseLocation` 在 App 端可用，但 QQ Map SDK 的 `wx.request` 调用会失败。

### 5.2 适配方案

**推荐方案**：将地图 API 调用迁移到后端代理，前端只保留 `uni.getLocation` / `uni.chooseLocation`。

#### 5.2.1 后端新增地图代理接口

```
POST /locations/poi-search
Body: { "keyword": "星巴克", "latitude": 30.0, "longitude": 120.0 }
Response: { "code": 0, "data": [{ "title": "...", "address": "...", "latitude": ..., "longitude": ... }] }

POST /locations/reverse-geocode
Body: { "latitude": 30.0, "longitude": 120.0 }
Response: { "code": 0, "data": { "address": "...", "province": "...", "city": "...", "district": "..." } }
```

> 注：`reverseGeocode` 接口已存在于 `api/location.ts`，确认后端已实现即可。

#### 5.2.2 改造 `utils/qqmap/location.ts`

```typescript
import { request } from '@/utils/request'

// pickCurrentLocationPayload — uni.getLocation 跨平台可用，无需大改
export async function pickCurrentLocationPayload() {
  const location = await uni.getLocation({ type: 'gcj02', geocode: true })
  // 逆地理编码改走后端（已有接口）
  const geo = await reverseGeocode(location.latitude, location.longitude)
  return { ...geo, latitude: location.latitude, longitude: location.longitude, sourceType: 'CURRENT' }
}

// searchLocations — 改为调用后端代理
export async function searchLocations(keyword: string) {
  const current = await uni.getLocation({ type: 'gcj02' })
  return request<LocationSearchResult[]>({
    url: '/locations/poi-search',
    method: 'POST',
    data: { keyword, latitude: current.latitude, longitude: current.longitude }
  })
}
```

#### 5.2.3 改造 `components/business/location-picker/index.vue`

```vue
<template>
  <view class="location-picker">
    <u-button size="small" @click="onCurrentLocation">📍 当前位置</u-button>
    <!-- #ifdef MP-WEIXIN -->
    <u-button size="small" @click="onManualLocation">🗺️ 微信地图选点</u-button>
    <!-- #endif -->
    <!-- #ifdef APP-PLUS -->
    <u-button size="small" @click="onAppMapPick">🗺️ 地图选点</u-button>
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
// #ifdef APP-PLUS
async function onAppMapPick() {
  // uni.chooseLocation 在 App 端可用（会调起系统地图）
  try {
    const location = await uni.chooseLocation({})
    emit('update:modelValue', {
      locationName: location.name,
      address: location.address,
      latitude: location.latitude,
      longitude: location.longitude,
      sourceType: 'MANUAL'
    })
  } catch {
    uni.$feedback.info('已取消选点')
  }
}
// #endif
</script>
```

#### 5.2.4 可选：移除 QQ Map SDK

如果 App 端完全不使用 QQ Map SDK，可以将 `utils/qqmap/sdk/` 目录标记为小程序专用：

```
utils/qqmap/sdk/qqmap-wx-jssdk.js    # 仅小程序使用
utils/qqmap/client.ts                 # 仅小程序使用
```

或在 `client.ts` 中增加条件编译：

```typescript
// #ifdef MP-WEIXIN
import QQMapWX from './sdk/qqmap-wx-jssdk'
export function createMapClient() {
  return new QQMapWX({ key: TENCENT_MAP_KEY })
}
// #endif
```

### 5.3 工作量评估

| 任务 | 耗时 |
|------|------|
| 后端新增 POI 搜索代理接口 | 0.5 天 |
| 前端 location.ts 改造 | 1 天 |
| location-picker 组件适配 | 0.5 天 |
| App 端地图选点测试 | 0.5 天 |
| **小计** | **2.5 天** |

---

## 六、头像选择适配 ⚠️ 中优先级

### 6.1 现状分析

`pages/profile/edit.vue:12` 使用微信小程序专有的头像选择按钮：

```html
<button open-type="chooseAvatar" @chooseavatar="onChooseAvatar">更换头像</button>
```

### 6.2 适配方案

```vue
<template>
  <!-- #ifdef MP-WEIXIN -->
  <button class="edit-hero__btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
    更换头像
  </button>
  <!-- #endif -->

  <!-- #ifdef APP-PLUS -->
  <button class="edit-hero__btn" @click="onAppChooseAvatar">更换头像</button>
  <!-- #endif -->
</template>

<script setup lang="ts">
// #ifdef APP-PLUS
async function onAppChooseAvatar() {
  try {
    const res = await uni.chooseImage({ count: 1, sizeType: ['compressed'] })
    const filePath = res.tempFilePaths[0]
    // 复用已有的 upload.ts 上传逻辑
    const url = await uploadToOss(filePath)
    // 更新头像
    await updateUserAvatar(url)
    avatarUrl.value = url
    uni.$feedback.success('头像已更新')
  } catch {
    uni.$feedback.info('已取消')
  }
}
// #endif
</script>
```

### 6.3 工作量评估

| 任务 | 耗时 |
|------|------|
| 头像选择条件编译 | 0.5 天 |
| **小计** | **0.5 天** |

---

## 七、订阅消息/推送适配 ⚠️ 中优先级

### 7.1 现状分析

`pages/profile/reminder.vue:132` 调用 `uni.requestSubscribeMessage`，仅小程序可用。配置文件 `config/app.ts` 中定义了 4 个订阅消息模板 ID。

### 7.2 适配方案

**推荐方案**：App 端使用 uni-push 2.0（DCloud 官方推送服务）。

#### 7.2.1 manifest.json 配置

```json
{
  "app-plus": {
    "modules": {
      "Push": {}
    },
    "distribute": {
      "sdkConfigs": {
        "push": {}
      }
    }
  }
}
```

#### 7.2.2 前端改造 `pages/profile/reminder.vue`

```typescript
async function subscribeNotification() {
  // #ifdef MP-WEIXIN
  uni.requestSubscribeMessage({
    tmplIds: templateIds,
    success: (result) => { /* 原有逻辑 */ },
    fail: reject
  })
  // #endif

  // #ifdef APP-PLUS
  try {
    const clientInfo = await uni.getPushClientId()
    // 将 pushClientId 上报后端，后端通过 uni-push 发送推送
    await reportPushClientId(clientInfo.cid)
    uni.$feedback.success('推送已开启')
  } catch {
    uni.$feedback.error('推送开启失败')
  }
  // #endif
}
```

#### 7.2.3 后端配合

- 对接 uni-push 服务端 API，或对接 FCM（Android）/ APNs（iOS）
- 新增 `POST /users/push-cid` 接口保存设备推送标识

### 7.3 工作量评估

| 任务 | 耗时 |
|------|------|
| uni-push 配置 | 0.5 天 |
| 前端条件编译改造 | 0.5 天 |
| 后端推送接口对接 | 0.5 天（如需即时推送） |
| **小计** | **1.5 天** |

---

## 八、API 请求层检查

### 8.1 现状

| 组件 | 技术 | App 兼容性 |
|------|------|-----------|
| HTTP 客户端 | luch-request | ✅ uni-app 原生，兼容 |
| 请求封装 | `utils/request.ts` | ✅ 无需改动 |
| 文件上传 | `utils/upload.ts`（uni.uploadFile） | ✅ 兼容 |
| SSE 流式 | `api/ai.ts`（uni.request + enableChunked） | ⚠️ 需验证 |

### 8.2 待确认项

- [ ] `API_BASE_URL`：App 端不能使用 `http://127.0.0.1`，需配置生产环境域名
- [ ] SSE 流式请求：`uni.request` 的 `enableChunked` 在 App 端的兼容性需要真机验证
- [ ] `uni.previewImage`：确认 App 端图片预览正常（应该兼容）

### 8.3 环境变量配置

创建 `.env.app.production`：

```env
VITE_API_BASE_URL=https://api.your-domain.com
VITE_OSS_BASE_URL=https://your-bucket.oss-cn-hangzhou.aliyuncs.com
```

---

## 九、UI / 样式适配

### 9.1 rpx 单位

uni-app 会自动将 rpx 转换为各平台适配单位，**无需手动修改**。

### 9.2 状态栏适配

App 端需要处理沉浸式状态栏：

```typescript
// App.vue onLaunch 中
// #ifdef APP-PLUS
plus.navigator.setStatusBarStyle('dark')
plus.navigator.setStatusBarBackground('#FFF8F0')
// #endif
```

### 9.3 安全区域

检查 `pages.json` 中的 `globalStyle` 配置，确保底部安全区适配：

```json
{
  "globalStyle": {
    "app-plus": {
      "safearea": {
        "background": "#fffaf4",
        "bottom": {
          "offset": "auto"
        }
      }
    }
  }
}
```

### 9.4 uview-pro 兼容性

uview-pro 基于 uni-app 组件规范开发，App 端基本兼容。需要验证的组件：

- [ ] `u-popup`：弹出层定位
- [ ] `u-button`：按钮样式
- [ ] `u-input`：输入框聚焦行为
- [ ] `u-tag`：标签渲染

### 9.5 tabBar 高度

App 端 tabBar 高度与小程序不同（通常更高），检查自定义 tabBar 相关样式是否有硬编码高度。

---

## 十、构建与打包

### 10.1 HBuilderX 云打包（推荐）

1. HBuilderX 打开 `miniapp` 目录
2. 菜单：发行 → 原生 App-云打包
3. 选择平台：Android / iOS
4. 填写包名（如 `com.yourname.liferecord`）
5. 选择证书：
   - Android：使用 DCloud 公用证书（测试）或自有证书（发布）
   - iOS：需要 Apple 开发者证书
6. 点击打包，等待 10-30 分钟
7. 下载安装包

### 10.2 本地打包（高级）

```bash
# Android
npm run build:app-android
# 产物路径：dist/build/app-android/

# iOS
npm run build:app-ios
# 产物路径：dist/build/app-ios/
```

本地打包需要安装 Android Studio（Android）或 Xcode（iOS）。

### 10.3 安装依赖

```bash
cd miniapp
# 确认 @dcloudio/uni-app-plus 已安装
npm ls @dcloudio/uni-app-plus
# 如未安装
npm install @dcloudio/uni-app-plus
```

---

## 十一、功能验证清单

打包后逐一验证以下功能：

| # | 功能 | 涉及文件 | App 端状态 | 备注 |
|---|------|----------|-----------|------|
| 1 | 登录 | `pages/auth/login.vue` | ❌ 需适配 | 见第四章 |
| 2 | 首页 | `pages/home/index.vue` | ✅ 可用 | — |
| 3 | 日记列表 | `pages/diary/index.vue` | ✅ 可用 | — |
| 4 | 写日记 | `pages/diary/editor.vue` | ⚠️ 需验证 | 定位功能需适配 |
| 5 | 日记详情 | `pages/diary/detail.vue` | ✅ 可用 | — |
| 6 | 记账本 | `pages/ledger/index.vue` | ✅ 可用 | — |
| 7 | 账本管理 | `pages/ledger/books.vue` | ✅ 可用 | — |
| 8 | 打卡 | `pages/checkin/index.vue` | ✅ 可用 | — |
| 9 | 创建打卡任务 | `pages/checkin/editor.vue` | ✅ 可用 | — |
| 10 | 我的成就 | `pages/checkin/medals.vue` | ✅ 可用 | — |
| 11 | 打卡历史 | `pages/checkin/history.vue` | ✅ 可用 | — |
| 12 | 纪念日 | `pages/memorialPage/index.vue` | ✅ 可用 | — |
| 13 | 回忆 | `pages/memory/index.vue` | ✅ 可用 | — |
| 14 | 个人中心 | `pages/profile/index.vue` | ✅ 可用 | — |
| 15 | 编辑资料 | `pages/profile/edit.vue` | ❌ 需适配 | 头像选择需适配 |
| 16 | 提醒设置 | `pages/profile/reminder.vue` | ❌ 需适配 | 推送需适配 |
| 17 | 回收站 | `pages/profile/recycle/index.vue` | ✅ 可用 | — |
| 18 | 标签管理 | `pages/profile/tags/index.vue` | ✅ 可用 | — |
| 19 | AI 聊天 | `pages/ai/index.vue` | ⚠️ 需验证 | SSE 流式需验证 |
| 20 | AI 账单分析 | `pages/ai/bill-analysis.vue` | ⚠️ 需验证 | SSE 流式需验证 |
| 21 | 图片上传 | `utils/upload.ts` | ✅ 可用 | — |
| 22 | 位置选择 | `components/business/location-picker` | ❌ 需适配 | 见第五章 |

---

## 十二、后端配合项汇总

| # | 接口 | 说明 | 优先级 |
|---|------|------|--------|
| 1 | `POST /auth/phone-login` | 手机号验证码登录 | 高 |
| 2 | `POST /auth/send-sms-code` | 发送短信验证码 | 高 |
| 3 | `POST /locations/poi-search` | POI 搜索代理 | 高 |
| 4 | `POST /locations/reverse-geocode` | 逆地理编码（已有，确认可用） | 中 |
| 5 | `POST /users/push-cid` | 上报推送设备标识 | 中 |
| 6 | 推送服务对接 | uni-push / FCM / APNs | 低 |
| 7 | CORS 配置 | 允许 App 端域名请求 | 中 |

---

## 十三、发布上架

### 13.1 Android

- [ ] 生成签名 APK/AAB（建议使用 AAB 格式上架 Google Play）
- [ ] 准备应用截图（手机 + 平板，各至少 2 张）
- [ ] 撰写应用描述
- [ ] 上架渠道：
  - Google Play（需开发者账号 $25 一次性）
  - 国内应用商店：华为、小米、OPPO、vivo、应用宝等（各需单独注册）

### 13.2 iOS

- [ ] 申请 Apple 开发者账号（$99/年）
- [ ] 创建 App ID + Provisioning Profile
- [ ] 在 App Store Connect 创建应用
- [ ] 准备应用截图（6.7 寸、6.5 寸、5.5 寸 iPhone + iPad）
- [ ] 撰写应用描述、隐私政策 URL
- [ ] 提交审核（通常 1-3 天）

### 13.3 隐私政策

App 上架需要提供隐私政策页面，建议：
- 在后端新增一个静态页面路由（如 `/privacy`）
- 或在前端 H5 页面中单独维护

---

## 十四、工作量总览

| 模块 | 改动量 | 预计耗时 |
|------|--------|----------|
| 环境准备 + manifest 配置 | 小 | 0.5 天 |
| 登录系统适配 | 大 | 3 天 |
| 地图/定位适配 | 大 | 2.5 天 |
| 头像选择适配 | 小 | 0.5 天 |
| 订阅消息/推送适配 | 中 | 1.5 天 |
| API 层检查 + 环境变量 | 小 | 0.5 天 |
| UI/样式调优 | 中 | 1 天 |
| 功能验证 + Bug 修复 | 中 | 1-2 天 |
| **总计** | | **10-12 天** |

---

## 十五、推荐执行顺序

```
第 1 天：环境准备 + manifest.json 配置 + 构建脚本
    ↓
第 2-4 天：登录系统适配（前后端同步进行）
    ↓
第 5-7 天：地图/定位系统适配（前后端同步进行）
    ↓
第 8 天：头像选择 + 订阅消息适配
    ↓
第 9 天：UI/样式调优 + 安全区域适配
    ↓
第 10-12 天：Android 云打包 → 真机测试 → 修复问题 → iOS 打包
```

> 💡 **关键路径**：登录系统和地图系统可以并行开发（分别涉及不同文件），是缩短工期的关键。
