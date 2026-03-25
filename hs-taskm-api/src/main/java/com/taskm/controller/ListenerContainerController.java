package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.service.ListenerContainerManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 监听器实例容器管理 REST Controller
 *
 * <p>提供监听器实例容器的生命周期管理 REST API 接口（1:1 映射）
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/listener-instances/{listenerInstanceId}/container")
@Tag(name = "监听器实例容器管理", description = "监听器实例容器的启动、停止、重启和状态查询接口")
public class ListenerContainerController {

    private final ListenerContainerManager listenerContainerManager;

    @Autowired
    public ListenerContainerController(ListenerContainerManager listenerContainerManager) {
        this.listenerContainerManager = listenerContainerManager;
    }

    /**
     * 启动监听器实例容器
     *
     * @param listenerInstanceId 监听器实例 ID
     * @return 容器 ID
     */
    @PostMapping("/start")
    @Operation(summary = "启动监听器实例容器", description = "启动指定监听器实例的 Docker 容器")
    @Parameter(name = "listenerInstanceId", description = "监听器实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "容器启动成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "容器启动失败"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "监听器实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Map<String, String>> startContainer(@PathVariable Long listenerInstanceId) {
        String containerId = listenerContainerManager.startListenerContainer(listenerInstanceId);
        return Result.success("Container started successfully", Map.of("containerId", containerId));
    }

    /**
     * 停止监听器实例容器
     *
     * @param listenerInstanceId 监听器实例 ID
     * @return 成功消息
     */
    @PostMapping("/stop")
    @Operation(summary = "停止监听器实例容器", description = "停止指定监听器实例的 Docker 容器")
    @Parameter(name = "listenerInstanceId", description = "监听器实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "容器停止成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "监听器实例或容器不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> stopContainer(@PathVariable Long listenerInstanceId) {
        listenerContainerManager.stopListenerContainer(listenerInstanceId);
        return Result.success("Container stopped successfully");
    }

    /**
     * 重启监听器实例容器
     *
     * @param listenerInstanceId 监听器实例 ID
     * @return 成功消息
     */
    @PostMapping("/restart")
    @Operation(summary = "重启监听器实例容器", description = "重启指定监听器实例的 Docker 容器")
    @Parameter(name = "listenerInstanceId", description = "监听器实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "容器重启成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "监听器实例或容器不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> restartContainer(@PathVariable Long listenerInstanceId) {
        listenerContainerManager.restartListenerContainer(listenerInstanceId);
        return Result.success("Container restarted successfully");
    }

    /**
     * 获取容器状态
     *
     * @param listenerInstanceId 监听器实例 ID
     * @return 容器状态
     */
    @GetMapping("/status")
    @Operation(summary = "查询容器状态", description = "查询指定监听器实例容器的运行状态")
    @Parameter(name = "listenerInstanceId", description = "监听器实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "监听器实例或容器不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Map<String, String>> getContainerStatus(@PathVariable Long listenerInstanceId) {
        String status = listenerContainerManager.getContainerStatus(listenerInstanceId);
        return Result.success(Map.of("status", status));
    }
}
