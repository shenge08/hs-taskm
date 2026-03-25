package com.taskm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
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
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Service implementation for listener instance container management.
 * Manages Docker container lifecycle for listener instance containers (1:1 mapping).
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
    public String startListenerContainer(Long listenerInstanceId) {
        logger.info("Starting container for listener instance {}", listenerInstanceId);

        // 1. Verify listener instance exists
        ListenerInstance listenerInstance = instanceMapper.selectById(listenerInstanceId);
        if (listenerInstance == null) {
            throw new ListenerNotFoundException("Listener instance not found with id: " + listenerInstanceId);
        }

        // 2. Get listener for container naming and image
        Listener listener = listenerMapper.selectById(listenerInstance.getListenerId());

        // 3. Check if container already exists
        String containerName = "listener-" + listener.getId() + "-" + listenerInstanceId;
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
            // 4. Create container
            ExposedPort exposedPort = new ExposedPort(8080);
            Ports bindings = new Ports();
            bindings.bind(exposedPort, Ports.Binding.empty());

            CreateContainerCmd cmd = dockerClient.createContainerCmd(listener.getImageName())
                    .withName(containerName)
                    .withEnv("LOG_TYPE=listener")
                    .withExposedPorts(exposedPort)
                    .withHostConfig(com.github.dockerjava.api.model.HostConfig.newHostConfig()
                            .withPortBindings(bindings)
                            .withBinds(Bind.parse("/var/log/taskm:/var/log/taskm:rw"))
                            .withRestartPolicy(RestartPolicy.onFailureRestart(3))
                    );

            // Add instance config as environment variables
            List<String> envs = new ArrayList<>();
            for (Map.Entry<String, Object> entry : listenerInstance.getConfig().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                envs.add(key + "=" + value);
            }
            cmd = cmd.withEnv(envs.toArray(new String[0]));

            CreateContainerResponse response = cmd.exec();

            String containerId = response.getId();
            logger.info("Created container {} with ID {}", containerName, containerId);

            // 5. Start container
            dockerClient.startContainerCmd(containerId).exec();
            logger.info("Started container {}", containerName);

            // 6. Update instance with container ID and status
            listenerInstance.setContainerId(containerId);
            listenerInstance.setStatus("RUNNING");
            instanceMapper.updateById(listenerInstance);

            logger.info("Listener instance {} container started successfully", listenerInstanceId);
            return containerId;

        } catch (Exception e) {
            logger.error("Failed to start container for listener instance {}", listenerInstanceId, e);
            throw new RuntimeException("Failed to start container: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void stopListenerContainer(Long listenerInstanceId) {
        logger.info("Stopping container for listener instance {}", listenerInstanceId);

        // 1. Get listener instance
        ListenerInstance instance = instanceMapper.selectById(listenerInstanceId);
        if (instance == null) {
            throw new ListenerNotFoundException("Listener instance not found with id: " + listenerInstanceId);
        }

        // 2. Get listener for container naming
        Listener listener = listenerMapper.selectById(instance.getListenerId());
        String containerName = "listener-" + listener.getId() + "-" + listenerInstanceId;

        try {
            // 3. Check if container exists and stop it
            InspectContainerResponse container = dockerClient.inspectContainerCmd(containerName).exec();
            dockerClient.stopContainerCmd(containerName).exec();
            logger.info("Stopped container {}", containerName);

            // 4. Update instance status to STOPPED
            instance.setStatus("STOPPED");
            instanceMapper.updateById(instance);

        } catch (Exception e) {
            logger.error("Failed to stop container for listener instance {}", listenerInstanceId, e);
            throw new RuntimeException("Failed to stop container: " + e.getMessage(), e);
        }
    }

    @Override
    public String getContainerStatus(Long listenerInstanceId) {
        // 1. Get listener instance
        ListenerInstance instance = instanceMapper.selectById(listenerInstanceId);
        if (instance == null) {
            logger.debug("Listener instance {} not found", listenerInstanceId);
            return "NOT_FOUND";
        }

        // 2. Get listener for container naming
        Listener listener = listenerMapper.selectById(instance.getListenerId());
        String containerName = "listener-" + listener.getId() + "-" + listenerInstanceId;

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
    public void restartListenerContainer(Long listenerInstanceId) {
        logger.info("Restarting container for listener instance {}", listenerInstanceId);

        stopListenerContainer(listenerInstanceId);
        startListenerContainer(listenerInstanceId);

        logger.info("Listener instance {} container restarted successfully", listenerInstanceId);
    }
}
