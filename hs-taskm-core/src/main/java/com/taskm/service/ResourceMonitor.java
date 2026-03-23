package com.taskm.service;

import com.taskm.dto.ContainerMetrics;
import com.taskm.entity.MonitorData;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for resource monitoring.
 * Monitors container resource usage (CPU, memory, network, I/O).
 */
public interface ResourceMonitor {

    /**
     * Start monitoring a container.
     *
     * @param containerId container ID
     * @param taskId task ID
     * @param intervalSeconds collection interval in seconds
     */
    void startMonitoring(String containerId, Long taskId, int intervalSeconds);

    /**
     * Stop monitoring a container.
     *
     * @param containerId container ID
     */
    void stopMonitoring(String containerId);

    /**
     * Collect metrics for a container and store in database.
     *
     * @param containerId container ID
     * @param taskId task ID
     * @return collected metrics
     */
    MonitorData collectMetrics(String containerId, Long taskId);

    /**
     * Get current metrics for a task.
     *
     * @param taskId task ID
     * @return current container metrics
     */
    ContainerMetrics getCurrentMetrics(Long taskId);

    /**
     * Get metrics history for a task.
     *
     * @param taskId task ID
     * @param startTime start time
     * @param endTime end time
     * @return list of monitor data
     */
    List<MonitorData> getMetricsHistory(Long taskId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Check if a container is being monitored.
     *
     * @param containerId container ID
     * @return true if monitoring is active
     */
    boolean isMonitoring(String containerId);
}
