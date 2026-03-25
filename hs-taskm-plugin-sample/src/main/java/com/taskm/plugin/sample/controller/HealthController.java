package com.taskm.plugin.sample.controller;

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
 * 健康检查控制器。
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
@Tag(name = "健康检查", description = "数据插件健康状态检查接口")
public class HealthController {

    @GetMapping("/health")
    @Operation(
        summary = "健康检查",
        description = "检查数据插件服务是否正常运行"
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
            "Data plugin service is running",
            Instant.now()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(
        summary = "服务信息",
        description = "获取数据插件服务的详细信息"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "获取成功"
        )
    })
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "TaskM Data Plugin Sample");
        info.put("version", "1.0.0");
        info.put("description", "Sample data plugin web application for TaskM");
        info.put("endpoints", Map.of(
            "data", "/api/data",
            "multi-data", "/api/data/multi",
            "market-info", "/api/market/info",
            "health", "/api/health"
        ));
        info.put("timestamp", Instant.now());
        return ResponseEntity.ok(info);
    }

    /**
     * 健康检查响应记录。
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
