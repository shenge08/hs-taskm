package com.taskm.service;

import java.io.File;
import java.util.List;

/**
 * Service interface for log routing and management.
 * Manages log file paths and directories for tasks.
 */
public interface LogRouter {

    /**
     * Get log file path for a task.
     * Returns the most recent log file for the task.
     *
     * @param taskId task ID
     * @return log file path
     */
    String getLogPath(Long taskId);

    /**
     * Get log directory path for a task.
     *
     * @param taskId task ID
     * @return log directory path
     */
    String getLogDirectoryPath(Long taskId);

    /**
     * Ensure log directory exists for a task.
     * Creates directory if it doesn't exist.
     *
     * @param taskId task ID
     */
    void ensureLogDirectory(Long taskId);

    /**
     * Generate log file name for a task.
     * Format: task_<taskId>_<timestamp>.log
     *
     * @param taskId task ID
     * @return log file name
     */
    String generateLogFileName(Long taskId);

    /**
     * List all log files for a task.
     *
     * @param taskId task ID
     * @return list of log file paths
     */
    List<File> listLogFiles(Long taskId);

    /**
     * Check if log file exists.
     *
     * @param taskId task ID
     * @return true if log file exists
     */
    boolean logFileExists(Long taskId);
}
