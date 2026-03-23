# PRD: 插件和监听器实例管理及 SDK

## Problem Statement

当前系统中，策略、数据插件和监听器的代码都注入到同一个容器中执行，存在以下问题：

1. **代码无法真正执行**：插件和监听器的代码只是作为环境变量传递，容器内部没有实际的执行逻辑
2. **资源共享冲突**：多个任务使用同一个插件时，配置会互相冲突（如不同的 API Key、不同的数据源）
3. **日志混乱**：多个任务共享同一个监听器时，日志难以按任务分离
4. **扩展性差**：无法针对不同的环境（开发/测试/生产）配置不同的插件实例
5. **重用性差**：每个任务都要重复配置相同的参数值

## Solution

引入**实例管理**机制，将插件和监听器独立部署为 HTTP 服务，并提供多语言 SDK 供策略代码调用。

### 核心概念

- **插件/监听器模板**：定义代码和参数结构（现有的 `data_plugin` 和 `listener` 表）
- **实例**：基于模板创建的命名配置，包含具体的参数值（新增 `data_plugin_instance` 和 `listener_instance` 表）
- **容器**：一个容器运行同一个模板的所有实例，通过 URL 路径区分不同实例
- **SDK**：提供 Java/Python/Go 三种语言的 SDK，封装 HTTP 调用

### 架构示意

```
┌─────────────────────────────────────────────────────────────┐
│                        TaskM Server                         │
│  ┌──────────────┐  ┌───────────────┐  ┌──────────────────┐ │
│  │   Strategy   │  │ Plugin Instance│  │Listener Instance│ │
│  │   Container  │─>│   HTTP API     │  │   HTTP API      │ │
│  └──────────────┘  └───────────────┘  └──────────────────┘ │
│         │                    │                    │          │
└─────────│────────────────────│────────────────────│──────────┘
          │                    │                    │
          ▼                    ▼                    ▼
    ┌─────────────┐    ┌──────────────────────┐  ┌──────────────────┐
    │   Strategy  │    │ Plugin Container     │  │ Listener Container│
    │   (with SDK)│    │ - Instance: prod     │  │ - Instance: prod  │
    │             │    │ - Instance: dev      │  │ - Instance: dev   │
    │             │    │ - /instances/{name}/ │  │ - /instances/{name}/│
    └─────────────┘    └──────────────────────┘  └──────────────────┘
```

### 日志存储策略

插件和监听器容器挂载宿主机卷：
- 挂载路径：`/var/log/tasks/`
- 日志文件：`/var/log/tasks/task_{id}.log`
- 每个任务独立的日志文件，通过 `taskId` 隔离

### 容器重启策略

- 容器配置自动重启策略（`--restart on-failure`）
- 实例崩溃后容器自动重启，不影响其他实例
- 任务需要处理网络错误，调用失败时记录错误日志

## User Stories

### 实例管理

1. 作为系统管理员，我希望创建一个数据插件实例，指定实例名称和具体配置参数（如 API Key、数据源 URL），以便在不同环境中使用不同的配置

2. 作为系统管理员，我希望设置某个插件实例为"默认实例"，以便用户在创建任务时如果不指定具体实例，系统自动使用默认实例

3. 作为系统管理员，我希望能够查看某个插件的所有实例及其状态（运行中/已停止），以便监控系统健康状况

4. 作为系统管理员，我希望能够更新实例的配置参数，以便在配置变更时无需删除重建实例

5. 作为系统管理员，我希望能够删除不再使用的插件实例，以便清理系统资源

6. 作为系统管理员，我希望能够启动和停止插件容器，以便在维护时暂停服务

7. 作为系统管理员，我希望系统能阻止删除正在被任务使用的实例，以便避免任务运行失败

8. 作为系统管理员，我希望插件容器崩溃时能够自动重启，以便保证服务可用性

9. 作为系统管理员，我希望为监听器创建多个实例（如 "email-alerts-prod" 和 "email-alerts-dev"），每个实例有不同的收件人列表和主题模板

10. 作为系统管理员，我希望为数据插件创建多个实例（如 "market-data-binance" 和 "market-data-okx"），每个实例连接不同的交易所

