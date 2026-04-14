package com.futuresroll.broker;

import com.taskm.sdk.DataPluginClient;
import com.taskm.sdk.ListenerClient;
import com.taskm.sdk.Logger;
import com.futuresroll.config.RollConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 经纪商实现 - 通过数据插件和监听器调用外部接口
 */
public class MockBroker implements IBroker {
    private final Logger logger = new Logger("MockBroker");
    private AtomicInteger orderCounter = new AtomicInteger(0);
    private boolean connected = false;
    private RollConfig rollConfig;
    private DataPluginClient dataPluginClient;
    private ListenerClient listenerClient;

    public void setRollConfig(RollConfig rollConfig) {
        this.rollConfig = rollConfig;
    }

    @Override
    public boolean connect() {
        try {
            // DataPluginClient 需要环境变量 PLUGIN_ENDPOINT
            // ListenerClient 需要环境变量 LISTENER_ENDPOINT
            dataPluginClient = new DataPluginClient();
            listenerClient = new ListenerClient();
            connected = true;
            logger.info("经纪商连接成功");
            return true;
        } catch (Exception e) {
            logger.error("经纪商连接失败", e);
            return false;
        }
    }

    @Override
    public void disconnect() {
        connected = false;
        logger.info("经纪商断开连接");
    }
    
    @Override
    public MarketData getMarketData(String symbol) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("symbol", symbol);
            params.put("field", "rt_ask1,rt_bid1,rt_last");

            Map<String, Object> data = dataPluginClient.getData(params);

            Double askPrice = getDoubleValue(data, "ask");
            Double bidPrice = getDoubleValue(data, "bid");
            Double lastPrice = getDoubleValue(data, "last");

            if (askPrice != null && bidPrice != null && lastPrice != null) {
                return new MarketData(symbol, bidPrice, askPrice, lastPrice);
            }

            Map<String, Object> logData = new HashMap<>();
            logData.put("symbol", symbol);
            logger.warning("获取行情数据失败，返回默认值", logData);
        } catch (Exception e) {
            Map<String, Object> logData = new HashMap<>();
            logData.put("symbol", symbol);
            logData.put("error", e.getMessage());
            logger.error("获取行情数据异常", logData);
        }

        // 返回默认值
        return new MarketData(symbol, 3499.5, 3500.5, 3500.0);
    }

    private Double getDoubleValue(Map<String, Object> data, String key) {
        if (data == null || !data.containsKey(key)) {
            return null;
        }
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }



    @Override
    public OrderResult placeLimitOrder(String symbol, Direction direction, int qty, double price, String orderId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("symbol", symbol);
            payload.put("price", price);
            payload.put("quantity", qty);
            payload.put("side", "buy");
            payload.put("trade_action",  direction == Direction.SELL ? "平仓" : "开仓");
            payload.put("task_id", rollConfig.getTaskId());
            payload.put("sub_order_id", orderId);

            Map<String, Object> response = listenerClient.notify("place_order", payload);

            Map<String, Object> logData = new HashMap<>();
            logData.put("symbol", symbol);
            logData.put("orderId", orderId);
            logData.put("response", response);
            logger.info("下限价单", logData);

            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Boolean result = (Boolean) data.get("result");
                if (result != null && result) {
                    return new OrderResult(orderId, OrderStatus.PENDING, qty, price);
                } else {
                    return new OrderResult(orderId, OrderStatus.REJECTED, qty, price);
                }
            }

            return new OrderResult(orderId, OrderStatus.REJECTED, qty, price);
        } catch (Exception e) {
            Map<String, Object> logData = new HashMap<>();
            logData.put("symbol", symbol);
            logData.put("orderId", orderId);
            logData.put("error", e.getMessage());
            logger.error("下限价单异常", logData);
            return new OrderResult(orderId, OrderStatus.REJECTED, qty, price);
        }
    }
    
    @Override
    public OrderResult placeMarketOrder(String symbol, Direction direction, int qty, String orderId) {
        MarketData market = getMarketData(symbol);
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("symbol", symbol);
            payload.put("quantity", qty);
            payload.put("side", "buy");
            payload.put("trade_action", direction == Direction.SELL ? "平仓" : "开仓");
            payload.put("price", direction == Direction.BUY ? market.getBid() : market.getAsk());
            payload.put("task_id", rollConfig.getTaskId());
            payload.put("sub_order_id", orderId);

            Map<String, Object> response = listenerClient.notify("place_order", payload);

            Map<String, Object> logData = new HashMap<>();
            logData.put("symbol", symbol);
            logData.put("orderId", orderId);
            logData.put("response", response);
            logger.info("下市价单", logData);

            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Boolean result = (Boolean) data.get("result");
                if (result != null && result) {
                    return new OrderResult(orderId, OrderStatus.PENDING, qty, 0.0);
                }
            }

            return new OrderResult(orderId, OrderStatus.REJECTED, qty, 0.0);
        } catch (Exception e) {
            Map<String, Object> logData = new HashMap<>();
            logData.put("symbol", symbol);
            logData.put("orderId", orderId);
            logData.put("error", e.getMessage());
            logger.error("下市价单异常", logData);
            return new OrderResult(orderId, OrderStatus.REJECTED, qty, 0.0);
        }
    }

    @Override
    public Boolean cancelOrder(String symbol, Direction direction, int qty, String orderId) {
        try {
            // 尝试撤单
            Map<String, Object> payload = new HashMap<>();
            payload.put("task_id", rollConfig.getTaskId());
            payload.put("sub_order_id", orderId);

            Map<String, Object> response = listenerClient.notify("cancel_order", payload);

            Map<String, Object> logData = new HashMap<>();
            logData.put("orderId", orderId);
            logData.put("response", response);
            logger.info("撤单请求", logData);

            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Boolean result = (Boolean) data.get("result");
                if (result != null && result) {
                    // 撤单成功，等待数据同步
                    Map<String, Object> successLogData = new HashMap<>();
                    successLogData.put("orderId", orderId);
                    logger.info("撤单成功，等待数据同步", successLogData);
//                    try {
//                        Thread.sleep(10000L);
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    }
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            Map<String, Object> logData = new HashMap<>();
            logData.put("orderId", orderId);
            logData.put("error", e.getMessage());
            logger.error("撤单异常", logData);
            return false;
        }
    }

    @Override
    public BimsOrderDetail getOrderSuccessCount(String orderId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("task_id", rollConfig.getTaskId());
            payload.put("sub_order_id", orderId);

            Map<String, Object> response = listenerClient.notify("check_order", payload);

            Map<String, Object> logData = new HashMap<>();
            logData.put("orderId", orderId);
            logData.put("response", response);
            logger.info("查询订单", logData);

            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Boolean result = (Boolean) data.get("result");
                if (result != null && result) {
                    Object dealQuantityObj = data.get("deal_quantity");
                    Object netPriceObj = data.get("net_price");

                    int dealQuantity = dealQuantityObj instanceof Number ? ((Number) dealQuantityObj).intValue() : 0;
                    double netPrice = netPriceObj instanceof Number ? ((Number) netPriceObj).doubleValue() : 0.0;
                    return BimsOrderDetail.success(orderId, dealQuantity, netPrice);
                }
            }

            return BimsOrderDetail.fail(orderId);
        } catch (Exception e) {
            Map<String, Object> logData = new HashMap<>();
            logData.put("orderId", orderId);
            logData.put("error", e.getMessage());
            logger.error("查询订单异常", logData);
            return BimsOrderDetail.fail(orderId);
        }
    }
}
