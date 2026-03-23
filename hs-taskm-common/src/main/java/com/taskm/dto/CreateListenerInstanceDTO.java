package com.taskm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * DTO for creating a new listener instance.
 */
@Data
public class CreateListenerInstanceDTO {

    /**
     * Listener ID (required).
     */
    @NotNull(message = "Listener ID is required")
    private Long listenerId;

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
     * Overrides listener default values.
     */
    private Map<String, Object> config;
}
