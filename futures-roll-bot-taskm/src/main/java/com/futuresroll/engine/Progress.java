package com.futuresroll.engine;

/**
 * 进度信息
 */
public class Progress {
    private int totalQty;
    private int completedQty;
    private int totalBatches;
    private int completedBatches;
    private double progressPct;
    
    public Progress(int totalQty, int completedQty, int totalBatches, int completedBatches, double progressPct) {
        this.totalQty = totalQty;
        this.completedQty = completedQty;
        this.totalBatches = totalBatches;
        this.completedBatches = completedBatches;
        this.progressPct = progressPct;
    }
    
    // Getters
    public int getTotalQty() { return totalQty; }
    public int getCompletedQty() { return completedQty; }
    public int getTotalBatches() { return totalBatches; }
    public int getCompletedBatches() { return completedBatches; }
    public double getProgressPct() { return progressPct; }
}
