package com.taskm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * DTO for updating an existing strategy.
 * All fields are optional - only provided fields will be updated.
 */
@Data
public class UpdateStrategyDTO {

    /**
     * Strategy name.
     */
    private String name;

    /**
     * Strategy description.
     */
    private String description;

    /**
     * Programming language.
     * Supported values: python, javascript, java
     */
    private String language;

    /**
     * Strategy code to be executed.
     */
    private String code;

    /**
     * Custom Docker image ID for strategy execution (optional).
     * <p>If specified, this image will be used instead of the default image for the language.
     * <p>Example: "my-registry.com/strategies/custom-python:1.0.0"
     */
    private String dockerImageId;

    /**
     * Configuration parameter definitions in JSON format.
     * <p>See {@link CreateStrategyDTO#getConfigParameters()} for example format.
     */
    private Map<String, Object> configParameters;

    /**
     * Default values for strategy parameters in JSON format.
     * <p>See {@link CreateStrategyDTO#getParameterDefaults()} for example format.
     */
    private Map<String, Object> parameterDefaults;
}
