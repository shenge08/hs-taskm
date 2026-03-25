package com.taskm.service;

/**
 * Service interface for plugin container management.
 * Provides container lifecycle operations for plugin containers.
 */
public interface PluginContainerManager {

    /**
     * Start a container for a plugin with all its instances.
     *
     * @param pluginInstanceId the plugin ID
     * @return container ID
     */
    String startPluginContainer(Long pluginInstanceId);

    /**
     * Stop the container for a plugin.
     *
     * @param pluginInstanceId the plugin ID
     */
    void stopPluginContainer(Long pluginInstanceId);

    /**
     * Get the status of a plugin container.
     *
     * @param pluginInstanceId the plugin ID
     * @return container status (RUNNING, STOPPED, NOT_FOUND)
     */
    String getContainerStatus(Long pluginInstanceId);

    /**
     * Restart a plugin container.
     *
     * @param pluginInstanceId the plugin ID
     */
    void restartPluginContainer(Long pluginInstanceId);
}
