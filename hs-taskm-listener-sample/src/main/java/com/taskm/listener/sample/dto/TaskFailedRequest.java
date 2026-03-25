package com.taskm.listener.sample.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

/**
 * Request DTO for task-failed events.
 *
 * <p>This DTO contains the data sent when a task fails during execution.
 * Strategies using the ListenerClient.onTaskFailed(error) method send this data.</p>
 */
@Data
public class TaskFailedRequest {

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
     * Error message describing the failure.
     * This is the message passed to ListenerClient.onTaskFailed().
     */
    @JsonProperty("error")
    private String error;

    /**
     * Optional stack trace or additional error details.
     */
    @JsonProperty("stack_trace")
    private String stackTrace;
}
