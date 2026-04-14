# 期货移仓机器人 - TaskM SDK 集成版

期货移仓策略程序的 TaskM SDK 集成版本，支持从环境变量读取配置。

## 环境变量配置

### 必需的环境变量

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `NEAR_SYMBOL` | 近月合约代码 | `IM2606.CFE` |
| `FAR_SYMBOL` | 远月合约代码 | `IM2609.CFE` |
| `TASK_ID` | 任务ID | `20260305000001` |

### 可选的环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `TOTAL_QTY` | 总移仓量（手） | `10` |
| `BATCH_COUNT` | 批次数量 | `5` |
| `MIN_SPREAD` | 最小价差 | `20.0` |
| `ALLOW_SLIPPAGE` | 允许滑点 | `1.0` |
| `MAX_CHASE_ATTEMPTS` | 最大追单次数 | `3` |
| `CLOSE_WAIT_TIME` | 平仓查询等待时间（秒） | `5` |
| `OPEN_WAIT_TIME` | 开仓查询等待时间（秒） | `5` |
| `CHASE_PRICE_INCREMENT` | 追单增加点数 | `1` |

## 使用方法

### 编译项目

```bash
cd /Users/shenge08/Documents/claude-project/hs-taskm/futures-roll-bot-taskm
mvn clean package
```

### 运行

#### 方式1：命令行设置环境变量

```bash
NEAR_SYMBOL=IM2606.CFE \
FAR_SYMBOL=IM2609.CFE \
TASK_ID=20260305000001 \
TOTAL_QTY=6 \
BATCH_COUNT=6 \
MIN_SPREAD=203.0 \
java -jar target/futures-roll-bot-taskm-1.0.0.jar
```

#### 方式2：export 环境变量

```bash
export NEAR_SYMBOL=IM2606.CFE
export FAR_SYMBOL=IM2609.CFE
export TASK_ID=20260305000001
export TOTAL_QTY=6
export BATCH_COUNT=6

java -jar target/futures-roll-bot-taskm-1.0.0.jar
```

#### 方式3：在 TaskM 中运行

将此程序作为 TaskM 的策略运行，环境变量由 TaskM 自动注入。

## 配置示例

```bash
# 基本配置
NEAR_SYMBOL=IM2606.CFE     # 近月合约
FAR_SYMBOL=IM2609.CFE      # 远月合约
TASK_ID=20260305000001     # 任务ID

# 数量配置
TOTAL_QTY=6                # 总共移仓6手
BATCH_COUNT=6              # 分6批执行（每批1手）

# 价差配置
MIN_SPREAD=203.0           # 最小价差203点
ALLOW_SLIPPAGE=1.0         # 允许滑点1点

# 追单配置
MAX_CHASE_ATTEMPTS=3       # 最多追单3次
CHASE_PRICE_INCREMENT=1    # 每次追单增加1点

# 等待时间
CLOSE_WAIT_TIME=5          # 平仓后等待5秒查询
OPEN_WAIT_TIME=5           # 开仓后等待5秒查询
```

## 项目结构

```
futures-roll-bot-taskm/
├── src/main/java/com/futuresroll/
│   ├── Main.java              # 程序入口
│   ├── config/
│   │   └── RollConfig.java    # 配置类（从环境变量读取）
│   ├── engine/                # 引擎核心
│   ├── executor/              # 订单执行器
│   └── broker/                # 券商接口
├── src/main/resources/
│   └── logback.xml           # 日志配置
├── pom.xml                   # Maven 配置
└── README.md                 # 本文件
```

## 依赖

- TaskM SDK (taskm-sdk-java)
- Hutool 工具包
- Jackson JSON 库
- SLF4J + Logback

## 特性

- ✅ 从环境变量读取配置
- ✅ 使用 TaskM SDK Logger 记录日志
- ✅ 参数校验和错误提示
- ✅ 支持默认值
- ✅ 详细的帮助信息

## 与原版区别

原版（futures-roll-bot-sdlc）：
- 配置硬编码在 Main.java 中
- 使用 System.out 输出日志

TaskM 版（futures-roll-bot-taskm）：
- 配置从环境变量读取
- 使用 TaskM SDK Logger 记录日志
- 更好的错误处理和提示
- 适合集成到 TaskM 系统
