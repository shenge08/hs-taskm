#!/bin/bash

# 期货移仓机器人 Docker 启动脚本
# 使用方法: ./docker-run.sh [环境]

# 默认配置
NEAR_SYMBOL=${NEAR_SYMBOL:-"IM2606.CFE"}
FAR_SYMBOL=${FAR_SYMBOL:-"IM2609.CFE"}
TASK_ID=${TASK_ID:-"20260305000001"}
TOTAL_QTY=${TOTAL_QTY:-10}
BATCH_COUNT=${BATCH_COUNT:-5}
MIN_SPREAD=${MIN_SPREAD:-20.0}
ALLOW_SLIPPAGE=${ALLOW_SLIPPAGE:-1.0}
CHASE_PRICE_INCREMENT=${CHASE_PRICE_INCREMENT:-1}

# 可选配置
MAX_CHASE_ATTEMPTS=${MAX_CHASE_ATTEMPTS:-3}
CLOSE_WAIT_TIME=${CLOSE_WAIT_TIME:-5}
OPEN_WAIT_TIME=${OPEN_WAIT_TIME:-5}
LOG_LEVEL=${LOG_LEVEL:-INFO}

# SDK 端点（本地测试用 Mock 服务）
PLUGIN_ENDPOINT=${PLUGIN_ENDPOINT:-"http://mock-plugin:8080/api"}
LISTENER_ENDPOINT=${LISTENER_ENDPOINT:-"http://mock-listener:8080/api"}

echo "启动期货移仓机器人..."
echo "  合约: $NEAR_SYMBOL / $FAR_SYMBOL"
echo "  数量: $TOTAL_QTY 手分 $BATCH_COUNT 批"
echo "  价差: $MIN_SPREAD ± $ALLOW_SLIPPAGE"
echo "  任务ID: $TASK_ID"

docker run -d \
  --name futures-roll-bot \
  --restart on-failure:3 \
  --network taskm-network \
  --memory="1g" \
  --cpus="1.0" \
  -e NEAR_SYMBOL="$NEAR_SYMBOL" \
  -e FAR_SYMBOL="$FAR_SYMBOL" \
  -e TASK_ID="$TASK_ID" \
  -e TOTAL_QTY="$TOTAL_QTY" \
  -e BATCH_COUNT="$BATCH_COUNT" \
  -e MIN_SPREAD="$MIN_SPREAD" \
  -e ALLOW_SLIPPAGE="$ALLOW_SLIPPAGE" \
  -e CHASE_PRICE_INCREMENT="$CHASE_PRICE_INCREMENT" \
  -e MAX_CHASE_ATTEMPTS="$MAX_CHASE_ATTEMPTS" \
  -e CLOSE_WAIT_TIME="$CLOSE_WAIT_TIME" \
  -e OPEN_WAIT_TIME="$OPEN_WAIT_TIME" \
  -e PLUGIN_ENDPOINT="$PLUGIN_ENDPOINT" \
  -e LISTENER_ENDPOINT="$LISTENER_ENDPOINT" \
  -e LOG_LEVEL="$LOG_LEVEL" \
  -v "$(pwd)/logs:/var/log/tasks" \
  futures-roll-bot-taskm:1.0.0

echo "容器已启动，使用以下命令查看日志："
echo "  docker logs -f futures-roll-bot"
echo "  tail -f logs/task_*.log"
