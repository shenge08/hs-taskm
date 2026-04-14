package com.futuresroll.executor;

/**
 * 价差检查结果
 */
public class SpreadCheckResult {
    private final boolean ok;
    private final double currentSpread;

    public SpreadCheckResult(boolean ok, double currentSpread) {
        this.ok = ok;
        this.currentSpread = currentSpread;
    }

    public boolean isOk() { return ok; }
    public double getCurrentSpread() { return currentSpread; }
}
