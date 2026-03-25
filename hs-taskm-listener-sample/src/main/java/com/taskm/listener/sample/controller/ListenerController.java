package com.taskm.listener.sample.controller;

import com.taskm.listener.sample.service.ListenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for receiving business events from strategies.
 *
 * <p>This controller provides a unified event interface that handles all types of business events.</p>
 *
 * <h3>Supported Event Types:</h3>
 * <ul>
 *   <li>place_order - 下订单事件 (调用BIMS API生成可转债订单)</li>
 *   <li>cancel_order - 撤销订单事件 (调用BIMS API根据任务ID和子单ID撤销，需提供taskId和subOrderId)</li>
 *   <li>check_order - 检查订单事件 (调用BIMS API根据任务ID查询成交量)</li>
 * </ul>
 *
 * <h3>Usage from Strategy Code:</h3>
 * <pre>{@code
 * import com.taskm.sdk.ListenerClient;
 *
 * // The listener endpoint is set via environment variable
 * // LISTENER_ENDPOINT=http://listener-host:8080
 *
 * ListenerClient listener = new ListenerClient();
 *
 * // 下订单
 * Map<String, Object> result = listener.notify("place_order", Map.of(
 *     "symbol", "BTC/USDT",
 *     "price", 50000,
 *     "quantity", 0.1
 * ));
 *
 * // 撤销订单
 * Map<String, Object> result = listener.notify("cancel_order", Map.of(
 *     "orderId", "12345"
 * ));
 *
 * // 检查订单
 * Map<String, Object> result = listener.notify("check_order", Map.of(
 *     "orderId", "12345"
 * ));
 * }</pre>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
@Tag(name = "监听器接口", description = "接收策略业务事件的统一 REST API")
public class ListenerController {

    private static final Logger logger = LoggerFactory.getLogger(ListenerController.class);

    private final ListenerService listenerService;

    @Autowired
    public ListenerController(ListenerService listenerService) {
        this.listenerService = listenerService;
    }

    /**
     * 统一事件处理接口
     *
     * <p>此接口接收所有类型的业务事件，根据 eventType 路径参数分发到不同的处理逻辑。</p>
     *
     * @param eventType 事件类型（place_order, cancel_order, check_order）
     * @param payload 事件数据（键值对）
     * @return 处理结果（键值对）
     */
    @PostMapping("/event/{eventType}")
    @Operation(
        summary = "统一事件接口",
        description = "接收并处理策略发送的业务事件。支持的事件类型：place_order（下订单，调用BIMS API生成可转债订单）、cancel_order（撤销订单，调用BIMS API根据任务ID和子单ID撤销，需提供taskId和subOrderId）、check_order（检查订单，调用BIMS API根据任务ID查询成交量）。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "事件处理成功",
            content = @Content(schema = @Schema(implementation = Map.class))
        ),
        @ApiResponse(responseCode = "400", description = "不支持的事件类型或数据格式错误", content = @Content),
        @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> handleEvent(
        @Parameter(description = "事件类型", required = true, example = "place_order")
        @PathVariable String eventType,

        @Parameter(description = "事件数据（键值对）", required = true)
        @RequestBody Map<String, Object> payload
    ) {
        logger.info("Received event: {}, payload: {}", eventType, payload);

        try {
            Map<String, Object> result = listenerService.handleEvent(eventType, payload);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid event type: {}", eventType, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Unknown event type: " + eventType
            ));
        } catch (Exception e) {
            logger.error("Failed to process event: {}", eventType, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
