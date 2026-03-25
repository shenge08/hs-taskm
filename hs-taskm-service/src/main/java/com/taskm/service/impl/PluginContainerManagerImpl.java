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
import com.taskm.entity.DataPlugin;
import com.taskm.entity.DataPluginInstance;
import com.taskm.exception.DataPluginNotFoundException;
import com.taskm.mapper.DataPluginInstanceMapper;
import com.taskm.mapper.DataPluginMapper;
import com.taskm.service.PluginContainerManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for plugin instance container management.
 * Manages Docker container lifecycle for plugin instance containers (1:1 mapping).
 */
@Service
public class PluginContainerManagerImpl implements PluginContainerManager {

    private static final Logger logger = LoggerFactory.getLogger(PluginContainerManagerImpl.class);

    private final DockerClient dockerClient;
    private final DataPluginMapper pluginMapper;
    private final DataPluginInstanceMapper instanceMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public PluginContainerManagerImpl(
            DockerClient dockerClient,
            DataPluginMapper pluginMapper,
            DataPluginInstanceMapper instanceMapper,
            ObjectMapper objectMapper) {
        this.dockerClient = dockerClient;
        this.pluginMapper = pluginMapper;
        this.instanceMapper = instanceMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public String startPluginContainer(Long pluginInstanceId) {
        logger.info("Starting container for plugin instance {}", pluginInstanceId);

        // 1. Verify plugin exists
        DataPluginInstance dataPluginInstance = instanceMapper.selectById(pluginInstanceId);
        if (dataPluginInstance == null) {
            throw new DataPluginNotFoundException("Plugin Instance not found with id: " + pluginInstanceId);
        }

        DataPlugin dataPlugin = pluginMapper.selectById(dataPluginInstance.getPluginId());
        // 3. Check if container already exists
        String containerName = "plugin-" +dataPlugin.getId()+"-"+ pluginInstanceId;
        try {
            InspectContainerResponse existingContainer = dockerClient.inspectContainerCmd(containerName).exec();
            if (existingContainer.getState().getRunning()) {
                logger.info("Container {} is already running", containerName);
                return existingContainer.getId();
            } else {
                // Container exists but is stopped, start it directly
                logger.info("Container {} exists but is stopped, starting it", containerName);
                dockerClient.startContainerCmd(existingContainer.getId()).exec();
                logger.info("Started existing container {}", containerName);

                // Update instance status
                dataPluginInstance.setContainerId(existingContainer.getId());
                dataPluginInstance.setStatus("RUNNING");
                instanceMapper.updateById(dataPluginInstance);

                return existingContainer.getId();
            }
        } catch (Exception e) {
            // Container doesn't exist, will create new
            logger.debug("Container {} does not exist, will create new", containerName);
        }

        try {
            // 4. Create container
            Integer port = (Integer) dataPluginInstance.getConfig().get("SERVER_PORT");
            ExposedPort exposedPort = new ExposedPort(port);

            CreateContainerCmd cmd = dockerClient.createContainerCmd(dataPlugin.getDockerImageId())
                    .withName(containerName)
                    .withExposedPorts(exposedPort)
                    .withHostConfig(com.github.dockerjava.api.model.HostConfig.newHostConfig())
                    .withRestartPolicy(RestartPolicy.onFailureRestart(3));

            List<String> envs = new ArrayList<>();
            for(Map.Entry<String,Object> entry:dataPluginInstance.getConfig().entrySet()){
                String key = entry.getKey();
                String value = entry.getValue().toString();
                envs.add(key+"="+value);
            }
            cmd = cmd.withEnv(envs.toArray(new String[0]));
            CreateContainerResponse response = cmd.exec();

            String containerId = response.getId();
            logger.info("Created container {} with ID {}", containerName, containerId);

            // 6. Start container
            dockerClient.startContainerCmd(containerId).exec();
            logger.info("Started container {}", containerName);

            // 7. Update instances with container ID and status

            dataPluginInstance.setContainerId(containerId);
            dataPluginInstance.setStatus("RUNNING");
            instanceMapper.updateById(dataPluginInstance);
            logger.info("Plugin {} container started successfully", pluginInstanceId);
            return containerId;

        } catch (Exception e) {
            logger.error("Failed to start container for plugin {}", pluginInstanceId, e);
            throw new RuntimeException("Failed to start container: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void stopPluginContainer(Long pluginInstanceId) {
        logger.info("Stopping container for plugin instance {}", pluginInstanceId);

        // 1. Get plugin instance
        DataPluginInstance instance = instanceMapper.selectById(pluginInstanceId);
        if (instance == null) {
            throw new DataPluginNotFoundException("Plugin instance not found with id: " + pluginInstanceId);
        }

        // 2. Get plugin for container naming
        DataPlugin plugin = pluginMapper.selectById(instance.getPluginId());
        String containerName = "plugin-" + plugin.getId() + "-" + pluginInstanceId;

        try {
            // 3. Check if container exists and stop it
            InspectContainerResponse container = dockerClient.inspectContainerCmd(containerName).exec();
            dockerClient.stopContainerCmd(containerName).exec();
            logger.info("Stopped container {}", containerName);

            // 4. Update instance status to STOPPED
            instance.setStatus("STOPPED");
            instanceMapper.updateById(instance);

        } catch (Exception e) {
            logger.error("Failed to stop container for plugin instance {}", pluginInstanceId, e);
            throw new RuntimeException("Failed to stop container: " + e.getMessage(), e);
        }
    }

    @Override
    public String getContainerStatus(Long pluginInstanceId) {
        // 1. Get plugin instance
        DataPluginInstance instance = instanceMapper.selectById(pluginInstanceId);
        if (instance == null) {
            logger.debug("Plugin instance {} not found", pluginInstanceId);
            return "NOT_FOUND";
        }

        // 2. Get plugin for container naming
        DataPlugin plugin = pluginMapper.selectById(instance.getPluginId());
        String containerName = "plugin-" + plugin.getId() + "-" + pluginInstanceId;

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
    public void restartPluginContainer(Long pluginInstanceId) {
        logger.info("Restarting container for plugin instance {}", pluginInstanceId);

        // 1. Check current container status
        String status = getContainerStatus(pluginInstanceId);
        logger.info("Current container status: {}", status);

        // 2. Stop container only if it's running
        if ("RUNNING".equals(status)) {
            logger.info("Container is running, stopping it first");
            try {
                stopPluginContainer(pluginInstanceId);
            } catch (Exception e) {
                logger.warn("Failed to stop container, will try to start anyway: {}", e.getMessage());
            }
        } else if ("STOPPED".equals(status)) {
            logger.info("Container is stopped, will start directly");
        } else {
            logger.info("Container not found ({}), will create new container", status);
        }

        // 3. Always try to start the container
        try {
            startPluginContainer(pluginInstanceId);
            logger.info("Plugin instance {} container restarted successfully", pluginInstanceId);
        } catch (Exception e) {
            logger.error("Failed to restart container for plugin instance {}", pluginInstanceId, e);
            throw new RuntimeException("Failed to restart container: " + e.getMessage(), e);
        }
    }
}
