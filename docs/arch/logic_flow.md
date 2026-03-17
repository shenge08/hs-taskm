# HS-TASKM 系统逻辑流转图

## 1. 核心系统架构

```mermaid
graph TB
    subgraph "用户层"
        CLI[CLI工具]
        API[REST API]
    end
    
    subgraph "核心服务层"
        TM[任务管理器 TaskManager]
        SM[策略管理器 StrategyManager]
        PM[插件管理器 PluginManager] 
        LM[监听器管理器 ListenerManager]
        CM[容器管理器 ContainerManager]
        MM[监控管理器 MonitorManager]
        LOG[日志管理器 LogManager]
        DIM[Docker镜像管理器 DockerImageManager]
    end
    
    subgraph "Docker层"
        DOCKER[Docker Runtime]
        IMAGES[Docker镜像库]
        STRATEGY[策略容器]
        PLUGIN[插件容器]
        LISTENER[监听器容器]
    end
    
    subgraph "数据层"
        DB[(PostgreSQL)]
        CONFIG[配置文件]
    end
    
    CLI --> TM
    API --> TM
    TM --> SM
    TM --> PM
    TM --> LM
    SM --> DIM
    DIM --> IMAGES
    SM --> CM
    PM --> CM
    LM --> CM
    CM --> DOCKER
    DOCKER --> STRATEGY
    DOCKER --> PLUGIN
    DOCKER --> LISTENER
    CM --> MM
    CM --> LOG
    TM --> DB
    SM --> DB
    PM --> DB
    LM --> DB
    MM --> DB
    LOG --> DB
```

## 2. 策略管理流程

```mermaid
graph TD
    A[策略注册请求] --> B[拉取Docker镜像]
    B --> C{镜像拉取成功?}
    C -->|失败| D[返回错误信息]
    C -->|成功| E[验证镜像]
    E --> F{镜像验证通过?}
    F -->|失败| G[清理镜像]
    G --> D
    F -->|成功| H[创建策略记录]
    H --> I[关联Docker镜像ID]
    I --> J[策略启用]
    J --> K[策略可用]
    
    L[策略执行] --> M[获取Docker镜像ID]
    M --> N[检查镜像是否存在]
    N --> O{镜像存在?}
    O -->|否| P[拉取镜像]
    P --> N
    O -->|是| Q[创建容器]
    Q --> R[启动策略容器]
```

## 3. Docker镜像管理流程

```mermaid
graph LR
    A[镜像拉取请求] --> B[检查本地镜像]
    B --> C{本地存在?}
    C -->|是| D[返回镜像ID]
    C -->|否| E[从仓库拉取镜像]
    E --> F{拉取成功?}
    F -->|失败| G[返回错误]
    F -->|成功| H[存储镜像信息]
    H --> I[返回镜像ID]
    
    J[镜像构建请求] --> K[检查Dockerfile]
    K --> L{Dockerfile存在?}
    L -->|否| M[返回错误]
    L -->|是| N[执行构建命令]
    N --> O{构建成功?}
    O -->|失败| P[返回错误]
    O -->|成功| Q[标记镜像]
    Q --> R[存储镜像信息]
    R --> S[返回镜像ID]
    
    T[镜像删除请求] --> U[检查镜像使用情况]
    U --> V{镜像被使用?}
    V -->|是| W[返回错误]
    V -->|否| X[删除镜像]
    X --> Y[清理相关记录]
    Y --> Z[删除成功]
```

## 4. 数据插件管理流程

```mermaid
graph LR
    A[插件注册] --> B[语言版本检查]
    B --> C[代码片段验证]
    C --> D[插件测试]
    D --> E{测试通过?}
    E -->|失败| F[返回错误信息]
    E -->|成功| G[存储插件元数据]
    G --> H[更新插件索引]
    H --> I[插件可用]
    
    J[插件使用] --> K[匹配策略语言]
    K --> L[加载插件代码]
    L --> M[执行插件逻辑]
    M --> N[返回Map数据]
    N --> O[数据传递给策略]
```

## 5. 监听器管理流程

```mermaid
graph TD
    A[监听器注册] --> B[语言版本检查]
    B --> C[代码片段验证]
    C --> D[配置元数据]
    D --> E[存储监听器信息]
    E --> F[监听器就绪]
    
    G[信号触发] --> H[匹配监听器]
    H --> I[加载监听器代码]
    I --> J[执行监听器逻辑]
    J --> K[发送通知]
    K --> L[邮件/Webhook/消息队列]
```

