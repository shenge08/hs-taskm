package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.service.ListenerContainerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Listener Container management.
 * Provides endpoints for container lifecycle operations.
 */
@RestController
@RequestMapping("/api/listeners/{listenerId}/container")
public class ListenerContainerController {

    private final ListenerContainerManager listenerContainerManager;

    @Autowired
    public ListenerContainerController(ListenerContainerManager listenerContainerManager) {
        this.listenerContainerManager = listenerContainerManager;
    }

    /**
     * Start a listener container.
     *
     * @param listenerId the listener ID
     * @return container ID
     */
    @PostMapping("/start")
    public Result<Map<String, String>> startContainer(@PathVariable Long listenerId) {
        String containerId = listenerContainerManager.startListenerContainer(listenerId);
        return Result.success("Container started successfully", Map.of("containerId", containerId));
    }

    /**
     * Stop a listener container.
     *
     * @param listenerId the listener ID
     * @return success message
     */
    @PostMapping("/stop")
    public Result<String> stopContainer(@PathVariable Long listenerId) {
        listenerContainerManager.stopListenerContainer(listenerId);
        return Result.success("Container stopped successfully");
    }

    /**
     * Restart a listener container.
     *
     * @param listenerId the listener ID
     * @return success message
     */
    @PostMapping("/restart")
    public Result<String> restartContainer(@PathVariable Long listenerId) {
        listenerContainerManager.restartListenerContainer(listenerId);
        return Result.success("Container restarted successfully");
    }

    /**
     * Get container status.
     *
     * @param listenerId the listener ID
     * @return container status
     */
    @GetMapping("/status")
    public Result<Map<String, String>> getContainerStatus(@PathVariable Long listenerId) {
        String status = listenerContainerManager.getContainerStatus(listenerId);
        return Result.success(Map.of("status", status));
    }
}
