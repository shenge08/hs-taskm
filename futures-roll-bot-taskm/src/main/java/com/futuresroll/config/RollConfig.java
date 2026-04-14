package com.futuresroll.config;

import com.taskm.sdk.Environment;

/**
 * 移仓配置 - 从 TaskM 注入的环境变量读取
 *
 * <p>所有配置参数由 TaskM 系统在创建任务时注入到环境变量中，
 * 使用 TaskM SDK 的 Environment 类统一读取。</p>
 *
 * <h3>任务参数（由 TaskM 注入）：</h3>
 * <ul>
 *   <li>NEAR_SYMBOL - 近月合约代码 (必填)</li>
 *   <li>FAR_SYMBOL - 远月合约代码 (必填)</li>
 *   <li>TOTAL_QTY - 总移仓量 (必填)</li>
 *   <li>BATCH_COUNT - 批次数量 (必填)</li>
 *   <li>MIN_SPREAD - 最小价差 (必填)</li>
 *   <li>ALLOW_SLIPPAGE - 允许滑点 (必填)</li>
 *   <li>CHASE_PRICE_INCREMENT - 追单增加点数 (必填)</li>
 *   <li>MAX_CHASE_ATTEMPTS - 最大追单次数 (默认: 3)</li>
 *   <li>CLOSE_WAIT_TIME - 平仓查询等待时间，秒 (默认: 5)</li>
 *   <li>OPEN_WAIT_TIME - 开仓查询等待时间，秒 (默认: 5)</li>
 * </ul>
 *
 * <h3>SDK 端点（由 TaskM 自动注入）：</h3>
 * <ul>
 *   <li>PLUGIN_ENDPOINT - 数据插件 HTTP 端点</li>
 *   <li>LISTENER_ENDPOINT - 监听器 HTTP 端点</li>
 *   <li>TASK_ID - 任务执行 ID</li>
 * </ul>
 */
public class RollConfig {
    private final String nearSymbol;
    private final String farSymbol;
    private final int totalQty;
    private final int batchCount;
    private final double minSpread;
    private final double allowSlippage;
    private final int maxChaseAttempts;
    private final String taskId;
    private final int closeWaitTime;
    private final int openWaitTime;
    private final int chasePriceIncrement;

    /**
     * 从 TaskM 注入的环境变量创建配置
     *
     * <p>使用 TaskM SDK 的 Environment 类读取环境变量，
     * 所有必填参数如果缺失会抛出 IllegalArgumentException。</p>
     *
     * @throws IllegalArgumentException 如果必填的环境变量未设置或格式错误
     */
    public RollConfig() {
        // 必填参数 - 使用 SDK Environment 类的 getRequired* 方法
        this.nearSymbol = Environment.getRequired("NEAR_SYMBOL").trim();
        this.farSymbol = Environment.getRequired("FAR_SYMBOL").trim();
        this.taskId = Environment.getRequired("TASK_ID").trim();

        this.totalQty = Environment.getRequiredInt("TOTAL_QTY");
        this.batchCount = Environment.getRequiredInt("BATCH_COUNT");
        this.minSpread = Environment.getRequiredDouble("MIN_SPREAD");
        this.allowSlippage = Environment.getRequiredDouble("ALLOW_SLIPPAGE");
        this.chasePriceIncrement = Environment.getRequiredInt("CHASE_PRICE_INCREMENT");

        // 可选参数 - 使用 SDK Environment 类的 get* 方法（带默认值）
        this.maxChaseAttempts = Environment.getInt("MAX_CHASE_ATTEMPTS", 3);
        this.closeWaitTime = Environment.getInt("CLOSE_WAIT_TIME", 5);
        this.openWaitTime = Environment.getInt("OPEN_WAIT_TIME", 5);
    }

    // Getters
    public String getNearSymbol() {
        return nearSymbol;
    }

    public String getFarSymbol() {
        return farSymbol;
    }

    public int getTotalQty() {
        return totalQty;
    }

    public int getBatchCount() {
        return batchCount;
    }

    public double getMinSpread() {
        return minSpread;
    }

    public double getAllowSlippage() {
        return allowSlippage;
    }

    public int getMaxChaseAttempts() {
        return maxChaseAttempts;
    }

    public String getTaskId() {
        return taskId;
    }

    public int getCloseWaitTime() {
        return closeWaitTime;
    }

    public int getOpenWaitTime() {
        return openWaitTime;
    }

    public int getChasePriceIncrement() {
        return chasePriceIncrement;
    }

    /**
     * 获取最低可接受价差
     *
     * @return 最小价差减去允许滑点
     */
    public double getMinAcceptableSpread() {
        return minSpread - allowSlippage;
    }

    @Override
    public String toString() {
        return "RollConfig{" +
                "nearSymbol='" + nearSymbol + '\'' +
                ", farSymbol='" + farSymbol + '\'' +
                ", totalQty=" + totalQty +
                ", batchCount=" + batchCount +
                ", minSpread=" + minSpread +
                ", allowSlippage=" + allowSlippage +
                ", maxChaseAttempts=" + maxChaseAttempts +
                ", taskId='" + taskId + '\'' +
                ", closeWaitTime=" + closeWaitTime +
                ", openWaitTime=" + openWaitTime +
                ", chasePriceIncrement=" + chasePriceIncrement +
                '}';
    }
}
