# TaskM 数据插件示例 - 快速开始指南

## 项目概述

这是一个基于 Spring Boot 的 TaskM 数据插件示例应用，演示了如何创建一个提供市场数据的 Web 服务。

## 快速开始

### 方式 1: 直接运行 JAR

```bash
# 1. 构建项目
mvn clean package -DskipTests

# 2. 运行应用
java -jar target/hs-taskm-plugin-sample-1.0.0-SNAPSHOT.jar

# 3. 测试健康检查
curl http://localhost:8080/api/health
```

### 方式 2: 使用 Docker

```bash
# 1. 先在本地构建 JAR 包
mvn clean package -DskipTests

# 2. 构建 Docker 镜像
docker build -t taskm-plugin-sample:1.0.0 .

# 3. 运行容器
docker run -d \
  -p 8080:8080 \
  -e LOG_LEVEL=DEBUG \
  -e SERVER_PORT=8080 \
  --name taskm-plugin \
  taskm-plugin-sample:1.0.0

# 4. 查看日志
docker logs -f taskm-plugin

# 5. 测试健康检查
curl http://localhost:8080/api/health
```

### 方式 3: 使用 Docker Compose

```bash
# 1. 先在本地构建 JAR 包
mvn clean package -DskipTests

# 2. 启动服务
docker-compose up -d

# 3. 查看日志
docker-compose logs -f plugin

# 4. 停止服务
docker-compose down
```

## API 端点

### 数据查询接口

```bash
# 获取数据
POST /api/data
Content-Type: application/json

{
  "symbol": "BTC/USDT",
  "interval": "1h",
  "limit": 100
}

# 获取多条数据
POST /api/data/multi
Content-Type: application/json

{
  "symbol": "BTC/USDT",
  "interval": "1h",
  "limit": 10
}

# 获取市场信息
GET /api/market/info
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
| `MARKET_DATA_URL` | http://172.16.90.99:8898/wdd/getBondInfoCompleteCommand | 市场数据接口地址 |
| `MARKET_DATA_TIMEOUT` | 5000 | 数据请求超时时间（毫秒） |

### 配置说明

#### 市场数据接口地址（MARKET_DATA_URL）

数据插件通过此环境变量配置的市场数据接口获取实时行情数据。

**默认地址：** `http://172.16.90.99:8898/wdd/getBondInfoCompleteCommand`

**自定义配置：**
```bash
# 方法1：直接设置环境变量
export MARKET_DATA_URL=http://your-market-data-server:8080/api/data

# 方法2：启动时设置
MARKET_DATA_URL=http://your-server:8080/api/data java -jar app.jar

# 方法3：Docker Compose
environment:
  - MARKET_DATA_URL=http://market-data:8080/api/data
```

**接口要求：**
- 接受 POST 请求
- 请求体格式：JSON
- 响应格式：JSON 数组，包含买价、卖价、最新价

#### 请求超时时间（MARKET_DATA_TIMEOUT）

设置数据请求的超时时间，单位为毫秒。

**默认值：** 5000（5秒）

**配置示例：**
```bash
# 设置为 10 秒
export MARKET_DATA_TIMEOUT=10000

# 设置为 3 秒
export MARKET_DATA_TIMEOUT=3000
```

## 策略代码集成

### 使用 TaskM SDK 调用数据插件

```java
import com.taskm.sdk.DataPluginClient;
import java.util.Map;

public class MyStrategy {

    public void execute() {
        DataPluginClient plugin = new DataPluginClient();

        // 获取债券市场数据
        Map<String, Object> data = plugin.getData(Map.of(
            "symbol", "10002335"  // 债券代码
        ));

        System.out.println("Symbol: " + data.get("symbol"));
        System.out.println("Bid: " + data.get("bid"));
        System.out.println("Ask: " + data.get("ask"));
        System.out.println("Last: " + data.get("last"));
        System.out.println("Spread: " + data.get("spread"));
        System.out.println("Mid Price: " + data.get("midPrice"));
    }
}
```

### 环境变量配置

```bash
# 策略容器中需要设置
export PLUGIN_ENDPOINT=http://plugin-host:8080/api

# 可选：自定义数据源
export MARKET_DATA_URL=http://your-market-server:8080/api/data
export MARKET_DATA_TIMEOUT=10000
```

## 测试

### 使用 cURL 测试

