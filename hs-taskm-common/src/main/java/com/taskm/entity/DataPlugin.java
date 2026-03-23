package com.taskm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.taskm.handler.JsonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Plugin entity.
 * Represents a data plugin that can be used to fetch external data.
 */
@Data
@TableName(value = "data_plugin", autoResultMap = true)
public class DataPlugin implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Plugin name (unique).
     */
    private String name;

    /**
     * Plugin description.
     */
    private String description;

    /**
     * Plugin type (e.g., "market_data", "news", "analytics").
     */
    private String pluginType;

    /**
     * Programming language (python, javascript, java).
     */
    private String language;

    /**
     * Plugin code (stored as text).
     */
    private String code;

    /**
     * Docker image name for this plugin.
     * Format: "repository/image:tag" e.g., "myrepo/stock-plugin:latest"
     */
    private String imageName;

    /**
     * Configuration parameters (JSON).
     * Format: [{"name":"symbol","type":"string","default":"AAPL","required":true}]
     */
    @TableField(typeHandler = JsonTypeHandler.class)
    private Map<String, Object> configParameters;

    /**
     * Plugin metadata (JSON).
     * Format: {"author":"name","version":"1.0","description":"Get stock quotes"}
     */
    @TableField(typeHandler = JsonTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * Creation timestamp.
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
