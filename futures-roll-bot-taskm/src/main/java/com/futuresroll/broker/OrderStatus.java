package com.futuresroll.broker;

/**
 * 订单状态
 */
public enum OrderStatus {
    PENDING,        // 待成交
    PARTIAL_FILLED, // 部分成交
    FILLED,         // 完全成交
    CANCELLED,      // 已撤销
    REJECTED        // 被拒绝
}
