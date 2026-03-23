package com.taskm.service;

import com.taskm.dto.ContainerConfig;
import com.taskm.dto.ContainerMetrics;
import com.taskm.dto.ContainerStatus;

/**
 * Service interface for container lifecycle management.
 * Encapsulates Docker operations for creating, starting, stopping containers.
 */
public interface ContainerLifecycleManager {

    /**
     * Create a new container.
     *
     * @param imageId Docker image ID or name
     * @param taskId task ID (used for container naming)
     * @param config container configuration
     * @return container ID
     * @throws com.taskm.exception.ContainerException if creation fails
     */
    String createContainer(String imageId, Long taskId, ContainerConfig config);

    /**
     * Start a container.
     *
     * @param containerId container ID
     * @throws com.taskm.exception.ContainerException if start fails
     */
    void startContainer(String containerId);

    /**
     * Stop a container gracefully.
     *
     * @param containerId container ID
     * @param timeoutSeconds timeout before force kill (0 = force kill immediately)
     * @throws com.taskm.exception.ContainerException if stop fails
     */
    void stopContainer(String containerId, int timeoutSeconds);

    /**
     * Get container status.
     *
     * @param containerId container ID
     * @return container status
     * @throws com.taskm.exception.ContainerException if query fails
     */
    ContainerStatus getContainerStatus(String containerId);

    /**
     * Get container resource usage metrics.
     *
     * @param containerId container ID
     * @return container metrics
     * @throws com.taskm.exception.ContainerException if query fails
     */
    ContainerMetrics getContainerMetrics(String containerId);

    /**
     * Remove a container (cleanup).
     *
     * @param containerId container ID
     * @param force whether to force removal (kill if running)
     * @throws com.taskm.exception.ContainerException if removal fails
     */
    void removeContainer(String containerId, boolean force);
}
