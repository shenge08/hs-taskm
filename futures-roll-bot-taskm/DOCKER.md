# Docker 部署说明

## 构建镜像

```bash
# 在项目根目录执行
docker build -t futures-roll-bot-taskm:1.0.0 .
```

## 运行容器

### 方式一：使用 docker-compose（推荐）

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f futures-roll-bot

# 停止服务
docker-compose down
```

### 方式二：使用 docker run

```bash
docker run -d \
  --name futures-roll-bot \
  --restart on-failure:3 \
  -e NEAR_SYMBOL=IM2606.CFE \
  -e FAR_SYMBOL=IM2609.CFE \
  -e TASK_ID=20260305000001 \
  -e TOTAL_QTY=10 \
  -e BATCH_COUNT=5 \
  -e MIN_SPREAD=20.0 \
  -e ALLOW_SLIPPAGE=1.0 \
  -e CHASE_PRICE_INCREMENT=1 \
  -e PLUGIN_ENDPOINT=http://plugin-container:8080/instances/prod/api \
  -e LISTENER_ENDPOINT=http://listener-container:8080/instances/bims/api \
  -v $(pwd)/logs:/var/log/tasks \
  futures-roll-bot-taskm:1.0.0
```

## 环境变量说明

### 必填环境变量

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `NEAR_SYMBOL` | 近月合约代码 | IM2606.CFE |
| `FAR_SYMBOL` | 远月合约代码 | IM2609.CFE |
| `TASK_ID` | 任务ID | 20260305000001 |
| `TOTAL_QTY` | 总移仓量 | 10 |
| `BATCH_COUNT` | 批次数量 | 5 |
| `MIN_SPREAD` | 最小价差 | 20.0 |
| `ALLOW_SLIPPAGE` | 允许滑点 | 1.0 |
| `CHASE_PRICE_INCREMENT` | 追单增加点数 | 1 |

### 可选环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `MAX_CHASE_ATTEMPTS` | 最大追单次数 | 3 |
| `CLOSE_WAIT_TIME` | 平仓查询等待时间（秒） | 5 |
| `OPEN_WAIT_TIME` | 开仓查询等待时间（秒） | 5 |
| `LOG_LEVEL` | 日志级别 | INFO |

### SDK 端点环境变量（由 TaskM 自动注入）

| 变量名 | 说明 |
|--------|------|
| `PLUGIN_ENDPOINT` | 数据插件端点 |
| `LISTENER_ENDPOINT` | 监听器端点 |

## 日志

日志文件位于容器内的 `/var/log/tasks/` 目录，通过 volume 挂载到宿主机：

```bash
# 查看日志
tail -f logs/task_*.log

# 或使用 docker logs
docker logs -f futures-roll-bot
```

## 健康检查

容器包含健康检查，每 30 秒检查一次：

```bash
# 查看健康状态
docker inspect --format='{{.State.Health.Status}}' futures-roll-bot
```

## 资源限制

可以在 `docker run` 时添加资源限制：

```bash
docker run -d \
  --name futures-roll-bot \
  --memory="1g" \
  --cpus="1.0" \
  ... # 其他参数
  futures-roll-bot-taskm:1.0.0
```

## 故障排查

### 1. 容器启动失败

```bash
# 查看容器日志
docker logs futures-roll-bot

# 检查环境变量
docker inspect futures-roll-bot | grep -A 20 "Env"
```

### 2. 连接插件/监听器失败

确认 `PLUGIN_ENDPOINT` 和 `LISTENER_ENDPOINT` 环境变量已正确设置，且网络可达。

### 3. 权限问题

确保日志挂载目录有写权限：

```bash
chmod 777 logs/
```

## 生产环境建议

1. **使用具体版本号**：不要使用 `latest` 标签
2. **配置资源限制**：防止容器占用过多资源
3. **日志轮转**：配置 logrotate 或使用日志收集系统
4. **监控告警**：配置容器健康检查和告警
5. **数据持久化**：确保日志数据正确挂载
