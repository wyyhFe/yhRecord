import { defineConfig } from 'vite'
import uniPlugin from '@dcloudio/vite-plugin-uni'
import UnoCSS from 'unocss/vite'
import { fileURLToPath, URL } from 'node:url'

/**
 * 统一处理 `@dcloudio/vite-plugin-uni` 的导出形式。
 * 某些安装环境下会拿到默认导出，某些环境下会直接拿到函数本身。
 */
const createUniPlugin = (uniPlugin as unknown as { default?: () => unknown; (): unknown }).default
  ?? (uniPlugin as unknown as () => unknown)

export default defineConfig({
  plugins: [
    UnoCSS(),
    createUniPlugin()
  ],
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: "@import 'uview-pro/theme.scss';"
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