### 任务创建和执行

11. 作为任务创建者，我希望在创建任务时能够指定使用哪个插件实例，以便使用正确的配置参数

12. 作为任务创建者，我希望在创建任务时能够指定使用哪个监听器实例，以便事件通知发送到正确的目标

13. 作为任务创建者，如果我只指定了插件 ID 但没有指定实例 ID，我希望系统自动使用该插件的默认实例，如果不存在默认实例则报错提示

14. 作为任务创建者，我希望任务执行失败时能够得到明确的错误信息，包括是插件调用失败还是监听器通知失败

15. 作为任务创建者，我希望能够查看任务执行时的日志，包括策略日志、插件调用日志和监听器通知日志

### SDK 使用

16. 作为策略开发者（Java），我希望使用 Java SDK 调用数据插件，以便在策略代码中获取市场数据

17. 作为策略开发者（Python），我希望使用 Python SDK 调用数据插件，以便在策略代码中获取市场数据

18. 作为策略开发者（Go），我希望使用 Go SDK 调用数据插件，以便在策略代码中获取市场数据

19. 作为策略开发者，我希望 SDK 能够自动从环境变量读取插件和监听器的 HTTP 端点，以便在代码中无需硬编码 URL

20. 作为策略开发者，我希望在任务完成时能够通过 SDK 通知监听器，以便触发后续的回调逻辑

21. 作为策略开发者，我希望 SDK 的 API 设计简洁直观，如 `plugin.get_market_data(symbol)` 和 `listener.on_task_completed(result)`，以便降低学习成本

22. 作为策略开发者，我希望 SDK 能够抛出清晰的异常信息，包括 HTTP 状态码和错误详情，以便快速定位问题

23. 作为策略开发者，我希望 SDK 方法调用超时后能够抛出超时异常，以便避免任务永久阻塞

### 日志和监控

24. 作为运维人员，我希望插件和监听器的日志按任务 ID 分离存储到独立的文件中，便于排查问题时快速定位特定任务的日志

25. 作为运维人员，我希望能够通过 `docker logs` 查看容器级别的日志，通过文件系统查看任务级别的日志

26. 作为运维人员，我希望日志文件路径规范命名（如 `/var/log/tasks/task_123.log`），便于自动化脚本收集和分析

### 参数管理

27. 作为系统管理员，我希望实例创建后不允许任务修改实例参数，以保证配置的一致性和可预测性

28. 作为系统管理员，我希望实例的配置参数能够覆盖模板的默认值，以便为不同环境定制配置

29. 作为系统管理员，我希望在创建实例时如果参数缺失必需值，系统能够给出明确的错误提示

## Implementation Decisions

### 1. 数据模型设计

#### 1.1 数据插件实例表

```sql
CREATE TABLE data_plugin_instance (
    id BIGSERIAL PRIMARY KEY,
    plugin_id BIGINT NOT NULL REFERENCES data_plugin(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    config JSONB NOT NULL,          -- 实例配置值，覆盖模板默认值
    container_id VARCHAR(255),      -- 容器 ID（一个容器运行该插件的所有实例）
    status VARCHAR(50) DEFAULT 'STOPPED', -- RUNNING, STOPPED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(plugin_id, name),
    CONSTRAINT check_single_default CHECK (
        NOT is_default OR NOT EXISTS (
            SELECT 1 FROM data_plugin_instance d2
            WHERE d2.plugin_id = data_plugin_instance.plugin_id
            AND d2.is_default = TRUE
            AND d2.id != data_plugin_instance.id
        )
    )
);
```

#### 1.2 监听器实例表

```sql
CREATE TABLE listener_instance (
    id BIGSERIAL PRIMARY KEY,
    listener_id BIGINT NOT NULL REFERENCES listener(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    config JSONB NOT NULL,          -- 实例配置值
    container_id VARCHAR(255),
    status VARCHAR(50) DEFAULT 'STOPPED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(listener_id, name),
    CONSTRAINT check_single_default CHECK (
        NOT is_default OR NOT EXISTS (
            SELECT 1 FROM listener_instance l2
            WHERE l2.listener_id = listener_instance.listener_id
            AND l2.is_default = TRUE
            AND l2.id != listener_instance.id
        )
    )
);
```

