package com.taskm.dto;

import lombok.Data;

import java.util.Map;

/**
 * Configuration for injecting code snippets into a container.
 * Contains all environment variables, volume mounts, and parameters needed for container startup.
 */
@Data
public class InjectionConfig {

    /**
     * Environment variables to set in the container.
     */
    private Map<String, String> environmentVariables;

    /**
     * Volume mounts (host path -> container path).
     */
    private Map<String, String> volumeMounts;

    /**
     * Working directory inside the container.
     */
    private String workingDirectory;

    /**
     * Log file path for this task.
     */
    private String logPath;

    /**
     * Strategy parameters (JSON).
     */
    private Map<String, Object> strategyParams;

    /**
     * Plugin code (Base64 encoded).
     */
    private String pluginCode;

    /**
     * Plugin parameters (JSON).
     */
    private Map<String, Object> pluginParams;

    /**
     * Plugin metadata (JSON).
     */
    private Map<String, Object> pluginMetadata;

    /**
     * Listener code (Base64 encoded).
     */
    private String listenerCode;

    /**
     * Listener parameters (JSON).
     */
    private Map<String, Object> listenerParams;

    /**
     * Listener metadata (JSON).
     */
    private Map<String, Object> listenerMetadata;
}
