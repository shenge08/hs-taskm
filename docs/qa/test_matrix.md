# HS-TASKM 基于契约的测试矩阵

## 1. 测试策略概述

### 1.1 测试分层
- **单元测试**: 针对单个类和方法的测试
- **集成测试**: 针对服务层和外部依赖的测试
- **系统测试**: 针对完整业务流程的测试
- **契约测试**: 针对API接口和数据契约的测试

### 1.2 测试优先级
- **P0**: 核心功能，必须通过
- **P1**: 重要功能，影响用户体验
- **P2**: 一般功能，可以延后修复

## 2. 策略管理测试矩阵

### 2.1 StrategyService 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| STR-001 | 注册新策略 | registerStrategy | 有效的StrategyDTO | 返回Strategy对象 | P0 | 单元测试 |
| STR-002 | 注册重复名称策略 | registerStrategy | 相同名称的StrategyDTO | 抛出StrategyException | P0 | 单元测试 |
| STR-003 | 更新策略 | updateStrategy | 有效的StrategyDTO | 返回更新后的Strategy | P0 | 单元测试 |
| STR-004 | 更新不存在的策略 | updateStrategy | 不存在的策略ID | 抛出StrategyException | P0 | 单元测试 |
| STR-005 | 删除策略 | deleteStrategy | 存在的策略ID | 删除成功 | P0 | 单元测试 |
| STR-006 | 删除不存在的策略 | deleteStrategy | 不存在的策略ID | 抛出StrategyException | P0 | 单元测试 |
| STR-007 | 获取策略 | getStrategy | 存在的策略ID | 返回Strategy对象 | P0 | 单元测试 |
| STR-008 | 获取不存在的策略 | getStrategy | 不存在的策略ID | 返回null | P0 | 单元测试 |
| STR-009 | 按语言获取策略 | getStrategiesByLanguage | "Python" | 返回Python策略列表 | P1 | 单元测试 |
| STR-010 | 策略版本回滚 | rollbackVersion | 有效的策略ID和版本 | 返回回滚后的策略 | P1 | 单元测试 |

### 2.2 DockerImageService 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| IMG-001 | 拉取镜像 | pullImage | 有效的镜像名和标签 | 返回镜像ID | P0 | 集成测试 |
| IMG-002 | 拉取不存在的镜像 | pullImage | 无效的镜像名 | 抛出DockerImageException | P0 | 集成测试 |
| IMG-003 | 删除镜像 | removeImage | 存在的镜像ID | 删除成功 | P0 | 集成测试 |
| IMG-004 | 删除不存在的镜像 | removeImage | 不存在的镜像ID | 抛出DockerImageException | P0 | 集成测试 |
| IMG-005 | 获取镜像信息 | getImageInfo | 存在的镜像ID | 返回DockerImageInfo | P0 | 集成测试 |
| IMG-006 | 获取不存在的镜像信息 | getImageInfo | 不存在的镜像ID | 抛出DockerImageException | P0 | 集成测试 |
| IMG-007 | 列出所有镜像 | listImages | 无参数 | 返回镜像列表 | P1 | 集成测试 |
| IMG-008 | 构建镜像 | buildImage | 有效的Dockerfile路径 | 返回镜像ID | P1 | 集成测试 |
| IMG-009 | 构建镜像失败 | buildImage | 无效的Dockerfile | 抛出DockerImageException | P1 | 集成测试 |

## 3. 数据插件管理测试矩阵

