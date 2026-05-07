import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
// 设计系统三件套，加载顺序很重要：
//   1. tokens —— 原子 token（圆角/间距/字号），跨主题不变
//   2. themes —— 4 套主题色变量（clay 默认在 page，其他用 .theme-{id} 覆盖）
//   3. globals —— 消费 token 的全局通用 class（.section-shell / .list-card 等）
import './styles/tokens.scss'
import './styles/themes/index.scss'
import './styles/globals.scss'
import { registerRouteGuard } from './utils/auth'
import feedbackPlugin from './plugins/feedback'
import uViewPro from 'uview-pro'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)
  app.use(uViewPro)
  app.use(feedbackPlugin)
  registerRouteGuard()
  return {
    app
  }
}
