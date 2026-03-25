package com.taskm.listener.sample.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Request DTO for task-completed events.
 *
 * <p>This DTO contains the data sent when a task completes successfully.
 * Strategies using the ListenerClient.onTaskCompleted(result) method send this data.</p>
 */
@Data
public class TaskCompletedRequest {

    /**
     * Task ID from TaskM system.
     */
    @JsonProperty("task_id")
    private Long taskId;

    /**
     * Timestamp when the event occurred (ISO 8601 format).
     */
    @JsonProperty("timestamp")
    private Instant timestamp;

    /**
     * Task execution result data.
     * This is the data passed to ListenerClient.onTaskCompleted().
     */
    @JsonProperty("result")
    private Map<String, Object> result;

    /**
     * Optional message describing the task completion.
     */
    @JsonProperty("message")
    private String message;
}