### 3.1 DataPluginService 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| PLG-001 | 注册插件 | registerPlugin | 有效的DataPluginDTO | 返回DataPlugin对象 | P0 | 单元测试 |
| PLG-002 | 注册重复名称插件 | registerPlugin | 相同名称的DataPluginDTO | 抛出异常 | P0 | 单元测试 |
| PLG-003 | 更新插件 | updatePlugin | 有效的DataPluginDTO | 返回更新后的DataPlugin | P0 | 单元测试 |
| PLG-004 | 更新不存在的插件 | updatePlugin | 不存在的插件ID | 抛出异常 | P0 | 单元测试 |
| PLG-005 | 删除插件 | deletePlugin | 存在的插件ID | 删除成功 | P0 | 单元测试 |
| PLG-006 | 删除不存在的插件 | deletePlugin | 不存在的插件ID | 抛出异常 | P0 | 单元测试 |
| PLG-007 | 执行插件 | executePlugin | 有效的插件ID和参数 | 返回Map数据 | P0 | 集成测试 |
| PLG-008 | 执行不存在的插件 | executePlugin | 不存在的插件ID | 抛出PluginExecutionException | P0 | 集成测试 |
| PLG-009 | 测试插件 | testPlugin | 有效的插件ID | 返回测试结果true | P1 | 集成测试 |
| PLG-010 | 测试无效插件 | testPlugin | 无效的插件ID | 返回测试结果false | P1 | 集成测试 |

### 3.2 DataPluginExecutor 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| PEX-001 | 执行Python插件 | execute | 有效的Python代码和参数 | 返回Map数据 | P0 | 单元测试 |
| PEX-002 | 执行Java插件 | execute | 有效的Java代码和参数 | 返回Map数据 | P0 | 单元测试 |
| PEX-003 | 执行Go插件 | execute | 有效的Go代码和参数 | 返回Map数据 | P0 | 单元测试 |
| PEX-004 | 执行无效代码 | execute | 无效的代码 | 抛出PluginExecutionException | P0 | 单元测试 |
| PEX-005 | 验证有效代码 | validate | 有效的代码 | 返回true | P1 | 单元测试 |
| PEX-006 | 验证无效代码 | validate | 无效的代码 | 返回false | P1 | 单元测试 |

## 4. 监听器管理测试矩阵

### 4.1 ListenerService 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| LSN-001 | 注册监听器 | registerListener | 有效的ListenerDTO | 返回Listener对象 | P0 | 单元测试 |
| LSN-002 | 注册重复名称监听器 | registerListener | 相同名称的ListenerDTO | 抛出异常 | P0 | 单元测试 |
| LSN-003 | 更新监听器 | updateListener | 有效的ListenerDTO | 返回更新后的Listener | P0 | 单元测试 |
| LSN-004 | 更新不存在的监听器 | updateListener | 不存在的监听器ID | 抛出异常 | P0 | 单元测试 |
| LSN-005 | 删除监听器 | deleteListener | 存在的监听器ID | 删除成功 | P0 | 单元测试 |
| LSN-006 | 删除不存在的监听器 | deleteListener | 不存在的监听器ID | 抛出异常 | P0 | 单元测试 |
| LSN-007 | 触发监听器 | triggerListener | 有效的监听器ID和信号 | 执行成功 | P0 | 集成测试 |
| LSN-008 | 触发不存在的监听器 | triggerListener | 不存在的监听器ID | 抛出异常 | P0 | 集成测试 |

### 4.2 ListenerExecutor 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| LEX-001 | 执行邮件监听器 | execute | 有效的邮件监听器代码 | 发送邮件成功 | P0 | 集成测试 |
| LEX-002 | 执行Webhook监听器 | execute | 有效的Webhook监听器代码 | 发送HTTP请求成功 | P0 | 集成测试 |
| LEX-003 | 执行消息队列监听器 | execute | 有效的消息队列监听器代码 | 发送消息成功 | P0 | 集成测试 |
| LEX-004 | 执行无效代码 | execute | 无效的监听器代码 | 抛出ListenerExecutionException | P0 | 单元测试 |
| LEX-005 | 验证有效代码 | validate | 有效的监听器代码 | 返回true | P1 | 单元测试 |
| LEX-006 | 验证无效代码 | validate | 无效的监听器代码 | 返回false | P1 | 单元测试 |

## 5. 任务管理测试矩阵

