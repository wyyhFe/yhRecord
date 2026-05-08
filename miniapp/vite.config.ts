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
  },
  // 微信小程序真机的 JS 引擎对 ES2020 支持不完整（可选链 ?. 和空值合并 ?? 在某些版本会报 Unexpected token ?）。
  // 强制 esbuild 把所有产物降到 ES2017，再让微信的 "es6: true + enhance: true" 把剩余 ES6 语法转 ES5。
  // 两层兜底，真机和模拟器都不会再撞这个语法坑。
  esbuild: {
    target: 'es2017'
  },
  build: {
    target: 'es2017'
  }
})
