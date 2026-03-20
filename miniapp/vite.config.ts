import { defineConfig } from 'vite'
import uniPlugin from '@dcloudio/vite-plugin-uni'
import { fileURLToPath, URL } from 'node:url'

/**
 * 兼容 `@dcloudio/vite-plugin-uni` 的旧版 CJS 导出。
 * 当前依赖在 ESM 配置文件里会返回一个带 `default` 的模块对象，因此这里做一次兜底展开。
 */
const createUniPlugin = (uniPlugin as unknown as { default?: () => unknown; (): unknown }).default
  ?? (uniPlugin as unknown as () => unknown)

export default defineConfig({
  plugins: [createUniPlugin()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
