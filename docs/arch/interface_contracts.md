# HS-TASKM 类/接口契约文档

## 1. 核心领域模型

### 1.1 策略管理领域

```java
/**
 * 策略实体类
 */
@Entity
@Table(name = "strategy")
public class Strategy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String language; // Python, Java, Go
    
    @Column(nullable = false)
    private String version;
    
    @Column(nullable = false)
    private String dockerImageId; // Docker镜像包ID
    
    @Column(nullable = false)
    private Boolean enabled;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Getters and Setters
}

/**
 * 策略管理接口
 */
public interface StrategyService {
    Strategy registerStrategy(StrategyDTO strategyDTO);
    Strategy updateStrategy(Long id, StrategyDTO strategyDTO);
    void deleteStrategy(Long id);
    Strategy getStrategy(Long id);
    List<Strategy> getAllStrategies();
    List<Strategy> getStrategiesByLanguage(String language);
    Strategy rollbackVersion(Long id, String targetVersion);
}

/**
 * Docker镜像管理接口
 */
public interface DockerImageService {
    String pullImage(String imageName, String tag);
    void removeImage(String imageId);
    DockerImageInfo getImageInfo(String imageId);
    List<DockerImageInfo> listImages();
    String buildImage(String dockerfilePath, String buildContext, String imageName);
}

/**
 * Docker镜像信息
 */
public class DockerImageInfo {
    private String id;
    private String repository;
    private String tag;
    private Long size;
    private LocalDateTime created;
    private Map<String, String> labels;
    
    // Getters and Setters
}
```

### 1.2 数据插件管理领域

```java
/**
 * 数据插件实体类
 */
@Entity
@Table(name = "data_plugin")
public class DataPlugin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String language; // Python, Java, Go
    
    @Column(nullable = false)
    private String code; // 插件代码片段
    
    @Column(nullable = false)
    private Boolean enabled;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Getters and Setters
}

/**
 * 数据插件管理接口
 */
public interface DataPluginService {
    DataPlugin registerPlugin(DataPluginDTO pluginDTO);
    DataPlugin updatePlugin(Long id, DataPluginDTO pluginDTO);
    void deletePlugin(Long id);
    DataPlugin getPlugin(Long id);
    List<DataPlugin> getPluginsByLanguage(String language);
    Map<String, Object> executePlugin(Long pluginId, Map<String, Object> params);
    boolean testPlugin(Long pluginId);
}

/**
 * 数据插件执行接口
 */
public interface DataPluginExecutor {
    Map<String, Object> execute(String code, Map<String, Object> params) throws PluginExecutionException;
    boolean validate(String code);
}
```

### 1.3 监听器管理领域

```java
/**
 * 监听器实体类
 */
@Entity
@Table(name = "listener")
public class Listener {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String language; // Python, Java, Go
    
    @Column(nullable = false)
    private String code; // 监听器代码片段
    
    @Column(nullable = false)
    private String notificationType; // EMAIL, WEBHOOK, MQ
    
    @Column(length = 1000)
    private String config; // 监听器配置JSON
    
    @Column(nullable = false)
    private Boolean enabled;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Getters and Setters
}

/**
 * 监听器管理接口
 */
public interface ListenerService {
    Listener registerListener(ListenerDTO listenerDTO);
    Listener updateListener(Long id, ListenerDTO listenerDTO);
    void deleteListener(Long id);
    Listener getListener(Long id);
    List<Listener> getListenersByLanguage(String language);
    void triggerListener(Long listenerId, Map<String, Object> signal);
}

/**
 * 监听器执行接口
 */
public interface ListenerExecutor {
    void execute(String code, Map<String, Object> signal, String config) throws ListenerExecutionException;
    boolean validate(String code);
}
```

### 1.4 任务管理领域

