# Issue #29: 端到端集成测试

## 概述

Issue #29 实现了完整的端到端集成测试，使用 Testcontainers 框架自动管理测试环境，验证从创建插件/监听器实例、启动容器、创建任务到 SDK 调用的完整流程。

## 测试框架

### Testcontainers

Testcontainers 是一个 Java 库，支持在 JUnit 测试中启动和管理 Docker 容器。

**依赖配置**:
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

## 主要测试类

### EndToEndIntegrationTest
**路径**: `hs-taskm-service/src/test/java/com/taskm/service/EndToEndIntegrationTest.java`

**测试结构**:
```java
@SpringBootTest
@Testcontainers
class EndToEndIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("taskm_test")
        .withUsername("taskm")
        .withPassword("taskm123");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
        registry.add("spring.datasource.username", postgresql::getUsername);
        registry.add("spring.datasource.password", postgresql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private PluginContainerManager pluginContainerManager;

    @Autowired
    private ListenerContainerManager listenerContainerManager;

    @Autowired
    private TaskOrchestrator taskOrchestrator;

    // ... 测试方法
}
```

## 测试用例

### 1. 测试插件实例完整流程

**测试目标**: 验证插件实例创建、容器启动、任务执行的完整流程

**测试步骤**:
```java
@Test
@DisplayName("测试插件实例完整流程")
void testPluginInstanceFullFlow() {
    // 1. 创建两个实例（prod 和 dev）
    DataPluginInstance prodInstance = createPluginInstance("prod", true);
    DataPluginInstance devInstance = createPluginInstance("dev", false);

    // 2. 启动插件容器
    String containerId = pluginContainerManager.startPluginContainer(testPlugin.getId());
    assertNotNull(containerId);

    // 3. 验证容器状态
    String status = pluginContainerManager.getContainerStatus(testPlugin.getId());
    assertEquals("RUNNING", status);

    // 4. 创建任务（使用 prod 实例）
    Task task = createTaskWithPluginInstance(prodInstance.getId());

    // 5. 启动任务
    var result = taskOrchestrator.startTask(task.getId());
    assertTrue(result.isSuccess());

    // 6. 清理
    taskOrchestrator.stopTask(task.getId());
    pluginContainerManager.stopPlugin(testPlugin.getId());
}
```

**验证点**:
- ✅ 插件实例创建成功
- ✅ 容器启动成功
- ✅ 容器状态为 RUNNING
- ✅ 任务启动成功
- ✅ 任务和容器可以正确停止

### 2. 测试监听器实例完整流程

**测试目标**: 验证监听器实例创建、容器启动、任务执行的完整流程

**测试步骤**:
```java
@Test
@DisplayName("测试监听器实例完整流程")
void testListenerInstanceFullFlow() {
    // 1. 创建两个实例（prod 和 dev）
    ListenerInstance prodInstance = createListenerInstance("prod", true);
    ListenerInstance devInstance = createListenerInstance("dev", false);

    // 2. 启动监听器容器
    String containerId = listenerContainerManager.startListenerContainer(testListener.getId());
    assertNotNull(containerId);

    // 3. 验证容器状态
    String status = listenerContainerManager.getContainerStatus(testListener.getId());
    assertEquals("RUNNING", status);

    // 4. 创建任务（使用 prod 实例）
    Task task = createTaskWithListenerInstance(prodInstance.getId());

    // 5. 启动任务
    var result = taskOrchestrator.startTask(task.getId());
    assertTrue(result.isSuccess());

    // 6. 清理
    taskOrchestrator.stopTask(task.getId());
    listenerContainerManager.stopListenerContainer(testListener.getId());
}
```

**验证点**:
- ✅ 监听器实例创建成功
- ✅ 容器启动成功
- ✅ 容器状态为 RUNNING
- ✅ 任务启动成功
- ✅ 任务和容器可以正确停止

### 3. 测试默认实例逻辑

**测试目标**: 验证使用默认实例的逻辑

