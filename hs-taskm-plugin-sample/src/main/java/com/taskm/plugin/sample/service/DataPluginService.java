package com.taskm.plugin.sample.service;

import com.taskm.sdk.Logger;
import com.taskm.plugin.sample.dto.MarketDataRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 数据插件服务类。
 *
 * <p>此服务演示如何处理数据请求。使用 MockBroker 获取真实的市场数据。</p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>通过 MockBroker 调用真实的市场数据 API</li>
 *   <li>将 MarketData 对象转换为 Map 格式返回</li>
 *   <li>支持灵活的参数传递</li>
 *   <li>完整的日志记录</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
public class DataPluginService {

    private final Logger logger;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    private final Random random = new Random();
    private final BrokerService brokerService;

    /**
     * 创建新的数据插件服务。
     */
    public DataPluginService() {
        this.logger = new Logger("DataPluginService");
        this.brokerService = new BrokerService();
    }

    /**
     * 创建带有自定义日志器和 BrokerService 的数据插件服务。
     *
     * @param logger 自定义日志器
     * @param brokerService BrokerService 实例
     */
    public DataPluginService(Logger logger, BrokerService brokerService) {
        this.logger = logger;
        this.brokerService = brokerService;
    }

    /**
     * 获取数据。
     *
     * <p>这是核心方法，策略调用 DataPluginClient.getData() 时会触发此方法。
     * 使用 BrokerService.getMarketData() 获取真实的市场数据。</p>
     *
     * <p>参照 MockBroker.getMarketData 的实现逻辑：</p>
     * <ol>
     *   <li>接收 symbol 参数（债券代码）</li>
     *   <li>发送 HTTP POST 请求到市场数据接口</li>
     *   <li>解析响应获取 bid, ask, last 价格</li>
     *   <li>转换为 Map 格式返回</li>
     * </ol>
     *
     * @param params 请求参数（可包含任意键值对）
     *               - symbol: 债券代码（必需）
     *               - interval: 时间间隔（可选）
     *               - 其他参数将记录到日志但不影响数据获取
     * @return 数据响应 Map，包含以下字段：
     *         - symbol: 债券代码
     *         - bid: 买价
     *         - ask: 卖价
     *         - last: 最新价
     *         - timestamp: 时间戳
     *         - spread: 买卖价差
     *         - midPrice: 中间价
     * @since 1.0.0
     */
    public Map<String, Object> getData(Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();

        try {
            // 提取 symbol 参数
            String symbol = null;
            if (params != null) {
                Object symbolObj = params.get("symbol");
                if (symbolObj != null) {
                    symbol = symbolObj.toString();
                }
            }

            // 如果没有提供 symbol，使用默认值
            if (symbol == null || symbol.trim().isEmpty()) {
                symbol = "10002335"; // 使用默认的债券代码
                Map<String, Object> logData = new HashMap<>();
                logData.put("message", "No symbol provided, using default");
                logData.put("defaultSymbol", symbol);
                logger.warning("Missing symbol parameter", logData);
            }

            // 记录请求
            Map<String, Object> logData = new HashMap<>();
            logData.put("symbol", symbol);
            if (params != null) {
                logData.put("params", params);
            }
            logger.info("Fetching market data via BrokerService", logData);

            // 调用 BrokerService 获取市场数据
            Map<String, Object> marketData = brokerService.getMarketData(symbol);

            // 提取价格数据
            Double bid = (Double) marketData.get("bid");
            Double ask = (Double) marketData.get("ask");
            Double last = (Double) marketData.get("last");

            // 构建响应数据
            data.put("symbol", symbol);
            data.put("bid", bid);
            data.put("ask", ask);
            data.put("last", last);
            data.put("timestamp", Instant.now().toEpochMilli());

            // 添加额外信息
            if (bid != null && ask != null) {
                data.put("spread", ask - bid);
                data.put("midPrice", (bid + ask) / 2.0);
            }

            // 记录响应
            Map<String, Object> responseLogData = new HashMap<>();
            responseLogData.put("symbol", symbol);
            responseLogData.put("bid", bid);
            responseLogData.put("ask", ask);
            responseLogData.put("last", last);
            logger.debug("Market data retrieved successfully", responseLogData);

        } catch (Exception e) {
            // 记录错误
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("params", params);
            errorData.put("error", e.getMessage());
            logger.error("Failed to fetch market data", errorData);

            // 返回错误信息
            data.put("error", true);
            data.put("errorMessage", e.getMessage());
            data.put("timestamp", Instant.now().toEpochMilli());
        }

        return data;
    }

    /**
     * 获取多个数据点。
     *
     * <p>此方法演示如何返回多条数据，例如 K线数据。</p>
     *
     * @param request 市场数据请求
     * @return 数据点列表
     */
    public Map<String, Object> getMultiData(MarketDataRequest request) {
        Map<String, Object> response = new HashMap<>();

        String symbol = request.getSymbol() != null ? request.getSymbol() : "BTC/USDT";
        String interval = request.getInterval() != null ? request.getInterval() : "1h";
        int limit = request.getLimit() != null ? request.getLimit() : 10;

        logger.info("Fetching multi-point data",
            Map.of("symbol", symbol, "interval", interval, "limit", limit));

        // 生成多条 K线数据
        java.util.List<Map<String, Object>> candles = new java.util.ArrayList<>();
        long currentTime = Instant.now().toEpochMilli();

        for (int i = 0; i < limit; i++) {
            Map<String, Object> candle = new HashMap<>();
            long timestamp = currentTime - (limit - i - 1) * 3600000L; // 每小时间隔

            double open = generatePrice();
            double close = generatePrice();
            double high = Math.max(open, close) + random.nextDouble() * 100;
            double low = Math.min(open, close) - random.nextDouble() * 100;

            candle.put("timestamp", timestamp);
            candle.put("open", open);
            candle.put("high", high);
            candle.put("low", low);
            candle.put("close", close);
            candle.put("volume", generateVolume());

            candles.add(candle);
        }

        response.put("symbol", symbol);
        response.put("interval", interval);
        response.put("data", candles);
        response.put("count", candles.size());

        return response;
    }

    /**
     * 获取市场信息。
     *
     * @return 市场信息
     */
    public Map<String, Object> getMarketInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("exchange", "Sample Exchange");
        info.put("serverTime", Instant.now().toEpochMilli());
        info.put("symbols", java.util.List.of("BTC/USDT", "ETH/USDT", "BNB/USDT", "SOL/USDT"));
        info.put("intervals", java.util.List.of("1m", "5m", "15m", "30m", "1h", "4h", "1d"));
        info.put("rateLimits", Map.of(
            "requests", 1200,
            "window", 60
        ));

        return info;
    }

    /**
     * 生成随机价格（示例）。
     *
     * @return 随机价格
     */
    private double generatePrice() {
        // 生成 30000-70000 之间的随机价格
        return 30000 + random.nextDouble() * 40000;
    }

    /**
     * 生成随机交易量（示例）。
     *
     * @return 随机交易量
     */
    private double generateVolume() {
        // 生成 100-10000 之间的随机交易量
        return 100 + random.nextDouble() * 9900;
    }
}