```java
/**
 * 任务实体类
 */
@Entity
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne
    @JoinColumn(name = "strategy_id", nullable = false)
    private Strategy strategy;
    
    @ManyToOne
    @JoinColumn(name = "plugin_id")
    private DataPlugin plugin;
    
    @ManyToOne
    @JoinColumn(name = "listener_id")
    private Listener listener;
    
    @Column(nullable = false)
    private String parameters; // 参数JSON
    
    @Column(nullable = false)
    private TaskStatus status; // CREATED, RUNNING, COMPLETED, FAILED
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime startedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column
    private String containerId;
    
    @Column(length = 2000)
    private String errorMessage;
    
    // Getters and Setters
}

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    CREATED, RUNNING, COMPLETED, FAILED, CANCELLED
}

/**
 * 任务管理接口
 */
public interface TaskService {
    Task createTask(TaskDTO taskDTO);
    Task startTask(Long taskId);
    Task stopTask(Long taskId);
    Task deleteTask(Long taskId);
    Task getTask(Long id);
    List<Task> getAllTasks();
    List<Task> getTasksByStatus(TaskStatus status);
    TaskStatus getTaskStatus(Long taskId);
}

/**
 * 任务执行器接口
 */
public interface TaskExecutor {
    void execute(Task task) throws TaskExecutionException;
    void stop(Task task);
    TaskStatus getStatus(Task task);
}
```

### 1.5 容器管理领域

```java
/**
 * 容器实体类
 */
@Entity
@Table(name = "container")
public class Container {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String containerId;
    
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @Column(nullable = false)
    private ContainerStatus status;
    
    @Column
    private String image; // Docker镜像ID
    
    @Column
    private Map<String, String> environment; // 环境变量JSON
    
    @Column
    private Map<String, String> ports; // 端口映射JSON
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime startedAt;
    
    @Column
    private LocalDateTime stoppedAt;
    
    // Getters and Setters
}

/**
 * 容器状态枚举
 */
public enum ContainerStatus {
    CREATED, RUNNING, STOPPED, FAILED, DESTROYED
}

/**
 * 容器管理接口
 */
public interface ContainerService {
    Container createContainer(ContainerDTO containerDTO);
    Container startContainer(Long containerId);
    Container stopContainer(Long containerId);
    Container deleteContainer(Long containerId);
    Container getContainer(Long id);
    Container getContainerByContainerId(String containerId);
    List<Container> getContainersByTask(Long taskId);
    ContainerStatus getContainerStatus(String containerId);
}

/**
 * Docker客户端接口
 */
public interface DockerClient {
    String createContainer(String imageId, Map<String, String> env, Map<String, String> ports);
    void startContainer(String containerId);
    void stopContainer(String containerId);
    void removeContainer(String containerId);
    ContainerInfo getContainerInfo(String containerId);
    List<ContainerInfo> listContainers();
    String getContainerLogs(String containerId);
    ContainerStats getContainerStats(String containerId);
    String pullImage(String imageName, String tag);
    void removeImage(String imageId);
    DockerImageInfo getImageInfo(String imageId);
}

/**
 * 容器信息
 */
public class ContainerInfo {
    private String id;
    private String image;
    private String status;
    private Map<String, String> ports;
    private Map<String, String> labels;
    private LocalDateTime created;
    
    // Getters and Setters
}

/**
 * 容器统计信息
 */
public class ContainerStats {
    private String containerId;
    private Double cpuUsage;
    private Double memoryUsage;
    private Long timestamp;
    
    // Getters and Setters
}
```

### 1.6 监控与日志领域

```java
/**
 * 监控数据实体类
 */
@Entity
@Table(name = "monitor_data")
public class MonitorData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "container_id", nullable = false)
    private Container container;
    
    @Column(nullable = false)
    private Double cpuUsage;
    
    @Column(nullable = false)
    private Double memoryUsage;
    
    @Column(nullable = false)
    private Long timestamp;
    
    // Getters and Setters
}

/**
 * 日志实体类
 */
@Entity
@Table(name = "task_log")
public class TaskLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private LogLevel level;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    // Getters and Setters
}

/**
 * 日志级别枚举
 */
public enum LogLevel {
    INFO, WARN, ERROR, DEBUG
}

/**
 * 监控服务接口
 */
public interface MonitorService {
    void collectContainerMetrics(String containerId);
    List<MonitorData> getMonitorData(String containerId, Long startTime, Long endTime);
    ContainerStats getCurrentStats(String containerId);
}

/**
 * 日志服务接口
 */
public interface LogService {
    void collectContainerLogs(String containerId);
    List<TaskLog> getTaskLogs(Long taskId, LogLevel level, LocalDateTime startTime, LocalDateTime endTime);
    void downloadTaskLogs(Long taskId, String outputPath);
    List<TaskLog> searchLogs(Long taskId, String keyword);
}
```

## 2. 数据传输对象 (DTO)

### 2.1 策略相关DTO

