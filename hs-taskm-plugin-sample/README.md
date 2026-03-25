# TaskM 数据插件示例

一个示例数据插件 Spring Boot 应用，演示如何提供市场数据供策略调用。

## 功能特性

- ✅ 提供数据查询接口（支持任意参数）
- ✅ 返回 K线数据和技术指标
- ✅ 支持多条数据查询
- ✅ 自动日志记录（支持文本和 JSON 格式）
- ✅ 健康检查端点
- ✅ OpenAPI/Swagger 文档
- ✅ 完整的示例代码和文档

## 快速开始

### 1. 构建

```bash
cd hs-taskm-plugin-sample
mvn clean package
```

### 2. 运行

```bash
java -jar target/hs-taskm-plugin-sample-1.0.0-SNAPSHOT.jar
```

### 3. 自定义配置

```bash
# 指定端口
SERVER_PORT=9090 java -jar target/hs-taskm-plugin-sample-1.0.0-SNAPSHOT.jar

# 设置日志级别和格式
LOG_LEVEL=DEBUG LOG_FORMAT=json java -jar target/hs-taskm-plugin-sample-1.0.0-SNAPSHOT.jar
```

## API 端点

### 数据查询接口

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/api/data` | 获取数据（通用接口） |
| POST | `/api/data/multi` | 获取多条数据 |
| GET | `/api/market/info` | 获取市场信息 |

### 管理接口

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
import com.taskm.sdk.DataPluginClient;
import java.util.Map;

// 初始化数据插件客户端（从环境变量读取配置）
DataPluginClient plugin = new DataPluginClient();

// 获取数据
Map<String, Object> data = plugin.getData(Map.of(
    "symbol", "BTC/USDT",
    "interval", "1h"
));

System.out.println("Symbol: " + data.get("symbol"));
System.out.println("Close: " + data.get("close"));
System.out.println("Volume: " + data.get("volume"));
```

### 环境变量配置

在策略容器中设置以下环境变量：

```bash
# 数据插件端点（必需）
export PLUGIN_ENDPOINT=http://plugin-host:8080/api

# 超时时间（可选，默认 30 秒）
export PLUGIN_TIMEOUT=30
```

### 手动测试 API

使用 cURL 测试数据插件端点：

```bash
# 测试获取数据
curl -X POST http://localhost:8080/api/data \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "BTC/USDT",
    "interval": "1h",
    "limit": 100
  }'

# 测试获取多条数据
curl -X POST http://localhost:8080/api/data/multi \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "BTC/USDT",
    "interval": "1h",
    "limit": 10
  }'

# 测试市场信息
curl http://localhost:8080/api/market/info
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

### 1. 继承 DataPluginService

创建自定义服务类来添加业务逻辑：

```java
package com.example.plugin;

import com.taskm.plugin.sample.service.DataPluginService;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class CustomDataPluginService extends DataPluginService {

    private final ExchangeApiClient exchangeClient;
    private final DataCache cache;

    public CustomDataPluginService(ExchangeApiClient client, DataCache cache) {
        super();
        this.exchangeClient = client;
        this.cache = cache;
    }

    @Override
    public Map<String, Object> getData(Map<String, Object> params) {
        String symbol = (String) params.get("symbol");
        String interval = (String) params.get("interval");

        // 检查缓存
        String cacheKey = symbol + "_" + interval;
        Map<String, Object> cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 调用交易所 API
        Map<String, Object> data = exchangeClient.getKlines(symbol, interval);

        // 保存到缓存
        cache.put(cacheKey, data, 60); // 缓存 60 秒

        return data;
    }

    @Override
    public Map<String, Object> getMultiData(MarketDataRequest request) {
        // 自定义多条数据获取逻辑
        return exchangeClient.getHistoricalKlines(
            request.getSymbol(),
            request.getInterval(),
            request.getLimit()
        );
    }
}
```

### 2. 添加新的端点

创建新的控制器：

```java
package com.example.plugin.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/custom")
public class CustomController {

    @GetMapping("/tickers")
    public Map<String, Object> getTickers() {
        // 返回所有交易对的ticker数据
        return Map.of("BTC/USDT", 45000.0, "ETH/USDT", 3000.0);
    }

    @GetMapping("/orderbook/{symbol}")
    public Map<String, Object> getOrderBook(@PathVariable String symbol) {
        // 返回订单簿数据
        return Map.of("symbol", symbol, "bids", List.of(), "asks", List.of());
    }
}
```

### 3. 集成外部 API

添加 HTTP 客户端依赖（已包含 Apache HttpClient5）：

```java
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

