package com.taskm.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * DTO for updating an existing listener instance.
 * All fields are optional - only provided fields will be updated.
 */
@Data
public class UpdateListenerInstanceDTO {

    /**
     * Instance name.
     */
    @Size(min = 1, max = 100, message = "Instance name must be between 1 and 100 characters")
    private String name;

    /**
     * Whether this is the default instance.
     */
    private Boolean isDefault;

    /**
     * Instance configuration values (JSON).
     * Overrides listener default values.
     */
    private Map<String, Object> config;

    /**
     * Instance status: RUNNING, STOPPED.
     */
    private String status;

    /**
     * Docker container ID if the instance is running.
     */
    private String containerId;
}