### 5.1 TaskService 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| TSK-001 | 创建任务 | createTask | 有效的TaskDTO | 返回Task对象，状态为CREATED | P0 | 单元测试 |
| TSK-002 | 创建任务-策略不存在 | createTask | 不存在的策略ID | 抛出异常 | P0 | 单元测试 |
| TSK-003 | 启动任务 | startTask | 存在的任务ID | 返回Task对象，状态为RUNNING | P0 | 集成测试 |
| TSK-004 | 启动已运行的任务 | startTask | 运行中的任务ID | 抛出异常 | P0 | 集成测试 |
| TSK-005 | 停止任务 | stopTask | 运行中的任务ID | 返回Task对象，状态为CANCELLED | P0 | 集成测试 |
| TSK-006 | 停止已停止的任务 | stopTask | 已停止的任务ID | 抛出异常 | P0 | 集成测试 |
| TSK-007 | 删除任务 | deleteTask | 存在的任务ID | 删除成功 | P0 | 单元测试 |
| TSK-008 | 删除运行中的任务 | deleteTask | 运行中的任务ID | 抛出异常 | P0 | 单元测试 |
| TSK-009 | 获取任务 | getTask | 存在的任务ID | 返回Task对象 | P0 | 单元测试 |
| TSK-010 | 获取不存在的任务 | getTask | 不存在的任务ID | 返回null | P0 | 单元测试 |
| TSK-011 | 按状态获取任务 | getTasksByStatus | "RUNNING" | 返回运行中的任务列表 | P1 | 单元测试 |
| TSK-012 | 获取任务状态 | getTaskStatus | 存在的任务ID | 返回任务状态 | P1 | 单元测试 |

### 5.2 TaskExecutor 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| TEX-001 | 执行任务 | execute | 有效的Task对象 | 任务执行成功 | P0 | 集成测试 |
| TEX-002 | 执行任务-镜像不存在 | execute | 镜像不存在的Task对象 | 抛出TaskExecutionException | P0 | 集成测试 |
| TEX-003 | 停止任务 | stop | 运行中的Task对象 | 任务停止成功 | P0 | 集成测试 |
| TEX-004 | 获取任务状态 | getStatus | 有效的Task对象 | 返回任务状态 | P0 | 单元测试 |

## 6. 容器管理测试矩阵

### 6.1 ContainerService 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| CNT-001 | 创建容器 | createContainer | 有效的ContainerDTO | 返回Container对象，状态为CREATED | P0 | 集成测试 |
| CNT-002 | 创建容器-镜像不存在 | createContainer | 镜像不存在的ContainerDTO | 抛出ContainerException | P0 | 集成测试 |
| CNT-003 | 启动容器 | startContainer | 存在的容器ID | 返回Container对象，状态为RUNNING | P0 | 集成测试 |
| CNT-004 | 启动已启动的容器 | startContainer | 运行中的容器ID | 抛出异常 | P0 | 集成测试 |
| CNT-005 | 停止容器 | stopContainer | 运行中的容器ID | 返回Container对象，状态为STOPPED | P0 | 集成测试 |
| CNT-006 | 停止已停止的容器 | stopContainer | 已停止的容器ID | 抛出异常 | P0 | 集成测试 |
| CNT-007 | 删除容器 | deleteContainer | 存在的容器ID | 删除成功 | P0 | 集成测试 |
| CNT-008 | 删除运行中的容器 | deleteContainer | 运行中的容器ID | 抛出异常 | P0 | 集成测试 |
| CNT-009 | 获取容器 | getContainer | 存在的容器ID | 返回Container对象 | P0 | 单元测试 |
| CNT-010 | 获取不存在的容器 | getContainer | 不存在的容器ID | 返回null | P0 | 单元测试 |
| CNT-011 | 按容器ID获取容器 | getContainerByContainerId | 存在的容器ID | 返回Container对象 | P1 | 单元测试 |
| CNT-012 | 按任务获取容器 | getContainersByTask | 存在的任务ID | 返回容器列表 | P1 | 单元测试 |
| CNT-013 | 获取容器状态 | getContainerStatus | 存在的容器ID | 返回容器状态 | P1 | 集成测试 |

