package com.taskm.listener.sample.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 *
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("TaskM Listener Sample API")
                .version("1.0.0")
                .description("""
                    TaskM 监听器示例应用 API 文档

                    本应用是一个示例监听器，展示了如何创建一个接收策略任务生命周期事件的 Web 服务。

                    **主要功能：**
                    - 接收任务开始事件
                    - 接收任务完成事件（包含执行结果）
                    - 接收任务失败事件（包含错误信息）

                    **使用方法：**
                    1. 策略代码使用 TaskM SDK 的 ListenerClient 调用这些接口
                    2. 设置环境变量 LISTENER_ENDPOINT 指向本服务
                    3. 策略执行时会自动发送事件通知

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
