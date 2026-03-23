package com.taskm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * HS-TASKM 主应用程序启动类
 *
 * <p>这是多语言策略容器执行平台的主入口点。
 * 提供策略、插件、监听器和任务的 REST API。
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.taskm"})
@EnableScheduling
public class HsTaskmApplication {

    public static void main(String[] args) {
        SpringApplication.run(HsTaskmApplication.class, args);
    }
}
