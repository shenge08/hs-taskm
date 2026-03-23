# HS-TASKM API 文档访问指南

## 概述

HS-TASKM 使用 **SpringDoc OpenAPI** 自动生成 REST API 文档，提供交互式的 Swagger UI 界面。

## 访问地址

### 1. Swagger UI（交互式文档）

启动应用后，在浏览器中访问：

```
http://localhost:8080/swagger-ui.html
```

**功能**：
- 📖 查看所有 REST API 接口
- 🔄 在线测试 API（Try it out）
- 📋 查看请求/响应示例
- 🏷️ 按功能模块分组查看

### 2. OpenAPI JSON

获取 OpenAPI 3.0 规范的 JSON 格式：

```
http://localhost:8080/api-docs
```

用于：
- 生成客户端 SDK
- 集成到其他 API 管理平台
- 自动化测试

### 3. Actuator 健康检查

```
http://localhost:8080/actuator/health
```

## API 分组

文档按功能模块分为以下分组：

### 1. 策略管理
- `GET /api/strategies` - 获取策略列表
- `POST /api/strategies` - 创建策略
- `GET /api/strategies/{id}` - 获取策略详情
- `PUT /api/strategies/{id}` - 更新策略
- `DELETE /api/strategies/{id}` - 删除策略

### 2. 插件管理
- `GET /api/plugins` - 获取插件列表
- `POST /api/plugins` - 创建插件
- `GET /api/plugins/{id}` - 获取插件详情
- `PUT /api/plugins/{id}` - 更新插件
- `DELETE /api/plugins/{id}` - 删除插件

### 3. 插件实例管理
- `GET /api/plugins/{pluginId}/instances` - 获取实例列表
- `POST /api/plugins/{pluginId}/instances` - 创建实例
- `DELETE /api/plugin-instances/{instanceId}` - 删除实例

### 4. 插件容器管理
- `POST /api/plugin-containers/{pluginId}/start` - 启动容器
- `POST /api/plugin-containers/{pluginId}/stop` - 停止容器
- `GET /api/plugin-containers/{pluginId}/status` - 查询状态

### 5. 监听器管理
- `GET /api/listeners` - 获取监听器列表
- `POST /api/listeners` - 创建监听器
- `GET /api/listeners/{id}` - 获取监听器详情
- `PUT /api/listeners/{id}` - 更新监听器
- `DELETE /api/listeners/{id}` - 删除监听器

### 6. 监听器实例管理
- `GET /api/listeners/{listenerId}/instances` - 获取实例列表
- `POST /api/listeners/{listenerId}/instances` - 创建实例
- `DELETE /api/listener-instances/{instanceId}` - 删除实例

### 7. 监听器容器管理
- `POST /api/listener-containers/{listenerId}/start` - 启动容器
- `POST /api/listener-containers/{listenerId}/stop` - 停止容器
- `GET /api/listener-containers/{listenerId}/status` - 查询状态

### 8. 任务管理
- `GET /api/tasks` - 获取任务列表
- `POST /api/tasks` - 创建任务
- `GET /api/tasks/{id}` - 获取任务详情
- `POST /api/tasks/{id}/start` - 启动任务
- `POST /api/tasks/{id}/stop` - 停止任务
- `GET /api/tasks/{id}/logs` - 获取任务日志
- `GET /api/tasks/{id}/logs/tail` - 获取日志尾部
- `GET /api/tasks/{id}/logs/search` - 搜索日志
- `GET /api/tasks/{id}/metrics` - 获取资源监控数据

## 使用 Swagger UI

### 1. 查看接口

1. 打开 `http://localhost:8080/swagger-ui.html`
2. 选择分组（如"策略管理"、"插件管理"）
3. 展开接口查看详情

### 2. 测试接口（Try it out）

1. 点击接口展开详情
2. 点击 "Try it out" 按钮
3. 填写请求参数
4. 点击 "Execute" 执行请求
5. 查看响应结果

**示例：创建策略**

