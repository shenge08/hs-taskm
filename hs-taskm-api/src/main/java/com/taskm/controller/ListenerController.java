package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.entity.Listener;
import com.taskm.service.ListenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Listener management.
 * Provides endpoints for querying listeners.
 */
@RestController
@RequestMapping("/api/listeners")
public class ListenerController {

    private final ListenerService listenerService;

    @Autowired
    public ListenerController(ListenerService listenerService) {
        this.listenerService = listenerService;
    }

    /**
     * Get all listeners.
     * Optionally filter by programming language or event type.
     *
     * @param language optional language filter
     * @param eventType optional event type filter
     * @return list of listeners
     */
    @GetMapping
    public Result<List<Listener>> getAllListeners(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String eventType) {

        if (language != null && !language.isEmpty()) {
            List<Listener> listeners = listenerService.getListenersByLanguage(language);
            return Result.success("Listeners filtered by language: " + language, listeners);
        }

        if (eventType != null && !eventType.isEmpty()) {
            List<Listener> listeners = listenerService.getListenersByEventType(eventType);
            return Result.success("Listeners filtered by event type: " + eventType, listeners);
        }

        List<Listener> listeners = listenerService.getAllListeners();
        return Result.success(listeners);
    }

    /**
     * Get listener by ID.
     *
     * @param id listener ID
     * @return listener details
     */
    @GetMapping("/{id}")
    public Result<Listener> getListener(@PathVariable Long id) {
        Listener listener = listenerService.getListener(id);
        return Result.success(listener);
    }
}
