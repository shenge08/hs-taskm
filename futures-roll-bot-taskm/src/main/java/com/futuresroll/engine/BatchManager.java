package com.futuresroll.engine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.futuresroll.executor.BatchResult;
import com.taskm.sdk.Logger;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分批管理器
 */
public class BatchManager {
    private int totalQty;
    private int batchCount;
    private int completedBatches;
    private int remainingBatches;
    private int qtyPerBatch;
    private int lastBatchNumber = 1;
    private AtomicInteger currentBatchSeq = new AtomicInteger(0);
    private String taskId;
    private List<BatchResult> results;
    private Logger logger = new Logger("BatchManager");
    
    public BatchManager(int totalQty, int batchCount) {
        this.totalQty = totalQty;
        this.batchCount = batchCount;
        this.completedBatches = 0;
        this.remainingBatches = batchCount;
        this.qtyPerBatch = totalQty / batchCount;
        this.results = new ArrayList<>();
    }
    
    /**
     * 获取下一批数量
     */
    public int getNextBatchQty() {
        if (isComplete()) {
            return 0;
        }
        
        // 最后一批获得所有剩余
        if (remainingBatches == 1) {
            int completedQty = results.stream()
                .filter(BatchResult::isSuccess)
                .mapToInt(BatchResult::getFilledNear)
                .sum();
            return totalQty - completedQty;
        }
        
        return qtyPerBatch;
    }
    
    /**
     * 记录批次结果
     */
    public void recordResult(BatchResult result) {
        results.add(result);
        if (result.isSuccess()) {
            lastBatchNumber = completedBatches + 1;
            completedBatches++;
            remainingBatches--;
        }
    }
    
    /**
     * 检查是否完成
     */
    public boolean isComplete() {
        return remainingBatches <= 0 || completedBatches >= batchCount;
    }
    
    /**
     * 获取进度信息
     */
    public Progress getProgress() {
        int completedQty = results.stream()
            .filter(BatchResult::isSuccess)
            .mapToInt(BatchResult::getFilledNear)
            .sum();
        
        double progressPct = totalQty > 0 ? (completedQty * 100.0 / totalQty) : 0;
        
        return new Progress(totalQty, completedQty, batchCount, completedBatches, progressPct);
    }
    
    // Getters
    public int getRemainingBatches() { return remainingBatches; }
    public List<BatchResult> getResults() { return results; }

    public String createOrderId() {
        int currentBatchNumber = completedBatches + 1;

        if (currentBatchNumber != lastBatchNumber) {
            currentBatchSeq.set(1);
            lastBatchNumber = currentBatchNumber;
        } else {
            currentBatchSeq.incrementAndGet();
        }

        return taskId + currentBatchNumber +getTimeStr()+ String.format("%04d", currentBatchSeq.get());
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    private String getTimeStr(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
        return now.format(formatter);
    }
}
