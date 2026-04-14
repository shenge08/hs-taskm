# Docker 启动命令指南

## 1. 本地测试环境

### 方式一：使用启动脚本（推荐）

```bash
# 给脚本添加执行权限
chmod +x docker-run.sh

# 使用默认配置启动
./docker-run.sh

# 自定义配置启动
NEAR_SYMBOL=IM2606.CFE \
FAR_SYMBOL=IM2609.CFE \
TOTAL_QTY=20 \
BATCH_COUNT=10 \
./docker-run.sh
```

### 方式二：直接使用 docker run

```bash
docker run -d \
  --name futures-roll-bot \
  --restart on-failure:3 \
  --network taskm-network \
  --memory="1g" \
  --cpus="1.0" \
  -e NEAR_SYMBOL=IM2606.CFE \
  -e FAR_SYMBOL=IM2609.CFE \
  -e TASK_ID=20260305000001 \
  -e TOTAL_QTY=10 \
  -e BATCH_COUNT=5 \
  -e MIN_SPREAD=20.0 \
  -e ALLOW_SLIPPAGE=1.0 \
  -e CHASE_PRICE_INCREMENT=1 \
  -e MAX_CHASE_ATTEMPTS=3 \
  -e CLOSE_WAIT_TIME=5 \
  -e OPEN_WAIT_TIME=5 \
  -e PLUGIN_ENDPOINT=http://mock-plugin:8080/api \
  -e LISTENER_ENDPOINT=http://mock-listener:8080/api \
  -e LOG_LEVEL=INFO \
  -v "$(pwd)/logs:/var/log/tasks" \
  futures-roll-bot-taskm:1.0.0
```

### 方式三：使用 docker-compose

```bash
# 启动完整环境（包含 Mock 服务）
docker-compose up -d

# 查看日志
docker-compose logs -f futures-roll-bot

# 停止服务
docker-compose down
```

## 2. TaskM 生产环境

TaskM 系统会自动生成并执行类似以下的启动命令：

```bash
docker run -d \
  --name "task-20260305000001" \
  --restart on-failure:3 \
  --network taskm-network \
  --memory="1g" \
  --cpus="1.0" \
  --memory-swap="2g" \
  -p 0:8080 \
  \
  # ========== 任务参数（从 task 表读取）==========
  -e NEAR_SYMBOL=IM2606.CFE \
  -e FAR_SYMBOL=IM2609.CFE \
  -e TASK_ID=20260305000001 \
  -e TOTAL_QTY=10 \
  -e BATCH_COUNT=5 \
  -e MIN_SPREAD=20.0 \
  -e ALLOW_SLIPPAGE=1.0 \
  -e CHASE_PRICE_INCREMENT=1 \
  \
  # ========== 可选参数 ==========
  -e MAX_CHASE_ATTEMPTS=3 \
  -e CLOSE_WAIT_TIME=5 \
  -e OPEN_WAIT_TIME=5 \
  \
  # ========== SDK 端点（TaskM 自动解析并注入）==========
  -e PLUGIN_ENDPOINT=http://plugin-1:8080/instances/market-data-prod/api \
  -e LISTENER_ENDPOINT=http://listener-1:8080/instances/bims-prod/api \
  \
  # ========== 日志配置 ==========
  -e LOG_LEVEL=INFO \
  -e LOG_FORMAT=text \
  -v /var/log/taskm/tasks:/var/log/tasks \
  \
  # ========== 镜像 ==========
  futures-roll-bot-taskm:1.0.0
```

## 3. 环境变量说明

### 必填参数

| 参数 | 说明 | 示例值 | 来源 |
|------|------|--------|------|
| `NEAR_SYMBOL` | 近月合约代码 | IM2606.CFE | 用户输入 |
| `FAR_SYMBOL` | 远月合约代码 | IM2609.CFE | 用户输入 |
| `TASK_ID` | 任务ID | 20260305000001 | TaskM 生成 |
| `TOTAL_QTY` | 总移仓量 | 10 | 用户输入 |
| `BATCH_COUNT` | 批次数量 | 5 | 用户输入 |
| `MIN_SPREAD` | 最小价差 | 20.0 | 用户输入 |
| `ALLOW_SLIPPAGE` | 允许滑点 | 1.0 | 用户输入 |
| `CHASE_PRICE_INCREMENT` | 追单增加点数 | 1 | 用户输入 |

### 可选参数（有默认值）

| 参数 | 说明 | 默认值 | 来源 |
|------|------|--------|------|
| `MAX_CHASE_ATTEMPTS` | 最大追单次数 | 3 | 用户配置 |
| `CLOSE_WAIT_TIME` | 平仓查询等待时间（秒） | 5 | 用户配置 |
| `OPEN_WAIT_TIME` | 开仓查询等待时间（秒） | 5 | 用户配置 |

