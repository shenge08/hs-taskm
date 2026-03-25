package com.taskm.listener.sample.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

/**
 * Request DTO for task-started events.
 *
 * <p>This DTO contains the data sent when a task starts execution.
 * Strategies using the ListenerClient.onTaskStarted() method send this data.</p>
 */
@Data
public class TaskStartedRequest {

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
     * Optional message describing the task start.
     */
    @JsonProperty("message")
    private String message;

    /**
     * Optional metadata about the task.
     */
    @JsonProperty("metadata")
    private Object metadata;
}
