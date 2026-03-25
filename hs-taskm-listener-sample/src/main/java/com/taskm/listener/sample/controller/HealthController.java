package com.taskm.listener.sample.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring listener status.
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
@Tag(name = "健康检查", description = "监听器健康状态检查接口")
public class HealthController {

    @GetMapping("/health")
    @Operation(
        summary = "健康检查",
        description = "检查监听器服务是否正常运行"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "服务正常",
            content = @Content(schema = @Schema(implementation = HealthResponse.class))
        )
    })
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = new HealthResponse(
            "UP",
            "Listener service is running",
            Instant.now()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(
        summary = "服务信息",
        description = "获取监听器服务的详细信息"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "获取成功"
        )
    })
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "TaskM Listener Sample");
        info.put("version", "1.0.0");
        info.put("description", "Sample listener web application for TaskM");
        info.put("endpoints", Map.of(
            "event", "/api/event/{eventType}",
            "health", "/api/health"
        ));
        info.put("supportedEventTypes", Map.of(
            "place_order", "下订单事件 (调用BIMS API生成可转债订单)",
            "cancel_order", "撤销订单事件 (调用BIMS API根据任务ID和子单ID撤销，需提供taskId和subOrderId)",
            "check_order", "检查订单事件 (调用BIMS API根据任务ID查询成交量)"
        ));
        info.put("timestamp", Instant.now());
        return ResponseEntity.ok(info);
    }

    /**
     * Health check response record.
     */
    public record HealthResponse(
        @Schema(description = "服务状态")
        String status,

        @Schema(description = "状态消息")
        String message,

        @Schema(description = "检查时间")
        Instant timestamp
    ) {}
}
