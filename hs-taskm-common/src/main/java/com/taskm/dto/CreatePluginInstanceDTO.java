package com.taskm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * DTO for creating a new plugin instance.
 */
@Data
public class CreatePluginInstanceDTO {

    /**
     * Plugin ID (required).
     */
    @NotNull(message = "Plugin ID is required")
    private Long pluginId;

    /**
     * Instance name (required).
     */
    @NotNull(message = "Instance name is required")
    @Size(min = 1, max = 100, message = "Instance name must be between 1 and 100 characters")
    private String name;

    /**
     * Whether this is the default instance.
     */
    private Boolean isDefault;

    /**
     * Instance configuration values (JSON).
     * Overrides plugin default values.
     */
    private Map<String, Object> config;
}
