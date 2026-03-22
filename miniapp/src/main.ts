import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './styles/theme.scss'
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
