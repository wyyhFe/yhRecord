import { defineConfig, type PluginOption } from 'vite'
import uniPlugin from '@dcloudio/vite-plugin-uni'
import { UniEcharts } from 'uni-echarts/vite'
import UnoCSS from 'unocss/vite'
import { fileURLToPath, URL } from 'node:url'

/**
 * 统一处理 `@dcloudio/vite-plugin-uni` 的导出形式。
 * 某些安装环境下会拿到默认导出，某些环境下会直接拿到函数本身。
 */
type UniPluginFactory = {
  default?: () => PluginOption
  (): PluginOption
}

const createUniPlugin = ((uniPlugin as unknown as UniPluginFactory).default
  ?? (uniPlugin as unknown as UniPluginFactory)) as () => PluginOption

export default defineConfig({
  plugins: [
    UnoCSS(),
    UniEcharts({
      echarts: {
        provide: false
      }
    }),
    createUniPlugin()
  ],
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: "@use 'uview-pro/theme.scss' as *;"
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
