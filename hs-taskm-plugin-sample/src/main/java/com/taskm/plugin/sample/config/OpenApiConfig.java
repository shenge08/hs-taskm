package com.taskm.plugin.sample.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger 配置。
 *
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("TaskM Data Plugin Sample API")
                .version("1.0.0")
                .description("""
                    TaskM 数据插件示例应用 API 文档

                    本应用是一个示例数据插件，展示了如何创建一个提供市场数据的 Web 服务。

                    **主要功能：**
                    - 提供数据查询接口（/api/data）
                    - 支持灵活的参数传递
                    - 返回 K线数据和技术指标
                    - 支持多条数据查询

                    **使用方法：**
                    1. 策略代码使用 TaskM SDK 的 DataPluginClient 调用这些接口
                    2. 设置环境变量 PLUGIN_ENDPOINT 指向本服务
                    3. 策略执行时自动从插件获取数据

                    **环境变量：**
                    - `SERVER_PORT`: 服务端口（默认：8080）
                    - `LOG_LEVEL`: 日志级别（DEBUG, INFO, WARNING, ERROR）
                    - `LOG_FORMAT`: 日志格式（text, json）

                    **相关文档：**
                    - [TaskM Java SDK](https://github.com/taskm/taskm-sdk-java)
                    - [API 文档](/swagger-ui.html)
                    """)
                .contact(new Contact()
                    .name("TaskM Team")
                    .email("support@taskm.com")
                    .url("https://github.com/taskm"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")));
    }
}