```java
public class StrategyDTO {
    private String name;
    private String language;
    private String version;
    private String dockerImageId; // Docker镜像包ID
    private Boolean enabled;
    
    // Getters and Setters
}

public class StrategyQueryDTO {
    private String name;
    private String language;
    private Boolean enabled;
    private Integer page;
    private Integer size;
    
    // Getters and Setters
}

public class DockerImageDTO {
    private String imageName;
    private String tag;
    private String dockerfilePath;
    private String buildContext;
    
    // Getters and Setters
}
```

### 2.2 插件相关DTO

```java
public class DataPluginDTO {
    private String name;
    private String language;
    private String code;
    private Boolean enabled;
    
    // Getters and Setters
}

public class PluginExecutionDTO {
    private Long pluginId;
    private Map<String, Object> parameters;
    
    // Getters and Setters
}
```

### 2.3 监听器相关DTO

```java
public class ListenerDTO {
    private String name;
    private String language;
    private String code;
    private String notificationType;
    private String config;
    private Boolean enabled;
    
    // Getters and Setters
}

public class ListenerSignalDTO {
    private Long listenerId;
    private Map<String, Object> signal;
    
    // Getters and Setters
}
```

### 2.4 任务相关DTO

```java
public class TaskDTO {
    private String name;
    private Long strategyId;
    private Long pluginId;
    private Long listenerId;
    private String parameters;
    
    // Getters and Setters
}

public class TaskExecutionDTO {
    private Long taskId;
    private Map<String, Object> parameters;
    
    // Getters and Setters
}
```

### 2.5 容器相关DTO

```java
public class ContainerDTO {
    private Long taskId;
    private String image; // Docker镜像ID
    private Map<String, String> environment;
    private Map<String, String> ports;
    
    // Getters and Setters
}

public class ContainerCreateDTO {
    private String imageId;
    private Map<String, String> environment;
    private Map<String, String> ports;
    private Map<String, String> volumes;
    private Map<String, String> labels;
    
    // Getters and Setters
}
```

## 3. 异常类设计

```java
/**
 * 策略异常
 */
public class StrategyException extends RuntimeException {
    public StrategyException(String message) {
        super(message);
    }
    
    public StrategyException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Docker镜像异常
 */
public class DockerImageException extends RuntimeException {
    public DockerImageException(String message) {
        super(message);
    }
    
    public DockerImageException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * 插件执行异常
 */
public class PluginExecutionException extends RuntimeException {
    public PluginExecutionException(String message) {
        super(message);
    }
    
    public PluginExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * 监听器执行异常
 */
public class ListenerExecutionException extends RuntimeException {
    public ListenerExecutionException(String message) {
        super(message);
    }
    
    public ListenerExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * 任务执行异常
 */
public class TaskExecutionException extends RuntimeException {
    public TaskExecutionException(String message) {
        super(message);
    }
    
    public TaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * 容器异常
 */
public class ContainerException extends RuntimeException {
    public ContainerException(String message) {
        super(message);
    }
    
    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## 4. 事件设计

```java
/**
 * 任务事件
 */
public class TaskEvent {
    private Long taskId;
    private TaskEventType eventType;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
    
    // Getters and Setters
}

/**
 * 任务事件类型
 */
public enum TaskEventType {
    TASK_CREATED, TASK_STARTED, TASK_COMPLETED, TASK_FAILED, TASK_CANCELLED
}

/**
 * 容器事件
 */
public class ContainerEvent {
    private String containerId;
    private ContainerEventType eventType;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
    
    // Getters and Setters
}

/**
 * 容器事件类型
 */
public enum ContainerEventType {
    CONTAINER_CREATED, CONTAINER_STARTED, CONTAINER_STOPPED, CONTAINER_FAILED
}

/**
 * Docker镜像事件
 */
public class DockerImageEvent {
    private String imageId;
    private DockerImageEventType eventType;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
    
    // Getters and Setters
}

/**
 * Docker镜像事件类型
 */
public enum DockerImageEventType {
    IMAGE_PULLED, IMAGE_BUILT, IMAGE_REMOVED, IMAGE_TAGGED
}

/**
 * 事件发布接口
 */
public interface EventPublisher {
    void publishTaskEvent(TaskEvent event);
    void publishContainerEvent(ContainerEvent event);
    void publishDockerImageEvent(DockerImageEvent event);
}

/**
 * 事件监听器接口
 */
public interface EventListener {
    void onTaskEvent(TaskEvent event);
    void onContainerEvent(ContainerEvent event);
    void onDockerImageEvent(DockerImageEvent event);
}
```