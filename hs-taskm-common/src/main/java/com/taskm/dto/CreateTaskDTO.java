package com.taskm.dto;

import lombok.Data;

import java.util.Map;

/**
 * DTO for creating a new task.
 */
@Data
public class CreateTaskDTO {

    /**
     * Task name.
     */
    private String name;

    /**
     * Strategy ID (required).
     */
    private Long strategyId;

    /**
     * Data plugin ID (optional).
     */
    private Long pluginId;

    /**
     * Plugin instance ID (optional).
     * If specified, takes precedence over pluginId.
     */
    private Long pluginInstanceId;

    /**
     * Listener ID (optional).
     */
    private Long listenerId;

    /**
     * Listener instance ID (optional).
     * If specified, takes precedence over listenerId.
     */
    private Long listenerInstanceId;

    /**
     * Strategy parameters (JSON).
     */
    private Map<String, Object> strategyParams;

    /**
     * Plugin parameters (JSON, optional).
     * Overrides plugin default values.
     */
    private Map<String, Object> pluginParams;

    /**
     * Listener parameters (JSON, optional).
     * Overrides listener default values.
     */
    private Map<String, Object> listenerParams;
}
