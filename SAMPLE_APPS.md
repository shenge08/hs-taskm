# TaskM 示例应用

本项目包含两个完整的示例应用，演示如何开发 TaskM 的监听器和数据插件。

## 📦 示例应用

### 1. 监听器示例 (`hs-taskm-listener-sample`)

一个接收策略任务生命周期事件的 Web 服务。

**主要功能：**
- 接收任务开始事件
- 接收任务完成事件（包含执行结果）
- 接收任务失败事件（包含错误信息）
- 自动日志记录
- 健康检查

**目录：** `hs-taskm-listener-sample/`

**文档：** [监听器 README](hs-taskm-listener-sample/README.md)

### 2. 数据插件示例 (`hs-taskm-plugin-sample`)

一个提供市场数据供策略调用的 Web 服务。

**主要功能：**
- 提供数据查询接口
- 返回 K线数据和技术指标
- 支持多条数据查询
- 市场信息接口

**目录：** `hs-taskm-plugin-sample/`

**文档：** [数据插件 README](hs-taskm-plugin-sample/README.md)

## 🚀 快速开始

### 构建所有示例

```bash
mvn clean package -DskipTests
```

### 运行监听器

```bash
cd hs-taskm-listener-sample
java -jar target/hs-taskm-listener-sample-1.0.0-SNAPSHOT.jar
```

监听器将在 `http://localhost:8080` 启动。

### 运行数据插件

```bash
cd hs-taskm-plugin-sample
java -jar target/hs-taskm-plugin-sample-1.0.0-SNAPSHOT.jar
```

数据插件将在 `http://localhost:8080` 启动。

**注意：** 两个应用默认使用相同的端口 8080，如果要同时运行，需要修改端口：

```bash
# 监听器使用 8080
SERVER_PORT=8080 java -jar hs-taskm-listener-sample-1.0.0-SNAPSHOT.jar

# 数据插件使用 8081
SERVER_PORT=8081 java -jar hs-taskm-plugin-sample-1.0.0-SNAPSHOT.jar
```

## 📡 API 端点对比

### 监听器端点

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/task-started` | POST | 接收任务开始事件 |
| `/api/task-completed` | POST | 接收任务完成事件 |
| `/api/task-failed` | POST | 接收任务失败事件 |
| `/api/health` | GET | 健康检查 |
| `/api/info` | GET | 服务信息 |

### 数据插件端点

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/data` | POST | 获取数据（通用接口） |
| `/api/data/multi` | POST | 获取多条数据 |
| `/api/market/info` | GET | 获取市场信息 |
| `/api/health` | GET | 健康检查 |
| `/api/info` | GET | 服务信息 |

## 💡 策略代码集成示例

这是一个完整的策略示例，同时使用监听器和数据插件：

```java
import com.taskm.sdk.DataPluginClient;
import com.taskm.sdk.ListenerClient;
import java.util.Map;

public class TradingStrategy {

    public void execute() {
        // 初始化客户端（从环境变量读取配置）
        DataPluginClient plugin = new DataPluginClient();
        ListenerClient listener = new ListenerClient();

        try {
            // 1. 通知任务开始
            listener.onTaskStarted();
            System.out.println("Task started");

            // 2. 获取市场数据
            Map<String, Object> data = plugin.getData(Map.of(
                "symbol", "BTC/USDT",
                "interval", "1h"
            ));
            System.out.println("Received data: " + data);

            // 3. 分析数据并执行交易
            double closePrice = (Double) data.get("close");
            double volume = (Double) data.get("volume");

            Map<String, Object> indicators = (Map<String, Object>) data.get("indicators");
            double rsi = (Double) indicators.get("rsi");

            // 4. 执行交易逻辑
            Map<String, Object> result = executeStrategy(closePrice, volume, rsi);

            // 5. 通知任务完成
            listener.onTaskCompleted(result);
            System.out.println("Task completed: " + result);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());

            // 6. 通知任务失败
            listener.onTaskFailed(e.getMessage());
        }
    }

    private Map<String, Object> executeStrategy(double price, double volume, double rsi) {
        // 这里实现您的交易逻辑
        return Map.of(
            "action", rsi < 30 ? "BUY" : "SELL",
            "price", price,
            "amount", volume * 0.1,
            "timestamp", System.currentTimeMillis()
        );
    }
}
```

### 环境变量配置

策略容器需要配置以下环境变量：

