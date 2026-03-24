package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.service.PluginContainerManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 插件容器管理 REST Controller
 *
 * <p>提供插件容器的生命周期管理 REST API 接口
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/plugins/{pluginId}/container")
@Tag(name = "插件容器管理", description = "插件容器的启动、停止、重启和状态查询接口")
public class PluginContainerController {

    private final PluginContainerManager pluginContainerManager;

    @Autowired
    public PluginContainerController(PluginContainerManager pluginContainerManager) {
        this.pluginContainerManager = pluginContainerManager;
    }

    /**
     * 启动插件容器
     *
     * @param pluginId 插件 ID
     * @return 容器 ID
     */
    @PostMapping("/start")
    @Operation(summary = "启动插件容器", description = "启动指定插件的 Docker 容器")
    @Parameter(name = "pluginId", description = "插件 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "容器启动成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "容器启动失败"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "插件不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Map<String, String>> startContainer(@PathVariable Long pluginId) {
        String containerId = pluginContainerManager.startPluginContainer(pluginId);
        return Result.success("Container started successfully", Map.of("containerId", containerId));
    }

    /**
     * 停止插件容器
     *
     * @param pluginId 插件 ID
     * @return 成功消息
     */
    @PostMapping("/stop")
    @Operation(summary = "停止插件容器", description = "停止指定插件的 Docker 容器")
    @Parameter(name = "pluginId", description = "插件 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "容器停止成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "插件或容器不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> stopContainer(@PathVariable Long pluginId) {
        pluginContainerManager.stopPluginContainer(pluginId);
        return Result.success("Container stopped successfully");
    }

    /**
     * 重启插件容器
     *
     * @param pluginId 插件 ID
     * @return 成功消息
     */
    @PostMapping("/restart")
    @Operation(summary = "重启插件容器", description = "重启指定插件的 Docker 容器")
    @Parameter(name = "pluginId", description = "插件 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "容器重启成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "插件或容器不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> restartContainer(@PathVariable Long pluginId) {
        pluginContainerManager.restartPluginContainer(pluginId);
        return Result.success("Container restarted successfully");
    }

    /**
     * 获取容器状态
     *
     * @param pluginId 插件 ID
     * @return 容器状态
     */
    @GetMapping("/status")
    @Operation(summary = "查询容器状态", description = "查询指定插件容器的运行状态")
    @Parameter(name = "pluginId", description = "插件 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "插件或容器不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Map<String, String>> getContainerStatus(@PathVariable Long pluginId) {
        String status = pluginContainerManager.getContainerStatus(pluginId);
        return Result.success(Map.of("status", status));
    }
}
