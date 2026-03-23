package com.taskm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taskm.entity.Listener;

import java.util.List;

/**
 * Service interface for Listener operations.
 */
public interface ListenerService extends IService<Listener> {

    /**
     * Get all listeners.
     *
     * @return list of all listeners
     */
    List<Listener> getAllListeners();

    /**
     * Get listener by ID.
     *
     * @param id listener ID
     * @return the listener
     * @throws com.taskm.exception.ListenerNotFoundException if listener not found
     */
    Listener getListener(Long id);

    /**
     * Get listeners by programming language.
     *
     * @param language programming language (python, javascript, java)
     * @return list of listeners
     */
    List<Listener> getListenersByLanguage(String language);

    /**
     * Get listeners by event type.
     *
     * @param eventType event type (TASK_STARTED, TASK_COMPLETED, TASK_FAILED, SIGNAL_GENERATED)
     * @return list of listeners
     */
    List<Listener> getListenersByEventType(String eventType);
}
