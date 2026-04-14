package com.taskm.service;

import java.util.List;

/**
 * Service interface for reading Docker container logs.
 * Provides methods to fetch logs from running/stopped containers.
 */
public interface DockerLogService {

    /**
     * Get logs from a Docker container with pagination.
     *
     * @param containerId Docker container ID or name
     * @param offset starting line number (0-based)
     * @param limit maximum number of lines to return
     * @return list of log lines
     */
    List<String> getContainerLogs(String containerId, int offset, int limit);

    /**
     * Get the last N lines of container logs.
     *
     * @param containerId Docker container ID or name
     * @param lines number of lines to return from the end
     * @return list of log lines
     */
    List<String> getLogTail(String containerId, int lines);

    /**
     * Get logs with type filter (stdout/stderr/all).
     *
     * @param containerId Docker container ID or name
     * @param type log type: "stdout", "stderr", or "all"
     * @param tail number of lines to return from the end (0 for all)
     * @return list of log lines
     */
    List<String> getContainerLogs(String containerId, String type, int tail);

    /**
     * Search logs for keyword in a container.
     *
     * @param containerId Docker container ID or name
     * @param keyword keyword to search for
     * @param limit maximum number of results (0 for no limit)
     * @return list of matching log lines
     */
    List<String> searchLogs(String containerId, String keyword, int limit);
}
