package com.taskm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Strategy entity representing a trading strategy.
 * Strategies can be written in multiple programming languages and executed in containers.
 */
@Data
@TableName("strategy")
public class Strategy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Unique strategy name
     */
    private String name;

    /**
     * Strategy description
     */
    private String description;

    /**
     * Programming language (python, javascript, java, etc.)
     */
    private String language;

    /**
     * Strategy code to be executed
     */
    private String code;

    /**
     * Custom Docker image ID for strategy execution.
     * <p>If specified, this image will be used instead of the default image for the language.
     * <p>Example: "my-registry.com/strategies/custom-python:1.0.0"
     */
    private String dockerImageId;

    /**
     * Configuration parameter definitions in JSON format
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
    private String configParameters;

    /**
     * Default values for strategy parameters in JSON format
     * <p>Example:
     * <pre>
     * {
     *   "symbol": "BTCUSDT",
     *   "quantity": 100,
     *   "interval": "1h"
     * }
     * </pre>
     */
    private String parameterDefaults;

    /**
     * Creation timestamp
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
