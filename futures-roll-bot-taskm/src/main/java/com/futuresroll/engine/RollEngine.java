package com.futuresroll.engine;

import com.futuresroll.broker.IBroker;
import com.futuresroll.broker.MockBroker;
import com.futuresroll.config.RollConfig;
import com.futuresroll.executor.BatchResult;
import com.futuresroll.executor.OrderExecutor;
import com.taskm.sdk.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 移仓引擎主控
 */
public class RollEngine {
    private RollConfig config;
    private IBroker broker;
    private BatchManager batchManager;
    private OrderExecutor executor;
    private Logger logger = new Logger("RollEngine");

    private boolean running = false;
    private EngineState state = EngineState.IDLE;
    
    public enum EngineState {
        IDLE, RUNNING, COMPLETED, ERROR
    }
    
    public RollEngine(RollConfig config) {
        this.config = config;
        this.broker = new MockBroker();
        broker.setRollConfig(config);
        this.batchManager = new BatchManager(config.getTotalQty(), config.getBatchCount());
        batchManager.setTaskId(config.getTaskId());
        this.executor = new OrderExecutor(broker, config);
    }
    
    /**
     * 初始化
     */
    public boolean initialize() {
        try {
            broker.connect();
            state = EngineState.IDLE;
            logger.info("引擎初始化完成");
            return true;
        } catch (Exception e) {
            state = EngineState.ERROR;
            logger.error("初始化失败", e);
            return false;
        }
    }
    
    /**
     * 启动移仓
     */
    public void start() {
        if (!initialize()) {
            return;
        }

        running = true;
        state = EngineState.RUNNING;

        Map<String, Object> startData = new HashMap<>();
        startData.put("总数量", config.getTotalQty());
        startData.put("批次", config.getBatchCount());
        logger.info("开始移仓", startData);

        while (running && !batchManager.isComplete()) {
            int qty = batchManager.getNextBatchQty();
            if (qty <= 0) break;

            // 执行一批
            BatchResult result = executor.executeBatch(qty,batchManager.createOrderId());
            batchManager.recordResult(result);

            if (result.isSuccess()) {
                Map<String, Object> batchData = new HashMap<>();
                batchData.put("平仓", result.getFilledNear() + "手");
                batchData.put("开仓", result.getFilledFar() + "手");
                logger.info("批次完成", batchData);

                if (batchManager.isComplete()) {
                    state = EngineState.COMPLETED;
                    logger.info("全部完成");
                    break;
                }

                // 批次间隔
                try {
                    Thread.sleep(100); // 简化，实际应该用配置中的间隔
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                logger.error("批次失败", "error", result.getError());
            }
        }

        cleanup();
    }
    
    /**
     * 停止
     */
    public void stop() {
        running = false;
        logger.info("引擎停止");
    }
    
    /**
     * 获取状态
     */
    public Progress getProgress() {
        return batchManager.getProgress();
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        broker.disconnect();
        running = false;
    }
    
    // Getters
    public EngineState getState() { return state; }
    public boolean isRunning() { return running; }
}
