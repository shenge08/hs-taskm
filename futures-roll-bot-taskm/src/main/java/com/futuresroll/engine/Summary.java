package com.futuresroll.engine;

/**
 * 执行摘要
 */
public class Summary {
    private final int totalBatches;
    private final int successfulBatches;
    private final int failedBatches;
    private final int totalFilledNear;
    private final int totalFilledFar;

    public Summary(int totalBatches, int successfulBatches, int failedBatches,
                   int totalFilledNear, int totalFilledFar) {
        this.totalBatches = totalBatches;
        this.successfulBatches = successfulBatches;
        this.failedBatches = failedBatches;
        this.totalFilledNear = totalFilledNear;
        this.totalFilledFar = totalFilledFar;
    }

    public int getTotalBatches() { return totalBatches; }
    public int getSuccessfulBatches() { return successfulBatches; }
    public int getFailedBatches() { return failedBatches; }
    public int getTotalFilledNear() { return totalFilledNear; }
    public int getTotalFilledFar() { return totalFilledFar; }
}