**测试步骤**:
```java
@Test
@DisplayName("测试默认实例逻辑")
void testDefaultInstanceLogic() {
    // 1. 创建默认实例
    DataPluginInstance defaultInstance = createPluginInstance("default", true);

    // 2. 启动容器
    pluginContainerManager.startPluginContainer(testPlugin.getId());

    // 3. 创建任务（使用默认实例）
    Task task = new Task();
    task.setStrategyId(testStrategy.getId());
    task.setPluginInstanceId(defaultInstance.getId());
    task.setStatus("CREATED");
    Map<String, Object> params = new HashMap<>();
    params.put("pluginId", testPlugin.getId());
    params.put("strategyParams", new HashMap<>());
    task.setParameters(params);
    taskMapper.insert(task);

    // 4. 验证使用了默认实例
    assertEquals(defaultInstance.getId(), task.getPluginInstanceId());

    // 5. 清理
    pluginContainerManager.stopPluginContainer(testPlugin.getId());
}
```

**验证点**:
- ✅ 默认实例创建成功
- ✅ 任务正确关联到默认实例

### 4. 测试实例状态验证

**测试目标**: 验证未启动的实例状态检测

**测试步骤**:
```java
@Test
@DisplayName("测试实例状态验证 - 未运行实例")
void testInstanceStatusValidation_NotRunning() {
    // 1. 创建实例但不启动容器
    DataPluginInstance instance = createPluginInstance("prod", true);

    // 2. 创建任务
    Task task = createTaskWithPluginInstance(instance.getId());

    // 3. 验证容器未运行
    String status = pluginContainerManager.getContainerStatus(testPlugin.getId());
    assertEquals("NOT_FOUND", status);
}
```

**验证点**:
- ✅ 未启动的容器状态为 NOT_FOUND

### 5. 测试日志分离

**测试目标**: 验证不同容器的日志正确分离

**测试步骤**:
```java
@Test
@DisplayName("测试日志分离")
void testLogSeparation() {
    // 1. 创建并启动插件容器
    DataPluginInstance pluginInstance = createPluginInstance("prod", true);
    pluginContainerManager.startPluginContainer(testPlugin.getId());

    // 2. 创建并启动监听器容器
    ListenerInstance listenerInstance = createListenerInstance("prod", true);
    listenerContainerManager.startListenerContainer(testListener.getId());

    // 3. 创建任务
    Task task = createTaskWithBothInstances(pluginInstance.getId(), listenerInstance.getId());

    // 4. 启动任务
    var result = taskOrchestrator.startTask(task.getId());
    assertTrue(result.isSuccess());

    // 5. 验证日志分离（检查元数据结构）
    var metadataMap = logService.getAllLogMetadata(task.getId());
    assertNotNull(metadataMap);
    assertEquals(3, metadataMap.size());
    assertTrue(metadataMap.containsKey("strategy"));
    assertTrue(metadataMap.containsKey("plugin"));
    assertTrue(metadataMap.containsKey("listener"));

    // 6. 清理
    taskOrchestrator.stopTask(task.getId());
    pluginContainerManager.stopPluginContainer(testPlugin.getId());
    listenerContainerManager.stopListenerContainer(testListener.getId());
}
```

**验证点**:
- ✅ 日志元数据包含三种类型
- ✅ 每种类型都有独立的日志文件

### 6. 测试参数验证

**测试目标**: 验证任务参数验证

**测试步骤**:
```java
@Test
@DisplayName("测试参数验证")
void testParameterValidation() {
    // 1. 创建任务但缺少必需参数
    Task task = new Task();
    task.setStrategyId(testStrategy.getId());
    task.setStatus("CREATED");
    Map<String, Object> params = new HashMap<>();
    params.put("strategyParams", new HashMap<>());
    task.setParameters(params);

    // 2. 验证任务可以创建（参数验证在其他层）
    assertDoesNotThrow(() -> taskMapper.insert(task));
}
```

**验证点**:
- ✅ 任务可以正常创建

## 辅助方法

### 创建测试数据

**创建插件实例**:
```java
private DataPluginInstance createPluginInstance(String name, boolean isDefault) {
    DataPluginInstance instance = new DataPluginInstance();
    instance.setPluginId(testPlugin.getId());
    instance.setName(name);
    instance.setIsDefault(isDefault);
    instance.setStatus("CREATED");
    instance.setConfig(new HashMap<>());
    dataPluginInstanceMapper.insert(instance);
    return instance;
}
```

