package com.taskm.service;

import com.taskm.dto.CreateListenerInstanceDTO;
import com.taskm.dto.ListenerInstanceVO;
import com.taskm.dto.UpdateListenerInstanceDTO;
import com.taskm.entity.ListenerInstance;

import java.util.List;

/**
 * Service interface for listener instance management.
 * Provides business logic for listener instance CRUD operations.
 */
public interface ListenerInstanceService {

    /**
     * Create a new listener instance.
     *
     * @param listenerId the listener ID
     * @param dto the create DTO
     * @return the created instance
     */
    ListenerInstanceVO createInstance(Long listenerId, CreateListenerInstanceDTO dto);

    /**
     * Get all instances for a listener.
     *
     * @param listenerId the listener ID
     * @return list of instances
     */
    List<ListenerInstanceVO> getInstancesByListenerId(Long listenerId);

    /**
     * Get an instance by ID.
     *
     * @param instanceId the instance ID
     * @return the instance
     */
    ListenerInstanceVO getInstanceById(Long instanceId);

    /**
     * Update an instance.
     *
     * @param instanceId the instance ID
     * @param dto the update DTO
     * @return the updated instance
     */
    ListenerInstanceVO updateInstance(Long instanceId, UpdateListenerInstanceDTO dto);

    /**
     * Delete an instance.
     *
     * @param instanceId the instance ID
     */
    void deleteInstance(Long instanceId);

    /**
     * Set an instance as the default instance for its listener.
     *
     * @param instanceId the instance ID
     */
    void setDefaultInstance(Long instanceId);

    /**
     * Get the default instance for a listener.
     *
     * @param listenerId the listener ID
     * @return the default instance, or null if none exists
     */
    ListenerInstance getDefaultInstance(Long listenerId);
}