```bash
# 请求
POST /api/strategies

# 请求体
{
  "name": "Test Strategy",
  "language": "python",
  "code": "print('Hello World')",
  "description": "Test strategy"
}

# 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "Test Strategy",
    "language": "python",
    ...
  }
}
```

### 3. 查看数据模型

Swagger UI 自动显示：
- 请求参数类型和说明
- 请求体示例（Schema）
- 响应数据结构
- 状态码说明

## 配置说明

### application.yml 中的 Swagger 配置

```yaml
springdoc:
  api-docs:
    enabled: true              # 启用 API 文档
    path: /api-docs            # API 文档路径
  swagger-ui:
    enabled: true              # 启用 Swagger UI
    path: /swagger-ui.html     # Swagger UI 路径
    default-models-expand-depth: 2  # 默认展开层级
    try-it-out-enabled: true   # 启用在线测试
  group-config:
    enabled: true              # 启用分组
```

### SwaggerConfig.java 配置类

**路径**: `hs-taskm-api/src/main/java/com/taskm/config/SwaggerConfig.java`

提供了 4 个 API 分组：
- 策略管理
- 插件管理
- 监听器管理
- 任务管理

## 开发环境 vs 生产环境

### 开发环境

默认启用 Swagger UI，方便调试和测试。

### 生产环境

**方式一：保持启用**（推荐用于内部系统）

生产环境可以继续使用 Swagger UI，方便调试。

**方式二：禁用 Swagger UI**

在生产环境配置文件 `application-prod.yml` 中：

```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

**方式三：仅允许内网访问**

通过 Nginx 或防火墙限制访问：

```nginx
# Nginx 配置
location /swagger-ui.html {
    allow 192.168.1.0/24;  # 仅允许内网访问
    deny all;
}
```

## 其他 API 文档工具

### 1. Postman Collection

导出 Postman Collection：

```bash
curl http://localhost:8080/api-docs -o openapi.json
```

然后在 Postman 中导入 `openapi.json`。

### 2. 生成客户端 SDK

使用 OpenAPI Generator：

```bash
# 生成 Java 客户端
java -jar openapi-generator-cli.jar generate \
  -i http://localhost:8080/api-docs \
  -g java \
  -o client-sdk/

# 生成 TypeScript 客户端
java -jar openapi-generator-cli.jar generate \
  -i http://localhost:8080/api-docs \
  -g typescript-axios \
  -o client-sdk/
```

### 3. 集成到 API 管理平台

支持的平台：
- Apigee
- Kong
- Azure API Management
- AWS API Gateway

## 故障排查

### 问题 1: 无法访问 Swagger UI

**检查应用是否启动**：
```bash
curl http://localhost:8080/actuator/health
```

**检查依赖**：
```bash
mvn dependency:tree | grep springdoc
```

### 问题 2: 分组不显示

检查 `SwaggerConfig.java` 中的分组配置是否正确。

### 问题 3: Try it out 失败

检查：
1. 应用是否正常运行
2. 请求参数是否正确
3. 浏览器控制台是否有 CORS 错误

## 最佳实践

### 1. 注释你的 API

在 Controller 中添加详细注释：

```java
/**
 * 创建新策略
 *
 * @param strategy 策略信息
 * @return 创建的策略
 * @throws InvalidCodeException 当代码为空时抛出
 */
@PostMapping
public Result<Strategy> createStrategy(@RequestBody Strategy strategy) {
    // ...
}
```

### 2. 使用 API 注解

```java
@Operation(summary = "创建策略", description = "创建一个新的交易策略")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "创建成功"),
    @ApiResponse(responseCode = "400", description = "参数错误")
})
@PostMapping
public Result<Strategy> createStrategy(@RequestBody Strategy strategy) {
    // ...
}
```

### 3. 保持文档更新

- 每次修改 API 后更新文档注释
- 及时删除废弃的接口
- 保持示例数据的准确性

## 相关链接

- [SpringDoc 官方文档](https://springdoc.org/)
- [OpenAPI 规范](https://swagger.io/specification/)
- [Swagger UI 使用指南](https://swagger.io/tools/swagger-ui/)

---

**文档版本**: 1.0
**最后更新**: 2026-03-23
