package com.taskm.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Ports;
import com.taskm.dto.ContainerMetrics;
import com.taskm.dto.ContainerStatus;
import com.taskm.exception.ContainerException;
import com.taskm.service.ContainerLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of container lifecycle management.
 * Encapsulates Docker operations.
 */
@Service
public class ContainerLifecycleManagerImpl implements ContainerLifecycleManager {

    private static final Logger logger = LoggerFactory.getLogger(ContainerLifecycleManagerImpl.class);

    private final DockerClient dockerClient;

    @Autowired
    public ContainerLifecycleManagerImpl(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @Override
    public String createContainer(String imageId, Long taskId, com.taskm.dto.ContainerConfig config) {
        try {
            logger.info("Creating container for task {} with image {}", taskId, imageId);

            // Create container command
            var createCmd = dockerClient.createContainerCmd(imageId);

            // Set container name
            String containerName = config.getName() != null ? config.getName() : "task-" + taskId;
            createCmd.withName(containerName);

            // Set environment variables
            if (config.getEnv() != null) {
                createCmd.withEnv(config.getEnv());
            }

            // Set command
            if (config.getCmd() != null) {
                createCmd.withCmd(config.getCmd());
            }

            // Set working directory
            if (config.getWorkingDir() != null) {
                createCmd.withWorkingDir(config.getWorkingDir());
            }

            // TODO: Implement binds and portBindings in future iterations
            // Current Docker Java Client API requires Bind objects, not Strings

            // Create container
            CreateContainerResponse response = createCmd.exec();

            String containerId = response.getId();
            logger.info("Created container {} for task {}", containerId, taskId);
            return containerId;

        } catch (DockerException e) {
            logger.error("Failed to create container for task {} with image {}", taskId, imageId, e);
            throw new ContainerException("Failed to create container: " + e.getMessage(), e);
        }
    }

    @Override
    public void startContainer(String containerId) {
        try {
            logger.info("Starting container {}", containerId);
            dockerClient.startContainerCmd(containerId).exec();
            logger.info("Started container {}", containerId);
        } catch (DockerException e) {
            logger.error("Failed to start container {}", containerId, e);
            throw new ContainerException("Failed to start container: " + e.getMessage(), e);
        }
    }

    @Override
    public void stopContainer(String containerId, int timeoutSeconds) {
        try {
            logger.info("Stopping container {} with timeout {}s", containerId, timeoutSeconds);
            dockerClient.stopContainerCmd(containerId).withTimeout(timeoutSeconds).exec();
            logger.info("Stopped container {}", containerId);
        } catch (DockerException e) {
            logger.error("Failed to stop container {}", containerId, e);
            throw new ContainerException("Failed to stop container: " + e.getMessage(), e);
        }
    }

    @Override
    public ContainerStatus getContainerStatus(String containerId) {
        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();

            if (!response.getState().getRunning()) {
                if (response.getState().getExitCode() != null && response.getState().getExitCode() != 0) {
                    return ContainerStatus.FAILED;
                }
                return ContainerStatus.STOPPED;
            }

            return ContainerStatus.RUNNING;

        } catch (DockerException e) {
            logger.error("Failed to get status for container {}", containerId, e);
            return ContainerStatus.UNKNOWN;
        }
    }

    @Override
    public ContainerMetrics getContainerMetrics(String containerId) {
        // TODO: Implement metrics collection in Issue #11 (Resource Monitor)
        // This requires async stats command and complex parsing
        throw new ContainerException("Metrics collection not yet implemented - see Issue #11");
    }

    @Override
    public void removeContainer(String containerId, boolean force) {
        try {
            logger.info("Removing container {} (force: {})", containerId, force);
            dockerClient.removeContainerCmd(containerId).withForce(force).exec();
            logger.info("Removed container {}", containerId);
        } catch (DockerException e) {
            logger.error("Failed to remove container {}", containerId, e);
            throw new ContainerException("Failed to remove container: " + e.getMessage(), e);
        }
    }
}
