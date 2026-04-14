package com.futuresroll.executor;

import com.futuresroll.broker.*;
import com.futuresroll.config.RollConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 订单执行器
 */
public class OrderExecutor {

    private static final Logger log = LoggerFactory.getLogger(OrderExecutor.class);
    private IBroker broker;
    private RollConfig config;
    
    public OrderExecutor(IBroker broker, RollConfig config) {
        this.broker = broker;
        this.config = config;
    }
    
    /**
     * 检查价差是否满足条件
     */
    public boolean checkSpread() {
        MarketData nearData = broker.getMarketData(config.getNearSymbol());
        MarketData farData = broker.getMarketData(config.getFarSymbol());
        
        double spread = nearData.getBid() - farData.getAsk();
        return spread >= config.getMinAcceptableSpread();
    }
    
    /**
     * 获取当前价差
     */
    public double getCurrentSpread() {
        MarketData nearData = broker.getMarketData(config.getNearSymbol());
        MarketData farData = broker.getMarketData(config.getFarSymbol());
        return nearData.getBid() - farData.getAsk();
    }
    
    /**
     * 近月平仓（限价单）
     */
    public OrderResult closeNearLeg(int qty,String orderId) {
        int tryCount = 0;
        int totalFilled = 0;
        int sellNum =1;
        String sOrderId = "0"+orderId+sellNum;

        broker.placeMarketOrder(
            config.getNearSymbol(),
            Direction.SELL,
            qty,
            sOrderId
        );

        while (totalFilled < qty){
            if (tryCount >= 3) {
                tryCount = 0;
                sellNum++;

                broker.cancelOrder(config.getNearSymbol(), Direction.SELL, qty, sOrderId);
                BimsOrderDetail cancelButBuyCount = broker.getOrderSuccessCount(sOrderId);
                totalFilled = cancelButBuyCount.getCount()+totalFilled;
                if(totalFilled == qty){
                    log.warn("{} 平仓成交 {} 手  成交均价", sOrderId,cancelButBuyCount.getCount(),cancelButBuyCount.getNetPrice());
                    return new OrderResult(sOrderId, OrderStatus.FILLED, totalFilled, cancelButBuyCount.getNetPrice());
                }else{
                    //3次还没有成交要追单了
                    sOrderId = "0"+orderId+sellNum;
                    broker.placeMarketOrder(
                        config.getNearSymbol(),
                        Direction.SELL,
                        qty,
                        sOrderId
                    );
                }
            }

            try{
                Thread.sleep(config.getCloseWaitTime() *1000L);
                BimsOrderDetail orderSuccessCount = broker.getOrderSuccessCount(sOrderId);
                //成交了
                if(orderSuccessCount.getCount() > 0){
                    totalFilled = orderSuccessCount.getCount()+totalFilled;
                    if(totalFilled == qty){
                        log.warn("{} 平仓成交 {} 手,全部完成  成交均价", sOrderId,orderSuccessCount.getCount(),orderSuccessCount.getNetPrice());
                        return new OrderResult(sOrderId, OrderStatus.FILLED, totalFilled, orderSuccessCount.getNetPrice());
                    }else{
                        log.warn("{} 平仓成交 {} 手  成交均价", sOrderId,orderSuccessCount.getCount(),orderSuccessCount.getNetPrice());
                        continue;
                    }
                }

                //没有成交
                if(orderSuccessCount.getCount()  == 0 && totalFilled == 0){
                   //没有成交,取消订单
                    broker.cancelOrder(config.getNearSymbol(), Direction.SELL, qty, sOrderId);
                    //没有成交,取消订单后查询是否有成交
                    BimsOrderDetail cancelButBuyCount = broker.getOrderSuccessCount(sOrderId);
                    if(cancelButBuyCount.getCount() > 0){
                        totalFilled = orderSuccessCount.getCount()+totalFilled;
                        if(totalFilled == qty){
                            return new OrderResult(sOrderId, OrderStatus.FILLED, totalFilled, cancelButBuyCount.getNetPrice());
                        }else{
                            continue;
                        }
                    }

                }
                if(orderSuccessCount.getCount()  < 0){
                    continue;
                }
                tryCount++;
            }catch (Exception e){

            }
        }
      return new OrderResult(sOrderId, OrderStatus.CANCELLED, 0, 0);
    }
    
