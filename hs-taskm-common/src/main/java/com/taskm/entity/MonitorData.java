package com.taskm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Monitor data entity.
 * Represents container resource usage metrics collected at a specific time.
 */
@Data
@TableName("monitor_data")
public class MonitorData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Docker container ID being monitored.
     */
    private String containerId;

    /**
     * Task ID associated with this container.
     */
    private Long taskId;

    /**
     * CPU usage percentage (0-100).
     */
    private Double cpuUsage;

    /**
     * Memory usage in bytes.
     */
    private Long memoryUsage;

    /**
     * Memory limit in bytes.
     */
    private Long memoryLimit;

    /**
     * Memory usage percentage (0-100).
     */
    private Double memoryUsagePercent;

    /**
     * Network RX bytes (total bytes received).
     */
    private Long networkRxBytes;

    /**
     * Network TX bytes (total bytes sent).
     */
    private Long networkTxBytes;

    /**
     * Block I/O read bytes (total bytes read).
     */
    private Long blockReadBytes;

    /**
     * Block I/O write bytes (total bytes written).
     */
    private Long blockWriteBytes;

    /**
     * Collection timestamp.
     */
    private LocalDateTime timestamp;

    /**
     * Record creation timestamp.
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
