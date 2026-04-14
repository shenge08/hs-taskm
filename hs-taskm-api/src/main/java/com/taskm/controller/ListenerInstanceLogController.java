package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.entity.ListenerInstance;
import com.taskm.mapper.ListenerInstanceMapper;
import com.taskm.service.DockerLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Listener Instance Log Controller
 *
 * <p>Provides REST API endpoints for fetching logs from listener instance containers
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/listener-instances")
@Tag(name = "监听器实例日志", description = "监听器实例容器日志查询接口")
public class ListenerInstanceLogController {

    private final ListenerInstanceMapper listenerInstanceMapper;
    private final DockerLogService dockerLogService;

    @Autowired
    public ListenerInstanceLogController(
        ListenerInstanceMapper listenerInstanceMapper,
            DockerLogService dockerLogService) {
        this.listenerInstanceMapper = listenerInstanceMapper;
        this.dockerLogService = dockerLogService;
    }

    /**
     * Get logs from listener instance container
     *
     * @param instanceId listener instance ID
     * @param type log type: stdout, stderr, or all (default: all)
     * @param offset starting line number (default: 0)
     * @param limit maximum number of lines to return (default: 100)
     * @return list of log lines
     */
    @GetMapping("/{instanceId}/logs")
    @Operation(summary = "获取监听器实例日志", description = "从监听器实例容器获取日志")
    @Parameter(name = "instanceId", description = "监听器实例 ID", required = true, example = "1")
    @Parameter(name = "type", description = "日志类型：stdout（标准输出）、stderr（错误输出）、all（全部）", required = false, example = "all")
    @Parameter(name = "offset", description = "起始行号，从 0 开始", required = false, example = "0")
    @Parameter(name = "limit", description = "最大返回行数", required = false, example = "100")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<List<String>> getListenerInstanceLogs(
            @PathVariable Long instanceId,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "100") int limit) {

        // Get listener instance to find container ID
        ListenerInstance instance = listenerInstanceMapper.selectById(instanceId);
        if (instance == null) {
            return Result.error(404, "Listener instance not found");
        }

        String containerId = instance.getContainerId();
        if (containerId == null || containerId.isEmpty()) {
            return Result.error(400, "Listener instance is not associated with a container");
        }

        // Get logs with pagination
        List<String> logs = dockerLogService.getContainerLogs(containerId, offset, limit);

        return Result.success(logs);
    }

    /**
     * Get the last N lines of listener instance logs
     *
     * @param instanceId listener instance ID
     * @param type log type: stdout, stderr, or all (default: all)
     * @param lines number of lines to return from the end (default: 50)
     * @return list of log lines
     */
    @GetMapping("/{instanceId}/logs/tail")
    @Operation(summary = "获取监听器实例日志尾部", description = "获取监听器实例容器日志的最后 N 行")
    @Parameter(name = "instanceId", description = "监听器实例 ID", required = true, example = "1")
    @Parameter(name = "type", description = "日志类型：stdout（标准输出）、stderr（错误输出）、all（全部）", required = false, example = "all")
    @Parameter(name = "lines", description = "返回的行数", required = false, example = "50")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<List<String>> getListenerLogTail(
            @PathVariable Long instanceId,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "50") int lines) {

        // Get listener instance to find container ID
        ListenerInstance instance = listenerInstanceMapper.selectById(instanceId);
        if (instance == null) {
            return Result.error(404, "Listener instance not found");
        }

        String containerId = instance.getContainerId();
        if (containerId == null || containerId.isEmpty()) {
            return Result.error(400, "Listener instance is not associated with a container");
        }

        // Get log tail
        List<String> logs = dockerLogService.getContainerLogs(containerId, type, lines);

        return Result.success(logs);
    }
}
