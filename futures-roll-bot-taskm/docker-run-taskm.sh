#!/bin/bash

# TaskM 生产环境 Docker 启动脚本
# 此脚本展示 TaskM 系统如何启动期货移仓机器人容器

# ============================================
# TaskM 系统生成的配置
# ============================================

# 任务信息（从数据库读取）
TASK_ID="20260305000001"
TASK_NAME="期货移仓策略"

# 策略参数（从 task 表的 parameters 字段读取）
NEAR_SYMBOL="IM2606.CFE"
FAR_SYMBOL="IM2609.CFE"
TOTAL_QTY=10
BATCH_COUNT=5
MIN_SPREAD=20.0
ALLOW_SLIPPAGE=1.0
CHASE_PRICE_INCREMENT=1

# 可选参数（使用默认值或从配置读取）
MAX_CHASE_ATTEMPTS=3
CLOSE_WAIT_TIME=5
OPEN_WAIT_TIME=5

# SDK 端点（TaskM 系统自动解析并注入）
# 根据 plugin_instance 和 listener_instance 表生成
PLUGIN_ENDPOINT="http://plugin-1:8080/instances/market-data-prod/api"
LISTENER_ENDPOINT="http://listener-1:8080/instances/bims-prod/api"

# ============================================
# 容器启动命令
# ============================================

echo "TaskM 启动任务: $TASK_NAME (ID: $TASK_ID)"

docker run -d \
  --name "task-${TASK_ID}" \
  --restart on-failure:3 \
  --network taskm-network \
  --memory="1g" \
  --cpus="1.0" \
  --memory-swap="2g" \
  -p 0:8080 \
  \
  `# ========== 任务参数（必填）==========` \
  -e NEAR_SYMBOL="$NEAR_SYMBOL" \
  -e FAR_SYMBOL="$FAR_SYMBOL" \
  -e TASK_ID="$TASK_ID" \
  -e TOTAL_QTY="$TOTAL_QTY" \
  -e BATCH_COUNT="$BATCH_COUNT" \
  -e MIN_SPREAD="$MIN_SPREAD" \
  -e ALLOW_SLIPPAGE="$ALLOW_SLIPPAGE" \
  -e CHASE_PRICE_INCREMENT="$CHASE_PRICE_INCREMENT" \
  \
  `# ========== 任务参数（可选）==========` \
  -e MAX_CHASE_ATTEMPTS="$MAX_CHASE_ATTEMPTS" \
  -e CLOSE_WAIT_TIME="$CLOSE_WAIT_TIME" \
  -e OPEN_WAIT_TIME="$OPEN_WAIT_TIME" \
  \
  `# ========== SDK 端点（TaskM 自动注入）==========` \
  -e PLUGIN_ENDPOINT="$PLUGIN_ENDPOINT" \
  -e LISTENER_ENDPOINT="$LISTENER_ENDPOINT" \
  \
  `# ========== 日志配置 ==========` \
  -e LOG_LEVEL="INFO" \
  -e LOG_FORMAT="text" \
  -v "/var/log/taskm/tasks:/var/log/tasks" \
  \
  `# ========== 镜像和启动命令 ==========` \
  futures-roll-bot-taskm:1.0.0

echo "任务容器已启动: task-${TASK_ID}"
echo "日志目录: /var/log/taskm/tasks/task_${TASK_ID}.log"
