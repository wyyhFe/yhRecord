package com.record.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档基础配置。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recordOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Life Record API")
                .version("v1")
                .description("生活记录小程序后端接口文档"));
    }
}
