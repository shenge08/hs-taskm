package com.taskm.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.taskm.dto.ContainerConfig;
import com.taskm.dto.ContainerMetrics;
import com.taskm.dto.ContainerStatus;
import com.taskm.exception.ContainerException;
import com.taskm.service.impl.ContainerLifecycleManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for ContainerLifecycleManager.
 * Tests Docker operations with mocked DockerClient.
 */
@ExtendWith(MockitoExtension.class)
class ContainerLifecycleManagerTest {

    @Mock
    private DockerClient dockerClient;

    @Mock
    private CreateContainerCmd createContainerCmd;

    @Mock
    private StartContainerCmd startContainerCmd;

    @Mock
    private StopContainerCmd stopContainerCmd;

    @Mock
    private InspectContainerCmd inspectContainerCmd;

    @Mock
    private RemoveContainerCmd removeContainerCmd;

    @Mock
    private CreateContainerResponse createContainerResponse;

    @Mock
    private InspectContainerResponse inspectContainerResponse;

    @Mock
    private InspectContainerResponse.ContainerState containerState;

    private ContainerLifecycleManager containerLifecycleManager;

    @BeforeEach
    void setUp() {
        containerLifecycleManager = new ContainerLifecycleManagerImpl(dockerClient);
    }

    @Test
    void canCreateContainer() {
        // Arrange
        String imageId = "python:3.9";
        Long taskId = 1L;
        ContainerConfig config = new ContainerConfig();
        config.setEnv(List.of("KEY=value"));
        config.setCmd(List.of("python", "script.py"));
        config.setWorkingDir("/app");

        when(dockerClient.createContainerCmd(any(String.class))).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn("container-id-123");

        // Act
        String containerId = containerLifecycleManager.createContainer(imageId, taskId, config);

        // Assert
        assertEquals("container-id-123", containerId);
        verify(dockerClient).createContainerCmd(any(String.class));
        verify(createContainerCmd).exec();
    }

    @Test
    void shouldThrowExceptionWhenCreateContainerFails() {
        // Arrange
        String imageId = "nonexistent:image";
        Long taskId = 1L;
        ContainerConfig config = new ContainerConfig();

        when(dockerClient.createContainerCmd(any(String.class)))
                .thenThrow(new RuntimeException("Image not found"));

        // Act & Assert
        assertThrows(ContainerException.class, () -> {
            containerLifecycleManager.createContainer(imageId, taskId, config);
        });
    }

    @Test
    void canStartContainer() {
        // Arrange
        String containerId = "container-id-123";

        when(dockerClient.startContainerCmd(containerId)).thenReturn(startContainerCmd);

        // Act
        containerLifecycleManager.startContainer(containerId);

        // Assert
        verify(dockerClient).startContainerCmd(containerId);
        verify(startContainerCmd).exec();
    }

    @Test
    void shouldThrowExceptionWhenStartContainerFails() {
        // Arrange
        String containerId = "nonexistent-container";

        when(dockerClient.startContainerCmd(containerId))
                .thenThrow(new RuntimeException("Container not found"));

        // Act & Assert
        assertThrows(ContainerException.class, () -> {
            containerLifecycleManager.startContainer(containerId);
        });
    }

    @Test
    void canStopContainer() {
        // Arrange
        String containerId = "container-id-123";
        int timeoutSeconds = 10;

        when(dockerClient.stopContainerCmd(containerId)).thenReturn(stopContainerCmd);

        // Act
        containerLifecycleManager.stopContainer(containerId, timeoutSeconds);

        // Assert
        verify(dockerClient).stopContainerCmd(containerId);
        verify(stopContainerCmd).withTimeout(timeoutSeconds);
        verify(stopContainerCmd).exec();
    }

    @Test
    void canGetRunningContainerStatus() {
        // Arrange
        String containerId = "container-id-123";

        when(dockerClient.inspectContainerCmd(containerId)).thenReturn(inspectContainerCmd);
        when(inspectContainerCmd.exec()).thenReturn(inspectContainerResponse);
        when(inspectContainerResponse.getState()).thenReturn(containerState);
        when(containerState.getRunning()).thenReturn(true);

        // Act
        ContainerStatus status = containerLifecycleManager.getContainerStatus(containerId);

        // Assert
        assertEquals(ContainerStatus.RUNNING, status);
        verify(dockerClient).inspectContainerCmd(containerId);
        verify(inspectContainerCmd).exec();
    }

    @Test
    void canGetStoppedContainerStatus() {
        // Arrange
        String containerId = "container-id-123";

        when(dockerClient.inspectContainerCmd(containerId)).thenReturn(inspectContainerCmd);
        when(inspectContainerCmd.exec()).thenReturn(inspectContainerResponse);
        when(inspectContainerResponse.getState()).thenReturn(containerState);
        when(containerState.getRunning()).thenReturn(false);
        when(containerState.getExitCode()).thenReturn(0);

        // Act
        ContainerStatus status = containerLifecycleManager.getContainerStatus(containerId);

        // Assert
        assertEquals(ContainerStatus.STOPPED, status);
    }

    @Test
    void canGetFailedContainerStatus() {
        // Arrange
        String containerId = "container-id-123";

        when(dockerClient.inspectContainerCmd(containerId)).thenReturn(inspectContainerCmd);
        when(inspectContainerCmd.exec()).thenReturn(inspectContainerResponse);
        when(inspectContainerResponse.getState()).thenReturn(containerState);
        when(containerState.getRunning()).thenReturn(false);
        when(containerState.getExitCode()).thenReturn(1);

        // Act
        ContainerStatus status = containerLifecycleManager.getContainerStatus(containerId);

        // Assert
        assertEquals(ContainerStatus.FAILED, status);
    }

    @Test
    void shouldThrowExceptionWhenGetMetricsNotImplemented() {
        // Arrange
        String containerId = "container-id-123";

        // Act & Assert
        assertThrows(ContainerException.class, () -> {
            containerLifecycleManager.getContainerMetrics(containerId);
        });
    }

    @Test
    void canRemoveContainer() {
        // Arrange
        String containerId = "container-id-123";
        boolean force = true;

        when(dockerClient.removeContainerCmd(containerId)).thenReturn(removeContainerCmd);

        // Act
        containerLifecycleManager.removeContainer(containerId, force);

        // Assert
        verify(dockerClient).removeContainerCmd(containerId);
        verify(removeContainerCmd).withForce(force);
        verify(removeContainerCmd).exec();
    }

    @Test
    void shouldThrowExceptionWhenRemoveContainerFails() {
        // Arrange
        String containerId = "nonexistent-container";

        when(dockerClient.removeContainerCmd(containerId))
                .thenThrow(new RuntimeException("Container not found"));

        // Act & Assert
        assertThrows(ContainerException.class, () -> {
            containerLifecycleManager.removeContainer(containerId, false);
        });
    }
}
