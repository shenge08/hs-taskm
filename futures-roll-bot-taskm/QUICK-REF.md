# Docker 启动命令快速参考

## 🚀 快速启动

### 本地测试
```bash
./docker-run.sh
```

### 生产环境（TaskM）
```bash
./docker-run-taskm.sh
```

### Docker Compose
```bash
docker-compose up -d
```

## 📝 必填环境变量

```bash
-e NEAR_SYMBOL=IM2606.CFE \
-e FAR_SYMBOL=IM2609.CFE \
-e TASK_ID=20260305000001 \
-e TOTAL_QTY=10 \
-e BATCH_COUNT=5 \
-e MIN_SPREAD=20.0 \
-e ALLOW_SLIPPAGE=1.0 \
-e CHASE_PRICE_INCREMENT=1
```

## 🔗 SDK 端点（TaskM 自动注入）

```bash
-e PLUGIN_ENDPOINT=http://plugin-1:8080/instances/prod/api \
-e LISTENER_ENDPOINT=http://listener-1:8080/instances/bims/api
```

## 📊 资源配置

```bash
--memory="1g" \
--cpus="1.0" \
--restart on-failure:3
```

## 📁 日志挂载

```bash
-v /var/log/taskm/tasks:/var/log/tasks
```

## 🔍 常用命令

```bash
# 查看日志
docker logs -f futures-roll-bot

# 查看健康状态
docker inspect --format='{{.State.Health.Status}}' futures-roll-bot

# 进入容器
docker exec -it futures-roll-bot sh

# 重启容器
docker restart futures-roll-bot

# 停止并删除
docker stop futures-roll-bot && docker rm futures-roll-bot
```

## 🏷️ 镜像标签

```bash
futures-roll-bot-taskm:1.0.0
```

## 🌐 网络配置

```bash
--network taskm-network
```