#### 1.3 任务表修改

```sql
ALTER TABLE task ADD COLUMN plugin_instance_id BIGINT REFERENCES data_plugin_instance(id);
ALTER TABLE task ADD COLUMN listener_instance_id BIGINT REFERENCES listener_instance(id);
ALTER TABLE task ADD COLUMN plugin_endpoint VARCHAR(500);
ALTER TABLE task ADD COLUMN listener_endpoint VARCHAR(500);
```

### 2. 实例管理 API 设计

#### 2.1 数据插件实例 API

```
POST   /api/plugins/{pluginId}/instances
       - 创建实例（如果 name 已存在返回 409）

GET    /api/plugins/{pluginId}/instances
       - 获取某个插件的所有实例

GET    /api/plugins/{pluginId}/instances/{instanceId}
       - 获取实例详情

PUT    /api/plugins/{pluginId}/instances/{instanceId}
       - 更新实例配置

DELETE /api/plugins/{pluginId}/instances/{instanceId}
       - 删除实例（如果有任务在使用返回 409）

POST   /api/plugins/{pluginId}/instances/{instanceId}/setDefault
       - 设置为默认实例（取消其他默认标记）

POST   /api/plugins/{pluginId}/start
       - 启动插件容器（运行该插件的所有实例）

POST   /api/plugins/{pluginId}/stop
       - 停止插件容器

GET    /api/plugins/{pluginId}/status
       - 查询容器状态
```

#### 2.2 监听器实例 API（同上）

路径：`/api/listeners/{listenerId}/instances/*`

### 3. 容器管理策略

#### 3.1 一容器多实例架构

每个插件/监听器模板启动一个容器，该容器运行该模板的所有实例：

```
容器名称：plugin-{pluginId} 或 listener-{listenerId}
容器端口：8080（内部），映射到宿主机随机端口
API 路径：http://container:8080/instances/{instanceName}/api/...
```

#### 3.2 容器启动流程

1. 查询某个插件/监听器的所有实例
2. 将实例配置合并为 JSON，通过环境变量 `INSTANCES_CONFIG` 传递给容器
3. 容器启动后加载所有实例配置
4. 容器提供统一的 HTTP API，通过 URL 路径区分实例

#### 3.3 环境变量传递

```json
INSTANCES_CONFIG={
  "prod": {
    "apiKey": "prod-key-123",
    "dataSource": "https://api.prod.com"
  },
  "dev": {
    "apiKey": "dev-key-456",
    "dataSource": "https://api.dev.com"
  }
}
```

#### 3.4 容器重启策略

Docker 启动参数：`--restart on-failure:3`

- 容器非正常退出时自动重启
- 最多重启 3 次
- 3 次失败后不再重启

### 4. 任务创建流程修改

#### 4.1 实例解析逻辑

```java
public Task createTask(CreateTaskDTO dto) {
    // 1. 解析插件实例
    Long pluginInstanceId = dto.getPluginInstanceId();
    if (pluginInstanceId != null) {
        PluginInstance instance = pluginInstanceService.getById(pluginInstanceId);

        // 验证状态
        if (!"RUNNING".equals(instance.getStatus())) {
            throw new InstanceNotRunningException(
                "Plugin instance is not running: " + instance.getName()
            );
        }

        task.setPluginInstanceId(pluginInstanceId);
        task.setPluginEndpoint(buildInstanceEndpoint(instance));
    } else if (dto.getPluginId() != null) {
        // 查找默认实例
        PluginInstance defaultInstance = pluginInstanceService
            .getDefaultInstance(dto.getPluginId());

        if (defaultInstance == null) {
            throw new NoDefaultInstanceException(
                "No default instance found for plugin " + dto.getPluginId()
            );
        }

        task.setPluginInstanceId(defaultInstance.getId());
        task.setPluginEndpoint(buildInstanceEndpoint(defaultInstance));
    }

    // 2. 解析监听器实例（同样逻辑）
    // ...

    return task;
}
```

#### 4.2 实例端点 URL 格式

```
插件：http://plugin-container:{port}/instances/{instanceName}/api
监听器：http://listener-container:{port}/instances/{instanceName}/api
```

