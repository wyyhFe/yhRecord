import { existsSync, rmSync } from 'node:fs'
import { resolve } from 'node:path'

const targets = [
  resolve('dist/dev/mp-weixin'),
  resolve('dist/build/mp-weixin')
]

for (const target of targets) {
  if (!existsSync(target)) continue
  try {
    rmSync(target, { recursive: true, force: true })
    console.log(`[clean-mp-output] 已清理 ${target}`)
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error)
    console.warn(`[clean-mp-output] 跳过 ${target}，当前可能正被占用：${message}`)
  }
}
