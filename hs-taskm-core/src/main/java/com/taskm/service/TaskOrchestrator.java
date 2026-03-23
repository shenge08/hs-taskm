package com.taskm.service;

import com.taskm.dto.TaskStartupResult;

/**
 * Service interface for task orchestration.
 * Coordinates task creation, code injection, container creation and startup.
 */
public interface TaskOrchestrator {

    /**
     * Start a task by orchestrating the complete startup flow.
     *
     * Process:
     * 1. Query task information
     * 2. Validate task status (must be CREATED/PENDING)
     * 3. Prepare injection configuration via CodeSnippetInjector
     * 4. Create container via ContainerLifecycleManager
     * 5. Start container
     * 6. Update task status to RUNNING
     * 7. Save containerId to task record
     * 8. Record startup timestamp
     *
     * @param taskId task ID
     * @return task startup result with status and container ID
     * @throws com.taskm.exception.TaskNotFoundException if task not found
     * @throws com.taskm.exception.InvalidTaskStatusException if task status is not valid for startup
     * @throws com.taskm.exception.ContainerException if container creation or startup fails
     */
    TaskStartupResult startTask(Long taskId);

    /**
     * Validate if a task can be started.
     * Task must be in CREATED or PENDING status.
     *
     * @param currentStatus current task status
     * @return true if task can be started, false otherwise
     */
    boolean canStartTask(String currentStatus);

    /**
     * Stop a running task by stopping its container.
     *
     * Process:
     * 1. Query task information
     * 2. Validate task status (must be RUNNING)
     * 3. Stop container via ContainerLifecycleManager
     * 4. Update task status to STOPPED
     * 5. Record stop timestamp
     *
     * Note: This operation is idempotent - stopping an already stopped task will succeed.
     *
     * @param taskId task ID
     * @return updated task status (STOPPED)
     * @throws com.taskm.exception.TaskNotFoundException if task not found
     * @throws com.taskm.exception.InvalidTaskStatusException if task status is not RUNNING
     */
    String stopTask(Long taskId);

    /**
     * Validate if a task can be stopped.
     * Task must be in RUNNING status.
     *
     * @param currentStatus current task status
     * @return true if task can be stopped, false otherwise
     */
    boolean canStopTask(String currentStatus);
}