#### 4.3 容器环境变量注入

```java
private ContainerConfig buildContainerConfig(Task task) {
    ContainerConfig config = new ContainerConfig();
    List<String> env = new ArrayList<>();

    // 注入端点 URL
    if (task.getPluginEndpoint() != null) {
        env.add("PLUGIN_ENDPOINT=" + task.getPluginEndpoint());
    }
    if (task.getListenerEndpoint() != null) {
        env.add("LISTENER_ENDPOINT=" + task.getListenerEndpoint());
    }

    // 挂载日志目录
    config.setVolumeBinds(List.of(
        "/var/log/tasks:/var/log/tasks:rw"
    ));

    config.setEnv(env);
    return config;
}
```

### 5. 插件/监听器 HTTP API 规范

#### 5.1 数据插件 API

```java
// 通用数据获取接口
POST /instances/{instanceName}/api/get-data
Request:  { "symbol": "BTC/USDT", "interval": "1h" }
Response: { "status": "success", "data": [...] }

// 健康检查
GET /instances/{instanceName}/health
Response: { "status": "healthy" }
```

#### 5.2 监听器 API

```java
// 任务完成事件
POST /instances/{instanceName}/api/task-completed
Request: {
  "taskId": 123,
  "result": { "profit": 100.5 },
  "timestamp": "2024-01-01T10:00:00Z"
}

// 任务失败事件
POST /instances/{instanceName}/api/task-failed
Request: {
  "taskId": 123,
  "error": "Insufficient balance",
  "timestamp": "2024-01-01T10:00:00Z"
}

// 任务启动事件
POST /instances/{instanceName}/api/task-started
Request: {
  "taskId": 123,
  "timestamp": "2024-01-01T10:00:00Z"
}

// 健康检查
GET /instances/{instanceName}/health
Response: { "status": "healthy" }
```

### 6. SDK 设计

#### 6.1 Python SDK

**包名**：`taskm-sdk-py`

**核心类**：

```python
from taskm_sdk import DataPluginClient, ListenerClient

# 数据插件客户端
plugin = DataPluginClient(
    endpoint=os.getenv("PLUGIN_ENDPOINT"),
    timeout=30
)

data = plugin.get_data(symbol="BTC/USDT", interval="1h")

# 监听器客户端
listener = ListenerClient(
    endpoint=os.getenv("LISTENER_ENDPOINT"),
    task_id=int(os.getenv("TASK_ID"))
)

listener.on_task_started()
listener.on_task_completed(result)
listener.on_task_failed(error)
```

#### 6.2 Java SDK

**包名**：`com.taskm.sdk`

**核心类**：

```java
// 数据插件客户端
DataPluginClient plugin = new DataPluginClient(
    System.getenv("PLUGIN_ENDPOINT")
);

Map<String, Object> data = plugin.getData(
    Map.of("symbol", "BTC/USDT", "interval", "1h")
);

// 监听器客户端
ListenerClient listener = new ListenerClient(
    System.getenv("LISTENER_ENDPOINT"),
    Long.parseLong(System.getenv("TASK_ID"))
);

listener.onTaskStarted();
listener.onTaskCompleted(result);
listener.onTaskFailed(error);
```

#### 6.3 Go SDK

**包名**：`github.com/taskm/sdk-go`

**核心类**：

```go
// 数据插件客户端
plugin := sdk.NewDataPluginClient(
    os.Getenv("PLUGIN_ENDPOINT"),
)

data, err := plugin.GetData(map[string]interface{}{
    "symbol": "BTC/USDT",
    "interval": "1h",
})

// 监听器客户端
listener := sdk.NewListenerClient(
    os.Getenv("LISTENER_ENDPOINT"),
    taskId,
)

listener.OnTaskStarted()
listener.OnTaskCompleted(result)
listener.OnTaskFailed(error)
```

#### 6.4 SDK 依赖

- **Python**: `requests` (HTTP 客户端)
- **Java**: Spring `RestTemplate` 或 `OkHttp`
- **Go**: 标准库 `net/http`

#### 6.5 SDK 异常处理

