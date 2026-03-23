package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.service.PluginContainerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Plugin Container management.
 * Provides endpoints for container lifecycle operations.
 */
@RestController
@RequestMapping("/api/plugins/{pluginId}/container")
public class PluginContainerController {

    private final PluginContainerManager pluginContainerManager;

    @Autowired
    public PluginContainerController(PluginContainerManager pluginContainerManager) {
        this.pluginContainerManager = pluginContainerManager;
    }

    /**
     * Start a plugin container.
     *
     * @param pluginId the plugin ID
     * @return container ID
     */
    @PostMapping("/start")
    public Result<Map<String, String>> startContainer(@PathVariable Long pluginId) {
        String containerId = pluginContainerManager.startPluginContainer(pluginId);
        return Result.success("Container started successfully", Map.of("containerId", containerId));
    }

    /**
     * Stop a plugin container.
     *
     * @param pluginId the plugin ID
     * @return success message
     */
    @PostMapping("/stop")
    public Result<String> stopContainer(@PathVariable Long pluginId) {
        pluginContainerManager.stopPluginContainer(pluginId);
        return Result.success("Container stopped successfully");
    }

    /**
     * Restart a plugin container.
     *
     * @param pluginId the plugin ID
     * @return success message
     */
    @PostMapping("/restart")
    public Result<String> restartContainer(@PathVariable Long pluginId) {
        pluginContainerManager.restartPluginContainer(pluginId);
        return Result.success("Container restarted successfully");
    }

    /**
     * Get container status.
     *
     * @param pluginId the plugin ID
     * @return container status
     */
    @GetMapping("/status")
    public Result<Map<String, String>> getContainerStatus(@PathVariable Long pluginId) {
        String status = pluginContainerManager.getContainerStatus(pluginId);
        return Result.success(Map.of("status", status));
    }
}
