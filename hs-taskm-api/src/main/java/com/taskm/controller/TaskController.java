package com.taskm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskm.dto.ContainerMetrics;
import com.taskm.dto.CreateTaskDTO;
import com.taskm.dto.LogResult;
import com.taskm.dto.Result;
import com.taskm.dto.TaskStartupResult;
import com.taskm.entity.MonitorData;
import com.taskm.entity.Task;
import com.taskm.service.LogService;
import com.taskm.service.ResourceMonitor;
import com.taskm.service.TaskOrchestrator;
import com.taskm.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务管理 REST Controller
 *
 * <p>提供任务的创建、查询、启动、停止、日志查看和资源监控等 REST API 接口
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "任务管理", description = "任务的创建、执行、监控和日志查询接口")
public class TaskController {

    private final TaskService taskService;
    private final TaskOrchestrator taskOrchestrator;
    private final ResourceMonitor resourceMonitor;
    private final LogService logService;

    @Autowired
    public TaskController(TaskService taskService, TaskOrchestrator taskOrchestrator, ResourceMonitor resourceMonitor, LogService logService) {
        this.taskService = taskService;
        this.taskOrchestrator = taskOrchestrator;
        this.resourceMonitor = resourceMonitor;
        this.logService = logService;
    }

    /**
     * 创建新任务
     *
     * @param taskDTO 任务创建请求
     * @return 创建的任务
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建任务", description = "创建一个新的任务，指定策略、插件和监听器配置")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "任务创建成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Task> createTask(@RequestBody CreateTaskDTO taskDTO) {
        Task task = taskService.createTask(taskDTO);
        return Result.success("Task created successfully", task);
    }

    /**
     * 获取所有任务列表（分页）
     *
     * @param page 页码（默认：0）
     * @param size 每页大小（默认：20，最大：100）
     * @param status 按状态过滤（可选）
     * @param sort 排序字段（可选：createdAt、startedAt、completedAt）
     * @return 分页任务列表
     */
    @GetMapping
    @Operation(summary = "获取任务列表", description = "分页获取所有任务，支持按状态过滤和排序")
    @Parameter(name = "page", description = "页码，从 0 开始", required = false, example = "0")
    @Parameter(name = "size", description = "每页大小，默认 20，最大 100", required = false, example = "20")
    @Parameter(name = "status", description = "任务状态：CREATED, RUNNING, STOPPED, COMPLETED, FAILED", required = false)
    @Parameter(name = "sort", description = "排序字段：createdAt（创建时间）、startedAt（启动时间）、completedAt（完成时间）", required = false)
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<IPage<Task>> getAllTasks(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "createdAt") String sort) {

        // Limit max page size
        if (size > 100) {
            size = 100;
        }

        Page<Task> pageRequest = new Page<>(page, size);

        // Set sort order (default descending)
        String dbColumn = switch (sort) {
            case "createdAt" -> "created_at";
            case "startedAt" -> "started_at";
            case "completedAt" -> "completed_at";
            default -> "created_at";
        };
        pageRequest.addOrder(OrderItem.desc(dbColumn));

        IPage<Task> tasks = taskService.getTasks(pageRequest, status);
        return Result.success(tasks);
    }

    /**
     * 获取任务统计信息
     *
     * @return 状态与计数的映射
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取任务统计", description = "获取各状态任务的数量统计")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Map<String, Long>> getTaskStatistics() {
        Map<String, Long> statistics = taskService.getTaskStatistics();
        return Result.success(statistics);
    }

    /**
     * 根据 ID 获取任务详情
     *
     * @param id 任务 ID
     * @return 任务详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取任务详情", description = "根据任务 ID 查询任务的详细信息")
    @Parameter(name = "id", description = "任务 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "任务不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Task> getTask(@PathVariable Long id) {
        Task task = taskService.getTask(id);
        return Result.success(task);
    }

    /**
     * 启动任务
     *
     * @param id 任务 ID
     * @return 任务启动结果，包含容器 ID
     */
    @PostMapping("/{id}/start")
    @Operation(summary = "启动任务", description = "启动指定任务，创建并启动策略、插件和监听器容器")
    @Parameter(name = "id", description = "任务 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "任务启动成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "任务状态错误或配置无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "任务不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<TaskStartupResult> startTask(@PathVariable Long id) {
        TaskStartupResult result = taskOrchestrator.startTask(id);
        if (result.isSuccess()) {
            return Result.success("Task started successfully", result);
        } else {
            return Result.error(400, result.getErrorMessage());
        }
    }

    /**
     * 停止任务
     *
     * @param id 任务 ID
     * @return 更新后的任务状态
     */
    @PostMapping("/{id}/stop")
    @Operation(summary = "停止任务", description = "停止正在运行的任务，停止所有相关容器")
    @Parameter(name = "id", description = "任务 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "任务停止成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "任务不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> stopTask(@PathVariable Long id) {
        String status = taskOrchestrator.stopTask(id);
        return Result.success("Task stopped successfully", status);
    }

    /**
     * 获取任务的当前资源监控数据
     *
     * @param id 任务 ID
     * @return 当前容器指标
     */
    @GetMapping("/{id}/metrics")
    @Operation(summary = "获取任务资源监控", description = "获取任务容器的实时资源使用情况（CPU、内存、磁盘 I/O、网络 I/O）")
    @Parameter(name = "id", description = "任务 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "任务不存在或无监控数据"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ContainerMetrics> getTaskMetrics(@PathVariable Long id) {
        ContainerMetrics metrics = resourceMonitor.getCurrentMetrics(id);
        if (metrics == null) {
            return Result.error(404, "No metrics found for task");
        }
        return Result.success(metrics);
    }

    /**
     * 获取任务的历史资源监控数据
     *
     * @param id 任务 ID
     * @param startTime 开始时间（可选，默认为 1 小时前）
     * @param endTime 结束时间（可选，默认为当前时间）
     * @return 监控数据列表
     */
    @GetMapping("/{id}/metrics/history")
    @Operation(summary = "获取历史监控数据", description = "获取指定时间范围内的任务资源监控历史数据")
    @Parameter(name = "id", description = "任务 ID", required = true, example = "1")
    @Parameter(name = "startTime", description = "开始时间（ISO 8601 格式），默认为 1 小时前", required = false, example = "2026-03-23T10:00:00")
    @Parameter(name = "endTime", description = "结束时间（ISO 8601 格式），默认为当前时间", required = false, example = "2026-03-23T11:00:00")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "时间格式错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "任务不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<List<MonitorData>> getTaskMetricsHistory(
        @PathVariable Long id,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        // Default to last 1 hour if not specified
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        if (startTime == null) {
            startTime = endTime.minusHours(1);
        }

        List<MonitorData> history = resourceMonitor.getMetricsHistory(id, startTime, endTime);
        return Result.success(history);
    }

    /**
     * 获取任务日志（分页）
     *
     * @param id 任务 ID
     * @param type 容器类型（strategy、plugin、listener）
     * @param offset 起始行号（默认：0）
     * @param limit 最大行数（默认：100）
     * @return 日志结果，包含行数据和元数据
     */
    @GetMapping("/{id}/logs")
    @Operation(summary = "获取任务日志", description = "分页获取指定类型的任务日志")
    @Parameter(name = "id", description = "任务 ID", required = true, example = "1")
    @Parameter(name = "type", description = "容器类型：strategy（策略）、plugin（插件）、listener（监听器）", required = false, example = "strategy")
    @Parameter(name = "offset", description = "起始行号，从 0 开始", required = false, example = "0")
    @Parameter(name = "limit", description = "最大返回行数", required = false, example = "100")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "任务不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<LogResult> getTaskLogs(
        @PathVariable Long id,
        @RequestParam(defaultValue = "strategy") String type,
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "100") int limit) {

        LogService.LogMetadata metadata = logService.getLogMetadata(id, type);
        List<String> lines = logService.getTaskLogs(id, type, offset, limit);

        LogResult result = LogResult.of(id, metadata.getFilePath(), metadata.getFileSize(),
            metadata.getLineCount(), metadata.getLastModified(), lines);
        result.setOffset(offset);
        result.setLimit(limit);

        return Result.success(result);
    }

    /**
     * 获取任务日志尾部内容
     *
     * @param id 任务 ID
     * @param type 容器类型（strategy、plugin、listener）
     * @param lines 返回的行数（默认：50）
     * @return 日志结果，包含尾部行数据
     */
    @GetMapping("/{id}/logs/tail")
    @Operation(summary = "获取日志尾部", description = "获取指定类型任务日志的最后 N 行内容")
    @Parameter(name = "id", description = "任务 ID", required = true, example = "1")
    @Parameter(name = "type", description = "容器类型：strategy（策略）、plugin（插件）、listener（监听器）", required = false, example = "strategy")
    @Parameter(name = "lines", description = "返回的行数", required = false, example = "50")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "任务不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<LogResult> getLogTail(
        @PathVariable Long id,
        @RequestParam(defaultValue = "strategy") String type,
        @RequestParam(defaultValue = "50") int lines) {

        LogService.LogMetadata metadata = logService.getLogMetadata(id, type);
        List<String> logLines = logService.getLogTail(id, type, lines);

        LogResult result = LogResult.of(id, metadata.getFilePath(), metadata.getFileSize(),
            metadata.getLineCount(), metadata.getLastModified(), logLines);
        result.setCount(logLines.size());

        return Result.success(result);
    }

    /**
     * 搜索日志中的关键词
     *
     * @param id 任务 ID
     * @param type 容器类型（strategy、plugin、listener）
     * @param keyword 要搜索的关键词
     * @param limit 最大结果数量（可选，默认：100）
     * @return 日志结果，包含匹配的行
     */
    @GetMapping("/{id}/logs/search")
    @Operation(summary = "搜索日志", description = "在指定类型的任务日志中搜索包含关键词的行")
    @Parameter(name = "id", description = "任务 ID", required = true, example = "1")
    @Parameter(name = "type", description = "容器类型：strategy（策略）、plugin（插件）、listener（监听器）", required = false, example = "strategy")
    @Parameter(name = "keyword", description = "要搜索的关键词", required = true, example = "ERROR")
    @Parameter(name = "limit", description = "最大返回结果数", required = false, example = "100")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "搜索成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "关键词为空"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "任务不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<LogResult> searchLogs(
        @PathVariable Long id,
        @RequestParam(defaultValue = "strategy") String type,
        @RequestParam String keyword,
        @RequestParam(defaultValue = "100") int limit) {

        List<String> lines = logService.searchLogs(id, type, keyword, limit);

        LogResult result = new LogResult();
        result.setTaskId(id);
        result.setLines(lines);
        result.setCount(lines.size());

        return Result.success(result);
    }
}
