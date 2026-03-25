package com.taskm.service;

/**
 * Service interface for listener instance container management.
 * Provides container lifecycle operations for listener instance containers (1:1 mapping).
 */
public interface ListenerContainerManager {

    /**
     * Start a container for a listener instance.
     *
     * @param listenerInstanceId the listener instance ID
     * @return container ID
     */
    String startListenerContainer(Long listenerInstanceId);

    /**
     * Stop the container for a listener instance.
     *
     * @param listenerInstanceId the listener instance ID
     */
    void stopListenerContainer(Long listenerInstanceId);

    /**
     * Get the status of a listener instance container.
     *
     * @param listenerInstanceId the listener instance ID
     * @return container status (RUNNING, STOPPED, NOT_FOUND)
     */
    String getContainerStatus(Long listenerInstanceId);

    /**
     * Restart a listener instance container.
     *
     * @param listenerInstanceId the listener instance ID
     */
    void restartListenerContainer(Long listenerInstanceId);
}
