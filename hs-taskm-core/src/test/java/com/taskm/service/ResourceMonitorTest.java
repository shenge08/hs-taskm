package com.taskm.service;

import com.taskm.dto.ContainerMetrics;
import com.taskm.entity.MonitorData;
import com.taskm.mapper.MonitorDataMapper;
import com.taskm.service.impl.ResourceMonitorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResourceMonitor.
 */
@ExtendWith(MockitoExtension.class)
class ResourceMonitorTest {

    @Mock
    private MonitorDataMapper monitorDataMapper;

    @Mock
    private ContainerLifecycleManager containerLifecycleManager;

    private ResourceMonitorImpl resourceMonitor;

    @BeforeEach
    void setUp() {
        resourceMonitor = new ResourceMonitorImpl(monitorDataMapper, containerLifecycleManager);
        ReflectionTestUtils.setField(resourceMonitor, "defaultIntervalSeconds", 30);
        ReflectionTestUtils.setField(resourceMonitor, "retentionDays", 7);
    }

    @Test
    void testStartMonitoring_shouldAddToMonitoringTasks() {
        // Given
        String containerId = "container-123";
        Long taskId = 1L;
        int intervalSeconds = 30;

        // When
        resourceMonitor.startMonitoring(containerId, taskId, intervalSeconds);

        // Then
        assertTrue(resourceMonitor.isMonitoring(containerId),
            "Should be monitoring after startMonitoring is called");
    }

    @Test
    void testStopMonitoring_shouldRemoveFromMonitoringTasks() {
        // Given
        String containerId = "container-123";
        Long taskId = 1L;
        resourceMonitor.startMonitoring(containerId, taskId, 30);

        // When
        resourceMonitor.stopMonitoring(containerId);

        // Then
        assertFalse(resourceMonitor.isMonitoring(containerId),
            "Should not be monitoring after stopMonitoring is called");
    }

    @Test
    void testIsMonitoring_shouldReturnFalseForNonExistentContainer() {
        // Given
        String containerId = "non-existent";

        // When & Then
        assertFalse(resourceMonitor.isMonitoring(containerId),
            "Should return false for container that was never started");
    }

    @Test
    void testCollectMetrics_shouldSaveMonitorDataToDatabase() {
        // Given
        String containerId = "container-123";
        Long taskId = 1L;
        when(monitorDataMapper.insert(any(MonitorData.class))).thenReturn(1);

        // When
        MonitorData result = resourceMonitor.collectMetrics(containerId, taskId);

        // Then
        assertNotNull(result, "Should return MonitorData object");
        assertEquals(containerId, result.getContainerId());
        assertEquals(taskId, result.getTaskId());
        assertNotNull(result.getTimestamp());

        // Verify insert was called
        verify(monitorDataMapper).insert(any(MonitorData.class));
    }

    @Test
    void testGetCurrentMetrics_shouldReturnMetricsFromDatabase() {
        // Given
        Long taskId = 1L;
        MonitorData dbData = new MonitorData();
        dbData.setTaskId(taskId);
        dbData.setContainerId("container-123");
        dbData.setCpuUsage(50.0);
        dbData.setMemoryUsage(1024L);
        dbData.setMemoryLimit(2048L);
        dbData.setMemoryUsagePercent(50.0);
        dbData.setNetworkRxBytes(1000L);
        dbData.setNetworkTxBytes(2000L);
        dbData.setBlockReadBytes(100L);
        dbData.setBlockWriteBytes(200L);
        dbData.setTimestamp(LocalDateTime.now());

        when(monitorDataMapper.getLatestMetricsByTaskId(taskId)).thenReturn(dbData);

        // When
        ContainerMetrics result = resourceMonitor.getCurrentMetrics(taskId);

        // Then
        assertNotNull(result, "Should return metrics");
        assertEquals("container-123", result.getContainerId());
        assertEquals(50.0, result.getCpuUsage(), 0.001);
        assertEquals(1024L, result.getMemoryUsage());
        assertEquals(2048L, result.getMemoryLimit());
    }

    @Test
    void testGetCurrentMetrics_shouldReturnNullWhenNoMetricsExist() {
        // Given
        Long taskId = 1L;
        when(monitorDataMapper.getLatestMetricsByTaskId(taskId)).thenReturn(null);

        // When
        ContainerMetrics result = resourceMonitor.getCurrentMetrics(taskId);

        // Then
        assertNull(result, "Should return null when no metrics exist");
    }

    @Test
    void testGetMetricsHistory_shouldReturnDataFromMapper() {
        // Given
        Long taskId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now();

        List<MonitorData> expectedData = List.of(
            createMonitorData(taskId, "container-123", startTime),
            createMonitorData(taskId, "container-123", startTime.plusMinutes(30))
        );

        when(monitorDataMapper.getMetricsByTaskIdAndTimeRange(taskId, startTime, endTime))
            .thenReturn(expectedData);

        // When
        List<MonitorData> result = resourceMonitor.getMetricsHistory(taskId, startTime, endTime);

        // Then
        assertNotNull(result, "Should return metrics history");
        assertEquals(2, result.size());
        assertEquals(expectedData, result);
    }

    @Test
    void testScheduledMetricCollection_shouldDoNothingWhenNoMonitoringTasks() {
        // When
        resourceMonitor.scheduledMetricCollection();

        // Then - should not throw exception, and should not call collectMetrics
        verify(monitorDataMapper, never()).insert(any(MonitorData.class));
    }

    @Test
    void testScheduledMetricCollection_shouldCollectMetricsForAllMonitoringTasks() {
        // Given
        String container1 = "container-1";
        String container2 = "container-2";
        resourceMonitor.startMonitoring(container1, 1L, 30);
        resourceMonitor.startMonitoring(container2, 2L, 30);

        when(monitorDataMapper.insert(any(MonitorData.class))).thenReturn(1);

        // When
        resourceMonitor.scheduledMetricCollection();

        // Then - should call insert twice (once for each container)
        verify(monitorDataMapper, times(2)).insert(any(MonitorData.class));
    }

    // Helper method
    private MonitorData createMonitorData(Long taskId, String containerId, LocalDateTime timestamp) {
        MonitorData data = new MonitorData();
        data.setTaskId(taskId);
        data.setContainerId(containerId);
        data.setTimestamp(timestamp);
        data.setCpuUsage(10.0);
        data.setMemoryUsage(512L);
        data.setMemoryLimit(1024L);
        data.setMemoryUsagePercent(50.0);
        data.setNetworkRxBytes(100L);
        data.setNetworkTxBytes(200L);
        data.setBlockReadBytes(10L);
        data.setBlockWriteBytes(20L);
        return data;
    }
}
