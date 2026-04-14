package com.futuresroll.executor;

/**
 * 批次执行结果
 */
public class BatchResult {
    private boolean success;
    private int filledNear;
    private int filledFar;
    private String error;
    
    public BatchResult(boolean success, int filledNear, int filledFar, String error) {
        this.success = success;
        this.filledNear = filledNear;
        this.filledFar = filledFar;
        this.error = error;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public int getFilledNear() { return filledNear; }
    public int getFilledFar() { return filledFar; }
    public String getError() { return error; }
}
