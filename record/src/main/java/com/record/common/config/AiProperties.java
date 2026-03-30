package com.record.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 业务配置。
 * 与 spring.ai 下的模型连接参数分开，专门放业务默认值和风控阈值。
 */
@Data
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /**
     * 总开关，便于分环境控制。
     */
    private boolean enabled;

    /**
     * 当前使用的模型供应商标识，仅用于业务日志和路由判断。
     */
    private String provider = "openai-compatible";

    /**
     * 默认系统提示词。
     */
    private String systemPrompt = "你是生活记录助手，负责聊天、账单总结和消费分析。回答需要准确、克制，不编造账单数据。";

    private Chat chat = new Chat();
    private BillAnalysis billAnalysis = new BillAnalysis();

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
        private String systemPrompt = "你是专业账单分析助手。你只能基于用户提供的账单数据做总结、分类观察、风险提示和建议，不能捏造金额、趋势和交易记录。";
    }
}