    /**
     * 远月开仓（追单+市价兜底）
     */
    public OrderResult openFarLeg(int qty, double nearFillPrice,String orderId) {
        int tryCount = 1;
        int totalFilled = 0;
        String sOrderId = "1"+orderId+tryCount;
        double currentSpread = getCurrentSpread();
        double targetSpread = Math.max(currentSpread, config.getMinAcceptableSpread());
        double targetPrice = nearFillPrice - targetSpread;

        //先下
        broker.placeLimitOrder(
            config.getFarSymbol(),
            Direction.BUY,
            qty,
            targetPrice,
            sOrderId
        );
        while (totalFilled < qty){
            try{
                Thread.sleep(config.getOpenWaitTime() *1000L);
                BimsOrderDetail orderSuccessCount = broker.getOrderSuccessCount(sOrderId);
                //成交了
                if(orderSuccessCount.getCount() > 0){
                    totalFilled = orderSuccessCount.getCount()+totalFilled;
                    if(totalFilled == qty){
                        log.warn("{} 开仓成交 {} 手,全部完成  成交均价", sOrderId,orderSuccessCount.getCount(),orderSuccessCount.getNetPrice());
                        return new OrderResult(sOrderId, OrderStatus.FILLED, totalFilled, orderSuccessCount.getNetPrice());
                    }else{
                        log.warn("{} 开仓成交 {} 手  成交均价", sOrderId,orderSuccessCount.getCount(),orderSuccessCount.getNetPrice());
                    }
                }

                broker.cancelOrder(config.getFarSymbol(), Direction.BUY, qty, sOrderId);
                BimsOrderDetail cancelButBuyCount = broker.getOrderSuccessCount(sOrderId);
                if(cancelButBuyCount.getCount() >= 0){
                    totalFilled = cancelButBuyCount.getCount()+totalFilled;
                    if(totalFilled == qty){
                        log.warn("{} 平仓成交 {} 手  成交均价", sOrderId,cancelButBuyCount.getCount(),cancelButBuyCount.getNetPrice());
                        return new OrderResult(sOrderId, OrderStatus.FILLED, totalFilled, cancelButBuyCount.getNetPrice());
                    }else{
                        tryCount ++;
                        sOrderId = "1"+orderId+tryCount;
                        if(tryCount <= config.getMaxChaseAttempts()){
                           broker.placeLimitOrder(
                               config.getFarSymbol(),
                               Direction.BUY,
                               qty-totalFilled,
                               targetPrice+(tryCount-1)*config.getChasePriceIncrement(),
                               sOrderId
                           );
                       }else{

                            broker.placeMarketOrder(
                                config.getFarSymbol(),
                                Direction.BUY,
                                qty-totalFilled,
                                sOrderId
                            );
                       }

                    }
                }else{
                    log.warn("查询订单失败..... 继续循环");
                    continue;
                }

            }catch (Exception e){

            }
        }
        return new OrderResult(sOrderId, OrderStatus.CANCELLED, -1, 0);
    }
    
    /**
     * 执行完整的一批移仓
     */
    public BatchResult executeBatch(int qty,String orderId) {
        // 1. 检查价差
        if (!checkSpread()) {
            return new BatchResult(false, 0, 0, "价差不满足");
        }

        // 2. 平近月
        OrderResult nearResult = closeNearLeg(qty,orderId);
        if (nearResult.getFilledQty() == 0) {
            return new BatchResult(false, 0, 0, "平仓失败");
        }
        
        // 3. 开远月
        OrderResult farResult = openFarLeg(nearResult.getFilledQty(), nearResult.getAvgPrice(),orderId);
        
        boolean success = farResult.getFilledQty() > 0;
        double actualSpread = nearResult.getAvgPrice() - farResult.getAvgPrice();
        log.info("执行结果: 平{}手, 开{}手, 价差{}", nearResult.getFilledQty(), farResult.getFilledQty(), actualSpread);
        
        return new BatchResult(success, nearResult.getFilledQty(), farResult.getFilledQty(), null);
    }
}
