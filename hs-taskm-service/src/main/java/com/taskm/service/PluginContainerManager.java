package com.taskm.service;

/**
 * Service interface for plugin instance container management.
 * Provides container lifecycle operations for plugin instance containers (1:1 mapping).
 */
public interface PluginContainerManager {

    /**
     * Start a container for a plugin instance.
     *
     * @param pluginInstanceId the plugin instance ID
     * @return container ID
     */
    String startPluginContainer(Long pluginInstanceId);

    /**
     * Stop the container for a plugin instance.
     *
     * @param pluginInstanceId the plugin instance ID
     */
    void stopPluginContainer(Long pluginInstanceId);

    /**
     * Get the status of a plugin instance container.
     *
     * @param pluginInstanceId the plugin instance ID
     * @return container status (RUNNING, STOPPED, NOT_FOUND)
     */
    String getContainerStatus(Long pluginInstanceId);

    /**
     * Restart a plugin instance container.
     *
     * @param pluginInstanceId the plugin instance ID
     */
    void restartPluginContainer(Long pluginInstanceId);
}
