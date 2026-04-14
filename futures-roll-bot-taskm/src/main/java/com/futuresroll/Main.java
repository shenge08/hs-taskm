package com.futuresroll;

import com.futuresroll.config.RollConfig;
import com.futuresroll.engine.Progress;
import com.futuresroll.engine.RollEngine;
import com.taskm.sdk.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 程序入口 - TaskM 版本
 *
 * <p>使用环境变量配置，支持的环境变量：</p>
 * <ul>
 *   <li>NEAR_SYMBOL - 近月合约代码 (必填)</li>
 *   <li>FAR_SYMBOL - 远月合约代码 (必填)</li>
 *   <li>TASK_ID - 任务ID (必填)</li>
 *   <li>TOTAL_QTY - 总移仓量 (默认: 10)</li>
 *   <li>BATCH_COUNT - 批次数量 (默认: 5)</li>
 *   <li>MIN_SPREAD - 最小价差 (默认: 20.0)</li>
 *   <li>ALLOW_SLIPPAGE - 允许滑点 (默认: 1.0)</li>
 *   <li>MAX_CHASE_ATTEMPTS - 最大追单次数 (默认: 3)</li>
 *   <li>CLOSE_WAIT_TIME - 平仓查询等待时间，秒 (默认: 5)</li>
 *   <li>OPEN_WAIT_TIME - 开仓查询等待时间，秒 (默认: 5)</li>
 *   <li>CHASE_PRICE_INCREMENT - 追单增加点数 (默认: 1)</li>
 * </ul>
 */
public class Main {

    private static final Logger logger = new Logger("FuturesRollBot");

    public static void main(String[] args) {
        printBanner();

        try {
            // 从环境变量创建配置
            logger.info("从环境变量加载配置...");
            RollConfig config = new RollConfig();

            Map<String, Object> configData = new HashMap<>();
            configData.put("合约", config.getNearSymbol() + "/" + config.getFarSymbol());
            configData.put("数量", config.getTotalQty() + "手分" + config.getBatchCount() + "批");
            configData.put("价差", config.getMinSpread() + "±" + config.getAllowSlippage());
            configData.put("任务ID", config.getTaskId());
            logger.info("配置加载成功", configData);

            // 创建并启动引擎
            logger.info("启动移仓引擎...");
            RollEngine engine = new RollEngine(config);
            engine.start();

            // 打印结果
            Progress progress = engine.getProgress();
            Map<String, Object> summaryData = new HashMap<>();
            summaryData.put("总批次", progress.getTotalBatches());
            summaryData.put("完成", progress.getCompletedBatches());
            summaryData.put("平仓", progress.getCompletedQty() + "手");
            logger.info("执行摘要", summaryData);

        } catch (IllegalArgumentException e) {
            logger.error("配置错误", "error", e.getMessage());
            System.err.println("配置错误: " + e.getMessage());
            printEnvHelp();
            System.exit(1);
        } catch (Exception e) {
            logger.error("执行失败", e);
            System.err.println("执行失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      期货移仓机器人 Futures Roll Bot     ║");
        System.out.println("║        TaskM SDK Integration v1.0.0      ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printEnvHelp() {
        System.out.println("\n必需的环境变量:");
        System.out.println("  NEAR_SYMBOL           - 近月合约代码，例如: IM2606.CFE");
        System.out.println("  FAR_SYMBOL            - 远月合约代码，例如: IM2609.CFE");
        System.out.println("  TASK_ID               - 任务ID，例如: 20260305000001");
        System.out.println("  TOTAL_QTY             - 总移仓量，例如: 10");
        System.out.println("  BATCH_COUNT           - 批次数量，例如: 5");
        System.out.println("  MIN_SPREAD            - 最小价差，例如: 20.0");
        System.out.println("  ALLOW_SLIPPAGE        - 允许滑点，例如: 1.0");
        System.out.println("  CHASE_PRICE_INCREMENT - 追单增加点数，例如: 1");
        System.out.println("\n可选的环境变量:");
        System.out.println("  MAX_CHASE_ATTEMPTS - 最大追单次数 (默认: 3)");
        System.out.println("  CLOSE_WAIT_TIME    - 平仓查询等待时间，秒 (默认: 5)");
        System.out.println("  OPEN_WAIT_TIME     - 开仓查询等待时间，秒 (默认: 5)");
        System.out.println("\n示例:");
        System.out.println("  NEAR_SYMBOL=IM2606.CFE \\");
        System.out.println("  FAR_SYMBOL=IM2609.CFE \\");
        System.out.println("  TASK_ID=20260305000001 \\");
        System.out.println("  TOTAL_QTY=6 \\");
        System.out.println("  BATCH_COUNT=6 \\");
        System.out.println("  MIN_SPREAD=20.0 \\");
        System.out.println("  ALLOW_SLIPPAGE=1.0 \\");
        System.out.println("  CHASE_PRICE_INCREMENT=1 \\");
        System.out.println("  java -jar futures-roll-bot-taskm.jar");
    }
}