- **网络错误**：抛出 `PluginConnectionException` / `ListenerConnectionException`
- **超时**：抛出 `PluginTimeoutException` / `ListenerTimeoutException`
- **HTTP 错误**：抛出 `PluginApiException` / `ListenerApiException`（包含状态码和错误详情）
- **解析错误**：抛出 `PluginResponseException` / `ListenerResponseException`

### 7. 日志管理

#### 7.1 插件/监听器日志写入

插件/监听器代码中：

```python
import os

task_id = os.getenv("TASK_ID")
log_file = f"/var/log/tasks/task_{task_id}.log"

with open(log_file, "a") as f:
    f.write(f"[{datetime.now()}] Fetching data for {symbol}\n")
    f.write(f"[{datetime.now()}] Data received: {data}\n")
```

#### 7.2 日志卷挂载

Docker 启动参数：

```bash
-v /var/log/taskm/tasks:/var/log/tasks:rw
```

#### 7.3 日志查询

策略容器、插件容器、监听器容器的日志都在同一个挂载卷下：

```
/var/log/taskm/tasks/
├── task_1.log   (策略容器写入)
├── task_1.log   (插件容器写入 - 同一个文件)
└── task_1.log   (监听器容器写入 - 同一个文件)
```

**注意**：多个容器写入同一个文件需要文件锁机制，或者按容器类型分离：

```
/var/log/taskm/tasks/
├── task_1/
│   ├── strategy.log
│   ├── plugin.log
│   └── listener.log
```

### 8. 参数验证

#### 8.1 实例创建验证

```java
public void validateInstanceConfig(Long templateId, Map<String, Object> config) {
    // 1. 获取模板参数定义
    Map<String, Object> templateParams = getTemplateParameters(templateId);

    // 2. 检查必需参数
    List<String> requiredParams = getRequiredParameters(templateParams);
    for (String param : requiredParams) {
        if (!config.containsKey(param)) {
            throw new MissingParameterException(
                "Required parameter missing: " + param
            );
        }
    }

    // 3. 验证参数类型
    validateParameterTypes(config, templateParams);
}
```

#### 8.2 参数合并策略

实例创建时的配置 = 模板默认值 + 实例覆盖值

```java
Map<String, Object> mergedConfig = new HashMap<>();
mergedConfig.putAll(template.getDefaultValues()); // 先加载默认值
mergedConfig.putAll(instanceConfig);              // 实例值覆盖
```

### 9. 任务删除时的级联处理

删除插件/监听器模板时：
- 如果有实例存在，级联删除所有实例
- 如果有正在运行的任务，拒绝删除（返回 409）

删除实例时：
- 如果有正在运行的任务，拒绝删除（返回 409）
- 如果容器正在运行，先停止容器再删除实例

## Testing Decisions

### 单元测试重点

1. **实例管理服务**
   - 测试创建实例时参数验证（必需参数、类型验证）
   - 测试设置默认实例时的互斥逻辑（只能有一个默认）
   - 测试删除使用中的实例时的保护逻辑
   - 测试实例配置与模板默认值的合并逻辑

2. **任务创建服务**
   - 测试指定实例 ID 时的正确加载
   - 测试不指定实例 ID 时使用默认实例
   - 测试没有默认实例时的错误处理
   - 测试实例未运行时的错误处理

3. **容器管理服务**
   - 测试容器启动和停止逻辑
   - 测试容器状态查询
   - 测试容器重启策略配置

### 集成测试重点

1. **端到端流程**
   - 创建插件模板 → 创建实例 → 启动容器 → 创建任务 → 验证 SDK 调用成功
   - 创建监听器模板 → 创建实例 → 启动容器 → 创建任务 → 验证回调触发

2. **容器通信**
   - 策略容器通过 SDK 成功调用插件 HTTP API
   - 策略容器通过 SDK 成功调用监听器 HTTP API
   - 验证请求超时时正确抛出异常

3. **日志验证**
   - 插件/监听器正确写入日志到挂载卷
   - 日志文件按任务 ID 正确分离
   - 日志内容包含完整的请求和响应信息

### 测试数据准备

1. **插件模板**
   - 市场数据插件（参数：symbol, interval, apiKey）
   - 新闻数据插件（参数：category, language, apiKey）

