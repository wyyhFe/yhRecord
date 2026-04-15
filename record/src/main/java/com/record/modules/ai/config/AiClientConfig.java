package com.record.modules.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiClientConfig {

    @Bean
    // @ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