### 6.2 DockerClient 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| DCK-001 | 创建容器 | createContainer | 有效的镜像ID和环境变量 | 返回容器ID | P0 | 集成测试 |
| DCK-002 | 创建容器-镜像不存在 | createContainer | 不存在的镜像ID | 抛出ContainerException | P0 | 集成测试 |
| DCK-003 | 启动容器 | startContainer | 存在的容器ID | 启动成功 | P0 | 集成测试 |
| DCK-004 | 启动不存在的容器 | startContainer | 不存在的容器ID | 抛出ContainerException | P0 | 集成测试 |
| DCK-005 | 停止容器 | stopContainer | 运行中的容器ID | 停止成功 | P0 | 集成测试 |
| DCK-006 | 停止不存在的容器 | stopContainer | 不存在的容器ID | 抛出ContainerException | P0 | 集成测试 |
| DCK-007 | 删除容器 | removeContainer | 存在的容器ID | 删除成功 | P0 | 集成测试 |
| DCK-008 | 删除不存在的容器 | removeContainer | 不存在的容器ID | 抛出ContainerException | P0 | 集成测试 |
| DCK-009 | 获取容器信息 | getContainerInfo | 存在的容器ID | 返回ContainerInfo | P0 | 集成测试 |
| DCK-010 | 获取容器日志 | getContainerLogs | 存在的容器ID | 返回容器日志 | P1 | 集成测试 |
| DCK-011 | 获取容器统计信息 | getContainerStats | 存在的容器ID | 返回ContainerStats | P1 | 集成测试 |
| DCK-012 | 列出所有容器 | listContainers | 无参数 | 返回容器列表 | P1 | 集成测试 |

## 7. 监控与日志测试矩阵

### 7.1 MonitorService 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| MON-001 | 收集容器指标 | collectContainerMetrics | 存在的容器ID | 监控数据收集成功 | P1 | 集成测试 |
| MON-002 | 收集不存在容器的指标 | collectContainerMetrics | 不存在的容器ID | 抛出异常 | P1 | 集成测试 |
| MON-003 | 获取监控数据 | getMonitorData | 存在的容器ID和时间范围 | 返回监控数据列表 | P1 | 单元测试 |
| MON-004 | 获取当前统计信息 | getCurrentStats | 存在的容器ID | 返回ContainerStats | P1 | 集成测试 |

### 7.2 LogService 测试用例

| 测试ID | 测试场景 | 测试方法 | 输入数据 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|----------|--------|----------|
| LOG-001 | 收集容器日志 | collectContainerLogs | 存在的容器ID | 日志收集成功 | P1 | 集成测试 |
| LOG-002 | 收集不存在容器的日志 | collectContainerLogs | 不存在的容器ID | 抛出异常 | P1 | 集成测试 |
| LOG-003 | 获取任务日志 | getTaskLogs | 存在的任务ID和过滤条件 | 返回任务日志列表 | P1 | 单元测试 |
| LOG-004 | 下载任务日志 | downloadTaskLogs | 存在的任务ID和输出路径 | 日志下载成功 | P1 | 集成测试 |
| LOG-005 | 搜索日志 | searchLogs | 存在的任务ID和关键字 | 返回匹配的日志列表 | P1 | 单元测试 |

## 8. 集成测试场景

### 8.1 完整任务执行流程

| 测试ID | 测试场景 | 测试步骤 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|--------|----------|
| INT-001 | 完整任务执行 | 1. 注册策略<br>2. 拉取镜像<br>3. 创建任务<br>4. 启动任务<br>5. 监控执行<br>6. 停止任务 | 任务执行完成，所有状态正确 | P0 | 系统测试 |
| INT-002 | 任务执行失败处理 | 1. 创建任务（无效镜像）<br>2. 启动任务<br>3. 验证异常处理 | 任务执行失败，异常信息正确 | P0 | 系统测试 |
| INT-003 | 并发任务执行 | 1. 同时创建多个任务<br>2. 启动所有任务<br>3. 监控资源使用 | 所有任务正常执行，资源使用正常 | P1 | 系统测试 |

