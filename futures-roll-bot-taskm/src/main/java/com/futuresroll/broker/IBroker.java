package com.futuresroll.broker;

import com.futuresroll.config.RollConfig;

/**
 * 经纪商接口
 */
public interface IBroker {
    
    /**
     * 连接
     */
    boolean connect();
    
    /**
     * 断开连接
     */
    void disconnect();
    
    /**
     * 获取行情数据
     */
    MarketData getMarketData(String symbol);
    
    /**
     * 下限价单
     */
    OrderResult placeLimitOrder(String symbol, Direction direction, int qty, double price,String orderId);
    
    /**
     * 下市价单
     */
    OrderResult placeMarketOrder(String symbol, Direction direction, int qty,String orderId);



    /**
     * 取消下单
     */
    Boolean cancelOrder(String symbol, Direction direction, int qty,String orderId);


    /**
     * 查询成交数量
     */
    BimsOrderDetail getOrderSuccessCount(String orderId);

    void setRollConfig(RollConfig config);
}
