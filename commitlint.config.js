export default {
  extends: ["@commitlint/config-conventional"],
  rules: {
    "body-leading-blank": [2, "always"],
    "footer-leading-blank": [2, "always"],
    "header-max-length": [2, "always", 108],
    "subject-case": [2, "never", ["start-case", "pascal-case"]],
    "subject-empty": [2, "never"],
    "type-empty": [2, "never"],
    "type-enum": [
      2,
      "always",
      [
        "feat",     // 新功能
        "fix",      // 修复
        "perf",     // 性能优化
        "style",    // 代码样式调整（非逻辑变更）
        "docs",     // 文档
        "test",     // 测试
        "refactor", // 重构
        "build",    // 构建/依赖
        "ci",       // CI 配置
        "chore",    // 杂项
        "revert"    // 回滚
      ]
    ]
  }
}