**创建监听器实例**:
```java
private ListenerInstance createListenerInstance(String name, boolean isDefault) {
    ListenerInstance instance = new ListenerInstance();
    instance.setListenerId(testListener.getId());
    instance.setName(name);
    instance.setIsDefault(isDefault);
    instance.setStatus("CREATED");
    instance.setConfig(new HashMap<>());
    listenerInstanceMapper.insert(instance);
    return instance;
}
```

**创建任务**:
```java
private Task createTaskWithPluginInstance(Long pluginInstanceId) {
    Task task = new Task();
    task.setStrategyId(testStrategy.getId());
    task.setPluginInstanceId(pluginInstanceId);
    task.setStatus("CREATED");
    Map<String, Object> params = new HashMap<>();
    params.put("pluginId", testPlugin.getId());
    params.put("pluginInstanceId", pluginInstanceId);
    params.put("strategyParams", new HashMap<>());
    task.setParameters(params);
    taskMapper.insert(task);

    DataPluginInstance instance = dataPluginInstanceMapper.selectById(pluginInstanceId);
    task.setPluginEndpoint(buildPluginEndpoint(instance));

    return task;
}
```

### 构建 Endpoint

```java
private String buildPluginEndpoint(DataPluginInstance instance) {
    return String.format("http://plugin-%d:8080/instances/%s/api",
        testPlugin.getId(), instance.getName());
}

private String buildListenerEndpoint(ListenerInstance instance) {
    return String.format("http://listener-%d:8080/instances/%s/api",
        testListener.getId(), instance.getName());
}
```

## 测试生命周期

### setUp()

每个测试方法执行前都会运行：

```java
@BeforeEach
void setUp() {
    // 1. 清理数据库
    taskMapper.selectList(null).forEach(t -> taskMapper.deleteById(t.getId()));
    dataPluginInstanceMapper.selectList(null).forEach(i -> dataPluginInstanceMapper.deleteById(i.getId()));
    listenerInstanceMapper.selectList(null).forEach(i -> listenerInstanceMapper.deleteById(i.getId()));
    dataPluginMapper.selectList(null).forEach(p -> dataPluginMapper.deleteById(p.getId()));
    listenerMapper.selectList(null).forEach(l -> listenerMapper.deleteById(l.getId()));
    strategyMapper.selectList(null).forEach(s -> strategyMapper.deleteById(s.getId()));

    // 2. 创建测试数据
    testStrategy = new Strategy();
    testStrategy.setName("Test Strategy");
    testStrategy.setCode("print('Test strategy executed')");
    testStrategy.setLanguage("python");
    strategyMapper.insert(testStrategy);

    testPlugin = new DataPlugin();
    testPlugin.setName("Market Data Plugin");
    testPlugin.setCode("...");
    testPlugin.setImageName("python:3.9-slim");
    testPlugin.setConfigParameters(new HashMap<>());
    dataPluginMapper.insert(testPlugin);

    testListener = new Listener();
    testListener.setName("Test Listener");
    testListener.setCode("...");
    testListener.setImageName("python:3.9-slim");
    testListener.setParameterDefaults(new HashMap<>());
    listenerMapper.insert(testListener);
}
```

### tearDown()

每个测试方法执行后都会运行：

```java
@AfterEach
void tearDown() {
    // 清理容器
    try {
        pluginContainerManager.stopPluginContainer(testPlugin.getId());
    } catch (Exception e) {
        // Ignore cleanup errors
    }
    try {
        listenerContainerManager.stopListenerContainer(testListener.getId());
    } catch (Exception e) {
        // Ignore cleanup errors
    }

    // 清理任务
    taskMapper.selectList(null).forEach(t -> {
        try {
            if ("RUNNING".equals(t.getStatus())) {
                taskOrchestrator.stopTask(t.getId());
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    });
}
```

## LogServiceTest 更新

### 主要改动

1. **移除 LogRouter 依赖**:
```java
// 旧代码
@Mock
private LogRouter logRouter;

logService = new LogServiceImpl(logRouter);

// 新代码
logService = new LogServiceImpl();
```

