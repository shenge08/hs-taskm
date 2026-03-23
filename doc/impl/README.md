# HS-TASKM 实现文档索引

本目录包含 HS-TASKM 项目各功能模块的详细实现文档。

## 文档列表

### Issue #1: 基础设施和项目搭建
**文件**: [issue-01-infrastructure.md](./issue-01-infrastructure.md)

**功能**: 多模块 Maven 项目结构、Spring Boot 框架、数据库集成

**核心内容**:
- 多模块项目结构设计
- Spring Boot 3.2.3 + MyBatis-Plus 配置
- PostgreSQL + Flyway 数据库迁移
- Docker Java Client 集成
- 公共模块设计（实体、DTO、异常）

---

### Issue #2-#4: 查询 API (策略/插件/监听器)
**文件**: [issue-02-04-query-api.md](./issue-02-04-query-api.md)

**功能**: 策略、数据插件、监听器的 RESTful CRUD API

**核心内容**:
- Mapper 层（DAO）
- Service 层（业务逻辑）
- Controller 层（REST API）
- 统一响应格式
- 全局异常处理

**主要类**:
- `StrategyService` / `StrategyController`
- `DataPluginService` / `DataPluginController`
- `ListenerService` / `ListenerController`

---

### Issue #5-#6: 任务创建和容器管理
**文件**: [issue-05-06-task-container.md](./issue-05-06-task-container.md)

**功能**: 任务创建验证、Docker 容器生命周期管理

**核心内容**:
- 任务创建 DTO 和验证逻辑
- 语言匹配验证（策略/插件/监听器）
- Docker 容器创建、启动、停止、删除
- 容器状态查询

**主要类**:
- `TaskServiceImpl` - 任务创建和持久化
- `ContainerLifecycleManagerImpl` - Docker 容器操作

---

### Issue #7-#9: 任务编排（注入/启动/停止）
**文件**: [issue-07-09-task-orchestration.md](./issue-07-09-task-orchestration.md)

**功能**: 代码注入、任务启动流程、任务停止流程

**核心内容**:
- 代码片段加载和 Base64 编码
- 参数合并（默认值 + 用户参数）
- 完整任务启动编排
- 完整任务停止编排
- 监控集成

**主要类**:
- `CodeSnippetInjectorImpl` - 代码注入器
- `TaskOrchestratorImpl` - 任务编排器

---

### Issue #10: Resource Monitor
**文件**: [issue-10-resource-monitor.md](./issue-10-resource-monitor.md)

**功能**: 容器资源监控（CPU、内存、网络、磁盘I/O）

**核心内容**:
- 定时采集容器指标（每5秒）
- 指标数据持久化
- 历史指标查询
- 自动清理过期数据

**主要类**:
- `ResourceMonitorImpl` - 监控服务实现
- `MonitorData` - 监控数据实体
- `MonitorDataMapper` - 数据访问层

---

### Issue #11: Log Router 和日志查看
**文件**: [issue-11-resource-monitor.md](./issue-11-resource-monitor.md) ⚠️ *编号错误，应为 Log Router*

**功能**: 任务日志管理和查询（分页/尾部/搜索）

**核心内容**:
- 日志文件路径管理
- 分页读取日志
- 尾部读取（tail）
- 关键字搜索

**主要类**:
- `LogRouterImpl` - 日志路径管理
- `LogServiceImpl` - 日志读取服务
- `LogResult` - 日志查询结果 DTO

---

### Issue #12: 任务状态查询
**文件**: [issue-12-log-router.md](./issue-12-log-router.md) ⚠️ *编号错误，应为 Task Query*

**功能**: 任务分页查询和统计

**核心内容**:
- 分页查询任务列表
- 状态筛选
- 多字段排序
- 任务状态统计

**主要类**:
- `MybatisPlusConfig` - 分页插件配置
- `TaskServiceImpl` - 服务层实现
- `TaskController` - REST API 控制器

---

### Issue #13: CLI 工具 - 查询命令
**文件**: [issue-13-task-query.md](./issue-13-task-query.md) ⚠️ *编号错误，应为 CLI Tool*

**功能**: 命令行查询工具

**核心内容**:
- Picocli 命令行框架集成
- 策略查询命令
- 任务查询命令
- 表格和 JSON 双格式输出

**主要类**:
- `HsTaskmCommand` - 主命令
- `StrategyCommand` / `TaskCommand` - 子命令
- `ApiClient` - REST API 客户端
- `OutputFormatter` - 输出格式化器

---

### Issue #28: 日志管理实现
**文件**: [issue-28-log-management.md](./issue-28-log-management.md)

**功能**: 基于容器类型的日志分离管理

**核心内容**:
- 日志目录结构设计（strategy/plugin/listener 分离）
- LogService 接口扩展（添加 type 参数）
- LogServiceImpl 重构（移除 LogRouter）
- 容器环境变量配置（LOG_TYPE）
- 卷挂载配置更新