@Service
public class BinanceApiClient {

    public Map<String, Object> getKlines(String symbol, String interval) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://api.binance.com/api/v3/klines");

            String json = String.format("{\"symbol\":\"%s\",\"interval\":\"%s\"}", symbol, interval);
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                // 处理响应
                return parseResponse(response);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
    }
}
```

### 4. 添加数据库支持

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
    url: jdbc:postgresql://localhost:5432/taskm_plugin
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

COPY target/hs-taskm-plugin-sample-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 构建和运行

```bash
# 构建镜像
docker build -t taskm-plugin-sample:1.0.0 .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e SERVER_PORT=8080 \
  -e LOG_LEVEL=DEBUG \
  --name taskm-plugin \
  taskm-plugin-sample:1.0.0
```

### Docker Compose

```yaml
version: '3.8'

services:
  plugin:
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
│  DataPluginClient│
└────────┬────────┘
         │ HTTP POST
         │ /api/data
         │ (with params)
         ▼
┌─────────────────────────┐
│  Data Plugin Sample App │
│                         │
│  ┌──────────────────┐  │
│  │DataPluginController│ │
│  └────────┬─────────┘  │
│           │             │
│  ┌────────▼─────────┐  │
│  │ DataPluginService│  │
│  │                  │  │
│  │ - getData()      │  │
│  │ - getMultiData() │  │
│  │ - getMarketInfo()│  │
│  └──────────────────┘  │
│                         │
│  ┌──────────────────┐  │
│  │     Logger       │  │
│  └──────────────────┘  │
└─────────────────────────┘
         │
         │ (可选) 调用外部 API
         ▼
┌─────────────────┐
│  交易所 API      │
│  数据库         │
│  缓存服务       │
└─────────────────┘
```

## 项目结构

```
hs-taskm-plugin-sample/
├── src/main/java/com/taskm/plugin/sample/
│   ├── PluginSampleApplication.java     # Spring Boot 主类
│   ├── config/
│   │   ├── OpenApiConfig.java           # OpenAPI 配置
│   │   └── WebConfig.java               # Web 和 CORS 配置
│   ├── controller/
│   │   ├── DataPluginController.java    # 数据插件接口
│   │   └── HealthController.java        # 健康检查接口
│   ├── dto/
│   │   ├── DataResponse.java            # 数据响应 DTO
│   │   └── MarketDataRequest.java       # 市场数据请求 DTO
│   ├── service/
│   │   └── DataPluginService.java       # 插件业务逻辑
│   └── exception/
│       └── GlobalExceptionHandler.java  # 全局异常处理
├── src/main/resources/
│   └── application.yml                   # 应用配置文件
├── src/test/
│   └── java/.../DataPluginControllerTest.java
├── pom.xml                               # Maven 配置
├── Dockerfile                            # Docker 镜像
├── docker-compose.yml                    # Docker Compose 配置
└── README.md                             # 本文档
```

## 依赖项

- Spring Boot 3.2.3
- TaskM Java SDK 1.0.0
- SpringDoc OpenAPI 2.3.0
- Jackson (JSON 处理)
- Apache HttpClient 5
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

## 性能优化建议

### 1. 添加缓存

```java
@Service
public class CachedDataPluginService extends DataPluginService {

    private final Cache<String, Map<String, Object>> cache =
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    @Override
    public Map<String, Object> getData(Map<String, Object> params) {
        String key = generateKey(params);

        return cache.get(key, k -> {
            // 从数据源获取
            return super.getData(params);
        });
    }
}
```

### 2. 异步处理

```java
@Service
public class AsyncDataPluginService extends DataPluginService {

    @Async
    public CompletableFuture<Map<String, Object>> getDataAsync(Map<String, Object> params) {
        return CompletableFuture.completedFuture(getData(params));
    }
}
```

### 3. 批量查询

```java
public Map<String, Object> getBatchData(List<Map<String, Object>> requests) {
    return requests.parallelStream()
        .collect(Collectors.toMap(
            r -> r.get("symbol").toString(),
            this::getData
        ));
}
```

## 许可证

MIT License

## 支持

- 文档：https://github.com/taskm/hs-taskm
- 问题反馈：https://github.com/taskm/hs-taskm/issues
- TaskM SDK：https://github.com/taskm/taskm-sdk-java
