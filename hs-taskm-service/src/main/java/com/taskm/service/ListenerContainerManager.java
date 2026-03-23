package com.taskm.service;

/**
 * Service interface for listener container management.
 * Provides container lifecycle operations for listener containers.
 */
public interface ListenerContainerManager {

    /**
     * Start a container for a listener with all its instances.
     *
     * @param listenerId the listener ID
     * @return container ID
     */
    String startListenerContainer(Long listenerId);

    /**
     * Stop the container for a listener.
     *
     * @param listenerId the listener ID
     */
    void stopListenerContainer(Long listenerId);

    /**
     * Get the status of a listener container.
     *
     * @param listenerId the listener ID
     * @return container status (RUNNING, STOPPED, NOT_FOUND)
     */
    String getContainerStatus(Long listenerId);

    /**
     * Restart a listener container.
     *
     * @param listenerId the listener ID
     */
    void restartListenerContainer(Long listenerId);
}
