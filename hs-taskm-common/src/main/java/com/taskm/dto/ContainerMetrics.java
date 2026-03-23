package com.taskm.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Container resource usage metrics.
 */
@Data
public class ContainerMetrics {

    /**
     * Container ID.
     */
    private String containerId;

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
     * Network RX bytes (received).
     */
    private Long networkRxBytes;

    /**
     * Network TX bytes (transmitted).
     */
    private Long networkTxBytes;

    /**
     * Block read bytes.
     */
    private Long blockReadBytes;

    /**
     * Block write bytes.
     */
    private Long blockWriteBytes;

    /**
     * Timestamp when metrics were collected.
     */
    private LocalDateTime timestamp;
}
