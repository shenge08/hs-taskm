# HS-TASKM

多语言策略容器执行平台 - 一个支持多种编程语言的交易策略执行和容器管理系统。

## 项目简介

HS-TASKM 是一个基于 Spring Boot 3.x 和 Docker 的分布式策略执行平台，支持在隔离的容器环境中执行多种编程语言编写的交易策略，并提供完整的数据插件和监听器机制。

## 核心特性

- **多语言支持**: 支持 Python、JavaScript 等多种编程语言编写的策略
- **容器隔离**: 基于 Docker 的容器化执行环境
- **数据插件**: 灵活的数据源、数据转换和数据输出插件
- **事件监听**: 可配置的事件监听器机制
- **任务管理**: 完整的任务生命周期管理
- **资源监控**: 容器资源使用监控
- **日志路由**: 容器日志统一收集和路由

## 技术栈

### 后端框架
- Spring Boot 3.2.3
- Spring Web
- Spring Data JPA
- MyBatis-Plus 3.5.5

### 数据库
- PostgreSQL 15+
- Flyway 10.10.0 (数据库迁移)

### 容器化
- Docker Java Client 3.3.6
- Docker Maven Plugin

### 其他工具
- Lombok (代码简化)
- Picocli 4.7.6 (CLI 工具)
- Testcontainers 1.19.7 (集成测试)
- Maven (构建工具)

## 项目结构

```
hs-taskm/
├── hs-taskm-common/          # 公共模块（实体、DTO、工具类）
├── hs-taskm-core/            # 核心模块（Spring Boot 应用入口）
├── hs-taskm-dao/             # 数据访问层（MyBatis-Plus Mapper）
├── hs-taskm-service/         # 业务逻辑层（Service 实现）
├── hs-taskm-api/             # REST API 层（Controller）
├── hs-taskm-cli/             # 命令行工具（Picocli CLI）
├── docs/                     # 项目文档
│   └── product/
│       └── PRD_v2.md         # 产品需求文档
├── pom.xml                   # 根 POM
├── Dockerfile                # Docker 镜像构建文件
└── README.md                 # 项目说明文档
```

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.8+
- PostgreSQL 15+
- Docker (用于容器管理)

### 1. 克隆项目

```bash
git clone git@gitee.com:shenge08/hs-taskm.git
cd hs-taskm
```

### 2. 配置数据库

创建 PostgreSQL 数据库：

```sql
CREATE DATABASE hs_taskm;
```

### 3. 配置应用

编辑 `hs-taskm-core/src/test/resources/application-test.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hs_taskm
    username: your_username
    password: your_password
```

### 4. 构建项目

```bash
mvn clean install
```

### 5. 运行应用

```bash
cd hs-taskm-core
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

## 开发指南

### 数据库迁移

项目使用 Flyway 进行数据库版本管理。迁移脚本位于：

```
hs-taskm-core/src/main/resources/db/migration/
```

数据库表结构：
- `strategy` - 策略定义
- `task` - 任务执行记录
- `container_execution` - 容器执行记录
- `data_plugin` - 数据插件配置
- `listener` - 监听器配置

### 运行测试

运行所有测试：

```bash
mvn test
```

运行特定模块的测试：

```bash
mvn test -pl hs-taskm-core
```

### 代码生成

使用 MyBatis-Plus Generator 生成代码：

```bash
cd hs-taskm-dao
mvn mybatis-plus:generator
```

## Docker 支持

### 构建 Docker 镜像

```bash
mvn clean package dockerfile:build
```

### 运行 Docker 容器

```bash
docker run -p 8080:8080 hs-taskm/hs-taskm-core:1.0.0-SNAPSHOT
```

### 配置 Docker 镜像仓库

在 `pom.xml` 中修改 `docker.image.prefix`：

```xml
<docker.image.prefix>your-registry/hs-taskm</docker.image.prefix>
```

## 配置说明

### MyBatis-Plus 配置

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      id-type: auto
```

### Docker Client 配置

Docker Client 自动连接到本地 Docker daemon：

```java
@Bean
public DockerClient dockerClient() {
    // Unix socket connection (Linux/Mac)
    // or TCP connection (Windows)
    return DockerClientImpl.getInstance(config, httpClient);
}
```

## API 文档

启动应用后，访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

## CLI 工具

构建并运行 CLI 工具：

```bash
cd hs-taskm-cli
mvn package exec:java
```

## 开发规范

### 分支管理

- `main` - 主分支（稳定版本）
- `matt` - 开发分支
- `feature/*` - 功能分支

### 提交规范

```
类型(范围): 简短描述

详细描述（可选）

关联 Issue: #issue_number
```

类型：
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式
- `refactor`: 重构
- `test`: 测试
- `chore`: 构建/工具

## 故障排查

### 常见问题

1. **数据库连接失败**
   - 检查 PostgreSQL 服务是否运行
   - 验证连接信息是否正确
   - 确认数据库已创建

2. **Docker 连接失败**
   - 检查 Docker 服务是否运行
   - 验证 Docker daemon 配置
   - Mac/Linux: 检查 `/var/run/docker.sock` 权限
   - Windows: 使用 TCP 连接

3. **Flyway 迁移失败**
   - 检查数据库迁移脚本版本
   - 清理 Flyway 历史表：`DELETE FROM flyway_schema_history;`

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

[待定]

## 联系方式

作者: shenge08
邮箱: shenge08@example.com

---

**注意**: 本项目目前处于开发阶段，API 和功能可能会有变化。
