package com.taskm.controller;

import com.taskm.dto.CreatePluginInstanceDTO;
import com.taskm.dto.PluginInstanceVO;
import com.taskm.dto.Result;
import com.taskm.dto.UpdatePluginInstanceDTO;
import com.taskm.service.PluginInstanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Plugin Instance management.
 * Provides endpoints for CRUD operations on plugin instances.
 */
@RestController
@RequestMapping("/api/plugins/{pluginId}/instances")
public class PluginInstanceController {

    private final PluginInstanceService pluginInstanceService;

    @Autowired
    public PluginInstanceController(PluginInstanceService pluginInstanceService) {
        this.pluginInstanceService = pluginInstanceService;
    }

    /**
     * Create a new plugin instance.
     *
     * @param pluginId the plugin ID
     * @param dto the create DTO
     * @return the created instance
     */
    @PostMapping
    public Result<PluginInstanceVO> createInstance(
            @PathVariable Long pluginId,
            @RequestBody @Valid CreatePluginInstanceDTO dto) {

        PluginInstanceVO instance = pluginInstanceService.createInstance(pluginId, dto);
        return Result.success("Plugin instance created successfully", instance);
    }

    /**
     * Get all instances for a plugin.
     *
     * @param pluginId the plugin ID
     * @return list of instances
     */
    @GetMapping
    public Result<List<PluginInstanceVO>> getInstances(@PathVariable Long pluginId) {
        List<PluginInstanceVO> instances = pluginInstanceService.getInstancesByPluginId(pluginId);
        return Result.success(instances);
    }

    /**
     * Get an instance by ID.
     *
     * @param pluginId the plugin ID
     * @param instanceId the instance ID
     * @return the instance details
     */
    @GetMapping("/{instanceId}")
    public Result<PluginInstanceVO> getInstance(
            @PathVariable Long pluginId,
            @PathVariable Long instanceId) {

        PluginInstanceVO instance = pluginInstanceService.getInstanceById(instanceId);
        return Result.success(instance);
    }

    /**
     * Update an instance.
     *
     * @param pluginId the plugin ID
     * @param instanceId the instance ID
     * @param dto the update DTO
     * @return the updated instance
     */
    @PutMapping("/{instanceId}")
    public Result<PluginInstanceVO> updateInstance(
            @PathVariable Long pluginId,
            @PathVariable Long instanceId,
            @RequestBody @Valid UpdatePluginInstanceDTO dto) {

        PluginInstanceVO instance = pluginInstanceService.updateInstance(instanceId, dto);
        return Result.success("Plugin instance updated successfully", instance);
    }

    /**
     * Delete an instance.
     *
     * @param pluginId the plugin ID
     * @param instanceId the instance ID
     * @return success message
     */
    @DeleteMapping("/{instanceId}")
    public Result<String> deleteInstance(
            @PathVariable Long pluginId,
            @PathVariable Long instanceId) {

        pluginInstanceService.deleteInstance(instanceId);
        return Result.success("Plugin instance deleted successfully");
    }

    /**
     * Set an instance as the default instance.
     *
     * @param pluginId the plugin ID
     * @param instanceId the instance ID
     * @return success message
     */
    @PostMapping("/{instanceId}/setDefault")
    public Result<String> setDefaultInstance(
            @PathVariable Long pluginId,
            @PathVariable Long instanceId) {

        pluginInstanceService.setDefaultInstance(instanceId);
        return Result.success("Default instance set successfully");
    }
}
