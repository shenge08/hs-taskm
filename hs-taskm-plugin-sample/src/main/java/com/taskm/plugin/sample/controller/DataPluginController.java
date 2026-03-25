package com.taskm.plugin.sample.controller;

import com.taskm.plugin.sample.dto.DataResponse;
import com.taskm.plugin.sample.dto.MarketDataRequest;
import com.taskm.plugin.sample.service.DataPluginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据插件 REST 控制器。
 *
 * <p>此控制器提供数据查询接口，策略可以通过 TaskM SDK 的 DataPluginClient 调用这些接口。</p>
 *
 * <h3>主要端点：</h3>
 * <ul>
 *   <li>POST /data - 获取数据（通用接口）</li>
 *   <li>POST /data/multi - 获取多条数据</li>
 *   <li>GET /market/info - 获取市场信息</li>
 * </ul>
 *
 * <h3>策略代码中使用：</h3>
 * <pre>{@code
 * import com.taskm.sdk.DataPluginClient;
 * import java.util.Map;
 *
 * // 初始化客户端（从环境变量读取配置）
 * DataPluginClient plugin = new DataPluginClient();
 *
 * // 获取数据
 * Map<String, Object> data = plugin.getData(Map.of(
 *     "symbol", "BTC/USDT",
 *     "interval", "1h"
 * ));
 *
 * System.out.println("Received data: " + data);
 * }</pre>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
@Tag(name = "数据插件接口", description = "提供市场数据查询的 REST API")
public class DataPluginController {

    private final DataPluginService dataPluginService;

    @Autowired
    public DataPluginController(DataPluginService dataPluginService) {
        this.dataPluginService = dataPluginService;
    }

    /**
     * 获取数据（通用接口）。
     *
     * <p>这是主要的数据查询接口，策略通过 DataPluginClient.getData() 调用此方法。
     * 支持传递任意参数，可以根据参数返回不同的数据。</p>
     *
     * <h3>请求示例：</h3>
     * <pre>{@code
     * POST /api/data
     * Content-Type: application/json
     *
     * {
     *   "symbol": "BTC/USDT",
     *   "interval": "1h",
     *   "limit": 100
     * }
     * }</pre>
     *
     * @param params 请求参数
     * @return 数据响应
     */
    @PostMapping("/data")
    @Operation(
        summary = "获取数据",
        description = """
            获取市场数据。策略代码调用 DataPluginClient.getData() 时会发送请求到此端点。

            **常见参数：**
            - `symbol`: 交易对符号（如 BTC/USDT）
            - `interval`: K线间隔（如 1m, 5m, 1h, 1d）
            - `limit`: 数据条数限制
            - `start_time`: 开始时间戳（毫秒）
            - `end_time`: 结束时间戳（毫秒）

            **返回数据：**
            - `symbol`: 交易对
            - `interval`: 时间间隔
            - `timestamp`: 时间戳
            - `open`: 开盘价
            - `high`: 最高价
            - `low`: 最低价
            - `close`: 收盘价
            - `volume`: 交易量
            - `indicators`: 技术指标
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "数据获取成功",
            content = @Content(schema = @Schema(implementation = DataResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "请求参数错误", content = @Content),
        @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
    })
    public ResponseEntity<DataResponse> getData(
        @Parameter(description = "查询参数，可包含任意键值对", required = true)
        @RequestBody Map<String, Object> params
    ) {
        try {
            Map<String, Object> data = dataPluginService.getData(params);
            return ResponseEntity.ok(DataResponse.success(data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DataResponse.error("Failed to retrieve data: " + e.getMessage()));
        }
    }

    /**
     * 获取多条数据。
     *
     * <p>此接口用于获取多条数据，例如 K线数据列表。</p>
     *
     * @param request 市场数据请求
     * @return 数据响应
     */
    @PostMapping("/data/multi")
    @Operation(
        summary = "获取多条数据",
        description = """
            获取多条市场数据，例如 K线数据列表。

            **请求参数：**
            - `symbol`: 交易对符号（必需）
            - `interval`: K线间隔（必需）
            - `limit`: 数据条数（默认：10）
            - `start_time`: 开始时间戳
            - `end_time`: 结束时间戳

            **返回数据：**
            - `symbol`: 交易对
            - `interval`: 时间间隔
            - `data`: K线数据数组
            - `count`: 数据条数
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "数据获取成功"
        ),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<DataResponse> getMultiData(
        @Parameter(description = "多数据查询请求", required = true)
        @Valid @RequestBody MarketDataRequest request
    ) {
        try {
            Map<String, Object> data = dataPluginService.getMultiData(request);
            return ResponseEntity.ok(DataResponse.success(data, "Retrieved " +
                ((java.util.List<?>) data.get("data")).size() + " data points"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DataResponse.error("Failed to retrieve multi data: " + e.getMessage()));
        }
    }

    /**
     * 获取市场信息。
     *
     * <p>返回交易所/数据源的基本信息，包括支持的交易对、时间间隔等。</p>
     *
     * @return 市场信息
     */
    @GetMapping("/market/info")
    @Operation(
        summary = "获取市场信息",
        description = """
            获取交易所或数据源的基本信息。

            **返回信息：**
            - `exchange`: 交易所名称
            - `serverTime`: 服务器时间
            - `symbols`: 支持的交易对列表
            - `intervals`: 支持的时间间隔列表
            - `rateLimits`: 速率限制信息
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<Map<String, Object>> getMarketInfo() {
        Map<String, Object> info = dataPluginService.getMarketInfo();
        return ResponseEntity.ok(info);
    }
}
