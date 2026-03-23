package com.taskm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskm.entity.MonitorData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mapper interface for MonitorData entity.
 * Provides database operations for container resource usage metrics.
 */
@Mapper
public interface MonitorDataMapper extends BaseMapper<MonitorData> {

    /**
     * Get metrics by container ID and time range.
     *
     * @param containerId container ID
     * @param startTime start time
     * @param endTime end time
     * @return list of monitor data
     */
    @Select("SELECT * FROM monitor_data " +
            "WHERE container_id = #{containerId} " +
            "AND timestamp BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY timestamp ASC")
    List<MonitorData> getMetricsByContainerIdAndTimeRange(
        @Param("containerId") String containerId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Get metrics by task ID and time range.
     *
     * @param taskId task ID
     * @param startTime start time
     * @param endTime end time
     * @return list of monitor data
     */
    @Select("SELECT * FROM monitor_data " +
            "WHERE task_id = #{taskId} " +
            "AND timestamp BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY timestamp ASC")
    List<MonitorData> getMetricsByTaskIdAndTimeRange(
        @Param("taskId") Long taskId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Get the latest metrics for a container.
     *
     * @param containerId container ID
     * @return latest monitor data
     */
    @Select("SELECT * FROM monitor_data " +
            "WHERE container_id = #{containerId} " +
            "ORDER BY timestamp DESC " +
            "LIMIT 1")
    MonitorData getLatestMetricsByContainerId(@Param("containerId") String containerId);

    /**
     * Get the latest metrics for a task.
     *
     * @param taskId task ID
     * @return latest monitor data
     */
    @Select("SELECT * FROM monitor_data " +
            "WHERE task_id = #{taskId} " +
            "ORDER BY timestamp DESC " +
            "LIMIT 1")
    MonitorData getLatestMetricsByTaskId(@Param("taskId") Long taskId);
}
