package com.taskm.service;

import com.taskm.dto.InjectionConfig;

import java.util.Map;

/**
 * Service interface for code snippet injection.
 * Prepares container startup configuration by loading code and parameters from database.
 */
public interface CodeSnippetInjector {

    /**
     * Prepare injection configuration for a task.
     * Loads plugin and listener code, merges parameters, and prepares environment variables.
     *
     * @param taskId task ID
     * @return injection configuration
     * @throws com.taskm.exception.PluginNotFoundException if plugin or listener not found
     * @throws com.taskm.exception.InvalidCodeException if code snippet is empty
     * @throws com.taskm.exception.InvalidParameterException if parameters are invalid
     */
    InjectionConfig prepareInjection(Long taskId);

    /**
     * Get injection files for a task.
     * Returns map of file names to their Base64-encoded content.
     *
     * @param taskId task ID
     * @return map of file names to content
     */
    Map<String, String> getInjectionFiles(Long taskId);

    /**
     * Get environment variables for a task.
     * Returns all environment variables needed for container startup.
     *
     * @param taskId task ID
     * @return map of environment variable names to values
     */
    Map<String, String> getEnvironmentVariables(Long taskId);

    /**
     * Get volume mounts for a task.
     * Returns map of host paths to container paths.
     *
     * @param taskId task ID
     * @return map of host paths to container paths
     */
    Map<String, String> getVolumeMounts(Long taskId);

    /**
     * Generate log file path for a task.
     * Format: task_<taskId>_<timestamp>.log
     *
     * @param taskId task ID
     * @return log file path
     */
    String generateLogPath(Long taskId);

    /**
     * Ensure log directory exists for a task.
     * Creates directory if it doesn't exist.
     *
     * @param taskId task ID
     */
    void ensureLogDirectory(Long taskId);
}
