package com.taskm.service.impl;

import com.taskm.dto.ContainerConfig;
import com.taskm.dto.InjectionConfig;
import com.taskm.dto.TaskStartupResult;
import com.taskm.entity.Task;
import com.taskm.exception.ContainerException;
import com.taskm.exception.InvalidTaskStatusException;
import com.taskm.exception.TaskNotFoundException;
import com.taskm.mapper.TaskMapper;
import com.taskm.service.CodeSnippetInjector;
import com.taskm.service.ContainerLifecycleManager;
import com.taskm.service.ResourceMonitor;
import com.taskm.service.TaskOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of task orchestration.
 * Coordinates task creation, code injection, container creation and startup.
 */
@Service
public class TaskOrchestratorImpl implements TaskOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(TaskOrchestratorImpl.class);

    private final TaskMapper taskMapper;
    private final CodeSnippetInjector codeSnippetInjector;
    private final ContainerLifecycleManager containerLifecycleManager;
    private final ResourceMonitor resourceMonitor;

    @Value("${docker.default.image:python:3.9-slim}")
    private String defaultDockerImage;

    @Value("${monitoring.default.interval:30}")
    private int monitoringIntervalSeconds;

    @Autowired
    public TaskOrchestratorImpl(
            TaskMapper taskMapper,
            CodeSnippetInjector codeSnippetInjector,
            ContainerLifecycleManager containerLifecycleManager,
            ResourceMonitor resourceMonitor) {
        this.taskMapper = taskMapper;
        this.codeSnippetInjector = codeSnippetInjector;
        this.containerLifecycleManager = containerLifecycleManager;
        this.resourceMonitor = resourceMonitor;
    }

    /**
     * Set default Docker image (for testing).
     */
    public void setDefaultDockerImage(String defaultDockerImage) {
        this.defaultDockerImage = defaultDockerImage;
    }

    @Override
    @Transactional
    public TaskStartupResult startTask(Long taskId) {
        logger.info("Starting task {}", taskId);

        // Step 1: Query task information
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new TaskNotFoundException("Task not found with id: " + taskId);
        }

        try {
            // Step 2: Validate task status
            if (!canStartTask(task.getStatus())) {
                throw new InvalidTaskStatusException(
                    "Task status is " + task.getStatus() + ", but must be CREATED or PENDING to start"
                );
            }

            // Step 3: Prepare injection configuration
            logger.info("Preparing injection config for task {}", taskId);
            InjectionConfig injectionConfig = codeSnippetInjector.prepareInjection(taskId);

            // Step 4: Create container configuration
            ContainerConfig containerConfig = buildContainerConfig(injectionConfig, task);

            // Step 5: Create container
            logger.info("Creating container for task {}", taskId);
            String containerId = containerLifecycleManager.createContainer(
                defaultDockerImage,
                taskId,
                containerConfig
            );

            // Step 6: Start container
            logger.info("Starting container {} for task {}", containerId, taskId);
            containerLifecycleManager.startContainer(containerId);

            // Step 7: Update task status to RUNNING
            task.setStatus("RUNNING");
            task.setStartedAt(LocalDateTime.now());
            task.setContainerId(containerId);
            taskMapper.updateById(task);

            // Step 8: Start resource monitoring
            logger.info("Starting resource monitoring for container {}", containerId);
            resourceMonitor.startMonitoring(containerId, taskId, monitoringIntervalSeconds);

            logger.info("Task {} started successfully in container {}", taskId, containerId);

            return TaskStartupResult.success(taskId, containerId);

        } catch (Exception e) {
            logger.error("Failed to start task {}", taskId, e);

            // Update task status to FAILED with error message
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            taskMapper.updateById(task);

            if (e instanceof InvalidTaskStatusException || e instanceof TaskNotFoundException) {
                throw e;
            }

            throw new ContainerException("Failed to start task: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean canStartTask(String currentStatus) {
        return "CREATED".equals(currentStatus) || "PENDING".equals(currentStatus);
    }

    @Override
    @Transactional
    public String stopTask(Long taskId) {
        logger.info("Stopping task {}", taskId);

        // Step 1: Query task information
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new TaskNotFoundException("Task not found with id: " + taskId);
        }

        // Step 2: Validate task status
        if (!canStopTask(task.getStatus())) {
            throw new InvalidTaskStatusException(
                "Task status is " + task.getStatus() + ", but must be RUNNING to stop"
            );
        }

        try {
            // Step 3: Stop container (if exists)
            String containerId = task.getContainerId();
            if (containerId != null && !containerId.isEmpty()) {
                logger.info("Stopping container {} for task {}", containerId, taskId);
                containerLifecycleManager.stopContainer(containerId, 10);

                // Stop resource monitoring
                logger.info("Stopping resource monitoring for container {}", containerId);
                resourceMonitor.stopMonitoring(containerId);
            }

            // Step 4: Update task status to STOPPED
            task.setStatus("STOPPED");
            task.setCompletedAt(LocalDateTime.now());
            taskMapper.updateById(task);

            logger.info("Task {} stopped successfully", taskId);

            return "STOPPED";

        } catch (Exception e) {
            logger.error("Failed to stop task {}", taskId, e);

            // Stop monitoring even if stop fails
            String containerId = task.getContainerId();
            if (containerId != null && !containerId.isEmpty()) {
                try {
                    resourceMonitor.stopMonitoring(containerId);
                } catch (Exception monitorException) {
                    logger.error("Failed to stop monitoring for container {}", containerId, monitorException);
                }
            }

            // Record error but don't fail the operation (best effort)
            task.setStatus("STOPPED");
            task.setCompletedAt(LocalDateTime.now());
            task.setErrorMessage(e.getMessage());
            taskMapper.updateById(task);

            // Still return STOPPED as the status
            return "STOPPED";
        }
    }

    @Override
    public boolean canStopTask(String currentStatus) {
        return "RUNNING".equals(currentStatus);
    }

    /**
     * Build container configuration from injection config.
     */
    private ContainerConfig buildContainerConfig(InjectionConfig injectionConfig, Task task) {
        ContainerConfig config = new ContainerConfig();

        // Environment variables
        config.setEnv(injectionConfig.getEnvironmentVariables().entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .toList());

        // Working directory
        config.setWorkingDir(injectionConfig.getWorkingDirectory());

        // Command to run the strategy
        config.setCmd(java.util.List.of("python", "-c",
            "import os; import base64; exec(base64.b64decode(os.environ.get('PLUGIN_CODE', '')))"
        ));

        // Container name
        config.setName("task-" + task.getId());

        // Volume binds (log directories)
        String taskLogDir = Paths.get("./logs", "task-" + task.getId()).toString();

        config.setBinds(java.util.List.of(
            "/var/log/tasks:/var/log/tasks:rw",
            taskLogDir + ":/app/logs:rw"
        ));

        return config;
    }
}