### SDK 端点（TaskM 自动注入）

| 参数 | 说明 | 示例值 | 来源 |
|------|------|--------|------|
| `PLUGIN_ENDPOINT` | 数据插件 HTTP 端点 | http://plugin-1:8080/instances/prod/api | TaskM 自动生成 |
| `LISTENER_ENDPOINT` | 监听器 HTTP 端点 | http://listener-1:8080/instances/bims/api | TaskM 自动生成 |

## 4. 资源配置建议

### 小规模测试
```bash
--memory="512m"
--cpus="0.5"
```

### 中等规模
```bash
--memory="1g"
--cpus="1.0"
```

### 大规模生产
```bash
--memory="2g"
--cpus="2.0"
--memory-swap="4g"
```

## 5. 日志配置

### 方式一：挂载到宿主机
```bash
-v /var/log/taskm/tasks:/var/log/tasks
```

### 方式二：使用日志驱动
```bash
--log-driver json-file \
--log-opt max-size=10m \
--log-opt max-file=3
```

### 方式三：发送到 syslog
```bash
--log-driver syslog \
--log-opt syslog-address=tcp://192.168.0.42:514
```

## 6. 网络配置

### 连接到 TaskM 网络
```bash
--network taskm-network
```

### 暴露端口（如果需要）
```bash
-p 8080:8080  # 暴露健康检查端口
```

### 自定义 DNS
```bash
--dns 8.8.8.8 --dns 8.8.4.4
```

## 7. 重启策略

### 总是重启
```bash
--restart always
```

### 失败时重启（最多 3 次）
```bash
--restart on-failure:3
```

### 除非手动停止
```bash
--restart unless-stopped
```

## 8. 健康检查

### Dockerfile 中已配置
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD java -cp /app/app.jar com.taskm.sdk.HealthCheck || exit 1
```

### 运行时覆盖
```bash
--health-cmd="java -cp /app/app.jar com.taskm.sdk.HealthCheck" \
--health-interval=30s \
--health-timeout=3s \
--health-retries=3 \
--health-start-period=40s
```

## 9. 查看和管理

### 查看容器状态
```bash
docker ps -a | grep futures-roll-bot
```

### 查看容器日志
```bash
docker logs -f futures-roll-bot
```

### 查看健康状态
```bash
docker inspect --format='{{.State.Health.Status}}' futures-roll-bot
```

### 进入容器调试
```bash
docker exec -it futures-roll-bot sh
```

### 停止容器
```bash
docker stop futures-roll-bot
```

### 删除容器
```bash
docker rm futures-roll-bot
```

### 重启容器
```bash
docker restart futures-roll-bot
```

## 10. 完整示例脚本

```bash
#!/bin/bash
# 完整的生产环境启动脚本

# 配置
IMAGE="futures-roll-bot-taskm:1.0.0"
CONTAINER_NAME="futures-roll-bot-prod"
NETWORK="taskm-network"
LOG_DIR="/var/log/taskm/tasks"

# 启动容器
docker run -d \
  --name "$CONTAINER_NAME" \
  --restart on-failure:3 \
  --network "$NETWORK" \
  --memory="1g" \
  --cpus="1.0" \
  --memory-swap="2g" \
  -p 0:8080 \
  \
  -e NEAR_SYMBOL="IM2606.CFE" \
  -e FAR_SYMBOL="IM2609.CFE" \
  -e TASK_ID="20260305000001" \
  -e TOTAL_QTY="10" \
  -e BATCH_COUNT="5" \
  -e MIN_SPREAD="20.0" \
  -e ALLOW_SLIPPAGE="1.0" \
  -e CHASE_PRICE_INCREMENT="1" \
  -e MAX_CHASE_ATTEMPTS="3" \
  -e CLOSE_WAIT_TIME="5" \
  -e OPEN_WAIT_TIME="5" \
  -e PLUGIN_ENDPOINT="http://plugin-1:8080/instances/prod/api" \
  -e LISTENER_ENDPOINT="http://listener-1:8080/instances/bims/api" \
  -e LOG_LEVEL="INFO" \
  \
  -v "$LOG_DIR:/var/log/tasks" \
  \
  --log-driver json-file \
  --log-opt max-size=10m \
  --log-opt max-file=5 \
  \
  --health-cmd="sh -c 'ps aux | grep java | grep -v grep'" \
  --health-interval=30s \
  --health-timeout=10s \
  --health-retries=3 \
  --health-start-period=40s \
  \
  "$IMAGE"

# 等待容器启动
sleep 5

# 检查状态
echo "容器状态："
docker ps | grep "$CONTAINER_NAME"

echo "健康状态："
docker inspect --format='{{.State.Health.Status}}' "$CONTAINER_NAME"

echo "日志（最近 20 行）："
docker logs --tail 20 "$CONTAINER_NAME"
```
