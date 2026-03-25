package com.taskm.listener.sample.controller;

import com.taskm.listener.sample.dto.TaskCompletedRequest;
import com.taskm.listener.sample.dto.TaskFailedRequest;
import com.taskm.listener.sample.dto.TaskStartedRequest;
import com.taskm.listener.sample.service.ListenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for receiving task lifecycle events from strategies.
 *
 * <p>This controller provides three endpoints that match the TaskM SDK's ListenerClient interface:</p>
 *
 * <ul>
 *   <li>POST /task-started - Notify that a task has started</li>
 *   <li>POST /task-completed - Notify that a task completed successfully</li>
 *   <li>POST /task-failed - Notify that a task has failed</li>
 * </ul>
 *
 * <h3>Usage from Strategy Code:</h3>
 * <pre>{@code
 * import com.taskm.sdk.ListenerClient;
 *
 * // The listener endpoint is set via environment variable
 * // LISTENER_ENDPOINT=http://listener-host:8080/api
 * // TASK_ID=123
 *
 * ListenerClient listener = new ListenerClient();
 *
 * // Notify task started
 * listener.onTaskStarted();
 *
 * // Notify task completed
 * listener.onTaskCompleted(Map.of("profit", 100.5, "trades", 5));
 *
 * // Notify task failed
 * listener.onTaskFailed("Connection timeout");
 * }</pre>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
@Tag(name = "监听器接口", description = "接收策略任务生命周期事件的 REST API")
public class ListenerController {

    private final ListenerService listenerService;

    @Autowired
    public ListenerController(ListenerService listenerService) {
        this.listenerService = listenerService;
    }

    /**
     * Handle task-started event.
     *
     * <p>This endpoint is called when a strategy task starts execution.
     * The ListenerClient.onTaskStarted() method in the strategy sends this event.</p>
     *
     * @param request task started request data
     * @return success response
     */
    @PostMapping("/task-started")
    @Operation(
        summary = "任务开始事件",
        description = "接收策略任务开始执行的通知。策略代码调用 ListenerClient.onTaskStarted() 时会发送此事件。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "事件处理成功",
            content = @Content(schema = @Schema(implementation = com.taskm.listener.sample.dto.ListenerResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "请求数据格式错误", content = @Content),
        @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
    })
    public ResponseEntity<com.taskm.listener.sample.dto.ListenerResponse> handleTaskStarted(
        @Parameter(description = "任务开始事件数据", required = true)
        @Valid @RequestBody TaskStartedRequest request
    ) {
        try {
            listenerService.handleTaskStarted(request);
            return ResponseEntity.ok(
                com.taskm.listener.sample.dto.ListenerResponse.success("Task started event received")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                com.taskm.listener.sample.dto.ListenerResponse.error("Failed to process task started: " + e.getMessage())
            );
        }
    }

    /**
     * Handle task-completed event.
     *
     * <p>This endpoint is called when a strategy task completes successfully.
     * The ListenerClient.onTaskCompleted(result) method in the strategy sends this event.</p>
     *
     * @param request task completed request data
     * @return success response
     */
    @PostMapping("/task-completed")
    @Operation(
        summary = "任务完成事件",
        description = "接收策略任务成功完成的通知。策略代码调用 ListenerClient.onTaskCompleted(result) 时会发送此事件，" +
                     "并包含任务执行结果数据。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "事件处理成功",
            content = @Content(schema = @Schema(implementation = com.taskm.listener.sample.dto.ListenerResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "请求数据格式错误", content = @Content),
        @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
    })
    public ResponseEntity<com.taskm.listener.sample.dto.ListenerResponse> handleTaskCompleted(
        @Parameter(description = "任务完成事件数据", required = true)
        @Valid @RequestBody TaskCompletedRequest request
    ) {
        try {
            listenerService.handleTaskCompleted(request);
            return ResponseEntity.ok(
                com.taskm.listener.sample.dto.ListenerResponse.success("Task completed event received")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                com.taskm.listener.sample.dto.ListenerResponse.error("Failed to process task completed: " + e.getMessage())
            );
        }
    }

    /**
     * Handle task-failed event.
     *
     * <p>This endpoint is called when a strategy task fails during execution.
     * The ListenerClient.onTaskFailed(error) method in the strategy sends this event.</p>
     *
     * @param request task failed request data
     * @return success response
     */
    @PostMapping("/task-failed")
    @Operation(
        summary = "任务失败事件",
        description = "接收策略任务执行失败的通知。策略代码调用 ListenerClient.onTaskFailed(error) 时会发送此事件，" +
                     "并包含错误信息。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "事件处理成功",
            content = @Content(schema = @Schema(implementation = com.taskm.listener.sample.dto.ListenerResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "请求数据格式错误", content = @Content),
        @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
    })
    public ResponseEntity<com.taskm.listener.sample.dto.ListenerResponse> handleTaskFailed(
        @Parameter(description = "任务失败事件数据", required = true)
        @Valid @RequestBody TaskFailedRequest request
    ) {
        try {
            listenerService.handleTaskFailed(request);
            return ResponseEntity.ok(
                com.taskm.listener.sample.dto.ListenerResponse.success("Task failed event received")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                com.taskm.listener.sample.dto.ListenerResponse.error("Failed to process task failed: " + e.getMessage())
            );
        }
    }
}