2. **监听器模板**
   - Email 通知监听器（参数：recipients, subject, smtpServer）
   - Webhook 监听器（参数：url, method, headers）

3. **测试实例**
   - Prod 实例（生产环境配置）
   - Dev 实例（开发环境配置）
   - 默认实例

## Out of Scope

以下功能**不在本次实现范围**内：

1. **实例版本管理**：不支持实例配置的版本控制和回滚
2. **实例权限控制**：不实现用户级别的实例访问权限管理
3. **实例依赖关系**：不支持实例之间的依赖配置
4. **实例热加载**：修改实例配置后需要重启容器才能生效
5. **负载均衡**：一个插件只启动一个容器，不支持多容器负载均衡
6. **SDK 高级功能**：不包括连接池、缓存、重试机制等高级功能
7. **性能监控**：不包括 SDK 调用的性能指标收集
8. **日志分析**：只提供日志存储，不提供日志查询和分析功能
9. **多租户隔离**：不实现多租户环境下的实例隔离
10. **实例迁移**：不支持实例在不同宿主机之间的迁移

## Further Notes

### 1. SDK 发布计划

- Python SDK: 发布到 PyPI (`taskm-sdk-py`)
- Java SDK: 发布到 Maven Central (`com.taskm:sdk`)
- Go SDK: 发布到 GitHub (`github.com/taskm/sdk-go`)

### 2. 插件/监听器开发指南

需要提供插件和监听器的开发文档，说明：

1. 如何实现符合规范的 HTTP API
2. 如何处理实例配置
3. 如何写入任务日志
4. 如何处理异常和错误

### 3. 容器镜像构建

提供插件和监听器的 Dockerfile 模板：

```dockerfile
FROM python:3.9-slim

WORKDIR /app
COPY plugin.py .
COPY requirements.txt .

RUN pip install -r requirements.txt

# 挂载日志目录
VOLUME /var/log/tasks

# 暴露 HTTP 端口
EXPOSE 8080

# 启动服务（加载实例配置）
CMD ["python", "plugin.py"]
```

### 4. 配置示例

#### 创建市场数据插件实例

```bash
curl -X POST http://localhost:8080/api/plugins/1/instances \
  -H "Content-Type: application/json" \
  -d '{
    "name": "binance-prod",
    "isDefault": true,
    "config": {
      "apiKey": "prod-key-123",
      "dataSource": "https://api.binance.com",
      "timeout": 30
    }
  }'
```

#### 创建 Email 监听器实例

```bash
curl -X POST http://localhost:8080/api/listeners/1/instances \
  -H "Content-Type: application/json" \
  -d '{
    "name": "email-alerts-prod",
    "isDefault": true,
    "config": {
      "recipients": ["admin@example.com"],
      "subject": "Task Notification",
      "smtpServer": "smtp.gmail.com",
      "smtpPort": 587
    }
  }'
```

### 5. 策略代码示例（使用 SDK）

```python
from taskm_sdk import DataPluginClient, ListenerClient
import os

def execute_strategy():
    # 初始化客户端
    plugin = DataPluginClient(os.getenv("PLUGIN_ENDPOINT"))
    listener = ListenerClient(
        os.getenv("LISTENER_ENDPOINT"),
        int(os.getenv("TASK_ID"))
    )

    try:
        # 通知任务启动
        listener.on_task_started()

        # 获取市场数据
        data = plugin.get_data(symbol="BTC/USDT", interval="1h")

        # 执行交易逻辑
        result = perform_trading(data)

        # 通知任务完成
        listener.on_task_completed(result)

    except Exception as e:
        # 通知任务失败
        listener.on_task_failed(str(e))
```

### 6. 未来扩展方向

1. **实例自动扩缩容**：根据负载自动增加或减少容器数量
2. **实例健康检查**：定期检查实例健康状态，异常时自动恢复
3. **实例监控面板**：提供 Web UI 展示实例状态和调用统计
4. **SDK 本地 Mock**：提供 SDK 的本地 Mock 模式，方便离线开发测试
5. **实例配置加密**：敏感参数（如 API Key）支持加密存储