```bash
# 测试获取债券市场数据
curl -X POST http://localhost:8080/api/data \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "10002335"
  }'

# 预期响应：
# {
#   "success": true,
#   "message": "Data retrieved successfully",
#   "data": {
#     "symbol": "10002335",
#     "bid": 3499.5,
#     "ask": 3500.5,
#     "last": 3500.0,
#     "spread": 1.0,
#     "midPrice": 3500.0,
#     "timestamp": 1711234567890
#   }
# }

# 测试获取多条数据
curl -X POST http://localhost:8080/api/data/multi \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "10002335",
    "interval": "1h",
    "limit": 5
  }'

# 测试市场信息
curl http://localhost:8080/api/market/info
```

### 运行单元测试

```bash
mvn test
```

## 自定义扩展示例

### 1. 调用真实交易所 API

```java
@Service
public class BinanceDataService extends DataPluginService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Object> getData(Map<String, Object> params) {
        String symbol = (String) params.get("symbol");
        String interval = (String) params.get("interval");

        // 调用币安 API
        String url = String.format(
            "https://api.binance.com/api/v3/klines?symbol=%s&interval=%s&limit=1",
            symbol.replace("/", ""),
            interval
        );

        Object[][] klines = restTemplate.getForObject(url, Object[][].class);

        // 转换数据格式
        return convertToDataFormat(klines[0]);
    }
}
```

### 2. 添加数据库缓存

```java
@Service
public class CachedDataService extends DataPluginService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> getData(Map<String, Object> params) {
        String symbol = (String) params.get("symbol");
        String interval = (String) params.get("interval");

        // 查询缓存
        String sql = "SELECT data FROM market_cache WHERE symbol = ? AND interval = ? AND timestamp > NOW() - INTERVAL '1 minute'";
        List<Map<String, Object>> cached = jdbcTemplate.queryForList(sql, symbol, interval);

        if (!cached.isEmpty()) {
            return (Map<String, Object>) cached.get(0).get("data");
        }

        // 从源获取数据
        Map<String, Object> data = super.getData(params);

        // 保存缓存
        String insertSql = "INSERT INTO market_cache (symbol, interval, data) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertSql, symbol, interval, new PGObject().setValue(JSONObject.toJSONString(data)));

        return data;
    }
}
```

### 3. 添加限流控制

```java
@RestController
@RequestMapping("/api")
public class RateLimitedDataPluginController {

    private final RateLimiter rateLimiter = RateLimiter.create(100); // 100 requests per second

    @PostMapping("/data")
    public ResponseEntity<DataResponse> getData(@RequestBody Map<String, Object> params) {
        if (!rateLimiter.tryAcquire()) {
            return ResponseEntity.status(429).body(DataResponse.error("Too many requests"));
        }

        Map<String, Object> data = dataPluginService.getData(params);
        return ResponseEntity.ok(DataResponse.success(data));
    }
}
```

## 完整工作流示例

```java
public class TradingStrategy {

    public void run() {
        // 1. 初始化客户端
        DataPluginClient plugin = new DataPluginClient();
        ListenerClient listener = new ListenerClient();

        try {
            // 2. 通知任务开始
            listener.onTaskStarted();

            // 3. 获取市场数据
            Map<String, Object> data = plugin.getData(Map.of(
                "symbol", "BTC/USDT",
                "interval", "1h"
            ));

            // 4. 分析数据
            double close = (Double) data.get("close");
            double volume = (Double) data.get("volume");

            // 5. 执行交易逻辑
            Map<String, Object> result = executeTrade(close, volume);

            // 6. 通知任务完成
            listener.onTaskCompleted(result);

        } catch (Exception e) {
            // 7. 通知任务失败
            listener.onTaskFailed(e.getMessage());
        }
    }
}
```

## 常见问题

### Q: 如何修改返回的数据格式？

A: 重写 `DataPluginService.getData()` 方法：

```java
@Override
public Map<String, Object> getData(Map<String, Object> params) {
    Map<String, Object> customData = new HashMap<>();
    // 添加自定义字段
    customData.put("customField", "customValue");
    return customData;
}
```

### Q: 如何添加身份验证？

A: 添加安全依赖并配置：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/health").permitAll()
            .anyRequest().authenticated()
        );
        return http.build();
    }
}
```

### Q: 如何连接多个交易所？

A: 创建多个服务类：

```java
@Service
public class MultiExchangeDataService {

    public Map<String, Object> getData(String exchange, String symbol) {
        switch (exchange.toLowerCase()) {
            case "binance":
                return binanceService.getData(symbol);
            case "okx":
                return okxService.getData(symbol);
            default:
                throw new IllegalArgumentException("Unsupported exchange");
        }
    }
}
```

## 相关链接

- [完整文档](README.md)
- [TaskM Java SDK](https://github.com/taskm/taskm-sdk-java)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Swagger UI](http://localhost:8080/swagger-ui.html)

## 许可证

MIT License
