import { existsSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs'
import { join, resolve } from 'node:path'

const roots = [
  resolve('dist/build/mp-weixin'),
  resolve('dist/dev/mp-weixin')
]

function walkWxss(dir, files = []) {
  if (!existsSync(dir)) return files
  for (const name of readdirSync(dir)) {
    const full = join(dir, name)
    const stat = statSync(full)
    if (stat.isDirectory()) {
      walkWxss(full, files)
    } else if (full.endsWith('.wxss')) {
      files.push(full)
    }
  }
  return files
}

let touched = 0

for (const root of roots) {
  for (const file of walkWxss(root)) {
    const source = readFileSync(file, 'utf8')
    const next = source
      .replace(/\[data-c-h="true"\]\s*\{[^}]*display\s*:\s*none\s*!important;?[^}]*\}/g, '')
      .replace('[data-c-h="true"]{display: none !important;}', '')
      .replace(/page\s*\{[^}]*display:\s*none;?[^}]*\}/g, '')
      .replace(/,\s*,/g, ',')

    if (next !== source) {
      writeFileSync(file, next, 'utf8')
      touched += 1
    }
  }
}

console.log(`[sanitize-mp-output] 已处理 ${touched} 个 WXSS 文件。`)
