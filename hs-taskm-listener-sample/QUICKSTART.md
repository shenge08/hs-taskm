# TaskM Listener Sample - 快速开始指南

## 项目概述

这是一个基于 Spring Boot 的 TaskM 监听器示例应用，演示了如何创建一个接收策略任务生命周期事件的 Web 服务。

## 项目结构

```
hs-taskm-listener-sample/
├── src/main/java/com/taskm/listener/sample/
│   ├── ListenerSampleApplication.java    # 主应用类
│   ├── config/                            # 配置类
│   │   ├── OpenApiConfig.java            # Swagger 配置
│   │   └── WebConfig.java                # Web/CORS 配置
│   ├── controller/                        # REST 控制器
│   │   ├── ListenerController.java       # 监听器接口
│   │   └── HealthController.java         # 健康检查
│   ├── dto/                              # 数据传输对象
│   │   ├── TaskStartedRequest.java       # 任务开始请求
│   │   ├── TaskCompletedRequest.java     # 任务完成请求
│   │   ├── TaskFailedRequest.java        # 任务失败请求
│   │   └── ListenerResponse.java         # 统一响应
│   ├── service/
│   │   └── ListenerService.java          # 业务逻辑
│   └── exception/
│       └── GlobalExceptionHandler.java   # 全局异常处理
├── src/main/resources/
│   └── application.yml                   # 应用配置
├── Dockerfile                            # Docker 镜像
├── docker-compose.yml                    # Docker Compose 配置
└── README.md                             # 完整文档
```

## 快速开始

### 方式 1: 直接运行 JAR

```bash
# 1. 构建项目
mvn clean package -DskipTests

# 2. 运行应用
java -jar target/hs-taskm-listener-sample-1.0.0-SNAPSHOT.jar

# 3. 测试健康检查
curl http://localhost:8080/api/health
```

### 方式 2: 使用 Docker

```bash
# 1. 构建 Docker 镜像
docker build -t taskm-listener-sample:1.0.0 .

# 2. 运行容器
docker run -d \
  -p 8080:8080 \
  -e LOG_LEVEL=DEBUG \
  --name taskm-listener \
  taskm-listener-sample:1.0.0

# 3. 查看日志
docker logs -f taskm-listener

# 4. 测试健康检查
curl http://localhost:8080/api/health
```

### 方式 3: 使用 Docker Compose

```bash
# 1. 启动服务
docker-compose up -d

# 2. 查看日志
docker-compose logs -f listener

# 3. 停止服务
docker-compose down
```

## API 端点

### 监听器接口

```bash
# 任务开始事件
POST /api/task-started
Content-Type: application/json

{
  "task_id": 123,
  "timestamp": "2026-03-24T10:30:00Z",
  "message": "Task started"
}

# 任务完成事件
POST /api/task-completed
Content-Type: application/json

{
  "task_id": 123,
  "timestamp": "2026-03-24T10:35:00Z",
  "result": {
    "profit": 100.5,
    "trades": 5
  },
  "message": "Task completed"
}

# 任务失败事件
POST /api/task-failed
Content-Type: application/json

{
  "task_id": 123,
  "timestamp": "2026-03-24T10:32:00Z",
  "error": "Connection timeout",
  "stack_trace": "..."
}
```

### 管理接口

```bash
# 健康检查
GET /api/health

# 服务信息
GET /api/info

# Swagger UI
GET /swagger-ui.html

# OpenAPI JSON
GET /api-docs
```

## 配置选项

### 环境变量

| 变量 | 默认值 | 描述 |
|------|--------|------|
| `SERVER_PORT` | 8080 | HTTP 服务端口 |
| `LOG_LEVEL` | INFO | 日志级别 (DEBUG, INFO, WARNING, ERROR) |
| `LOG_FORMAT` | text | 日志格式 (text, json) |

### application.yml 配置

