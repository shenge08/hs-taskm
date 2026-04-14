package com.futuresroll.executor;

/**
 * 开仓结果
 */
public class OpenResult {
    private final int filled;
    private final double avgPrice;
    private final boolean usedMarket;
    private final int chaseAttempts;

    public OpenResult(int filled, double avgPrice, boolean usedMarket, int chaseAttempts) {
        this.filled = filled;
        this.avgPrice = avgPrice;
        this.usedMarket = usedMarket;
        this.chaseAttempts = chaseAttempts;
    }

    public int getFilled() { return filled; }
    public double getAvgPrice() { return avgPrice; }
    public boolean isUsedMarket() { return usedMarket; }
    public int getChaseAttempts() { return chaseAttempts; }
}
