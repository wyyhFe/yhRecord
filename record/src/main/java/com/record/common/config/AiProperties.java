package com.record.common.config;

import com.record.modules.ai.config.AiProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.EnumMap;
import java.util.Map;

/**
 * AI 业务配置。
 * 与 spring.ai 下的模型连接参数分开，专门放业务默认值和风控阈值。
 *
 * 多供应商切换说明：
 * - {@link #active} 决定当前激活的供应商
 * - {@link #providers} 按枚举名映射到具体的 api-key / base-url / chat 模型
 * - 启动时 {@code AiProviderEnvironmentPostProcessor} 会把激活供应商的参数注入到
 *   {@code spring.ai.openai.*}，让 Spring AI auto-config 无感切换底层模型
 */
@Data
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /**
     * 当前激活的 AI 供应商。
     * 默认 OpenAI；切换时只改这个字段（或对应的 APP_AI_ACTIVE 环境变量）。
     */
    private AiProvider active = AiProvider.OPENAI;

    /**
     * 各供应商的端点配置。
     * 仅在覆盖 {@link AiProvider} 枚举内置默认值或填写 api-key 时才需要在 yaml 中出现。
     */
    private Map<AiProvider, ProviderEndpoint> providers = new EnumMap<>(AiProvider.class);

    /**
     * 默认系统提示词。
     */
    private String systemPrompt;

    private Chat chat = new Chat();
    private BillAnalysis billAnalysis = new BillAnalysis();
    private Blog blog = new Blog();
    private Rag rag = new Rag();

    /**
     * 单个供应商的端点参数。
     * 任何字段为空都会回落到 {@link AiProvider} 的内置默认值。
     */
    @Data
    public static class ProviderEndpoint {
        /** OpenAI 兼容服务的 API key，**必填**，否则模型调用 401。 */
        private String apiKey;
        /** 服务端点，缺省时用枚举内置默认。 */
        private String baseUrl;
        /** chat 模型名。 */
        private String chatModel;
        /** embedding 模型名，没有 embedding 能力的供应商可留空。 */
        private String embeddingModel;
        /** chat 接口完整路径，比如 OpenAI 是 /v1/chat/completions，智谱是 /chat/completions。 */
        private String chatCompletionsPath;
        /** embedding 接口完整路径，没有 embedding 能力的供应商留空。 */
        private String embeddingsPath;
    }

    @Data
    public static class Chat {
        /**
         * 是否开启多轮会话记忆。
         */
        private boolean memoryEnabled = true;

        /**
         * 单个会话最多保留的消息条数。
         */
        private int maxMessages = 20;
    }

    @Data
    public static class BillAnalysis {
        /**
         * 单次分析最大账单条数，防止 prompt 过大。
         */
        private int maxEntries = 200;

        /**
         * 默认分析时间窗口，单位天。
         */
        private int defaultDays = 30;

        /**
         * 账单分析系统提示词。
         */
        private String systemPrompt;
    }

    @Data
    public static class Blog {
        /**
         * slug 生成 prompt。
         * 留空则从 classpath:prompts/ai/blog/slug-generation.md 读取默认模板。
         */
        private String slugPrompt;
    }

    @Data
    public static class Rag {
        /**
         * 是否启用 RAG 知识库问答。
         */
        private boolean enabled = true;

        /**
         * 检索返回的最相关切片数量。
         */
        private int topK = 5;

        /**
         * 相似度阈值，低于此值的切片不纳入上下文。
         */
        private double similarityThreshold = 0.65;

        /**
         * 切片目标字符数。
         */
        private int chunkSize = 600;

        /**
         * 切片重叠字符数。
         */
        private int chunkOverlap = 100;

        /**
         * Embedding 向量维度（必须与 Milvus collection 一致）。
         */
        private int embeddingDimension = 1536;
    }
}
