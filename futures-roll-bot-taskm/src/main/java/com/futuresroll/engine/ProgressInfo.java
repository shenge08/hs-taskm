package com.futuresroll.engine;

/**
 * 进度信息
 */
public class ProgressInfo {
    private final int totalQty;
    private final int completedQty;
    private final int remainingQty;
    private final int batchCount;
    private final int completedBatches;
    private final int remainingBatches;
    private final double progressPct;
    private final boolean complete;

    public ProgressInfo(int totalQty, int completedQty, int remainingQty,
                        int batchCount, int completedBatches, int remainingBatches,
                        double progressPct, boolean complete) {
        this.totalQty = totalQty;
        this.completedQty = completedQty;
        this.remainingQty = remainingQty;
        this.batchCount = batchCount;
        this.completedBatches = completedBatches;
        this.remainingBatches = remainingBatches;
        this.progressPct = progressPct;
        this.complete = complete;
    }

    // Getters
    public int getTotalQty() { return totalQty; }
    public int getCompletedQty() { return completedQty; }
    public int getRemainingQty() { return remainingQty; }
    public int getBatchCount() { return batchCount; }
    public int getCompletedBatches() { return completedBatches; }
    public int getRemainingBatches() { return remainingBatches; }
    public double getProgressPct() { return progressPct; }
    public boolean isComplete() { return complete; }
}
