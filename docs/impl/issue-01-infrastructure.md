# Issue #1: 基础设施和项目搭建实现文档

## 概述

Issue #1 负责 HS-TASKM 项目的基础设施搭建，包括多模块 Maven 项目结构、Spring Boot 应用框架、数据库集成、Docker 客户端配置等核心基础组件。

## 主要实现内容

### 1. 多模块 Maven 项目结构

```
hs-taskm/
├── pom.xml (父 POM)
├── hs-taskm-common/ (公共模块)
│   ├── entity/ (实体类)
│   ├── dto/ (数据传输对象)
│   ├── exception/ (自定义异常)
│   └── handler/ (MyBatis 类型处理器)
├── hs-taskm-dao/ (数据访问层)
│   └── mapper/ (MyBatis Mapper)
├── hs-taskm-service/ (服务层)
│   └── service/ (业务逻辑)
├── hs-taskm-core/ (核心模块)
│   ├── config/ (配置类)
│   ├── service/ (核心服务)
│   └── resources/db/migration/ (Flyway 迁移脚本)
├── hs-taskm-api/ (REST API 层)
│   └── controller/ (REST 控制器)
└── hs-taskm-cli/ (命令行工具)
    └── cli/ (CLI 命令)
```

### 2. 依赖管理 (父 POM)

**核心依赖版本**:
```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.3</spring-boot.version>
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
    <docker-java.version>3.3.6</docker-java.version>
    <picocli.version>4.7.5</picocli.version>
</properties>
```

**依赖管理**:
- Spring Boot 3.2.3
- MyBatis-Plus 3.5.5
- Docker Java Client 3.3.6
- PostgreSQL 15
- Flyway (数据库迁移)
- Lombok (代码生成)
- Picocli (CLI 框架)

### 3. 数据库配置

**application.yml** (hs-taskm-core):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hs_taskm
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 4. 核心配置类

#### Application (主启动类)
**路径**: `hs-taskm-core/src/main/java/com/taskm/Application.java`

```java
@SpringBootApplication
@EnableTransactionManagement
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

#### MyBatis-Plus 配置
**路径**: `hs-taskm-service/src/main/java/com/taskm/config/MybatisPlusConfig.java`

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor =
            new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
```

### 5. 数据库迁移 (Flyway)

**迁移脚本位置**: `hs-taskm-core/src/main/resources/db/migration/`

**示例脚本**: `V1__create_task_table.sql`
```sql
CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    container_id VARCHAR(255),
    parameters JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);
```

### 6. 公共模块设计

#### 实体类 (Entity)
**路径**: `hs-taskm-common/src/main/java/com/taskm/entity/`

```java
@Data
@TableName("task")
public class Task implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long strategyId;
    private String status;
    private String containerId;
    private Map<String, Object> parameters;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
```

#### 自定义异常
**路径**: `hs-taskm-common/src/main/java/com/taskm/exception/`

```java
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}

public class ContainerException extends RuntimeException {
    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

#### JSON 类型处理器
**路径**: `hs-taskm-common/src/main/java/com/taskm/handler/JsonTypeHandler.java`

```java
@MappedTypes({Map.class, Object.class})
public class JsonTypeHandler extends BaseTypeHandler<Object> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
        Object param, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSON.toJSONString(param));
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName)
        throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }
}
```

### 7. REST API 基础架构

#### 统一响应格式
**路径**: `hs-taskm-common/src/main/java/com/taskm/dto/Result.java`

```java
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
```

#### 全局异常处理
**路径**: `hs-taskm-api/src/main/java/com/taskm/exception/GlobalExceptionHandler.java`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Result<?>> handleTaskNotFound(TaskNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Result.error(404, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Result.error(500, "Internal server error"));
    }
}
```

### 8. Docker 客户端配置

**路径**: `hs-taskm-core/src/main/java/com/taskm/config/DockerConfig.java`

```java
@Configuration
public class DockerConfig {

    @Bean
    public DockerClient dockerClient() {
        return DockerClientBuilder.getInstance()
            .withDockerHost("unix:///var/run/docker.sock")
            .build();
    }
}
```

### 9. 测试基础设施

#### Testcontainers 配置
**路径**: `hs-taskm-core/src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:15:///hs_taskm_test
    username: test
    password: test

  testcontainers:
    reuse:
      enable: true
```

## 数据库设计

### 核心表结构

1. **strategy** - 策略表
2. **data_plugin** - 数据插件表
3. **listener** - 监听器表
4. **task** - 任务表
5. **monitor_data** - 监控数据表 (Issue #11)

## 技术栈总结

| 层次 | 技术栈 | 版本 |
|------|--------|------|
| 后端框架 | Spring Boot | 3.2.3 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | PostgreSQL | 15 |
| 数据库迁移 | Flyway | - |
| 容器化 | Docker Java Client | 3.3.6 |
| 测试框架 | JUnit 5 + Testcontainers | - |
| 构建工具 | Maven | 3.9.0 |
| JDK | Java | 17 |

## 配置文件说明

### application.yml (生产环境)
```yaml
server:
  port: 8080

spring:
  application:
    name: hs-taskm

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/hs_taskm}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}

  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false

logging:
  level:
    root: INFO
    com.taskm: ${LOG_LEVEL:DEBUG}
```

### application-test.yml (测试环境)
```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:15:///hs_taskm_test

logging:
  level:
    root: WARN
    com.taskm: DEBUG
```

## 模块依赖关系

```
hs-taskm-api
├── hs-taskm-service
│   ├── hs-taskm-dao
│   │   └── hs-taskm-common
│   └── hs-taskm-common
├── hs-taskm-core
│   ├── hs-taskm-service
│   └── hs-taskm-common
└── hs-taskm-cli
    └── hs-taskm-common
```

## 构建和运行

### Maven 编译
```bash
mvn clean compile
```

### 运行测试
```bash
mvn test
```

### 打包
```bash
mvn clean package
```

### 运行应用
```bash
java -jar hs-taskm-core/target/hs-taskm-core-1.0.0-SNAPSHOT.jar
```

## 相关 Issue

- **后继**: Issue #2 (策略查询 API) - 基于 Issue #1 的基础设施
- **后继**: Issue #5 (任务创建和持久化) - 使用 Issue #1 的数据模型
