package com.futuresroll.broker;

/**
 * 行情数据
 */
public class MarketData {
    private String symbol;
    private double bid;     // 买价
    private double ask;     // 卖价
    private double last;    // 最新价

    public MarketData(String symbol, double bid, double ask, double last) {
        this.symbol = symbol;
        this.bid = bid;
        this.ask = ask;
        this.last = last;
    }

    // Getters
    public String getSymbol() { return symbol; }
    public double getBid() { return bid; }
    public double getAsk() { return ask; }
    public double getLast() { return last; }
}
