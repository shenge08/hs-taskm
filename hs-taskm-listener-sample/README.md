# TaskM Listener Sample

一个示例监听器 Spring Boot 应用，演示如何接收和处理策略任务生命周期事件。

## 功能特性

- ✅ 接收任务开始事件
- ✅ 接收任务完成事件（包含执行结果）
- ✅ 接收任务失败事件（包含错误信息）
- ✅ 自动日志记录（支持文本和 JSON 格式）
- ✅ 健康检查端点
- ✅ OpenAPI/Swagger 文档
- ✅ 完整的示例代码和文档

## 快速开始

### 1. 构建

```bash
cd hs-taskm-listener-sample
mvn clean package
```

### 2. 运行

```bash
java -jar target/hs-taskm-listener-sample-1.0.0-SNAPSHOT.jar
```

### 3. 自定义配置

```bash
# 指定端口
SERVER_PORT=9090 java -jar target/hs-taskm-listener-sample-1.0.0-SNAPSHOT.jar

# 设置日志级别和格式
LOG_LEVEL=DEBUG LOG_FORMAT=json java -jar target/hs-taskm-listener-sample-1.0.0-SNAPSHOT.jar
```

## API 端点

### 监听器接口

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/api/task-started` | 接收任务开始事件 |
| POST | `/api/task-completed` | 接收任务完成事件 |
| POST | `/api/task-failed` | 接收任务失败事件 |

### 健康检查

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/health` | 健康检查 |
| GET | `/api/info` | 服务信息 |
| GET | `/actuator/health` | Spring Boot Actuator 健康检查 |

### 文档

| 端点 | 描述 |
|------|------|
| `/swagger-ui.html` | Swagger UI 交互式文档 |
| `/api-docs` | OpenAPI JSON 规范 |

## 使用示例

### 策略代码中使用

```java
import com.taskm.sdk.ListenerClient;
import java.util.Map;

// 初始化监听器客户端（从环境变量读取配置）
ListenerClient listener = new ListenerClient();

// 通知任务开始
listener.onTaskStarted();

try {
    // 执行策略逻辑
    Map<String, Object> result = executeStrategy();

    // 通知任务完成
    listener.onTaskCompleted(result);

} catch (Exception e) {
    // 通知任务失败
    listener.onTaskFailed(e.getMessage());
}
```

### 环境变量配置

在策略容器中设置以下环境变量：

```bash
# 监听器端点（必需）
export LISTENER_ENDPOINT=http://listener-host:8080/api

# 任务 ID（必需）
export TASK_ID=123

# 超时时间（可选，默认 30 秒）
export LISTENER_TIMEOUT=30
```

### 手动测试 API

使用 cURL 测试监听器端点：

```bash
# 测试任务开始事件
curl -X POST http://localhost:8080/api/task-started \
  -H "Content-Type: application/json" \
  -d '{
    "task_id": 123,
    "timestamp": "2026-03-24T10:30:00Z",
    "message": "Task started"
  }'

# 测试任务完成事件
curl -X POST http://localhost:8080/api/task-completed \
  -H "Content-Type: application/json" \
  -d '{
    "task_id": 123,
    "timestamp": "2026-03-24T10:35:00Z",
    "result": {
      "profit": 100.5,
      "trades": 5,
      "win_rate": 0.6
    }
  }'

# 测试任务失败事件
curl -X POST http://localhost:8080/api/task-failed \
  -H "Content-Type: application/json" \
  -d '{
    "task_id": 123,
    "timestamp": "2026-03-24T10:32:00Z",
    "error": "Connection timeout"
  }'
```

## 配置选项

### 应用配置

| 配置项 | 环境变量 | 默认值 | 描述 |
|--------|----------|--------|------|
| 服务端口 | `SERVER_PORT` | 8080 | HTTP 服务器端口 |
| 上下文路径 | `server.servlet.context-path` | `/` | 应用上下文路径 |

### 日志配置

| 配置项 | 环境变量 | 默认值 | 描述 |
|--------|----------|--------|------|
| 日志级别 | `LOG_LEVEL` | INFO | DEBUG, INFO, WARNING, ERROR, CRITICAL |
| 日志格式 | `LOG_FORMAT` | text | text 或 json |

### OpenAPI 配置

| 配置项 | 默认值 | 描述 |
|--------|--------|------|
| springdoc.api-docs.path | /api-docs | OpenAPI JSON 路径 |
| springdoc.swagger-ui.path | /swagger-ui.html | Swagger UI 路径 |

## 自定义扩展

### 1. 继承 ListenerService

创建自定义服务类来添加业务逻辑：

