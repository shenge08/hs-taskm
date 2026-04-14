package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.service.ListenerContainerManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Listener Instance Container Controller
 *
 * <p>Provides REST API endpoints for managing listener instance containers
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/listener-instances")
@Tag(name = "监听器实例容器管理", description = "监听器实例容器的启动、停止和重启接口")
public class ListenerInstanceContainerController {

    private final ListenerContainerManager listenerContainerManager;

    @Autowired
    public ListenerInstanceContainerController(ListenerContainerManager listenerContainerManager) {
        this.listenerContainerManager = listenerContainerManager;
    }

    /**
     * Start listener instance container
     *
     * @param instanceId listener instance ID
     * @return container ID
     */
    @PostMapping("/{instanceId}/start")
    @Operation(summary = "启动监听器实例容器", description = "启动指定监听器实例的Docker容器")
    @Parameter(name = "instanceId", description = "监听器实例 ID", required = true, example = "1")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "启动成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> startContainer(@PathVariable Long instanceId) {
        String containerId = listenerContainerManager.startListenerContainer(instanceId);
        return Result.success("Container started successfully", containerId);
    }

    /**
     * Stop listener instance container
     *
     * @param instanceId listener instance ID
     * @return success message
     */
    @PostMapping("/{instanceId}/stop")
    @Operation(summary = "停止监听器实例容器", description = "停止指定监听器实例的Docker容器")
    @Parameter(name = "instanceId", description = "监听器实例 ID", required = true, example = "1")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "停止成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> stopContainer(@PathVariable Long instanceId) {
        listenerContainerManager.stopListenerContainer(instanceId);
        return Result.success("Container stopped successfully");
    }

    /**
     * Restart listener instance container
     *
     * @param instanceId listener instance ID
     * @return container ID
     */
    @PostMapping("/{instanceId}/restart")
    @Operation(summary = "重启监听器实例容器", description = "重启指定监听器实例的Docker容器")
    @Parameter(name = "instanceId", description = "监听器实例 ID", required = true, example = "1")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "重启成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> restartContainer(@PathVariable Long instanceId) {
        listenerContainerManager.restartListenerContainer(instanceId);
        return Result.success("Container restarted successfully");
    }
}
