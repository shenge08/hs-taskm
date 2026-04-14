package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.entity.DataPluginInstance;
import com.taskm.mapper.DataPluginInstanceMapper;
import com.taskm.service.DockerLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Plugin Instance Log Controller
 *
 * <p>Provides REST API endpoints for fetching logs from plugin instance containers
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/plugin-instances")
@Tag(name = "插件实例日志", description = "插件实例容器日志查询接口")
public class PluginInstanceLogController {

    private final DataPluginInstanceMapper pluginInstanceMapper;
    private final DockerLogService dockerLogService;

    @Autowired
    public PluginInstanceLogController(
            DataPluginInstanceMapper pluginInstanceMapper,
            DockerLogService dockerLogService) {
        this.pluginInstanceMapper = pluginInstanceMapper;
        this.dockerLogService = dockerLogService;
    }

    /**
     * Get logs from plugin instance container
     *
     * @param instanceId plugin instance ID
     * @param type log type: stdout, stderr, or all (default: all)
     * @param offset starting line number (default: 0)
     * @param limit maximum number of lines to return (default: 100)
     * @return list of log lines
     */
    @GetMapping("/{instanceId}/logs")
    @Operation(summary = "获取插件实例日志", description = "从插件实例容器获取日志")
    @Parameter(name = "instanceId", description = "插件实例 ID", required = true, example = "1")
    @Parameter(name = "type", description = "日志类型：stdout（标准输出）、stderr（错误输出）、all（全部）", required = false, example = "all")
    @Parameter(name = "offset", description = "起始行号，从 0 开始", required = false, example = "0")
    @Parameter(name = "limit", description = "最大返回行数", required = false, example = "100")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<List<String>> getPluginInstanceLogs(
            @PathVariable Long instanceId,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "100") int limit) {

        // Get plugin instance to find container ID
        DataPluginInstance instance = pluginInstanceMapper.selectById(instanceId);
        if (instance == null) {
            return Result.error(404, "Plugin instance not found");
        }

        String containerId = instance.getContainerId();
        if (containerId == null || containerId.isEmpty()) {
            return Result.error(400, "Plugin instance is not associated with a container");
        }

        // Get logs with pagination
        List<String> logs = dockerLogService.getContainerLogs(containerId, offset, limit);

        return Result.success(logs);
    }

    /**
     * Get the last N lines of plugin instance logs
     *
     * @param instanceId plugin instance ID
     * @param type log type: stdout, stderr, or all (default: all)
     * @param lines number of lines to return from the end (default: 50)
     * @return list of log lines
     */
    @GetMapping("/{instanceId}/logs/tail")
    @Operation(summary = "获取插件实例日志尾部", description = "获取插件实例容器日志的最后 N 行")
    @Parameter(name = "instanceId", description = "插件实例 ID", required = true, example = "1")
    @Parameter(name = "type", description = "日志类型：stdout（标准输出）、stderr（错误输出）、all（全部）", required = false, example = "all")
    @Parameter(name = "lines", description = "返回的行数", required = false, example = "50")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<List<String>> getPluginLogTail(
            @PathVariable Long instanceId,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "50") int lines) {

        // Get plugin instance to find container ID
        DataPluginInstance instance = pluginInstanceMapper.selectById(instanceId);
        if (instance == null) {
            return Result.error(404, "Plugin instance not found");
        }

        String containerId = instance.getContainerId();
        if (containerId == null || containerId.isEmpty()) {
            return Result.error(400, "Plugin instance is not associated with a container");
        }

        // Get log tail
        List<String> logs = dockerLogService.getContainerLogs(containerId, type, lines);

        return Result.success(logs);
    }
}
