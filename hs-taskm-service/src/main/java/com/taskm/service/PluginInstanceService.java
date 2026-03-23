package com.taskm.service;

import com.taskm.dto.CreatePluginInstanceDTO;
import com.taskm.dto.PluginInstanceVO;
import com.taskm.dto.UpdatePluginInstanceDTO;
import com.taskm.entity.DataPluginInstance;

import java.util.List;

/**
 * Service interface for plugin instance management.
 * Provides business logic for plugin instance CRUD operations.
 */
public interface PluginInstanceService {

    /**
     * Create a new plugin instance.
     *
     * @param pluginId the plugin ID
     * @param dto the create DTO
     * @return the created instance
     */
    PluginInstanceVO createInstance(Long pluginId, CreatePluginInstanceDTO dto);

    /**
     * Get all instances for a plugin.
     *
     * @param pluginId the plugin ID
     * @return list of instances
     */
    List<PluginInstanceVO> getInstancesByPluginId(Long pluginId);

    /**
     * Get an instance by ID.
     *
     * @param instanceId the instance ID
     * @return the instance
     */
    PluginInstanceVO getInstanceById(Long instanceId);

    /**
     * Update an instance.
     *
     * @param instanceId the instance ID
     * @param dto the update DTO
     * @return the updated instance
     */
    PluginInstanceVO updateInstance(Long instanceId, UpdatePluginInstanceDTO dto);

    /**
     * Delete an instance.
     *
     * @param instanceId the instance ID
     */
    void deleteInstance(Long instanceId);

    /**
     * Set an instance as the default instance for its plugin.
     *
     * @param instanceId the instance ID
     */
    void setDefaultInstance(Long instanceId);

    /**
     * Get the default instance for a plugin.
     *
     * @param pluginId the plugin ID
     * @return the default instance, or null if none exists
     */
    DataPluginInstance getDefaultInstance(Long pluginId);
}
