package com.taskm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for plugin container management.
 * Manages Docker container lifecycle for plugin containers.
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
    public String startPluginContainer(Long pluginId) {
        logger.info("Starting container for plugin {}", pluginId);

        // 1. Verify plugin exists
        DataPlugin plugin = pluginMapper.selectById(pluginId);
        if (plugin == null) {
            throw new DataPluginNotFoundException("Plugin not found with id: " + pluginId);
        }

        // 2. Get all instances for this plugin
        List<DataPluginInstance> instances = getInstancesByPluginId(pluginId);
        if (instances.isEmpty()) {
            throw new IllegalStateException("Cannot start plugin: no instances found for plugin " + pluginId);
        }

        // 3. Check if container already exists
        String containerName = "plugin-" + pluginId;
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
            for (DataPluginInstance instance : instances) {
                instancesConfig.put(instance.getName(), instance.getConfig());
            }
            String instancesConfigJson = objectMapper.writeValueAsString(instancesConfig);

            // 5. Create container
            ExposedPort exposedPort = new ExposedPort(8080);
            Ports bindings = new Ports();
            bindings.bind(exposedPort, Ports.Binding.empty());

            CreateContainerResponse response = dockerClient.createContainerCmd(plugin.getImageName())
                    .withName(containerName)
                    .withEnv("INSTANCES_CONFIG=" + instancesConfigJson)
                    .withEnv("LOG_TYPE=plugin")
                    .withExposedPorts(exposedPort)
                    .withHostConfig(com.github.dockerjava.api.model.HostConfig.newHostConfig()
                            .withPortBindings(bindings)
                            .withBinds(Bind.parse("/var/log/taskm:/var/log/taskm:rw"))
                            .withRestartPolicy(RestartPolicy.onFailureRestart(3))
                    )
                    .exec();

            String containerId = response.getId();
            logger.info("Created container {} with ID {}", containerName, containerId);

            // 6. Start container
            dockerClient.startContainerCmd(containerId).exec();
            logger.info("Started container {}", containerName);

            // 7. Update instances with container ID and status
            for (DataPluginInstance instance : instances) {
                instance.setContainerId(containerId);
                instance.setStatus("RUNNING");
                instanceMapper.updateById(instance);
            }

            logger.info("Plugin {} container started successfully", pluginId);
            return containerId;

        } catch (Exception e) {
            logger.error("Failed to start container for plugin {}", pluginId, e);
            throw new RuntimeException("Failed to start container: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void stopPluginContainer(Long pluginId) {
        logger.info("Stopping container for plugin {}", pluginId);

        String containerName = "plugin-" + pluginId;

        try {
            // Check if container exists
            InspectContainerResponse container = dockerClient.inspectContainerCmd(containerName).exec();

            // Stop container
            dockerClient.stopContainerCmd(containerName).exec();
            logger.info("Stopped container {}", containerName);

            // Update all instances status to STOPPED
            List<DataPluginInstance> instances = getInstancesByPluginId(pluginId);
            for (DataPluginInstance instance : instances) {
                if (containerName.equals(instance.getContainerId())) {
                    instance.setStatus("STOPPED");
                    instanceMapper.updateById(instance);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to stop container for plugin {}", pluginId, e);
            throw new RuntimeException("Failed to stop container: " + e.getMessage(), e);
        }
    }

    @Override
    public String getContainerStatus(Long pluginId) {
        String containerName = "plugin-" + pluginId;

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
    public void restartPluginContainer(Long pluginId) {
        logger.info("Restarting container for plugin {}", pluginId);

        stopPluginContainer(pluginId);
        startPluginContainer(pluginId);

        logger.info("Plugin {} container restarted successfully", pluginId);
    }

    /**
     * Get all instances for a plugin.
     */
    private List<DataPluginInstance> getInstancesByPluginId(Long pluginId) {
        return instanceMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataPluginInstance>()
                .eq("plugin_id", pluginId)
        );
    }
}
