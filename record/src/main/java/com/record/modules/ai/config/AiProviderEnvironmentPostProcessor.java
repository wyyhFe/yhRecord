package com.record.modules.ai.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 启动期把 {@code app.ai.active} 选中的供应商参数注入到 {@code spring.ai.openai.*}。
 *
 * 这样做的好处：
 * - Spring AI 的 OpenAI auto-config 不用改一行，照常从 {@code spring.ai.openai.*} 读
 * - 用户视角只有"换 active + 填对应 provider 的 key"两步
 * - 所有列出的供应商都是 OpenAI 兼容端点，共用一套 starter
 *
 * 执行时机：必须在 {@code @ConfigurationProperties} 绑定和 auto-config 之前，所以走
 * {@link EnvironmentPostProcessor}。注册位置在 {@code META-INF/spring.factories}。
 * （Spring Boot 3.x 的 .imports 文件只服务 @AutoConfiguration，EnvironmentPostProcessor 仍走 spring.factories。）
 *
 * 用 LOWEST_PRECEDENCE 是为了让 yaml 配置先加载完，placeholder 才能解析（比如
 * {@code ${ZHIPU_API_KEY:}} 需要环境变量已经在 environment 里）。
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class AiProviderEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "ai-provider-active";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 解析当前激活的供应商。yaml 写错或缺失就退回 OPENAI，启动不挂。
        String activeRaw = environment.getProperty("app.ai.active");
        AiProvider active;
        try {
            active = AiProvider.parse(activeRaw);
        } catch (IllegalArgumentException ex) {
            // 名字写错时把信息打到标准错误，启动期就让人看见
            System.err.println("[AiProviderEnvPostProcessor] 未知的 app.ai.active=" + activeRaw + "，回退 OPENAI");
            active = AiProvider.OPENAI;
        }

        // yaml 里 providers map 的 key 是小写枚举名，比如 app.ai.providers.zhipu.*
        String providerKey = active.name().toLowerCase();
        String prefix = "app.ai.providers." + providerKey + ".";

        // 优先用 yaml 里 providers 配置，缺省回落到枚举内置默认值。
        // api-key 没有内置默认（每个用户自己的 key），缺失就让 Spring AI 用空 key 启动 → 真实调用时 401
        String apiKey = trim(environment.getProperty(prefix + "api-key"));
        String baseUrl = firstNonEmpty(
                trim(environment.getProperty(prefix + "base-url")),
                active.defaultBaseUrl());
        String chatModel = firstNonEmpty(
                trim(environment.getProperty(prefix + "chat-model")),
                active.defaultChatModel());
        String embeddingModel = firstNonEmpty(
                trim(environment.getProperty(prefix + "embedding-model")),
                active.defaultEmbeddingModel());
        // 不同供应商 OpenAI 兼容路径不同（智谱不带 /v1，OpenAI 带），需要逐个覆盖 Spring AI 的默认路径
        String chatCompletionsPath = firstNonEmpty(
                trim(environment.getProperty(prefix + "chat-completions-path")),
                active.defaultChatCompletionsPath());
        String embeddingsPath = firstNonEmpty(
                trim(environment.getProperty(prefix + "embeddings-path")),
                active.defaultEmbeddingsPath());

        // 写入一组高优先级的 PropertySource，覆盖 application.yaml 里 spring.ai.openai 的硬编码。
        // 用 addFirst 保证优先级最高 —— 但允许真正的环境变量（系统级 env / -D 参数）继续覆盖。
        Map<String, Object> overrides = new HashMap<>();
        if (StringUtils.hasText(apiKey)) {
            overrides.put("spring.ai.openai.api-key", apiKey);
        }
        if (StringUtils.hasText(baseUrl)) {
            overrides.put("spring.ai.openai.base-url", baseUrl);
        }
        if (StringUtils.hasText(chatModel)) {
            overrides.put("spring.ai.openai.chat.options.model", chatModel);
        }
        if (StringUtils.hasText(embeddingModel)) {
            // P1 接 RAG 时 spring-ai-openai-embedding starter 会读这个属性
            overrides.put("spring.ai.openai.embedding.options.model", embeddingModel);
        }
        // chat 接口路径覆盖：Spring AI 1.x 的属性是 spring.ai.openai.chat.completions-path
        if (StringUtils.hasText(chatCompletionsPath)) {
            overrides.put("spring.ai.openai.chat.completions-path", chatCompletionsPath);
        }
        // embedding 接口路径覆盖
        if (StringUtils.hasText(embeddingsPath)) {
            overrides.put("spring.ai.openai.embedding.embeddings-path", embeddingsPath);
        }

        if (!overrides.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, overrides));
        }

        // 启动期 logger 还没初始化，用 System.out 打一行简短摘要：当前激活的供应商和 chat 模型。
        System.out.println("[AiProviderEnvPostProcessor] active=" + active
                + " chatModel=" + chatModel
                + " baseUrl=" + baseUrl);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String firstNonEmpty(String first, String fallback) {
        return StringUtils.hasText(first) ? first : fallback;
    }
}
