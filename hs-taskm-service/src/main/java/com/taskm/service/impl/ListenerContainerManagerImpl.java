package com.taskm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.RestartPolicy;
import com.taskm.entity.Listener;
import com.taskm.entity.ListenerInstance;
import com.taskm.exception.ListenerNotFoundException;
import com.taskm.mapper.ListenerInstanceMapper;
import com.taskm.mapper.ListenerMapper;
import com.taskm.service.ListenerContainerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for listener container management.
 * Manages Docker container lifecycle for listener containers.
 */
@Service
public class ListenerContainerManagerImpl implements ListenerContainerManager {

    private static final Logger logger = LoggerFactory.getLogger(ListenerContainerManagerImpl.class);

    private final DockerClient dockerClient;
    private final ListenerMapper listenerMapper;
    private final ListenerInstanceMapper instanceMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public ListenerContainerManagerImpl(
            DockerClient dockerClient,
            ListenerMapper listenerMapper,
            ListenerInstanceMapper instanceMapper,
            ObjectMapper objectMapper) {
        this.dockerClient = dockerClient;
        this.listenerMapper = listenerMapper;
        this.instanceMapper = instanceMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public String startListenerContainer(Long listenerId) {
        logger.info("Starting container for listener {}", listenerId);

        // 1. Verify listener exists
        Listener listener = listenerMapper.selectById(listenerId);
        if (listener == null) {
            throw new ListenerNotFoundException("Listener not found with id: " + listenerId);
        }

        // 2. Get all instances for this listener
        List<ListenerInstance> instances = getInstancesByListenerId(listenerId);
        if (instances.isEmpty()) {
            throw new IllegalStateException("Cannot start listener: no instances found for listener " + listenerId);
        }

        // 3. Check if container already exists
        String containerName = "listener-" + listenerId;
        try {
            InspectContainerResponse existingContainer = dockerClient.inspectContainerCmd(containerName).exec();
            if (existingContainer.getState().getRunning()) {
                logger.info("Container {} is already running", containerName);
                return existingContainer.getId();
            } else {
                // Remove stopped container
                dockerClient.removeContainerCmd(containerName).exec();
            }
        } catch (Exception e) {
            // Container doesn't exist, continue with creation
            logger.debug("Container {} does not exist, will create new", containerName);
        }

        try {
            // 4. Build instances configuration JSON
            Map<String, Map<String, Object>> instancesConfig = new HashMap<>();
            for (ListenerInstance instance : instances) {
                instancesConfig.put(instance.getName(), instance.getConfig());
            }
            String instancesConfigJson = objectMapper.writeValueAsString(instancesConfig);

            // 5. Create container
            ExposedPort exposedPort = new ExposedPort(8080);
            Ports bindings = new Ports();
            bindings.bind(exposedPort, Ports.Binding.empty());

            CreateContainerResponse response = dockerClient.createContainerCmd(listener.getImageName())
                    .withName(containerName)
                    .withEnv("INSTANCES_CONFIG=" + instancesConfigJson)
                    .withExposedPorts(exposedPort)
                    .withHostConfig(com.github.dockerjava.api.model.HostConfig.newHostConfig()
                            .withPortBindings(bindings)
                            .withBinds(Bind.parse("/var/log/tasks:/var/log/tasks:rw"))
                            .withRestartPolicy(RestartPolicy.onFailureRestart(3))
                    )
                    .exec();

            String containerId = response.getId();
            logger.info("Created container {} with ID {}", containerName, containerId);

            // 6. Start container
            dockerClient.startContainerCmd(containerId).exec();
            logger.info("Started container {}", containerName);

            // 7. Update instances with container ID and status
            for (ListenerInstance instance : instances) {
                instance.setContainerId(containerId);
                instance.setStatus("RUNNING");
                instanceMapper.updateById(instance);
            }

            logger.info("Listener {} container started successfully", listenerId);
            return containerId;

        } catch (Exception e) {
            logger.error("Failed to start container for listener {}", listenerId, e);
            throw new RuntimeException("Failed to start container: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void stopListenerContainer(Long listenerId) {
        logger.info("Stopping container for listener {}", listenerId);

        String containerName = "listener-" + listenerId;

        try {
            // Check if container exists
            InspectContainerResponse container = dockerClient.inspectContainerCmd(containerName).exec();

            // Stop container
            dockerClient.stopContainerCmd(containerName).exec();
            logger.info("Stopped container {}", containerName);

            // Update all instances status to STOPPED
            List<ListenerInstance> instances = getInstancesByListenerId(listenerId);
            for (ListenerInstance instance : instances) {
                if (containerName.equals(instance.getContainerId())) {
                    instance.setStatus("STOPPED");
                    instanceMapper.updateById(instance);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to stop container for listener {}", listenerId, e);
            throw new RuntimeException("Failed to stop container: " + e.getMessage(), e);
        }
    }

    @Override
    public String getContainerStatus(Long listenerId) {
        String containerName = "listener-" + listenerId;

        try {
            InspectContainerResponse container = dockerClient.inspectContainerCmd(containerName).exec();
            boolean running = container.getState().getRunning();
            return running ? "RUNNING" : "STOPPED";
        } catch (Exception e) {
            logger.debug("Container {} not found", containerName);
            return "NOT_FOUND";
        }
    }

    @Override
    @Transactional
    public void restartListenerContainer(Long listenerId) {
        logger.info("Restarting container for listener {}", listenerId);

        stopListenerContainer(listenerId);
        startListenerContainer(listenerId);

        logger.info("Listener {} container restarted successfully", listenerId);
    }

    /**
     * Get all instances for a listener.
     */
    private List<ListenerInstance> getInstancesByListenerId(Long listenerId) {
        return instanceMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ListenerInstance>()
                .eq("listener_id", listenerId)
        );
    }
}
