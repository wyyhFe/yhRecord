package com.record.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties({AppProperties.class, AliOssProperties.class, AiProperties.class})
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                // 不拦截 4xx（微信 API 有时返回 HTTP 412），让调用方自己解析响应体
                if (response.getStatusCode().is4xxClientError()) {
                    return;
                }
                super.handleError(response);
            }
        });
        return restTemplate;
    }
}

