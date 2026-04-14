package com.futuresroll.executor;

/**
 * 平仓结果
 */
public class CloseResult {
    private final boolean success;
    private final int filled;
    private final double avgPrice;
    private final String error;

    public CloseResult(boolean success, int filled, double avgPrice, String error) {
        this.success = success;
        this.filled = filled;
        this.avgPrice = avgPrice;
        this.error = error;
    }

    public boolean isSuccess() { return success; }
    public int getFilled() { return filled; }
    public double getAvgPrice() { return avgPrice; }
    public String getError() { return error; }
}
