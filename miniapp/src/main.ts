import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './styles/tailwind.safe.css'
import './styles/theme.scss'
import { registerRouteGuard } from './utils/auth'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)
  registerRouteGuard()
  return {
    app
  }
}
