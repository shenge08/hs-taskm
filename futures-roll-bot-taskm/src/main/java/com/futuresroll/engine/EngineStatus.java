package com.futuresroll.engine;

/**
 * 引擎状态信息
 */
public class EngineStatus {
    private final EngineState state;
    private final boolean running;
    private final ProgressInfo progress;
    private final int currentBatch;
    private final int totalBatches;

    public EngineStatus(EngineState state, boolean running, ProgressInfo progress,
                        int currentBatch, int totalBatches) {
        this.state = state;
        this.running = running;
        this.progress = progress;
        this.currentBatch = currentBatch;
        this.totalBatches = totalBatches;
    }

    public EngineState getState() { return state; }
    public boolean isRunning() { return running; }
    public ProgressInfo getProgress() { return progress; }
    public int getCurrentBatch() { return currentBatch; }
    public int getTotalBatches() { return totalBatches; }
}
