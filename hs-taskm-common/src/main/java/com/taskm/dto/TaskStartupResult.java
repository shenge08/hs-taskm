package com.taskm.dto;

import lombok.Data;

/**
 * Result of task startup operation.
 */
@Data
public class TaskStartupResult {

    /**
     * Task ID.
     */
    private Long taskId;

    /**
     * Task status after startup attempt.
     */
    private String status;

    /**
     * Docker container ID (null if startup failed).
     */
    private String containerId;

    /**
     * Error message (null if startup succeeded).
     */
    private String errorMessage;

    /**
     * Whether the startup was successful.
     */
    private boolean success;

    /**
     * Create a successful result.
     */
    public static TaskStartupResult success(Long taskId, String containerId) {
        TaskStartupResult result = new TaskStartupResult();
        result.setTaskId(taskId);
        result.setStatus("RUNNING");
        result.setContainerId(containerId);
        result.setSuccess(true);
        return result;
    }

    /**
     * Create a failed result.
     */
    public static TaskStartupResult failure(Long taskId, String errorMessage) {
        TaskStartupResult result = new TaskStartupResult();
        result.setTaskId(taskId);
        result.setStatus("FAILED");
        result.setErrorMessage(errorMessage);
        result.setSuccess(false);
        return result;
    }
}
