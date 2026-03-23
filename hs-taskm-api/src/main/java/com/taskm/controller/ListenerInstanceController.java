package com.taskm.controller;

import com.taskm.dto.CreateListenerInstanceDTO;
import com.taskm.dto.ListenerInstanceVO;
import com.taskm.dto.Result;
import com.taskm.dto.UpdateListenerInstanceDTO;
import com.taskm.service.ListenerInstanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Listener Instance management.
 * Provides endpoints for CRUD operations on listener instances.
 */
@RestController
@RequestMapping("/api/listeners/{listenerId}/instances")
public class ListenerInstanceController {

    private final ListenerInstanceService listenerInstanceService;

    @Autowired
    public ListenerInstanceController(ListenerInstanceService listenerInstanceService) {
        this.listenerInstanceService = listenerInstanceService;
    }

    /**
     * Create a new listener instance.
     *
     * @param listenerId the listener ID
     * @param dto the create DTO
     * @return the created instance
     */
    @PostMapping
    public Result<ListenerInstanceVO> createInstance(
            @PathVariable Long listenerId,
            @RequestBody @Valid CreateListenerInstanceDTO dto) {

        ListenerInstanceVO instance = listenerInstanceService.createInstance(listenerId, dto);
        return Result.success("Listener instance created successfully", instance);
    }

    /**
     * Get all instances for a listener.
     *
     * @param listenerId the listener ID
     * @return list of instances
     */
    @GetMapping
    public Result<List<ListenerInstanceVO>> getInstances(@PathVariable Long listenerId) {
        List<ListenerInstanceVO> instances = listenerInstanceService.getInstancesByListenerId(listenerId);
        return Result.success(instances);
    }

    /**
     * Get an instance by ID.
     *
     * @param listenerId the listener ID
     * @param instanceId the instance ID
     * @return the instance details
     */
    @GetMapping("/{instanceId}")
    public Result<ListenerInstanceVO> getInstance(
            @PathVariable Long listenerId,
            @PathVariable Long instanceId) {

        ListenerInstanceVO instance = listenerInstanceService.getInstanceById(instanceId);
        return Result.success(instance);
    }

    /**
     * Update an instance.
     *
     * @param listenerId the listener ID
     * @param instanceId the instance ID
     * @param dto the update DTO
     * @return the updated instance
     */
    @PutMapping("/{instanceId}")
    public Result<ListenerInstanceVO> updateInstance(
            @PathVariable Long listenerId,
            @PathVariable Long instanceId,
            @RequestBody @Valid UpdateListenerInstanceDTO dto) {

        ListenerInstanceVO instance = listenerInstanceService.updateInstance(instanceId, dto);
        return Result.success("Listener instance updated successfully", instance);
    }

    /**
     * Delete an instance.
     *
     * @param listenerId the listener ID
     * @param instanceId the instance ID
     * @return success message
     */
    @DeleteMapping("/{instanceId}")
    public Result<String> deleteInstance(
            @PathVariable Long listenerId,
            @PathVariable Long instanceId) {

        listenerInstanceService.deleteInstance(instanceId);
        return Result.success("Listener instance deleted successfully");
    }

    /**
     * Set an instance as the default instance.
     *
     * @param listenerId the listener ID
     * @param instanceId the instance ID
     * @return success message
     */
    @PostMapping("/{instanceId}/setDefault")
    public Result<String> setDefaultInstance(
            @PathVariable Long listenerId,
            @PathVariable Long instanceId) {

        listenerInstanceService.setDefaultInstance(instanceId);
        return Result.success("Default instance set successfully");
    }
}
