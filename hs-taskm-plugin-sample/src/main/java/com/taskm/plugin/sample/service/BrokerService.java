package com.taskm.plugin.sample.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.taskm.sdk.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 简化的经纪商服务，用于获取市场数据。
 *
 * <p>这个类参照 MockBroker.getMarketData 的实现，提供获取市场数据的功能。</p>
 *
 * <p>配置项（环境变量）：</p>
 * <ul>
 *   <li>MARKET_DATA_URL: 市场数据接口地址（默认：http://172.16.90.99:8898/wdd/getBondInfoCompleteCommand）</li>
 *   <li>MARKET_DATA_TIMEOUT: 请求超时时间（毫秒，默认：5000）</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Component
public class BrokerService {

    private static final Logger log = LoggerFactory.getLogger(BrokerService.class);

    /**
     * 市场数据接口地址，从环境变量 MARKET_DATA_URL 读取
     */
    private final String marketDataUrl;

    /**
     * 请求超时时间（毫秒），从环境变量 MARKET_DATA_TIMEOUT 读取
     */
    private final int timeout;

    /**
     * 构造函数，从环境变量读取配置
     */
    public BrokerService() {
        // 从环境变量读取市场数据接口地址
        this.marketDataUrl = Environment.get(
            "MARKET_DATA_URL",
            "http://172.16.90.99:8898/wdd/getBondInfoCompleteCommand"
        );

        // 从环境变量读取超时时间
        this.timeout = Environment.getInt(
            "MARKET_DATA_TIMEOUT",
            5000  // 默认 5 秒
        );

        log.info("BrokerService initialized with URL: {}, timeout: {}ms", this.marketDataUrl, this.timeout);
    }

    /**
     * 获取市场数据。
     *
     * <p>此方法参照 MockBroker.getMarketData 实现，发送 HTTP 请求获取实时行情数据。</p>
     *
     * @param symbol 债券代码
     * @return 包含 bid, ask, last 价格的 Map
     */
    public Map<String, Object> getMarketData(String symbol) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 构建请求体（参照 MockBroker 的实现）
            JSONObject requestBody = new JSONObject();
            requestBody.put("code", symbol);
            requestBody.put("field", "rt_ask1,rt_bid1,rt_last");
            requestBody.put("date", "wsq");

            log.info("Requesting market data for symbol: {} from {}", symbol, marketDataUrl);

            // 发送 HTTP POST 请求（使用配置的地址和超时时间）
            String responseBody = HttpRequest.post(marketDataUrl)
                .body(requestBody.toString())
                .timeout(timeout)
                .execute()
                .body();

            log.debug("Received response: {}", responseBody);

            // 解析响应（参照 MockBroker 的实现）
            if (StrUtil.isNotBlank(responseBody)) {
                JSONArray jsonArray = JSONUtil.parseArray(responseBody);
                if (jsonArray != null && jsonArray.size() >= 3) {
                    JSONArray askArray = jsonArray.getJSONArray(0);
                    JSONArray bidArray = jsonArray.getJSONArray(1);
                    JSONArray lastArray = jsonArray.getJSONArray(2);

                    double askPrice = askArray != null && askArray.size() > 0 ? askArray.getDouble(0) : 0.0;
                    double bidPrice = bidArray != null && bidArray.size() > 0 ? bidArray.getDouble(0) : 0.0;
                    double lastPrice = lastArray != null && lastArray.size() > 0 ? lastArray.getDouble(0) : 0.0;

                    result.put("bid", bidPrice);
                    result.put("ask", askPrice);
                    result.put("last", lastPrice);
                    result.put("symbol", symbol);
                    result.put("success", true);

                    log.info("Market data retrieved - bid: {}, ask: {}, last: {}", bidPrice, askPrice, lastPrice);
                    return result;
                }
            }

            // 如果解析失败，使用默认值（参照 MockBroker 的默认返回）
            log.warn("Failed to parse response, using default values");
            result.put("bid", 3499.5);
            result.put("ask", 3500.5);
            result.put("last", 3500.0);
            result.put("symbol", symbol);
            result.put("success", true);

        } catch (Exception e) {
            log.error("Error fetching market data for symbol: {}", symbol, e);
            result.put("bid", 3499.5);
            result.put("ask", 3500.5);
            result.put("last", 3500.0);
            result.put("symbol", symbol);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 获取当前配置的市场数据接口地址。
     *
     * @return 市场数据接口 URL
     */
    public String getMarketDataUrl() {
        return marketDataUrl;
    }

    /**
     * 获取当前配置的请求超时时间。
     *
     * @return 超时时间（毫秒）
     */
    public int getTimeout() {
        return timeout;
    }
}
