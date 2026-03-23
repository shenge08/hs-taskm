package com.taskm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 配置类
 *
 * <p>提供 REST API 文档的自动生成和展示
 *
 * @see <a href="https://springdoc.org/">SpringDoc OpenAPI</a>
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置 OpenAPI 信息
     */
    @Bean
    public OpenAPI hsTaskmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HS-TASKM API")
                        .description("多语言策略容器执行平台 REST API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HS-TASKM Team")
                                .email("support@taskm.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("开发环境"),
                        new Server().url("https://api.taskm.com").description("生产环境")
                ));
    }

    /**
     * 策略 API 分组
     */
    @Bean
    public GroupedOpenApi strategyApi() {
        return GroupedOpenApi.builder()
                .group("策略管理")
                .pathsToMatch(
                        "/api/strategies/**",
                        "/api/tasks/**"
                )
                .build();
    }

    /**
     * 插件 API 分组
     */
    @Bean
    public GroupedOpenApi pluginApi() {
        return GroupedOpenApi.builder()
                .group("插件管理")
                .pathsToMatch(
                        "/api/plugins/**",
                        "/api/plugin-instances/**",
                        "/api/plugin-containers/**"
                )
                .build();
    }

    /**
     * 监听器 API 分组
     */
    @Bean
    public GroupedOpenApi listenerApi() {
        return GroupedOpenApi.builder()
                .group("监听器管理")
                .pathsToMatch(
                        "/api/listeners/**",
                        "/api/listener-instances/**",
                        "/api/listener-containers/**"
                )
                .build();
    }

    /**
     * 任务 API 分组
     */
    @Bean
    public GroupedOpenApi taskApi() {
        return GroupedOpenApi.builder()
                .group("任务管理")
                .pathsToMatch("/api/tasks/**")
                .build();
    }
}