```bash
# 数据插件配置
export PLUGIN_ENDPOINT=http://plugin-host:8080/api
export PLUGIN_TIMEOUT=30

# 监听器配置
export LISTENER_ENDPOINT=http://listener-host:8080/api
export LISTENER_TIMEOUT=30

# 任务配置
export TASK_ID=123
```

## 🐳 Docker 部署

### 使用 Docker Compose 部署两个服务

创建 `docker-compose.yml` 文件：

```yaml
version: '3.8'

services:
  # 数据插件
  plugin:
    build: ./hs-taskm-plugin-sample
    container_name: taskm-plugin
    ports:
      - "8080:8080"
    environment:
      - SERVER_PORT=8080
      - LOG_LEVEL=INFO
    restart: unless-stopped
    networks:
      - taskm-network

  # 监听器
  listener:
    build: ./hs-taskm-listener-sample
    container_name: taskm-listener
    ports:
      - "8081:8080"
    environment:
      - SERVER_PORT=8080
      - LOG_LEVEL=INFO
    restart: unless-stopped
    networks:
      - taskm-network

  # 策略容器
  strategy:
    image: your-strategy-image:latest
    container_name: taskm-strategy
    environment:
      - PLUGIN_ENDPOINT=http://plugin:8080/api
      - LISTENER_ENDPOINT=http://listener:8080/api
      - TASK_ID=1
    depends_on:
      - plugin
      - listener
    networks:
      - taskm-network

networks:
  taskm-network:
    driver: bridge
```

启动服务：

```bash
docker-compose up -d
```

## 🏗️ 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        策略容器                               │
│                                                              │
│  ┌──────────────────┐         ┌──────────────────┐          │
│  │ DataPluginClient │         │ ListenerClient   │          │
│  └────────┬─────────┘         └────────┬─────────┘          │
│           │                             │                     │
│           │ HTTP                        │ HTTP               │
└───────────┼─────────────────────────────┼─────────────────────┘
            │                             │
            ▼                             ▼
┌───────────────────────┐    ┌───────────────────────┐
│   数据插件容器          │    │   监听器容器           │
│  (Plugin Sample)       │    │  (Listener Sample)     │
│                       │    │                       │
│  POST /api/data       │    │ POST /task-started    │
│  POST /api/data/multi │    │ POST /task-completed  │
│  GET  /market/info    │    │ POST /task-failed     │
│                       │    │                       │
│  - 返回市场数据        │    │  - 记录任务事件        │
│  - K线数据            │    │  - 发送通知           │
│  - 技术指标            │    │  - 更新状态           │
└───────────────────────┘    └───────────────────────┘
```

## 📚 完整文档

### 监听器文档

- [README.md](hs-taskm-listener-sample/README.md) - 完整文档
- [QUICKSTART.md](hs-taskm-listener-sample/QUICKSTART.md) - 快速开始

### 数据插件文档

- [README.md](hs-taskm-plugin-sample/README.md) - 完整文档
- [QUICKSTART.md](hs-taskm-plugin-sample/QUICKSTART.md) - 快速开始

## 🧪 测试

### 测试监听器

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
    "result": {"profit": 100.5}
  }'
```

### 测试数据插件

```bash
# 测试获取数据
curl -X POST http://localhost:8080/api/data \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "BTC/USDT",
    "interval": "1h"
  }'

# 测试获取多条数据
curl -X POST http://localhost:8080/api/data/multi \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "BTC/USDT",
    "interval": "1h",
    "limit": 5
  }'
```

## 🔧 自定义和扩展

### 扩展监听器

继承 `ListenerService` 添加自定义逻辑：

```java
@Service
public class CustomListenerService extends ListenerService {

    @Override
    public void handleTaskCompleted(TaskCompletedRequest request) {
        super.handleTaskCompleted(request);

        // 添加自定义逻辑
        sendNotification(request.getResult());
        saveToDatabase(request.getTaskId(), request.getResult());
    }
}
```

### 扩展数据插件

继承 `DataPluginService` 添加自定义逻辑：

```java
@Service
public class CustomDataPluginService extends DataPluginService {

    private final ExchangeApiClient exchangeClient;

    @Override
    public Map<String, Object> getData(Map<String, Object> params) {
        // 调用真实交易所 API
        return exchangeClient.fetchData(params);
    }
}
```

## 📝 相关资源

- [TaskM Java SDK](https://github.com/taskm/taskm-sdk-java)
- [主项目文档](README.md)
- [API 文档](http://localhost:8080/swagger-ui.html)

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**TaskM Team** - https://github.com/taskm
