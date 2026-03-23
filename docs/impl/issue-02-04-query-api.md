# Issue #2-#4: 查询 API 实现文档

## 概述

Issue #2、#3、#4 实现了策略、数据插件和监听器的查询 REST API。这三个 Issue 的实现模式相同，提供了基本的 CRUD 操作。

## 主要实现类

### 1. Mapper 层 (DAO)

#### StrategyMapper
**路径**: `hs-taskm-dao/src/main/java/com/taskm/mapper/StrategyMapper.java`

```java
@Mapper
public interface StrategyMapper extends BaseMapper<Strategy> {
    // 继承 BaseMapper，自动获得 CRUD 方法
    // - insert(Strategy)
    // - deleteById(Serializable)
    // - updateById(Strategy)
    // - selectById(Serializable)
    // - selectList(Wrapper)
    // - selectPage(Page, Wrapper)
}
```

#### DataPluginMapper
**路径**: `hs-taskm-dao/src/main/java/com/taskm/mapper/DataPluginMapper.java`

```java
@Mapper
public interface DataPluginMapper extends BaseMapper<DataPlugin> {
    // 同上
}
```

#### ListenerMapper
**路径**: `hs-taskm-dao/src/main/java/com/taskm/mapper/ListenerMapper.java`

```java
@Mapper
public interface ListenerMapper extends BaseMapper<Listener> {
    // 同上
}
```

### 2. Service 层

#### StrategyService
**路径**: `hs-taskm-service/src/main/java/com/taskm/service/StrategyService.java`

```java
public interface StrategyService extends IService<Strategy> {
    // 继承 IService，获得丰富的方法
    // - getStrategy(Long id)
    // - getAllStrategies()
    // - createStrategy(Strategy)
    // - updateStrategy(Strategy)
    // - deleteStrategy(Long id)
}
```

**实现**: `StrategyServiceImpl`
```java
@Service
public class StrategyServiceImpl extends ServiceImpl<StrategyMapper, Strategy>
        implements StrategyService {
    // 大部分方法已由 ServiceImpl 实现
}
```

#### DataPluginService & ListenerService
结构相同，不再赘述。

### 3. Controller 层

#### StrategyController
**路径**: `hs-taskm-api/src/main/java/com/taskm/controller/StrategyController.java`

```java
@RestController
@RequestMapping("/api/strategies")
public class StrategyController {

    @Autowired
    private StrategyService strategyService;

    @GetMapping
    public Result<List<Strategy>> getAllStrategies() {
        List<Strategy> strategies = strategyService.getAllStrategies();
        return Result.success(strategies);
    }

    @GetMapping("/{id}")
    public Result<Strategy> getStrategy(@PathVariable Long id) {
        Strategy strategy = strategyService.getStrategy(id);
        return Result.success(strategy);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Strategy> createStrategy(@RequestBody Strategy strategy) {
        strategyService.createStrategy(strategy);
        return Result.success("Strategy created successfully", strategy);
    }

    @PutMapping("/{id}")
    public Result<Strategy> updateStrategy(@PathVariable Long id,
                                           @RequestBody Strategy strategy) {
        strategy.setId(id);
        strategyService.updateById(strategy);
        return Result.success("Strategy updated successfully", strategy);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteStrategy(@PathVariable Long id) {
        strategyService.removeById(id);
        return Result.success("Strategy deleted successfully");
    }
}
```

#### DataPluginController & ListenerController
结构相同，路径分别为 `/api/plugins` 和 `/api/listeners`。

## REST API 端点

### 策略 API (Issue #2)

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/strategies | 获取所有策略 |
| GET | /api/strategies/{id} | 获取策略详情 |
| POST | /api/strategies | 创建策略 |
| PUT | /api/strategies/{id} | 更新策略 |
| DELETE | /api/strategies/{id} | 删除策略 |

### 插件 API (Issue #3)

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/plugins | 获取所有插件 |
| GET | /api/plugins/{id} | 获取插件详情 |
| POST | /api/plugins | 创建插件 |
| PUT | /api/plugins/{id} | 更新插件 |
| DELETE | /api/plugins/{id} | 删除插件 |

### 监听器 API (Issue #4)

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/listeners | 获取所有监听器 |
| GET | /api/listeners/{id} | 获取监听器详情 |
| POST | /api/listeners | 创建监听器 |
| PUT | /api/listeners/{id} | 更新监听器 |
| DELETE | /api/listeners/{id} | 删除监听器 |

## 数据结构

### Strategy 实体
```java
@Data
@TableName("strategy")
public class Strategy {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String language;  // java, python, go, javascript
    private String code;      // Base64 编码的代码
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### DataPlugin 实体
```java
@Data
@TableName("data_plugin")
public class DataPlugin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String pluginType;  // market_data, order_execution, etc.
    private String language;
    private String code;
    private Map<String, Object> configParameters;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Listener 实体
```java
@Data
@TableName("listener")
public class Listener {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String eventType;  // TASK_COMPLETED, TASK_FAILED, etc.
    private String language;
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## 数据库表

### strategy 表
```sql
CREATE TABLE strategy (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    language VARCHAR(50) NOT NULL,
    code TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_strategy_language ON strategy(language);
```

### data_plugin 表
```sql
CREATE TABLE data_plugin (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    plugin_type VARCHAR(100) NOT NULL,
    language VARCHAR(50) NOT NULL,
    code TEXT NOT NULL,
    config_parameters JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_plugin_type ON data_plugin(plugin_type);
CREATE INDEX idx_plugin_language ON data_plugin(language);
```

### listener 表
```sql
CREATE TABLE listener (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    event_type VARCHAR(100) NOT NULL,
    language VARCHAR(50) NOT NULL,
    code TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_listener_event ON listener(event_type);
CREATE INDEX idx_listener_language ON listener(language);
```

## 使用示例

### 查询所有策略
```bash
curl http://localhost:8080/api/strategies
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "python-strategy",
      "description": "Python trading strategy",
      "language": "python",
      "code": "def strategy(): pass",
      "createdAt": "2024-01-01T10:00:00"
    }
  ]
}
```

### 创建策略
```bash
curl -X POST http://localhost:8080/api/strategies \
  -H "Content-Type: application/json" \
  -d '{
    "name": "java-strategy",
    "description": "Java trading strategy",
    "language": "java",
    "code": "public class Strategy { public void execute() {} }"
  }'
```

## 测试用例

### StrategyControllerTest
```java
@SpringBootTest
@AutoConfigureMockMvc
class StrategyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void canGetAllStrategies() throws Exception {
        mockMvc.perform(get("/api/strategies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void canGetStrategyById() throws Exception {
        mockMvc.perform(get("/api/strategies/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1));
    }
}
```

## 相关 Issue

- **使用方**: Issue #5 (任务创建和持久化) - 使用策略 API 查询可用策略
- **使用方**: Issue #7 (Code Snippet Injector) - 使用策略/插件/监听器 API 加载配置
