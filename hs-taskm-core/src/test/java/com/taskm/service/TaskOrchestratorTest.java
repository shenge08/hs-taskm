package com.taskm.service;

import com.taskm.dto.ContainerConfig;
import com.taskm.dto.InjectionConfig;
import com.taskm.dto.TaskStartupResult;
import com.taskm.entity.Task;
import com.taskm.exception.ContainerException;
import com.taskm.exception.InvalidTaskStatusException;
import com.taskm.exception.TaskNotFoundException;
import com.taskm.mapper.TaskMapper;
import com.taskm.service.impl.TaskOrchestratorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for TaskOrchestrator.
 * Tests task startup flow coordination.
 */
@ExtendWith(MockitoExtension.class)
class TaskOrchestratorTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private CodeSnippetInjector codeSnippetInjector;

    @Mock
    private ContainerLifecycleManager containerLifecycleManager;

    @Mock
    private ResourceMonitor resourceMonitor;

    private TaskOrchestrator taskOrchestrator;

    private Task testTask;

    @BeforeEach
    void setUp() {
        taskOrchestrator = new TaskOrchestratorImpl(taskMapper, codeSnippetInjector, containerLifecycleManager, resourceMonitor);
        ((TaskOrchestratorImpl) taskOrchestrator).setDefaultDockerImage("python:3.9-slim");

        // Create test task
        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("CREATED");
        testTask.setParameters(new HashMap<>());
        testTask.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void canStartTaskSuccessfully() {
        // Arrange
        InjectionConfig injectionConfig = createMockInjectionConfig();

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(codeSnippetInjector.prepareInjection(1L)).thenReturn(injectionConfig);
        when(containerLifecycleManager.createContainer(eq("python:3.9-slim"), eq(1L), any(ContainerConfig.class)))
            .thenReturn("container-id-123");
        doNothing().when(containerLifecycleManager).startContainer("container-id-123");
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        // Act
        TaskStartupResult result = taskOrchestrator.startTask(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getTaskId());
        assertEquals("container-id-123", result.getContainerId());
        assertEquals("RUNNING", result.getStatus());
        assertNull(result.getErrorMessage());

        // Verify interactions
        verify(taskMapper).selectById(1L);
        verify(codeSnippetInjector).prepareInjection(1L);
        verify(containerLifecycleManager).createContainer(eq("python:3.9-slim"), eq(1L), any(ContainerConfig.class));
        verify(containerLifecycleManager).startContainer("container-id-123");
        verify(taskMapper).updateById(any(Task.class));
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {
        // Arrange
        when(taskMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> {
            taskOrchestrator.startTask(999L);
        });

        verify(taskMapper).selectById(999L);
        verifyNoInteractions(codeSnippetInjector, containerLifecycleManager);
    }

    @Test
    void shouldThrowExceptionWhenTaskStatusIsInvalid() {
        // Arrange
        testTask.setStatus("RUNNING");
        when(taskMapper.selectById(1L)).thenReturn(testTask);

        // Act & Assert
        assertThrows(InvalidTaskStatusException.class, () -> {
            taskOrchestrator.startTask(1L);
        });

        verify(taskMapper).selectById(1L);
        verifyNoInteractions(codeSnippetInjector, containerLifecycleManager);
    }

    @Test
    void shouldThrowExceptionWhenTaskStatusIsCompleted() {
        // Arrange
        testTask.setStatus("COMPLETED");
        when(taskMapper.selectById(1L)).thenReturn(testTask);

        // Act & Assert
        assertThrows(InvalidTaskStatusException.class, () -> {
            taskOrchestrator.startTask(1L);
        });
    }

    @Test
    void shouldThrowExceptionWhenContainerCreationFails() {
        // Arrange
        InjectionConfig injectionConfig = createMockInjectionConfig();

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(codeSnippetInjector.prepareInjection(1L)).thenReturn(injectionConfig);
        when(containerLifecycleManager.createContainer(eq("python:3.9-slim"), eq(1L), any(ContainerConfig.class)))
            .thenThrow(new ContainerException("Failed to create container"));

        // Act & Assert
        assertThrows(ContainerException.class, () -> {
            taskOrchestrator.startTask(1L);
        });

        // Verify that task status was updated to FAILED
        verify(taskMapper).updateById(argThat(task ->
            task.getStatus().equals("FAILED") && task.getErrorMessage() != null
        ));
    }

    @Test
    void shouldThrowExceptionWhenContainerStartFails() {
        // Arrange
        InjectionConfig injectionConfig = createMockInjectionConfig();

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(codeSnippetInjector.prepareInjection(1L)).thenReturn(injectionConfig);
        when(containerLifecycleManager.createContainer(eq("python:3.9-slim"), eq(1L), any(ContainerConfig.class)))
            .thenReturn("container-id-123");
        doThrow(new ContainerException("Failed to start container"))
            .when(containerLifecycleManager).startContainer("container-id-123");

        // Act & Assert
        assertThrows(ContainerException.class, () -> {
            taskOrchestrator.startTask(1L);
        });

        // Verify that task status was updated to FAILED
        verify(taskMapper).updateById(argThat(task ->
            task.getStatus().equals("FAILED") && task.getErrorMessage() != null
        ));
    }

    @Test
    void canStartTaskWithPendingStatus() {
        // Arrange
        testTask.setStatus("PENDING");
        InjectionConfig injectionConfig = createMockInjectionConfig();

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(codeSnippetInjector.prepareInjection(1L)).thenReturn(injectionConfig);
        when(containerLifecycleManager.createContainer(eq("python:3.9-slim"), eq(1L), any(ContainerConfig.class)))
            .thenReturn("container-id-456");
        doNothing().when(containerLifecycleManager).startContainer("container-id-456");
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        // Act
        TaskStartupResult result = taskOrchestrator.startTask(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("container-id-456", result.getContainerId());
    }

    @Test
    void canValidateTaskStatus() {
        // Assert
        assertTrue(taskOrchestrator.canStartTask("CREATED"));
        assertTrue(taskOrchestrator.canStartTask("PENDING"));
        assertFalse(taskOrchestrator.canStartTask("RUNNING"));
        assertFalse(taskOrchestrator.canStartTask("COMPLETED"));
        assertFalse(taskOrchestrator.canStartTask("FAILED"));
    }

    @Test
    void shouldUpdateTaskStatusToRunningOnSuccess() {
        // Arrange
        InjectionConfig injectionConfig = createMockInjectionConfig();

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(codeSnippetInjector.prepareInjection(1L)).thenReturn(injectionConfig);
        when(containerLifecycleManager.createContainer(eq("python:3.9-slim"), eq(1L), any(ContainerConfig.class)))
            .thenReturn("container-id-789");
        doNothing().when(containerLifecycleManager).startContainer("container-id-789");
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        // Act
        taskOrchestrator.startTask(1L);

        // Assert
        verify(taskMapper).updateById(argThat(task ->
            task.getStatus().equals("RUNNING") &&
            task.getStartedAt() != null &&
            task.getId().equals(1L)
        ));
    }

    @Test
    void shouldUpdateTaskStatusToFailedOnException() {
        // Arrange
        testTask.setStatus("PENDING");
        InjectionConfig injectionConfig = createMockInjectionConfig();

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(codeSnippetInjector.prepareInjection(1L)).thenReturn(injectionConfig);
        when(containerLifecycleManager.createContainer(eq("python:3.9-slim"), eq(1L), any(ContainerConfig.class)))
            .thenThrow(new ContainerException("Docker daemon not running"));

        // Act & Assert
        assertThrows(ContainerException.class, () -> {
            taskOrchestrator.startTask(1L);
        });

        // Verify
        verify(taskMapper).updateById(argThat(task ->
            task.getStatus().equals("FAILED") &&
            task.getErrorMessage().equals("Docker daemon not running") &&
            task.getCompletedAt() != null
        ));
    }

    @Test
    void canStopTaskSuccessfully() {
        // Arrange
        testTask.setStatus("RUNNING");
        testTask.setContainerId("container-123");

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        doNothing().when(containerLifecycleManager).stopContainer("container-123", 10);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        // Act
        String status = taskOrchestrator.stopTask(1L);

        // Assert
        assertEquals("STOPPED", status);
        verify(containerLifecycleManager).stopContainer("container-123", 10);
        verify(taskMapper).updateById(argThat(task ->
            task.getStatus().equals("STOPPED") &&
            task.getCompletedAt() != null
        ));
    }

    @Test
    void shouldThrowExceptionWhenStoppingNonExistentTask() {
        // Arrange
        when(taskMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> {
            taskOrchestrator.stopTask(999L);
        });

        verifyNoInteractions(containerLifecycleManager);
    }

    @Test
    void shouldThrowExceptionWhenStoppingTaskWithInvalidStatus() {
        // Arrange
        testTask.setStatus("PENDING");
        when(taskMapper.selectById(1L)).thenReturn(testTask);

        // Act & Assert
        assertThrows(InvalidTaskStatusException.class, () -> {
            taskOrchestrator.stopTask(1L);
        });

        verifyNoInteractions(containerLifecycleManager);
    }

    @Test
    void shouldStopTaskEvenWhenContainerStopFails() {
        // Arrange
        testTask.setStatus("RUNNING");
        testTask.setContainerId("container-456");

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        doThrow(new ContainerException("Container already stopped"))
            .when(containerLifecycleManager).stopContainer("container-456", 10);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        // Act
        String status = taskOrchestrator.stopTask(1L);

        // Assert - should still return STOPPED despite failure (best effort)
        assertEquals("STOPPED", status);
        // Verify that updateById was called with STOPPED status despite container stop failure
        verify(taskMapper).updateById(argThat(task ->
            task.getStatus().equals("STOPPED") &&
            task.getCompletedAt() != null &&
            task.getErrorMessage().equals("Container already stopped")
        ));
        verify(containerLifecycleManager).stopContainer("container-456", 10);
    }

    @Test
    void canStopTaskWithoutContainerId() {
        // Arrange
        testTask.setStatus("RUNNING");
        testTask.setContainerId(null);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        // Act
        String status = taskOrchestrator.stopTask(1L);

        // Assert
        assertEquals("STOPPED", status);
        // Should not try to stop container if containerId is null
        verifyNoInteractions(containerLifecycleManager);
        verify(taskMapper).updateById(argThat(task ->
            task.getStatus().equals("STOPPED")
        ));
    }

    @Test
    void canValidateTaskCanStop() {
        // Assert
        assertTrue(taskOrchestrator.canStopTask("RUNNING"));
        assertFalse(taskOrchestrator.canStopTask("PENDING"));
        assertFalse(taskOrchestrator.canStopTask("CREATED"));
        assertFalse(taskOrchestrator.canStopTask("STOPPED"));
        assertFalse(taskOrchestrator.canStopTask("COMPLETED"));
        assertFalse(taskOrchestrator.canStopTask("FAILED"));
    }

    /**
     * Create a mock injection config for testing.
     */
    private InjectionConfig createMockInjectionConfig() {
        InjectionConfig config = new InjectionConfig();

        // Environment variables
        Map<String, String> envVars = new HashMap<>();
        envVars.put("PLUGIN_CODE", "ZGVmIHJ1bigpOiBwcmludCgnSGVsbG8nKQ=="); // Base64 encoded
        envVars.put("PLUGIN_PARAMS", "{}");
        envVars.put("LOG_PATH", "task_1_20230319_120000.log");
        config.setEnvironmentVariables(envVars);

        // Working directory
        config.setWorkingDirectory("/app");

        return config;
    }
}
