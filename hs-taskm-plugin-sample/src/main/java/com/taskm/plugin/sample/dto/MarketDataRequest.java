package com.taskm.plugin.sample.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 市场数据请求 DTO。
 *
 * <p>策略通过 DataPluginClient 调用数据插件时，可以传递任意参数。
 * 此 DTO 定义了一些常见的市场数据请求参数。</p>
 *
 * <h3>常见参数：</h3>
 * <ul>
 *   <li>symbol: 交易对符号（如 BTC/USDT）</li>
 *   <li>interval: K线间隔（如 1m, 5m, 1h, 1d）</li>
 *   <li>limit: 数据条数限制</li>
 *   <li>startTime: 开始时间戳</li>
 *   <li>endTime: 结束时间戳</li>
 * </ul>
 */
@Data
public class MarketDataRequest {

    /**
     * 交易对符号。
     * 示例：BTC/USDT, ETH/USDT
     */
    @JsonProperty("symbol")
    private String symbol;

    /**
     * K线间隔。
     * 示例：1m, 5m, 15m, 30m, 1h, 4h, 1d
     */
    @JsonProperty("interval")
    private String interval;

    /**
     * 数据条数限制。
     */
    @JsonProperty("limit")
    private Integer limit;

    /**
     * 开始时间戳（毫秒）。
     */
    @JsonProperty("start_time")
    private Long startTime;

    /**
     * 结束时间戳（毫秒）。
     */
    @JsonProperty("end_time")
    private Long endTime;

    /**
     * 其他自定义参数。
     */
    @JsonProperty("params")
    private Map<String, Object> params;
}
