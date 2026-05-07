package com.record.modules.ai.config;

import org.springframework.util.StringUtils;

/**
 * AI 模型供应商枚举。
 *
 * 当前所有列出的供应商都提供 OpenAI 兼容端点，所以底层共用
 * {@code spring.ai.openai.*} 那一套 starter，切换时只需要改 base-url、api-key、chat 模型、路径。
 *
 * 使用方式：在 application.yaml 里配置 {@code app.ai.active: zhipu} 选择当前激活的供应商，
 * 启动时 {@link AiProviderEnvironmentPostProcessor} 会把对应供应商的参数注入到
 * {@code spring.ai.openai.*}，Spring AI auto-config 无感生效。
 *
 * 想新增供应商：
 * 1. 在这里加一个枚举常量，把默认 base-url / chat 模型 / embedding 模型 / 路径填好
 * 2. （可选）在 application.yaml 的 {@code app.ai.providers.{name}} 下覆盖默认值或填 api-key
 * 3. 想切换就改 {@code app.ai.active}
 *
 * 关于路径：Spring AI OpenAI 客户端默认路径是 {@code /v1/chat/completions} 和 {@code /v1/embeddings}。
 * 不同供应商的 OpenAI 兼容入口路径形式不同，所以这里允许每家自定义。
 */
public enum AiProvider {

    /** OpenAI 官方。国内访问需要走代理。 */
    OPENAI("https://api.openai.com",
            "gpt-4o-mini", "text-embedding-3-small",
            "/v1/chat/completions", "/v1/embeddings"),

    /**
     * 智谱 GLM。OpenAI 兼容入口在 /api/paas/v4。
     * 注意：智谱不在 base-url 后再叠 /v1，所以路径前缀不带 /v1。
     */
    ZHIPU("https://open.bigmodel.cn/api/paas/v4",
            "glm-4-flash", "embedding-3",
            "/chat/completions", "/embeddings"),

    /** DeepSeek。chat 能力强，目前不提供 embedding。 */
    DEEPSEEK("https://api.deepseek.com",
            "deepseek-chat", null,
            "/v1/chat/completions", null),

    /**
     * 阿里 DashScope，通义系列。
     * OpenAI 兼容入口在 /compatible-mode/v1，base-url 把 /v1 抠掉，
     * 让默认路径 /v1/chat/completions 自然拼上。
     */
    DASHSCOPE("https://dashscope.aliyuncs.com/compatible-mode",
            "qwen-turbo", "text-embedding-v3",
            "/v1/chat/completions", "/v1/embeddings");

    private final String defaultBaseUrl;
    private final String defaultChatModel;
    /** 不支持 embedding 的供应商这里写 null。后续 RAG 用到 embedding 时按 null 直接报"不支持"。 */
    private final String defaultEmbeddingModel;
    private final String defaultChatCompletionsPath;
    /** 同样：不支持 embedding 的供应商这里写 null。 */
    private final String defaultEmbeddingsPath;

    AiProvider(String defaultBaseUrl,
               String defaultChatModel,
               String defaultEmbeddingModel,
               String defaultChatCompletionsPath,
               String defaultEmbeddingsPath) {
        this.defaultBaseUrl = defaultBaseUrl;
        this.defaultChatModel = defaultChatModel;
        this.defaultEmbeddingModel = defaultEmbeddingModel;
        this.defaultChatCompletionsPath = defaultChatCompletionsPath;
        this.defaultEmbeddingsPath = defaultEmbeddingsPath;
    }

    public String defaultBaseUrl() {
        return defaultBaseUrl;
    }

    public String defaultChatModel() {
        return defaultChatModel;
    }

    public String defaultEmbeddingModel() {
        return defaultEmbeddingModel;
    }

    public String defaultChatCompletionsPath() {
        return defaultChatCompletionsPath;
    }

    public String defaultEmbeddingsPath() {
        return defaultEmbeddingsPath;
    }

    /**
     * 从字符串解析供应商，大小写不敏感。
     * 解析失败抛 IllegalArgumentException，让启动期就暴露配置错误。
     */
    public static AiProvider parse(String value) {
        if (!StringUtils.hasText(value)) {
            return OPENAI;
        }
        return AiProvider.valueOf(value.trim().toUpperCase());
    }
}
