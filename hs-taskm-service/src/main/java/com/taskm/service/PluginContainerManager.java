package com.taskm.service;

/**
 * Service interface for plugin container management.
 * Provides container lifecycle operations for plugin containers.
 */
public interface PluginContainerManager {

    /**
     * Start a container for a plugin with all its instances.
     *
     * @param pluginId the plugin ID
     * @return container ID
     */
    String startPluginContainer(Long pluginId);

    /**
     * Stop the container for a plugin.
     *
     * @param pluginId the plugin ID
     */
    void stopPluginContainer(Long pluginId);

    /**
     * Get the status of a plugin container.
     *
     * @param pluginId the plugin ID
     * @return container status (RUNNING, STOPPED, NOT_FOUND)
     */
    String getContainerStatus(Long pluginId);

    /**
     * Restart a plugin container.
     *
     * @param pluginId the plugin ID
     */
    void restartPluginContainer(Long pluginId);
}
