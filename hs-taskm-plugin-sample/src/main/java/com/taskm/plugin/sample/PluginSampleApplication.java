package com.taskm.plugin.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sample Data Plugin Application for TaskM.
 *
 * <p>这是一个示例 Spring Boot 应用，演示如何创建一个数据插件，
 * 策略可以通过此插件获取市场数据、配置信息等。</p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>提供数据查询接口 /data</li>
 *   <li>支持灵活的参数传递</li>
 *   <li>自动日志记录</li>
 *   <li>健康检查端点</li>
 *   <li>OpenAPI/Swagger 文档</li>
 * </ul>
 *
 * <h3>环境变量：</h3>
 * <ul>
 *   <li>SERVER_PORT: 服务端口（默认：8080）</li>
 *   <li>LOG_LEVEL: 日志级别（DEBUG, INFO, WARNING, ERROR，默认：INFO）</li>
 *   <li>LOG_FORMAT: 日志格式（text, json，默认：text）</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * # 构建应用
 * mvn clean package
 *
 * # 运行应用
 * java -jar target/hs-taskm-plugin-sample-1.0.0-SNAPSHOT.jar
 *
 * # 自定义配置运行
 * SERVER_PORT=9090 LOG_LEVEL=DEBUG java -jar target/hs-taskm-plugin-sample-1.0.0-SNAPSHOT.jar
 * }</pre>
 *
 * @see com.taskm.plugin.sample.controller.DataPluginController
 * @since 1.0.0
 */
@SpringBootApplication
public class PluginSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(PluginSampleApplication.class, args);
    }
}
