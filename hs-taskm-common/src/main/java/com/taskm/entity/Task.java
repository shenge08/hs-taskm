package com.taskm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.taskm.handler.JsonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Task entity.
 * Represents a trading strategy execution task.
 */
@Data
@TableName("task")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Foreign key to Strategy.
     */
    private Long strategyId;

    /**
     * Task status.
     * Values: PENDING, RUNNING, COMPLETED, FAILED, STOPPED
     */
    private String status;

    /**
     * Docker container ID.
     * Populated after container creation.
     */
    private String containerId;

    /**
     * Plugin instance ID.
     * Populated when a plugin instance is used for the task.
     */
    private Long pluginInstanceId;

    /**
     * Listener instance ID.
     * Populated when a listener instance is used for the task.
     */
    private Long listenerInstanceId;

    /**
     * Plugin endpoint URL.
     * Format: http://plugin-{pluginId}:{port}/instances/{instanceName}/api
     */
    private String pluginEndpoint;

    /**
     * Listener endpoint URL.
     * Format: http://listener-{listenerId}:{port}/instances/{instanceName}/api
     */
    private String listenerEndpoint;

    /**
     * Task parameters (JSON).
     * Includes strategy parameters and any plugin/listener configurations.
     */
    @TableField(typeHandler = JsonTypeHandler.class)
    private Map<String, Object> parameters;

    /**
     * Task execution result (JSON).
     * Populated after task completion.
     */
    @TableField(typeHandler = JsonTypeHandler.class)
    private Map<String, Object> result;

    /**
     * Error message if task failed.
     */
    private String errorMessage;

    /**
     * Task creation timestamp.
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * Task start timestamp.
     */
    private LocalDateTime startedAt;

    /**
     * Task completion timestamp.
     */
    private LocalDateTime completedAt;
}
