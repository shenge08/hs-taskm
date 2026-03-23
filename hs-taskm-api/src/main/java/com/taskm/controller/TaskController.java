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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Task management.
 * Provides endpoints for creating, querying, and starting tasks.
 */
@RestController
@RequestMapping("/api/tasks")
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
     * Create a new task.
     *
     * @param taskDTO task creation request
     * @return created task
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Task> createTask(@RequestBody CreateTaskDTO taskDTO) {
        Task task = taskService.createTask(taskDTO);
        return Result.success("Task created successfully", task);
    }

    /**
     * Get all tasks with pagination and filtering.
     *
     * @param page page number (default: 0)
     * @param size page size (default: 20, max: 100)
     * @param status filter by status (optional)
     * @param sort sort field (optional: createdAt, startedAt, completedAt)
     * @return paginated tasks
     */
    @GetMapping
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
     * Get task statistics.
     *
     * @return map of status to count
     */
    @GetMapping("/statistics")
    public Result<Map<String, Long>> getTaskStatistics() {
        Map<String, Long> statistics = taskService.getTaskStatistics();
        return Result.success(statistics);
    }

    /**
     * Get task by ID.
     *
     * @param id task ID
     * @return task details
     */
    @GetMapping("/{id}")
    public Result<Task> getTask(@PathVariable Long id) {
        Task task = taskService.getTask(id);
        return Result.success(task);
    }

    /**
     * Start a task.
     *
     * @param id task ID
     * @return task startup result with container ID
     */
    @PostMapping("/{id}/start")
    public Result<TaskStartupResult> startTask(@PathVariable Long id) {
        TaskStartupResult result = taskOrchestrator.startTask(id);
        if (result.isSuccess()) {
            return Result.success("Task started successfully", result);
        } else {
            return Result.error(400, result.getErrorMessage());
        }
    }

    /**
     * Stop a task.
     *
     * @param id task ID
     * @return updated task status
     */
    @PostMapping("/{id}/stop")
    public Result<String> stopTask(@PathVariable Long id) {
        String status = taskOrchestrator.stopTask(id);
        return Result.success("Task stopped successfully", status);
    }

    /**
     * Get current resource metrics for a task.
     *
     * @param id task ID
     * @return current container metrics
     */
    @GetMapping("/{id}/metrics")
    public Result<ContainerMetrics> getTaskMetrics(@PathVariable Long id) {
        ContainerMetrics metrics = resourceMonitor.getCurrentMetrics(id);
        if (metrics == null) {
            return Result.error(404, "No metrics found for task");
        }
        return Result.success(metrics);
    }

    /**
     * Get metrics history for a task.
     *
     * @param id task ID
     * @param startTime start time (optional, defaults to 1 hour ago)
     * @param endTime end time (optional, defaults to now)
     * @return list of monitor data
     */
    @GetMapping("/{id}/metrics/history")
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
     * Get task logs with pagination.
     *
     * @param id task ID
     * @param type container type (strategy, plugin, listener)
     * @param offset starting line number (default: 0)
     * @param limit maximum number of lines (default: 100)
     * @return log result with lines and metadata
     */
    @GetMapping("/{id}/logs")
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
     * Get the last N lines of task logs.
     *
     * @param id task ID
     * @param type container type (strategy, plugin, listener)
     * @param lines number of lines to return (default: 50)
     * @return log result with tail lines
     */
    @GetMapping("/{id}/logs/tail")
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
     * Search logs for keyword.
     *
     * @param id task ID
     * @param type container type (strategy, plugin, listener)
     * @param keyword keyword to search for
     * @param limit maximum number of results (optional, default: 100)
     * @return log result with matching lines
     */
    @GetMapping("/{id}/logs/search")
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
