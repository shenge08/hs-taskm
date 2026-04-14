package com.futuresroll.broker;

/**
 * 文件描述
 *
 * @ProductName: Hundsun Tianjian 4
 * @ProjectName: futures-roll-bot-sdlc
 * @Package: com.futuresroll.broker
 * @Description: note
 * @Author: chenhs37028
 * @date: 2026/3/10 10:17
 * @UpdateUser: shenge08
 * @UpdateDate: 2026/3/10 10:17
 * @UpdateRemark: The modified content
 * @Version: 1.0 Copyright © 2026 Hundsun Technologies Inc. All Rights Reserved
 **/
public class BimsOrderDetail {

  public BimsOrderDetail(String orderId, Integer count, Double netPrice) {
    this.orderId = orderId;
    this.count = count;
    this.netPrice = netPrice;
  }

  public static BimsOrderDetail fail(String orderId){
    return new BimsOrderDetail(orderId,-1,-1.0);
  }

  public static BimsOrderDetail success(String orderId, Integer count, Double netPrice){
    return new BimsOrderDetail(orderId,count,netPrice);
  }
  //成交
  private String orderId;

  //成交数量
  private Integer count;

  //成交均价
  private Double netPrice;

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public Double getNetPrice() {
    return netPrice;
  }

  public void setNetPrice(Double netPrice) {
    this.netPrice = netPrice;
  }
}
