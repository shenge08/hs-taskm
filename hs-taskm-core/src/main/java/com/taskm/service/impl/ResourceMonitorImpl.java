package com.taskm.service.impl;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.taskm.dto.ContainerMetrics;
import com.taskm.entity.MonitorData;
import com.taskm.exception.ContainerException;
import com.taskm.mapper.MonitorDataMapper;
import com.taskm.service.ContainerLifecycleManager;
import com.taskm.service.ResourceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of resource monitoring.
 * Monitors container resource usage (CPU, memory, network, I/O).
 */
@Service
public class ResourceMonitorImpl implements ResourceMonitor {

    private static final Logger logger = LoggerFactory.getLogger(ResourceMonitorImpl.class);

    private final MonitorDataMapper monitorDataMapper;
    private final ContainerLifecycleManager containerLifecycleManager;

    // Map to track monitoring status: containerId -> taskId
    private final Map<String, MonitoringTask> monitoringTasks = new ConcurrentHashMap<>();

    @Value("${monitoring.default.interval:30}")
    private int defaultIntervalSeconds;

    @Value("${monitoring.retention.days:7}")
    private int retentionDays;

    @Autowired
    public ResourceMonitorImpl(
            MonitorDataMapper monitorDataMapper,
            ContainerLifecycleManager containerLifecycleManager) {
        this.monitorDataMapper = monitorDataMapper;
        this.containerLifecycleManager = containerLifecycleManager;
    }

    @Override
    public void startMonitoring(String containerId, Long taskId, int intervalSeconds) {
        logger.info("Starting monitoring for container {} (task {}) with interval {}s",
            containerId, taskId, intervalSeconds);

        monitoringTasks.put(containerId, new MonitoringTask(taskId, intervalSeconds));
    }

    @Override
    public void stopMonitoring(String containerId) {
        logger.info("Stopping monitoring for container {}", containerId);
        monitoringTasks.remove(containerId);
    }

    @Override
    public boolean isMonitoring(String containerId) {
        return monitoringTasks.containsKey(containerId);
    }

    @Scheduled(fixedRateString = "${monitoring.schedule.rate:5000}")
    public void scheduledMetricCollection() {
        if (monitoringTasks.isEmpty()) {
            return;
        }

        logger.debug("Collecting metrics for {} containers", monitoringTasks.size());

        monitoringTasks.forEach((containerId, task) -> {
            try {
                collectMetrics(containerId, task.taskId);
            } catch (Exception e) {
                logger.error("Failed to collect metrics for container {}", containerId, e);
            }
        });
    }

    @Override
    public MonitorData collectMetrics(String containerId, Long taskId) {
        try {
            // Get container stats from Docker
            // Note: ContainerLifecycleManager.getContainerMetrics() is not implemented yet
            // For now, we'll create placeholder data
            MonitorData data = new MonitorData();
            data.setContainerId(containerId);
            data.setTaskId(taskId);
            data.setTimestamp(LocalDateTime.now());

            // TODO: Get actual metrics from Docker
            // ContainerMetrics metrics = containerLifecycleManager.getContainerMetrics(containerId);
            // For now, set placeholder values
            data.setCpuUsage(0.0);
            data.setMemoryUsage(0L);
            data.setMemoryLimit(0L);
            data.setMemoryUsagePercent(0.0);
            data.setNetworkRxBytes(0L);
            data.setNetworkTxBytes(0L);
            data.setBlockReadBytes(0L);
            data.setBlockWriteBytes(0L);

            // Save to database
            monitorDataMapper.insert(data);

            return data;

        } catch (Exception e) {
            logger.error("Failed to collect metrics for container {}", containerId, e);
            throw new ContainerException("Failed to collect metrics: " + e.getMessage(), e);
        }
    }

    @Override
    public ContainerMetrics getCurrentMetrics(Long taskId) {
        // Get latest metrics from database
        MonitorData latestData = monitorDataMapper.getLatestMetricsByTaskId(taskId);

        if (latestData == null) {
            return null;
        }

        // Convert to ContainerMetrics DTO
        ContainerMetrics metrics = new ContainerMetrics();
        metrics.setContainerId(latestData.getContainerId());
        metrics.setCpuUsage(latestData.getCpuUsage());
        metrics.setMemoryUsage(latestData.getMemoryUsage());
        metrics.setMemoryLimit(latestData.getMemoryLimit());
        metrics.setMemoryUsagePercent(latestData.getMemoryUsagePercent());
        metrics.setNetworkRxBytes(latestData.getNetworkRxBytes());
        metrics.setNetworkTxBytes(latestData.getNetworkTxBytes());
        metrics.setBlockReadBytes(latestData.getBlockReadBytes());
        metrics.setBlockWriteBytes(latestData.getBlockWriteBytes());
        metrics.setTimestamp(latestData.getTimestamp());

        return metrics;
    }

    @Override
    public List<MonitorData> getMetricsHistory(Long taskId, LocalDateTime startTime, LocalDateTime endTime) {
        return monitorDataMapper.getMetricsByTaskIdAndTimeRange(taskId, startTime, endTime);
    }

    /**
     * Clean up old monitoring data.
     * Runs daily at 2 AM.
     */
    @Scheduled(cron = "${monitoring.cleanup.cron:0 0 2 * * ?}")
    public void cleanupOldMetrics() {
        logger.info("Cleaning up monitoring data older than {} days", retentionDays);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        // Delete old data
        // Note: MyBatis-Plus doesn't have a simple way to do this without XML
        // For now, we'll use a simple approach
        int deleted = monitorDataMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MonitorData>()
                .lt(MonitorData::getTimestamp, cutoffDate)
        );

        logger.info("Deleted {} old monitoring records", deleted);
    }

    /**
     * Internal class to track monitoring tasks.
     */
    private static class MonitoringTask {
        final Long taskId;
        final int intervalSeconds;
        LocalDateTime lastCollection;

        MonitoringTask(Long taskId, int intervalSeconds) {
            this.taskId = taskId;
            this.intervalSeconds = intervalSeconds;
            this.lastCollection = null;
        }
    }
}
