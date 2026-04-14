package com.futuresroll.broker;

/**
 * 订单结果
 */
public class OrderResult {
    private String orderId;
    private OrderStatus status;
    private int filledQty;
    private double avgPrice;

    public OrderResult(String orderId, OrderStatus status, int filledQty, double avgPrice) {
        this.orderId = orderId;
        this.status = status;
        this.filledQty = filledQty;
        this.avgPrice = avgPrice;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
    public int getFilledQty() { return filledQty; }
    public double getAvgPrice() { return avgPrice; }
}
