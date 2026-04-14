package com.futuresroll.executor;

/**
 * 批次执行结果
 */
public class BatchExecuteResult {
    private final boolean success;
    private final int filledNear;
    private final int filledFar;
    private final double farAvgPrice;
    private final double actualSpread;
    private final boolean usedMarket;
    private final int chaseAttempts;
    private final String error;

    // 简化构造函数
    public BatchExecuteResult(boolean success, int filledNear, int filledFar, String error) {
        this(success, filledNear, filledFar, 0, 0, false, 0, error);
    }

    // 完整构造函数
    public BatchExecuteResult(boolean success, int filledNear, int filledFar,
                               double farAvgPrice, double actualSpread,
                               boolean usedMarket, int chaseAttempts) {
        this(success, filledNear, filledFar, farAvgPrice, actualSpread, 
             usedMarket, chaseAttempts, null);
    }

    private BatchExecuteResult(boolean success, int filledNear, int filledFar,
                                double farAvgPrice, double actualSpread,
                                boolean usedMarket, int chaseAttempts, String error) {
        this.success = success;
        this.filledNear = filledNear;
        this.filledFar = filledFar;
        this.farAvgPrice = farAvgPrice;
        this.actualSpread = actualSpread;
        this.usedMarket = usedMarket;
        this.chaseAttempts = chaseAttempts;
        this.error = error;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public int getFilledNear() { return filledNear; }
    public int getFilledFar() { return filledFar; }
    public double getFarAvgPrice() { return farAvgPrice; }
    public double getActualSpread() { return actualSpread; }
    public boolean isUsedMarket() { return usedMarket; }
    public int getChaseAttempts() { return chaseAttempts; }
    public String getError() { return error; }
}