**主要类**:
- `LogService` / `LogServiceImpl` - 日志服务
- `CodeSnippetInjectorImpl` - 添加 LOG_TYPE 环境变量
- `PluginContainerManagerImpl` - 插件容器配置
- `ListenerContainerManagerImpl` - 监听器容器配置

---

### Issue #29: 端到端集成测试
**文件**: [issue-29-integration-tests.md](./issue-29-integration-tests.md)

**功能**: 完整的端到端集成测试

**核心内容**:
- Testcontainers 框架集成
- PostgreSQL 容器自动化管理
- 插件/监听器实例完整流程测试
- 日志分离验证
- 默认实例逻辑测试

**主要测试类**:
- `EndToEndIntegrationTest` - 端到端集成测试
- `LogServiceTest` - 日志服务测试（已更新）

---

## 文档修正说明

由于 GitHub Issue 编号与内部实现编号存在偏差，部分文档文件名与内容不对应：

| 文件名 | 实际内容 | 对应 Issue |
|--------|---------|---------|
| issue-11-resource-monitor.md | Resource Monitor | #10 |
| issue-12-log-router.md | Log Router | #11 |
| issue-13-task-query.md | Task Query | #12 |
| issue-14-cli-tool.md | CLI Tool | #13 |

建议后续文档按功能命名而非 Issue 编号。

## 文档统计

| 类别 | Issue 数量 | 文档数量 | 总大小 |
|------|-----------|---------|--------|
| 基础设施 | #1 | 1 | 5.3 KB |
| 查询 API | #2-4 | 1 | 11.2 KB |
| 任务管理 | #5-6 | 1 | 12.8 KB |
| 任务编排 | #7-9 | 1 | 16.4 KB |
| 监控日志 | #10-11 | 2 | 29.9 KB |
| 查询统计 | #12 | 1 | 16.2 KB |
| CLI 工具 | #13-14 | 1 | 25.2 KB |
| 日志管理 | #28 | 1 | 12.5 KB |
| 集成测试 | #29 | 1 | 14.8 KB |
| **总计** | **16** | **10** | **144.3 KB** |

## 按功能分类查阅

### 核心功能
- [基础设施](./issue-01-infrastructure.md) - 项目搭建和配置
- [查询 API](./issue-02-04-query-api.md) - 策略/插件/监听器查询
- [任务创建](./issue-05-06-task-container.md) - 任务创建和验证
- [容器管理](./issue-05-06-task-container.md) - Docker 容器操作

### 编排功能
- [代码注入](./issue-07-09-task-orchestration.md) - Code Snippet Injector
- [任务启动](./issue-07-09-task-orchestration.md) - 启动流程编排
- [任务停止](./issue-07-09-task-orchestration.md) - 停止流程编排

### 监控和日志
- [资源监控](./issue-10-resource-monitor.md) - Container 资源监控
- [日志路由](./issue-11-resource-monitor.md) - 日志查询和路由
- [日志管理](./issue-28-log-management.md) - 基于容器类型的日志分离

### 测试
- [端到端集成测试](./issue-29-integration-tests.md) - Testcontainers 集成测试

### 查询和统计
- [任务查询](./issue-12-log-router.md) - 分页查询和统计
- [CLI 工具](./issue-13-task-query.md) - 命令行查询工具

## Mermaid 图表索引

文档中使用的 Mermaid 图表类型：

- **flowchart** - 业务流程图（15个）
- **sequenceDiagram** - 时序图（12个）
- **stateDiagram** - 状态图（2个）

## 快速导航

### 开发者
- 从 [基础设施](./issue-01-infrastructure.md) 开始了解项目结构
- 查看 [任务编排](./issue-07-09-task-orchestration.md) 理解核心流程
- 参考 [监控和日志](#监控和日志) 了解运维功能

### 测试人员
- 查看 [查询 API](./issue-02-04-query-api.md) 了解接口
- 参考 [任务查询](./issue-12-log-router.md) 了解测试场景
- 查看 [端到端集成测试](./issue-29-integration-tests.md) 了解测试覆盖

### 运维人员
- [资源监控](./issue-10-resource-monitor.md) - 监控配置
- [日志管理](./issue-11-resource-monitor.md) - 日志查询
- [CLI 工具](./issue-13-task-query.md) - 命令行操作

## 文档维护

### 创建日期
2024-01-01 (根据实际创建日期更新)

### 更新记录
- 2024-01-01: 初始版本，创建所有 Issue 文档
- 2026-03-23: 添加 Issue #28 和 #29 文档

### 维护者
HS-TASKM 开发团队

## 相关文档

- [项目主 README](../README.md)
- [部署文档](../deployment.md)
- [API 文档](../api/README.md)
- [数据库设计](../database/README.md)

---

**最后更新**: 2026-03-23
**文档版本**: 1.1
