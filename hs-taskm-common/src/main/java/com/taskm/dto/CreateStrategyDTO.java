package com.taskm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * DTO for creating a new strategy.
 */
@Data
public class CreateStrategyDTO {

    /**
     * Strategy name (required).
     */
    @NotBlank(message = "策略名称不能为空")
    private String name;

    /**
     * Strategy description.
     */
    private String description;

    /**
     * Programming language (required).
     * Supported values: python, javascript, java
     */
    @NotBlank(message = "编程语言不能为空")
    private String language;

    /**
     * Strategy code to be executed (required).
     */
    @NotBlank(message = "策略代码不能为空")
    private String code;

    /**
     * Custom Docker image ID for strategy execution (optional).
     * <p>If specified, this image will be used instead of the default image for the language.
     * <p>Example: "my-registry.com/strategies/custom-python:1.0.0"
     */
    private String dockerImageId;

    /**
     * Configuration parameter definitions in JSON format.
     * <p>Example:
     * <pre>
     * {
     *   "symbol": {
     *     "type": "string",
     *     "description": "Trading symbol",
     *     "required": true
     *   },
     *   "quantity": {
     *     "type": "number",
     *     "description": "Trade quantity",
     *     "default": 100
     *   }
     * }
     * </pre>
     */
    private Map<String, Object> configParameters;

    /**
     * Default values for strategy parameters in JSON format.
     * <p>Example:
     * <pre>
     * {
     *   "symbol": "BTCUSDT",
     *   "quantity": 100,
     *   "interval": "1h"
     * }
     * </pre>
     */
    private Map<String, Object> parameterDefaults;
}