```java
package com.example.listener;

import com.taskm.listener.sample.service.ListenerService;
import com.taskm.listener.sample.dto.*;
import org.springframework.stereotype.Service;

@Service
public class CustomListenerService extends ListenerService {

    private final EmailService emailService;
    private final DatabaseService databaseService;

    public CustomListenerService(EmailService emailService,
                                 DatabaseService databaseService) {
        super();
        this.emailService = emailService;
        this.databaseService = databaseService;
    }

    @Override
    public void handleTaskStarted(TaskStartedRequest request) {
        super.handleTaskStarted(request);

        // 添加自定义逻辑
        databaseService.updateTaskStatus(
            request.getTaskId(),
            "STARTED"
        );

        emailService.sendNotification(
            "Task " + request.getTaskId() + " has started"
        );
    }

    @Override
    public void handleTaskCompleted(TaskCompletedRequest request) {
        super.handleTaskCompleted(request);

        // 处理任务结果
        double profit = (Double) request.getResult().get("profit");

        databaseService.saveTaskResult(
            request.getTaskId(),
            request.getResult()
        );

        if (profit > 0) {
            emailService.sendAlert(
                "Task " + request.getTaskId() + " profitable!",
                profit
            );
        }
    }

    @Override
    public void handleTaskFailed(TaskFailedRequest request) {
        super.handleTaskFailed(request);

        // 记录失败信息
        databaseService.logTaskFailure(
            request.getTaskId(),
            request.getError(),
            request.getStackTrace()
        );

        // 发送告警
        emailService.sendErrorAlert(
            "Task " + request.getTaskId() + " failed",
            request.getError()
        );
    }
}
```

### 2. 添加新的端点

创建新的控制器：

```java
package com.example.listener.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/custom")
public class CustomController {

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        // 返回自定义统计信息
        return Map.of(
            "totalTasks", 1000,
            "successRate", 0.85,
            "avgDuration", "5m"
        );
    }
}
```

### 3. 添加数据库支持

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

配置数据库连接（`application.yml`）：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskm_listener
    username: taskm
    password: taskm
  jpa:
    hibernate:
      ddl-auto: update
```

## Docker 部署

### Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/hs-taskm-listener-sample-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 构建和运行

```bash
# 构建镜像
docker build -t taskm-listener-sample:1.0.0 .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e SERVER_PORT=8080 \
  -e LOG_LEVEL=DEBUG \
  --name taskm-listener \
  taskm-listener-sample:1.0.0
```

### Docker Compose

```yaml
version: '3.8'

services:
  listener:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SERVER_PORT=8080
      - LOG_LEVEL=INFO
      - LOG_FORMAT=json
    restart: unless-stopped
```

## 架构说明

```
┌─────────────────┐
│   策略容器        │
│                 │
│  ListenerClient │
└────────┬────────┘
         │ HTTP POST
         │ (task-started,
         │  task-completed,
         │  task-failed)
         ▼
┌─────────────────────────┐
│  Listener Sample App    │
│                         │
│  ┌──────────────────┐  │
│  │ ListenerController│  │
│  └────────┬─────────┘  │
│           │             │
│  ┌────────▼─────────┐  │
│  │ ListenerService  │  │
│  │                  │  │
│  │ - handleTaskStarted() │
│  │ - handleTaskCompleted()│
│  │ - handleTaskFailed()   │
│  └──────────────────┘  │
│                         │
│  ┌──────────────────┐  │
│  │     Logger       │  │
│  └──────────────────┘  │
└─────────────────────────┘
```

## 项目结构

```
hs-taskm-listener-sample/
├── src/main/java/com/taskm/listener/sample/
│   ├── ListenerSampleApplication.java    # Spring Boot 主类
│   ├── config/
│   │   ├── OpenApiConfig.java           # OpenAPI 配置
│   │   └── WebConfig.java               # Web 和 CORS 配置
│   ├── controller/
│   │   ├── ListenerController.java      # 监听器接口
│   │   └── HealthController.java        # 健康检查接口
│   ├── dto/
│   │   ├── TaskStartedRequest.java      # 任务开始请求 DTO
│   │   ├── TaskCompletedRequest.java    # 任务完成请求 DTO
│   │   ├── TaskFailedRequest.java       # 任务失败请求 DTO
│   │   └── ListenerResponse.java        # 统一响应 DTO
│   ├── service/
│   │   └── ListenerService.java         # 监听器业务逻辑
│   └── exception/
│       └── GlobalExceptionHandler.java  # 全局异常处理
├── src/main/resources/
│   └── application.yml                   # 应用配置文件
├── pom.xml                               # Maven 配置
└── README.md                             # 本文档
```

## 依赖项

- Spring Boot 3.2.3
- TaskM Java SDK 1.0.0
- SpringDoc OpenAPI 2.3.0
- Jackson (JSON 处理)
- Lombok (代码简化)

## 系统要求

- Java 17 或更高版本
- Maven 3.6 或更高版本
- 内存：至少 512MB
- 磁盘：至少 100MB

## 故障排除

### 端口已被占用

```bash
# 使用不同的端口
SERVER_PORT=9090 java -jar app.jar
```

### 查看详细日志

```bash
# 启用 DEBUG 级别日志
LOG_LEVEL=DEBUG java -jar app.jar

# 使用 JSON 格式日志
LOG_FORMAT=json java -jar app.jar
```

### 检查服务状态

```bash
# 健康检查
curl http://localhost:8080/api/health

# 查看 Spring Boot Actuator 信息
curl http://localhost:8080/actuator/health
```

## 许可证

MIT License

## 支持

- 文档：https://github.com/taskm/hs-taskm
- 问题反馈：https://github.com/taskm/hs-taskm/issues
- TaskM SDK：https://github.com/taskm/taskm-sdk-java
