package com.taskm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.taskm.handler.JsonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;

/**
 * Data Plugin Instance entity.
 * Represents a named instance of a data plugin with specific configuration.
 */
@Data
@TableName(value = "data_plugin_instance", autoResultMap = true)
public class DataPluginInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Foreign key to the parent plugin.
     */
    private Long pluginId;

    /**
     * Instance name (unique per plugin).
     */
    private String name;

    /**
     * Whether this is the default instance for the plugin.
     */
    private Boolean isDefault;

    /**
     * Instance configuration values (JSON).
     * Overrides plugin default values.
     */
    @TableField(typeHandler = JsonTypeHandler.class)
    private Map<String, Object> config;

    /**
     * Docker container ID if the instance is running.
     */
    private String containerId;

    /**
     * Instance status: RUNNING, STOPPED.
     */
    private String status;

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