## 6. 任务执行流程

```mermaid
graph TD
    A[任务创建] --> B[选择策略]
    B --> C[获取策略Docker镜像ID]
    C --> D[选择数据插件]
    D --> E[选择监听器]
    E --> F[配置参数]
    F --> G[检查Docker镜像]
    G --> H{镜像存在?}
    H -->|否| I[拉取镜像]
    I --> G
    H -->|是| J[创建容器]
    J --> K{容器创建成功?}
    K -->|失败| L[记录错误]
    K -->|成功| M[启动容器]
    M --> N[注入数据插件]
    N --> O[执行策略逻辑]
    O --> P[收集执行结果]
    P --> Q[触发监听器]
    Q --> R[更新任务状态]
    R --> S[记录日志]
    S --> T[执行完成]
    L --> U[任务失败]
    U --> V[清理资源]
    T --> W[资源清理]
```

## 7. 容器生命周期管理

```mermaid
graph TD
    A[容器创建请求] --> B[检查Docker环境]
    B --> C[获取Docker镜像ID]
    C --> D[准备容器配置]
    D --> E[创建容器]
    E --> F{创建成功?}
    F -->|失败| G[清理失败容器]
    F -->|成功| H[启动容器]
    H --> I[注入策略/插件/监听器]
    I --> J[监控容器状态]
    J --> K{容器运行中?}
    K -->|是| L[收集资源使用情况]
    K -->|否| M[检查异常原因]
    L --> N[更新监控数据]
    M --> O[记录异常日志]
    O --> P[容器停止]
    P --> Q[清理容器资源]
```

## 8. 监控与日志流程

```mermaid
graph LR
    A[Docker API调用] --> B[查询容器状态]
    B --> C[获取资源使用情况]
    C --> D[更新监控数据]
    D --> E[存储到数据库]
    
    F[日志采集] --> G[Docker Logs API]
    G --> H[实时日志流]
    H --> I[日志过滤和解析]
    I --> J[日志存储]
    J --> K[日志索引]
    K --> L[日志查询]
    L --> M[日志下载]
```

## 9. 异常处理流程

```mermaid
graph TD
    A[异常检测] --> B[异常类型判断]
    B --> C{Docker镜像异常?}
    C -->|是| D[重新拉取镜像]
    C -->|否| E{容器异常?}
    E -->|是| F[容器重启/重建]
    E -->|否| G{策略异常?}
    G -->|是| H[策略重试]
    G -->|否| I{插件异常?}
    I -->|是| J[插件重新加载]
    I -->|否| K{监听器异常?}
    K -->|是| L[监听器重新执行]
    K -->|否| M[系统异常]
    D --> N[记录异常日志]
    F --> N
    H --> N
    J --> N
    L --> N
    M --> N
    N --> O[触发告警通知]
    O --> P[通知管理员]
```

## 10. 系统状态流转

```mermaid
stateDiagram-v2
    [*] --> INIT: 系统启动
    INIT --> READY: 初始化完成
    READY --> RUNNING: 任务执行中
    RUNNING --> MONITORING: 监控中
    MONITORING --> RUNNING: 正常运行
    RUNNING --> ERROR: 异常发生
    ERROR --> RECOVERY: 故障恢复
    RECOVERY --> RUNNING: 恢复成功
    RECOVERY --> ERROR: 恢复失败
    RUNNING --> STOPPED: 任务完成
    STOPPED --> CLEANUP: 资源清理
    CLEANUP --> READY: 清理完成
```

## 11. 策略与Docker镜像关联流程

```mermaid
graph TD
    A[策略定义] --> B[指定Docker镜像]
    B --> C[验证镜像可用性]
    C --> D{镜像可用?}
    D -->|否| E[选择其他镜像]
    E --> C
    D -->|是| F[记录镜像ID]
    F --> G[策略与镜像关联]
    G --> H[策略注册完成]
    
    I[策略执行] --> J[获取关联镜像ID]
    J --> K[检查镜像本地存在]
    K --> L{镜像存在?}
    L -->|否| M[拉取镜像]
    M --> N[等待拉取完成]
    N --> K
    L -->|是| O[使用镜像创建容器]
    O --> P[执行策略]
```