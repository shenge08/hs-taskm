package com.taskm.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * View Object for plugin instance.
 * Used for API responses.
 */
@Data
public class PluginInstanceVO {

    /**
     * Primary key.
     */
    private Long id;

    /**
     * Foreign key to the parent plugin.
     */
    private Long pluginId;

    /**
     * Instance name.
     */
    private String name;

    /**
     * Whether this is the default instance.
     */
    private Boolean isDefault;

    /**
     * Instance configuration values (JSON).
     */
    private Map<String, Object> config;

    /**
     * Docker container ID if running.
     */
    private String containerId;

    /**
     * Instance status: RUNNING, STOPPED.
     */
    private String status;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    private LocalDateTime updatedAt;
}