```yaml
server:
  port: 8080

logging:
  level:
    root: INFO
    com.taskm.listener.sample: DEBUG

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

## 自定义扩展

### 1. 继承 ListenerService

```java
@Service
public class CustomListenerService extends ListenerService {

    @Override
    public void handleTaskCompleted(TaskCompletedRequest request) {
        super.handleTaskCompleted(request);

        // 添加自定义逻辑
        Double profit = (Double) request.getResult().get("profit");
        if (profit != null && profit > 0) {
            // 发送通知、保存到数据库等
        }
    }
}
```

### 2. 添加新的端点

```java
@RestController
@RequestMapping("/api/custom")
public class CustomController {

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return Map.of("totalTasks", 1000);
    }
}
```

## 策略代码集成

### 使用 TaskM SDK 调用监听器

```java
import com.taskm.sdk.ListenerClient;

public class MyStrategy {

    public void execute() {
        ListenerClient listener = new ListenerClient();

        try {
            // 通知任务开始
            listener.onTaskStarted();

            // 执行策略逻辑
            Map<String, Object> result = executeLogic();

            // 通知任务完成
            listener.onTaskCompleted(result);

        } catch (Exception e) {
            // 通知任务失败
            listener.onTaskFailed(e.getMessage());
        }
    }
}
```

### 环境变量配置

```bash
# 在策略容器中设置
export LISTENER_ENDPOINT=http://listener-host:8080/api
export TASK_ID=123
```

## 测试

### 使用 cURL 测试

```bash
# 测试任务开始
curl -X POST http://localhost:8080/api/task-started \
  -H "Content-Type: application/json" \
  -d '{
    "task_id": 123,
    "timestamp": "2026-03-24T10:30:00Z",
    "message": "Test task started"
  }'

# 测试任务完成
curl -X POST http://localhost:8080/api/task-completed \
  -H "Content-Type: application/json" \
  -d '{
    "task_id": 123,
    "timestamp": "2026-03-24T10:35:00Z",
    "result": {"profit": 100.5, "trades": 5}
  }'

# 测试任务失败
curl -X POST http://localhost:8080/api/task-failed \
  -H "Content-Type: application/json" \
  -d '{
    "task_id": 123,
    "timestamp": "2026-03-24T10:32:00Z",
    "error": "Test error"
  }'
```

### 运行单元测试

```bash
mvn test
```

## 故障排查

### 端口已被占用

```bash
# 使用不同端口
SERVER_PORT=9090 java -jar app.jar
```

### 查看详细日志

```bash
# 启用 DEBUG 级别
LOG_LEVEL=DEBUG java -jar app.jar

# 使用 JSON 格式日志
LOG_FORMAT=json java -jar app.jar
```

### 健康检查失败

```bash
# 检查服务状态
curl http://localhost:8080/api/health

# 检查服务信息
curl http://localhost:8080/api/info

# 查看 Spring Boot Actuator
curl http://localhost:8080/actuator/health
```

## 生产部署建议

1. **使用外部配置**
   ```bash
   java -jar app.jar --spring.config.location=file:/etc/taskm/listener/
   ```

2. **启用 HTTPS**
   ```yaml
   server:
     ssl:
       enabled: true
       key-store:classpath:keystore.p12
   ```

3. **配置日志文件**
   ```yaml
   logging:
     file:
       name: /var/log/taskm/listener.log
     logback:
       rollingpolicy:
         max-size: 100MB
         max-history: 30
   ```

4. **资源限制**
   ```yaml
   # docker-compose.yml
   services:
     listener:
       deploy:
         resources:
           limits:
             cpus: '1'
             memory: 1G
           reservations:
             cpus: '0.5'
             memory: 512M
   ```

## 相关链接

- [完整文档](README.md)
- [TaskM Java SDK](https://github.com/taskm/taskm-sdk-java)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Swagger UI](http://localhost:8080/swagger-ui.html)

## 许可证

MIT License
