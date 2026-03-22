import { defineConfig, presetUno } from 'unocss'
import transformerDirectives from '@unocss/transformer-directives'

export default defineConfig({
  presets: [
    presetUno()
  ],
  transformers: [
    transformerDirectives()
  ],
  shortcuts: {
    // 页面安全区容器，只保留简单稳定的布局类。
    'page-shell-safe': 'min-h-screen box-border px-6 pb-40 pt-6',

    // 通用页面块的基础间距。
    'page-block': 'mt-6',

    // 常用横向布局。
    'row-between': 'flex items-center justify-between',
    'row-start': 'flex items-start',
    'row-center': 'flex items-center',

    // 常用纵向布局。
    'col-start': 'flex flex-col',
    'col-gap-3': 'flex flex-col gap-3',
    'col-gap-4': 'flex flex-col gap-4',

    // 常见文本语义，避免每次手写同样的简单类。
    'text-muted': 'text-sm text-[#8a735f]',
    'text-soft': 'text-sm leading-7 text-[#7f7366]',
    'text-strong': 'font-semibold text-[#2b2118]',

    // 轻量级横向标签或按钮排列。
    'chip-row': 'flex flex-wrap gap-3',
    'inline-center': 'inline-flex items-center justify-center'
  }
})