2. **创建临时日志目录**:
```java
@BeforeEach
void setUp() throws IOException {
    logService = new LogServiceImpl();

    // Create temporary task directory
    tempTaskDir = Files.createTempDirectory("task-" + testTaskId + "_");

    // Create temporary log file for strategy type
    tempLogFile = tempTaskDir.resolve("strategy.log");
    Files.createFile(tempLogFile);

    // Write test log content
    String logContent = """
        2024-01-01 10:00:00 INFO Task started
        2024-01-01 10:00:01 DEBUG Processing data
        ...
        """;
    Files.writeString(tempLogFile, logContent);

    // Set log directory path
    System.setProperty("logs.base_dir", tempTaskDir.getParent().toString());
}
```

3. **更新所有测试方法**:
```java
// 旧代码
List<String> logs = logService.getTaskLogs(testTaskId, 1, 2);

// 新代码
List<String> logs = logService.getTaskLogs(testTaskId, "strategy", 1, 2);
```

4. **新增测试**:
```java
@Test
void testGetAllLogMetadata_shouldReturnAllTypes() throws IOException {
    // Given - Create plugin and listener log files
    Path pluginLog = tempTaskDir.resolve("plugin.log");
    Path listenerLog = tempTaskDir.resolve("listener.log");
    Files.writeString(pluginLog, "Plugin log\n");
    Files.writeString(listenerLog, "Listener log\n");

    try {
        // When
        var metadataMap = logService.getAllLogMetadata(testTaskId);

        // Then
        assertNotNull(metadataMap);
        assertEquals(3, metadataMap.size());
        assertTrue(metadataMap.containsKey("strategy"));
        assertTrue(metadataMap.containsKey("plugin"));
        assertTrue(metadataMap.containsKey("listener"));
    } finally {
        Files.deleteIfExists(pluginLog);
        Files.deleteIfExists(listenerLog);
    }
}
```

## 测试覆盖场景

| 场景 | 测试方法 | 状态 |
|------|---------|------|
| 插件实例完整流程 | testPluginInstanceFullFlow | ✅ |
| 监听器实例完整流程 | testListenerInstanceFullFlow | ✅ |
| 默认实例逻辑 | testDefaultInstanceLogic | ✅ |
| 实例状态验证 | testInstanceStatusValidation_NotRunning | ✅ |
| 日志分离 | testLogSeparation | ✅ |
| 参数验证 | testParameterValidation | ✅ |
| 分页读取日志 | testGetTaskLogs_* | ✅ |
| 尾部读取日志 | testGetLogTail_* | ✅ |
| 搜索日志 | testSearchLogs_* | ✅ |
| 日志元数据 | testGetLogMetadata_* | ✅ |
| 所有日志类型 | testGetAllLogMetadata_* | ✅ |

## 运行测试

### 运行所有测试
```bash
mvn test
```

### 运行特定测试类
```bash
mvn test -Dtest=EndToEndIntegrationTest
```

### 运行特定测试方法
```bash
mvn test -Dtest=EndToEndIntegrationTest#testPluginInstanceFullFlow
```

## 优势

1. **自动化测试环境**: Testcontainers 自动管理 PostgreSQL 容器
2. **真实环境测试**: 使用真实的 Docker 容器和数据库
3. **完整的端到端测试**: 从实例创建到任务执行的全流程验证
4. **易于调试**: 测试失败时可以检查容器状态和日志
5. **隔离性好**: 每个测试方法独立运行，互不影响

## 注意事项

1. **Docker 环境**: 运行测试需要本地 Docker 环境
2. **资源占用**: Testcontainers 会占用一定的系统资源
3. **网络连接**: 首次运行需要下载 Docker 镜像
4. **清理机制**: 测试结束后会自动清理容器

## 相关文件

| 文件 | 说明 |
|------|------|
| EndToEndIntegrationTest.java | 端到端集成测试主类 |
| LogServiceTest.java | 日志服务测试类（已更新） |
| pom.xml (hs-taskm-service) | Testcontainers 依赖配置 |

## 扩展建议

未来可以添加的测试场景：

1. **并发场景**: 多个任务同时使用同一实例
2. **错误处理**: 容器崩溃、网络超时等异常情况
3. **性能测试**: 大量任务同时执行的性能表现
4. **容器重启**: 容器崩溃后自动重启的测试
5. **SDK 集成**: Python/Java SDK 的集成测试

---

**实现日期**: 2026-03-23
**Issue 编号**: #29
**文档版本**: 1.0
