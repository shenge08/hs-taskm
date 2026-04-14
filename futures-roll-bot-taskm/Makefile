.PHONY: help build run stop logs clean test

# 默认目标
help:
	@echo "可用命令:"
	@echo "  make build    - 构建 Docker 镜像"
	@echo "  make run      - 运行容器"
	@echo "  make stop     - 停止容器"
	@echo "  make logs     - 查看日志"
	@echo "  make clean    - 清理容器和镜像"
	@echo "  make test     - 运行单元测试"
	@echo "  make package  - 打包 JAR 文件"

# 构建 Docker 镜像
build:
	@echo "构建 Docker 镜像..."
	docker build -t futures-roll-bot-taskm:1.0.0 .
	@echo "构建完成"

# 运行容器
run:
	@echo "启动容器..."
	docker run -d \
		--name futures-roll-bot \
		--restart on-failure:3 \
		-e NEAR_SYMBOL=${NEAR_SYMBOL:-IM2606.CFE} \
		-e FAR_SYMBOL=${FAR_SYMBOL:-IM2609.CFE} \
		-e TASK_ID=${TASK_ID:-20260305000001} \
		-e TOTAL_QTY=${TOTAL_QTY:-10} \
		-e BATCH_COUNT=${BATCH_COUNT:-5} \
		-e MIN_SPREAD=${MIN_SPREAD:-20.0} \
		-e ALLOW_SLIPPAGE=${ALLOW_SLIPPAGE:-1.0} \
		-e CHASE_PRICE_INCREMENT=${CHASE_PRICE_INCREMENT:-1} \
		-e MAX_CHASE_ATTEMPTS=${MAX_CHASE_ATTEMPTS:-3} \
		-e CLOSE_WAIT_TIME=${CLOSE_WAIT_TIME:-5} \
		-e OPEN_WAIT_TIME=${OPEN_WAIT_TIME:-5} \
		-e LOG_LEVEL=${LOG_LEVEL:-INFO} \
		-v $(PWD)/logs:/var/log/tasks \
		futures-roll-bot-taskm:1.0.0
	@echo "容器已启动，使用 'make logs' 查看日志"

# 停止容器
stop:
	@echo "停止容器..."
	docker stop futures-roll-bot || true
	docker rm futures-roll-bot || true
	@echo "容器已停止"

# 查看日志
logs:
	docker logs -f futures-roll-bot

# 清理
clean: stop
	@echo "清理镜像..."
	docker rmi futures-roll-bot-taskm:1.0.0 || true
	@echo "清理完成"

# 运行测试
test:
	@echo "运行单元测试..."
	mvn test

# 打包 JAR
package:
	@echo "打包 JAR 文件..."
	mvn clean package -DskipTests
	@echo "打包完成，JAR 文件位于 target/ 目录"
