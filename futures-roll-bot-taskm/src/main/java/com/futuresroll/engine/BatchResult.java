package com.futuresroll.engine;

/**
 * 批次执行结果
 */
public class BatchResult {
    private int batchIdx;
    private boolean success;
    private int filledNear;
    private int filledFar;
    private String error;

    public BatchResult() {}

    public BatchResult(int batchIdx, boolean success, int filledNear, int filledFar) {
        this.batchIdx = batchIdx;
        this.success = success;
        this.filledNear = filledNear;
        this.filledFar = filledFar;
    }

    public BatchResult(int batchIdx, boolean success, int filledNear, int filledFar, String error) {
        this(batchIdx, success, filledNear, filledFar);
        this.error = error;
    }

    // Getters and Setters
    public int getBatchIdx() { return batchIdx; }
    public void setBatchIdx(int batchIdx) { this.batchIdx = batchIdx; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public int getFilledNear() { return filledNear; }
    public void setFilledNear(int filledNear) { this.filledNear = filledNear; }
    
    public int getFilledFar() { return filledFar; }
    public void setFilledFar(int filledFar) { this.filledFar = filledFar; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