### 8.2 容器生命周期测试

| 测试ID | 测试场景 | 测试步骤 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|--------|----------|
| INT-004 | 容器完整生命周期 | 1. 创建容器<br>2. 启动容器<br>3. 监控容器<br>4. 停止容器<br>5. 删除容器 | 容器生命周期完整执行 | P0 | 系统测试 |
| INT-005 | 容器异常处理 | 1. 创建容器（无效镜像）<br>2. 验证异常处理 | 容器创建失败，异常信息正确 | P0 | 系统测试 |

### 8.3 插件和监听器测试

| 测试ID | 测试场景 | 测试步骤 | 预期结果 | 优先级 | 测试类型 |
|--------|----------|----------|----------|--------|----------|
| INT-006 | 插件执行测试 | 1. 注册插件<br>2. 创建任务（使用插件）<br>3. 启动任务<br>4. 验证插件执行 | 插件执行成功，数据正确 | P1 | 系统测试 |
| INT-007 | 监听器触发测试 | 1. 注册监听器<br>2. 创建任务（使用监听器）<br>3. 启动任务<br>4. 验证监听器触发 | 监听器触发成功，通知发送 | P1 | 系统测试 |

## 9. 性能测试矩阵

| 测试ID | 测试场景 | 测试指标 | 目标值 | 优先级 | 测试类型 |
|--------|----------|----------|--------|--------|----------|
| PERF-001 | 并发容器执行 | 最大并发容器数 | 100+ | P0 | 性能测试 |
| PERF-002 | 任务创建性能 | 任务创建响应时间 | < 500ms | P0 | 性能测试 |
| PERF-003 | 容器启动性能 | 容器启动时间 | < 30s | P0 | 性能测试 |
| PERF-004 | 内存使用性能 | 容器内存使用峰值 | < 1GB | P1 | 性能测试 |
| PERF-005 | CPU使用性能 | 容器CPU使用率 | < 80% | P1 | 性能测试 |

## 10. 测试执行计划

### 10.1 测试执行顺序
1. **单元测试** (STR-001 ~ STR-010, IMG-001 ~ IMG-009, etc.)
2. **集成测试** (DCK-001 ~ DCK-012, PEX-001 ~ PEX-006, etc.)
3. **系统测试** (INT-001 ~ INT-007)
4. **性能测试** (PERF-001 ~ PERF-005)

### 10.2 测试覆盖率目标
- **单元测试覆盖率**: ≥ 80%
- **集成测试覆盖率**: ≥ 90%
- **系统测试覆盖率**: ≥ 95%

### 10.3 测试环境要求
- **Docker环境**: Docker 20.10+
- **数据库**: PostgreSQL 13+
- **Java环境**: Java 17+
- **测试数据**: 预置测试镜像和插件

## 11. 测试报告模板

### 11.1 测试执行报告
```
测试执行报告
================================
测试周期: [开始日期] - [结束日期]
测试环境: [环境信息]
执行人员: [测试人员]

测试统计:
- 总测试用例: [数量]
- 通过: [数量]
- 失败: [数量]
- 跳过: [数量]
- 通过率: [百分比]

模块统计:
- 策略管理: [统计]
- 插件管理: [统计]
- 监听器管理: [统计]
- 任务管理: [统计]
- 容器管理: [统计]
- 监控日志: [统计]
```

### 11.2 缺陷报告模板
```
缺陷报告
================================
缺陷ID: [唯一标识]
缺陷标题: [简短描述]
严重程度: [致命/严重/一般/轻微]
优先级: [高/中/低]
发现时间: [发现时间]
发现环境: [环境信息]

复现步骤:
1. [步骤1]
2. [步骤2]
3. [步骤3]

期望结果: [期望结果]
实际结果: [实际结果]

截图/日志: [附件信息]
```