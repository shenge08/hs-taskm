package com.taskm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.taskm.handler.JsonTypeHandler;
import java.util.List;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Listener entity.
 * Represents an event listener that can trigger notifications.
 */
@Data
@TableName("listener")
public class Listener implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Listener name (unique).
     */
    private String name;

    /**
     * Listener description.
     */
    private String description;

    /**
     * Event type that triggers this listener.
     * Examples: TASK_STARTED, TASK_COMPLETED, TASK_FAILED, SIGNAL_GENERATED
     */
    private String eventType;

    /**
     * Programming language (python, javascript, java).
     */
    private String language;

    /**
     * Listener code (stored as text).
     */
    private String code;

    /**
     * Docker image name for this listener.
     * Format: "repository/image:tag" e.g., "myrepo/webhook-listener:latest"
     */
    private String imageName;

    /**
     * Parameters definition (JSON).
     * Format: [{"name":"recipients","type":"array","default":["admin@example.com"],"required":true}]
     */
    @TableField(typeHandler = JsonTypeHandler.class)
    private Map<String, Object> parameters;

    /**
     * Parameter default values (JSON).
     * Format: {"recipients":["admin@example.com"],"subject":"Trade signal notification"}
     */
    @TableField(typeHandler = JsonTypeHandler.class)
    private List<Map<String, Object>> parameterDefaults;

    /**
     * Listener metadata (JSON).
     * Format: {"author":"name","version":"1.0","description":"Send notifications"}
     */
    @TableField(typeHandler = JsonTypeHandler.class)
    private List<Map<String, Object>> metadata;

    /**
     * Whether the listener is enabled.
     */
    private Boolean enabled;

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
